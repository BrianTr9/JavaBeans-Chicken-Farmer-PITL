package scenarios.analysers;

import engine.art.sprites.Sprite;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * An analyser tracks the state of a unique renderable drawn to a screen during each game tick. The
 * analyser class can be used to query the history of a renderable throughout a test scenario.
 */
public abstract class Analyser {

    private final String id;
    private final List<FrameRecord> frames = new ArrayList<>();

    /**
     * Construct a new empty analyser for a renderable of the given ID.
     *
     * @param id A unique ID of a renderable to analyse.
     */
    public Analyser(String id) {
        this.id = id;
    }

    /**
     * Construct a new analyser with an existing list of frame data. Useful for creating copies of
     * existing analysers.
     *
     * @param id A unique ID of a renderable to analyse.
     * @param frames An existing list of frame data.
     */
    public Analyser(String id, List<FrameRecord> frames) {
        this.id = id;
        this.frames.addAll(frames);
    }

    /**
     * Return the Stringified UUID this analyzer is looking to sync to in any data it is fed.
     *
     * @return the Stringified UUID this analyzer is looking to sync to in any data it is fed.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the frame record for the specified frame.
     *
     * @param frameCount - the number of frame we are sorting for i.e 33 for the 34th frame etc
     * @return the frame record for the specified frame or null if the Renderable did not exist
     *     during that given frame.
     */
    public FrameRecord getFrame(int frameCount) {
        for (final FrameRecord frame : this.frames) {
            if (frame.getFrame() == frameCount) {
                return frame;
            }
        }
        return null;
    }

    /**
     * Return the first frame recorded by this analyser.
     *
     * @return The first frame recorded by this analyser.
     */
    public FrameRecord getFirstFrame() {
        return this.getFrames().getFirst();
    }

    /**
     * Returns all the frames recorded by the analyser.
     *
     * @return All frame records tracked by the analyser.
     */
    public List<FrameRecord> getFrames() {
        return frames;
    }

    /**
     * Adds a {@link Renderable}s internal data to the frame Data for our entity analyzer, IF it
     * matches the ID our Analyzer cares about.
     *
     * @param frame specific frame from {@link engine.EngineState} this is from
     * @param renderable specific {@link Renderable}
     */
    public void addFrameData(int frame, Renderable renderable) {
        assert frame > -1;
        if (!this.id.equals(renderable.getID())) { // if id is not a valid match, don't add it
            return;
        }
        frames.add(
                new FrameData(frame, renderable.getX(), renderable.getY(), renderable.getSprite()));
    }

    // return how many frames this was around for
    public int frameLifespan() {
        return this.frames.size();
    }

    /**
     * Return a 2dVector {@link XyPair} containing the initial position for this Renderable.
     *
     * @return a 2dVector {@link XyPair} containing the initial position for this Renderable.
     */
    public XyPair spawnPosition() {
        return new XyPair(this.frames.getFirst().getX(), this.frames.getFirst().getY());
    }

    /**
     * Return how far away this npc is from the given position
     *
     * @param xCoordinate - x coordinate
     * @param yCoordinate - y coordinate
     * @return integer representation for how far apart they are
     */
    public static int distanceFrom(FrameRecord data, int xCoordinate, int yCoordinate) {
        final int deltaX = xCoordinate - data.getX();
        final int deltaY = yCoordinate - data.getY();
        return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Return all stored frames that fall between (inclusively) the given start and end frame
     *
     * @param start starting frame
     * @param end ending frame
     * @return all stored frames that fall between (inclusively) the given start and end frame
     */
    public List<FrameRecord> getFramesBetween(int start, int end) {
        assert start < end;
        final List<FrameRecord> filteredFrames = new ArrayList<>();
        for (final FrameRecord frameRecord : frames) {
            if (frameRecord.getFrame() >= start && frameRecord.getFrame() <= end) {
                filteredFrames.add(frameRecord);
            }
        }
        return filteredFrames;
    }

    /**
     * Used to store frame data describing a {@link engine.renderer.Renderable} entities state for a
     * frame in the renderer.
     */
    private static class FrameData implements FrameRecord {
        private final int frame;
        private final int x;
        private final int y;
        private final Sprite sprite;

        /**
         * Constructs a new instance to represent a single 'tick' or frame of a renderables state
         * during the lifespan of a game using the given information.
         *
         * @param frameCount the current frame counting from 0 (for example frame 1 is the 2nd
         *     frame)
         * @param x horizontal coordinate of the {@link engine.renderer.Renderable} this frameData
         *     is partially representing.
         * @param y vertical coordinate of the {@link engine.renderer.Renderable} this frameData is
         *     partially representing.
         * @param sprite {@link Sprite} the current sprite being displayed for this frame of the
         *     {@link engine.renderer.Renderable} this frame data is partially representing.
         */
        public FrameData(int frameCount, int x, int y, Sprite sprite) {
            this.frame = frameCount;
            this.x = x;
            this.y = y;
            this.sprite = sprite;
        }

        /**
         * Returns a string representation of the frame data.
         *
         * @return A string representation of the frame data.
         */
        @Override
        public String toString() {
            return "FrameData [frame="
                    + frame
                    + ", x="
                    + x
                    + ", y="
                    + y
                    + ", sprite="
                    + sprite.getLabel()
                    + "]";
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public Sprite getSprite() {
            return this.sprite;
        }

        @Override
        public int getFrame() {
            return this.frame;
        }
    }
}
