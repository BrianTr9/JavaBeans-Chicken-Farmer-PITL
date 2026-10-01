package scenarios.mocks;

import engine.input.MouseState;

/** Generated Mouse State used for the Headless mode, see {@link MockCore}. */
public class MockMouse implements MouseState {

    private final int mouseX;
    private final int mouseY;
    private final boolean left;
    private final boolean right;
    private final boolean middle;

    /**
     * Is a representation of the mouse input state for one frame Used primarily in frame extraction
     * for ScriptReader and/or some unit testing to reduce need for mocks or similar.
     *
     * @param mouseX - where the mouse was horizontally to a "pixel" level of accuracy
     * @param mouseY - where the mouse was vertically to a "pixel" level of accuracy
     * @param leftDown - whether the left mouse was down
     * @param rightDown - whether the right mouse was down
     * @param middleDown - whether the middle mouse was down
     */
    public MockMouse(
            int mouseX, int mouseY, boolean leftDown, boolean rightDown, boolean middleDown) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.left = leftDown;
        this.right = rightDown;
        this.middle = middleDown;
    }

    @Override
    public String toString() {
        return "MouseState{"
                + "x:"
                + this.mouseX
                + ", "
                + "y:"
                + this.mouseY
                + ", "
                + "left:"
                + this.left
                + "right:"
                + this.right
                + "middle:"
                + this.middle
                + "}";
    }

    /** x-coordinate for the mouse. */
    @Override
    public int getMouseX() {
        return mouseX;
    }

    /** y-coordinate for the mouse. */
    @Override
    public int getMouseY() {
        return mouseY;
    }

    /**
     * indication of whether a mouse button is down. true if left, right or middle mouse button is
     * down.
     */
    @Override
    public boolean isLeftPressed() {
        return left;
    }

    @Override
    public boolean isRightPressed() {
        return right;
    }

    @Override
    public boolean isMiddlePressed() {
        return middle;
    }
}
