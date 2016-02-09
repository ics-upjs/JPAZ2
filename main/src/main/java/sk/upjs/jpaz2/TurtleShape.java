package sk.upjs.jpaz2;

import java.awt.Graphics2D;

/**
 * The interface for painters of turtles' shapes.
 */
public interface TurtleShape {
    /**
     * Paints the turtle shape.
     * 
     * @param t
     *            the turtle whose shape is paint
     * @param g
     *            the graphics where the turtle's shape should be paint
     */
    void paintTurtle(Turtle t, Graphics2D g);

    /**
     * Returns immutable number of views provided by the shape. A shape can
     * provided more views on the shape, e.g. depending on current direction of
     * the turtle. Each shape must define at least one view.
     * 
     * @return the number of views on the shape
     */
    int getViewCount();

    /**
     * Returns immutable number of animation frames defined for each view of the
     * shape. A view on a shape can consist of several frames. They are used to
     * animate shapes. All views have the same number of frames. Each view must
     * provide at least one animation frame.
     * 
     * @return the number of animation frames
     */
    int getFrameCount();

    /**
     * Returns immutable animation delay (time period between animation frames)
     * in milliseconds.
     * 
     * @return preferred animation delay in milliseconds
     */
    long getFrameDuration();

    /**
     * Returns whether a given point is internal point of current turtle's
     * shape.
     * 
     * @param t
     *            the turtle whose shape is considered
     * @param x
     *            the x coordinate of point
     * @param y
     *            the y coordinate of point
     * @return true, if the point [x, y] is an internal point of turtle's shape,
     *         false otherwise
     */
    boolean isPointOfShape(Turtle t, double x, double y);
}
