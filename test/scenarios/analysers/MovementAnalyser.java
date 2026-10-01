package scenarios.analysers;

import java.util.List;

/**
 * The movement analyser composes {@link RenderableAnalyser} and includes helpful query methods
 * about the movement of the renderable.
 */
public class MovementAnalyser extends Analyser {

    /** Takes a given {@link RenderableAnalyser}, extracts the relevant information */
    public MovementAnalyser(RenderableAnalyser analyzer) {
        super(analyzer.getId(), analyzer.getFrames());
    }

    /**
     * Return a 2dVector {@link XyPair} representation of how the {@link engine.renderer.Renderable}
     * has moved over its lifespan.
     *
     * @return a 2dVector representing the overall movement of the entity between the given start
     *     and end frame.
     */
    public XyPair measureOverallMove() {
        final List<FrameRecord> frames = this.getFrames();
        int xDistance = 0;
        int YDistance = 0;
        FrameRecord previousFrame = null;
        for (final FrameRecord frame : frames) {
            if (previousFrame != null) {
                final int tempDistanceX = frame.getX() - previousFrame.getX();
                final int tempDistanceY = frame.getY() - previousFrame.getY();
                xDistance += tempDistanceX;
                YDistance += tempDistanceY;
            }
            previousFrame = frame;
        }
        return new XyPair(xDistance, YDistance);
    }

    /**
     * Return 2dVector {@link XyPair} representation of how the Renderable has moved between the
     * given starting and ending frame (inclusive of those frames)
     *
     * @param start starting frame to check from inclusively.
     * @param end end frame to check too inclusively.
     * @return a 2dVector {@link XyPair} representing the overall movement of the entity between the
     *     given start and end frame.
     */
    public XyPair measureOverallMoveBetween(int start, int end) {
        assert start < end;
        final List<FrameRecord> frames = this.getFramesBetween(start, end);
        int xDistance = 0;
        int YDistance = 0;
        FrameRecord previousFrame = null;
        for (final FrameRecord frame : frames) {
            if (previousFrame != null) {
                final int tempDistanceX = frame.getX() - previousFrame.getX();
                final int tempDistanceY = frame.getY() - previousFrame.getY();
                xDistance += tempDistanceX;
                YDistance += tempDistanceY;
            }
            previousFrame = frame;
        }
        return new XyPair(xDistance, YDistance);
    }

    /**
     * Check if the Renderable stayed within the given radial area for the given frame lifespan.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param radialDistance radial distance from the given coordinates
     * @param start starting frame we are checking from (inclusive)
     * @param end ending frame we are checking to (inclusive)
     * @return if the Renderable stayed within the given radial area for the given frame lifespan.
     */
    public boolean stayedInRadialAreaBetweenFrames(
            int x, int y, int radialDistance, int start, int end) {
        assert start < end;
        assert radialDistance > 1;
        for (final FrameRecord frame : this.getFramesBetween(start, end)) {
            final boolean inRange = distanceFrom(frame, x, y) <= radialDistance;
            if (!inRange) {
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if Renderable stayed within the given radial area for its entire lifespan.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param radialDistance radial distance from the given coordinates
     * @return if the entity stayed in the given area across all frames.
     */
    public boolean stayedInRadialArea(int x, int y, int radialDistance) {
        assert radialDistance > 1;
        for (final FrameRecord frame : this.getFrames()) {
            final boolean inRange = distanceFrom(frame, x, y) <= radialDistance;
            if (!inRange) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the Renderable visited the radial area specified for at least one frame.
     *
     * @param x horizontal position
     * @param y vertical position
     * @param radialDistance radial distance measured from the given x,y
     * @return if the Renderable visited the radial area specified for at least one frame.
     */
    public boolean visitedRadialArea(int x, int y, int radialDistance) {
        assert radialDistance > 1;
        for (final FrameRecord frame : this.getFrames()) {
            final boolean inRange = distanceFrom(frame, x, y) <= radialDistance;
            if (inRange) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the entity was at any point in the given rectangular area.
     *
     * @param x center horizontal coordinate for the rectangle.
     * @param y center vertical coordinate for the rectangle.
     * @param w width for the rectangle.
     * @param h height for the rectangle.
     * @return if the entity was at any point in the given rectangular area.
     */
    public boolean visitedRectangularArea(int x, int y, int w, int h) {
        assert w > 1; // prevent Nans and also nonsensical results you should not be checking a 1x1
        // pixel with this method
        assert h > 1; // prevent Nans and also nonsensical results you should not be checking a 1x1
        // pixel with this method

        final int leftEdge = x - w / 2;
        final int topEdge = y - h / 2;
        final int rightEdge = x + w / 2;
        final int bottomEdge = y + h / 2;
        for (final FrameRecord frame : this.getFrames()) {
            final boolean isLeftOfRightEdge = frame.getX() <= rightEdge;
            final boolean isRightOfLeftEdge = frame.getX() >= leftEdge;
            final boolean isBelowTopEdge = frame.getY() >= topEdge;
            final boolean isAboveBottomEdge = frame.getY() <= bottomEdge;
            final boolean withinEdges =
                    (isLeftOfRightEdge && isRightOfLeftEdge && isBelowTopEdge && isAboveBottomEdge);
            if (withinEdges) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if for every frame the entity stayed in the given rectangular area (drawing the
     * rectangle from its center).
     *
     * @param x center horizontal coordinate for the rectangle.
     * @param y center vertical coordinate for the rectangle.
     * @param w width for the rectangle.
     * @param h height for the rectangle.
     * @return if the entity stayed in the given area across all frames.
     */
    public boolean stayedInRectangularArea(int x, int y, int w, int h) {
        assert w > 1; // prevent Nans and also nonsensical results you should not be checking a 1x1
        // pixel with this method
        assert h > 1; // prevent Nans and also nonsensical results you should not be checking a 1x1
        // pixel with this method

        final int leftEdge = x - w / 2;
        final int topEdge = y - h / 2;
        final int rightEdge = x + w / 2;
        final int bottomEdge = y + h / 2;
        for (final FrameRecord frame : this.getFrames()) {
            final boolean isLeftOfRightEdge = frame.getX() <= rightEdge;
            final boolean isRightOfLeftEdge = frame.getX() >= leftEdge;
            final boolean isBelowTopEdge = frame.getY() >= topEdge;
            final boolean isAboveBottomEdge = frame.getY() <= bottomEdge;
            final boolean notWithinEdges =
                    !(isLeftOfRightEdge
                            && isRightOfLeftEdge
                            && isBelowTopEdge
                            && isAboveBottomEdge);
            if (notWithinEdges) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if for every frame the entity stayed in the given rectangular area (drawing the
     * rectangle from its center).
     *
     * @param x center horizontal coordinate for the rectangle.
     * @param y center vertical coordinate for the rectangle.
     * @param w width for the rectangle.
     * @param h height for the rectangle.
     * @param start starting frame to check from inclusively.
     * @param end end frame to check too inclusively.
     * @return if the entity stayed in the given area across all frames.
     */
    public boolean stayedInRectangularAreaBetweenFrames(
            int x, int y, int w, int h, int start, int end) {
        assert w > 1;
        assert h > 1;
        assert start < end;
        final List<FrameRecord> frames = this.getFramesBetween(start, end);
        final int leftEdge = x - w / 2;
        final int topEdge = y - h / 2;
        final int rightEdge = x + w / 2;
        final int bottomEdge = y + h / 2;
        for (final FrameRecord frame : frames) {
            final boolean isLeftOfRightEdge = frame.getX() <= rightEdge;
            final boolean isRightOfLeftEdge = frame.getX() >= leftEdge;
            final boolean isBelowTopEdge = frame.getY() >= topEdge;
            final boolean isAboveBottomEdge = frame.getY() <= bottomEdge;
            final boolean notWithinEdges =
                    !(isLeftOfRightEdge
                            && isRightOfLeftEdge
                            && isBelowTopEdge
                            && isAboveBottomEdge);
            if (notWithinEdges) {
                return false;
            }
        }
        return true;
    }
}
