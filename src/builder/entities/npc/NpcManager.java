package builder.entities.npc;

import builder.GameState;
import builder.Tickable;
import builder.entities.Interactable;
import builder.ui.RenderableGroup;

import engine.EngineState;
import engine.renderer.Renderable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager responsible for non-player characters (NPCs).
 *
 * <p>Handles ticking, interaction and rendering of NPCs. Also provides lifecycle management
 * utilities such as cleanup and adding new NPCs.
 */
public class NpcManager implements Interactable, Tickable, RenderableGroup {

    /** The list of NPCs currently managed. */
    private final List<Npc> npcs = new ArrayList<>();

    /** Construct a new NPC manager. */
    public NpcManager() {}

    /**
     * Remove any NPCs marked for removal from the internal list.
     */
    public void cleanup() {
        for (int i = this.npcs.size() - 1; i >= 0; i -= 1) {
            if (this.npcs.get(i).isMarkedForRemoval()) {
                this.npcs.remove(i);
            }
        }
    }

    /**
     * Add an NPC to be managed.
     *
     * @param npc the NPC to add
     */
    public void addNpc(Npc npc) {
        this.npcs.add(npc);
    }

    /**
     * Returns a snapshot list of current NPCs. Modifying the returned list has no effect on the
     * manager's internal state.
     *
     * @return a list of NPCs currently managed
     */
    public List<Npc> getNpcs() {
        return new ArrayList<>(this.npcs);
    }

    @Override
    public void tick(EngineState state, GameState game) {
        this.cleanup();
        for (Npc npc : this.npcs) {
            npc.tick(state, game);
        }
    }

    @Override
    public void interact(EngineState state, GameState game) {
        for (Interactable interactable : this.getInteractables()) {
            interactable.interact(state, game);
        }
    }

    // Private helper to collect interactable NPCs.
    private List<Interactable> getInteractables() {
        // Construct a new list of Interactable directly from the NPC collection.
        return new ArrayList<Interactable>(this.npcs);
    }

    @Override
    public List<Renderable> render() {
        return new ArrayList<>(this.npcs);
    }
}
