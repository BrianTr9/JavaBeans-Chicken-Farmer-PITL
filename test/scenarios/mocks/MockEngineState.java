package scenarios.mocks;

import engine.EngineState;
import engine.input.KeyState;
import engine.input.MouseState;
import engine.renderer.Dimensions;
import engine.renderer.TileGrid;

import java.util.ArrayList;
import java.util.List;

public class MockEngineState implements EngineState {
    private Dimensions dimensions;
    private MouseState mouse;
    private KeyState keys;

    private int frame;

    public MockEngineState() {
        this.dimensions = new TileGrid(25, 2000);
        this.mouse = new MockMouse(2, 2, false, false, false);
        this.keys = new MockKeys(List.of());
        this.frame = 0;
    }

    public MockEngineState(KeyState keys) {
        this();
        this.keys = keys;
    }

    public MockEngineState(KeyState keys, int frame) {
        this(keys);
        this.frame = frame;
    }

    public MockEngineState(int frame) {
        this();
        this.frame = frame;
    }

    public MockEngineState(Dimensions dimensions, int frame) {
        this();
        this.frame = frame;
        this.dimensions = dimensions;
    }

    public MockEngineState(Dimensions dimensions) {
        this();
        this.dimensions = dimensions;
    }

    public MockEngineState(MouseState mouse) {
        this();
        this.mouse = mouse;
    }

    public MockEngineState(Dimensions dimensions, MouseState mouse, KeyState keys) {
        this(dimensions, mouse, keys, 0);
    }

    public MockEngineState(Dimensions dimensions, MouseState mouse, KeyState keys, int frame) {
        this.dimensions = dimensions;
        this.mouse = mouse;
        this.keys = keys;
        this.frame = frame;
    }

    public MockEngineState withFrame(int frame) {
        return new MockEngineState(dimensions, mouse, keys, frame);
    }

    public MockEngineState press(char key) {
        return new MockEngineState(dimensions, mouse, new MockKeys(key), frame);
    }

    public MockEngineState leftClick() {
        MouseState newMouse =
                new MockMouse(
                        mouse.getMouseX(),
                        mouse.getMouseY(),
                        true,
                        mouse.isRightPressed(),
                        mouse.isMiddlePressed());
        return new MockEngineState(dimensions, newMouse, keys, frame);
    }

    @Override
    public Dimensions getDimensions() {
        return dimensions;
    }

    @Override
    public MouseState getMouse() {
        return mouse;
    }

    @Override
    public KeyState getKeys() {
        return keys;
    }

    @Override
    public int currentTick() {
        return frame;
    }
}
