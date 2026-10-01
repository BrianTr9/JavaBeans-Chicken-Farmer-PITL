package scenarios.analysers;

import engine.EngineState;
import engine.renderer.Renderable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Responsible for holding all the generated {@link RenderableAnalyser}s identifiable by their
 * stringified UUIDS. Holds several useful predicate driven methods like .every, .count, .filter to
 * help with common checks when interrogating overall game state in our tests.
 */
public class AnalyserManager {

    private final Map<String, RenderableAnalyser> data = new HashMap<>();

    /** Constructs a new empty AnalyserManager. */
    public AnalyserManager() {}

    /**
     * Record the state of the given renderable during the given frame. If the renderable has not
     * previously been tracked, it will be added to the internal tracking.
     *
     * @param frame The current frame number according to {@link EngineState#currentTick()}.
     * @param renderable The renderable we want to begin tracking or update the state of one we are
     *     currently tracking.
     */
    public void add(int frame, Renderable renderable) {
        if (!data.containsKey(renderable.getID())) {
            data.put(renderable.getID(), new RenderableAnalyser(renderable.getID()));
        }
        data.get(renderable.getID()).addFrameData(frame, renderable);
    }

    /**
     * Return a {@link RenderableAnalyser} that matches the given id.
     *
     * @param id id we are filtering by.
     * @return a {@link RenderableAnalyser} that matches the given id or null.
     */
    public RenderableAnalyser get(String id) {
        return data.get(id);
    }

    /**
     * Return the first {@link RenderableAnalyser} spawned that belongs to the given spriteGroup.
     *
     * @param label label we wish to filter for spriteGroup by.
     * @return the first {@link RenderableAnalyser} spawned that belongs to the given spriteGroup.
     */
    public RenderableAnalyser getFirstSpawnedOfSpriteGroup(String label) {
        int spawnTime = Integer.MAX_VALUE;
        RenderableAnalyser renderable = null;
        for (final RenderableAnalyser entry : this.getBySpriteGroup(label)) {
            if (entry.getFirstFrame().getFrame() < spawnTime) {
                spawnTime = entry.getFirstFrame().getFrame();
                renderable = entry;
            }
        }
        return renderable;
    }

    /**
     * Returns an {@link ArrayList} of {@link RenderableAnalyser}s filtered by the given label
     * against each {@link RenderableAnalyser}s spriteGroup.
     *
     * @param label spriteGroup label we wish to filter for.
     * @return an *unsorted* list of {@link RenderableAnalyser}s filtered by the given label against
     *     each {@link RenderableAnalyser}s spriteGroup.
     */
    public List<RenderableAnalyser> getBySpriteGroup(String label) {
        final List<RenderableAnalyser> result = new ArrayList<>();
        for (final RenderableAnalyser analyser : this.getAll()) {
            if (Objects.equals(analyser.spriteGroup(), label)) {
                result.add(analyser);
            }
        }
        return result;
    }

    /**
     * Returns every {@link RenderableAnalyser} stored in this {@link AnalyserManager}.
     *
     * @return every {@link RenderableAnalyser} stored in this {@link AnalyserManager}.
     */
    public List<RenderableAnalyser> getAll() {
        return new ArrayList<>(data.values());
    }

    /**
     * Checks if every {@link RenderableAnalyser} in the target spriteGroup matches against the
     * given conditional function.
     *
     * @param label label
     * @param func conditional function
     * @return if every {@link RenderableAnalyser} in the target spriteGroup matches against the
     *     given conditional function.
     */
    public boolean every(String label, Predicate<RenderableAnalyser> func) {
        for (final RenderableAnalyser analyser : getBySpriteGroup(label)) {
            if (!func.test(analyser)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the given function against the target spriteGroup and returns how many of the {@link
     * RenderableAnalyser} fulfill that conditional function
     */
    public int count(String label, Predicate<RenderableAnalyser> func) {
        int count = 0;
        for (final RenderableAnalyser analyser : getBySpriteGroup(label)) {
            if (func.test(analyser)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks the given function against the target spriteGroup and returns any of the {@link
     * RenderableAnalyser}s fulfill the conditional function condition.
     *
     * @param label spriteGroup label we use to help filter to what we want to filter further
     * @param func - conditional func we use to assess whether we wish the time being checked by the
     *     func should be a part of the returned {@link ArrayList}
     * @return returns any of the {@link RenderableAnalyser}s fulfill the conditional function
     *     condition.
     */
    public List<RenderableAnalyser> filter(String label, Predicate<RenderableAnalyser> func) {
        final List<RenderableAnalyser> result = new ArrayList<>();
        for (final RenderableAnalyser analyser : getBySpriteGroup(label)) {
            if (func.test(analyser)) {
                result.add(analyser);
            }
        }
        return result;
    }
}
