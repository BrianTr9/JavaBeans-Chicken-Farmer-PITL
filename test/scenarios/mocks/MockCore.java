package scenarios.mocks;

import engine.EngineState;
import engine.core.Core;
import engine.core.Debug;
import engine.renderer.Renderable;
import scenarios.analysers.AnalyserManager;

import java.util.List;

/**
 * Mock the core, configuring a specific engine state via {@link #setState(EngineState)}.
 */
public class MockCore extends Core {

    private final AnalyserManager record;
    private EngineState currentState;

    /**
     * Construct a new core mock that logs all render events to an analyser for test inspection.
     *
     * @param record An analyser to record to.
     */
    public MockCore(AnalyserManager record) {
        super(new Debug(false));
        this.record = record;
    }

    @Override
    public void draw(List<Renderable> renderables) {
        if (record == null) {
            return;
        }
        for (Renderable renderable : renderables) {
            this.record.add(currentState.currentTick(), renderable);
        }
    }

    /**
     * Set the engine state to use when queried.
     *
     * @param engineState An engine state.
     */
    public void setState(EngineState engineState) {
        this.currentState = engineState;
    }

    @Override
    public int getMouseX() {
        return this.currentState.getMouse().getMouseX();
    }

    @Override
    public int getMouseY() {
        return this.currentState.getMouse().getMouseY();
    }

    @Override
    public boolean isLeftPressed() {
        return this.currentState.getMouse().isLeftPressed();
    }

    @Override
    public boolean isRightPressed() {
        return this.currentState.getMouse().isRightPressed();
    }

    @Override
    public boolean isMiddlePressed() {
        return this.currentState.getMouse().isMiddlePressed();
    }

    @Override
    public List<Character> getDown() {
        return this.currentState.getKeys().getDown();
    }

    @Override
    public boolean isDown(char character) {
        return this.currentState.getKeys().isDown(character);
    }
}
