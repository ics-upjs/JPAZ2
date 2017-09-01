package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import sk.upjs.jpaz2.animators.*;

/**
 * Represents a turtle living on a pane. The turtle provides basic functionality
 * to draw turtle graphics.
 */
public class Turtle implements PaneObject {

	/**
	 * The default turtle shape.
	 */
	private static final TurtleShape DEFAULT_TURTLE_SHAPE = new DefaultTurtleShape();

	/**
	 * The default font used by turtles to print text.
	 */
	private static final Font DEFAULT_FONT = new Font(null, Font.PLAIN, 12);

	/**
	 * Counter for the number of created turtles (it is used only for generating
	 * unique turtle names)
	 */
	private static int turtleCounter = 0;

	// ---------------------------------------------------------------------------------------------------
	// Internal movement/turning animations
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Painter of lines (used during animations).
	 */
	private static class LinePainter implements PanePainter {
		/**
		 * Starting point of line.
		 */
		private final Point2D start;

		/**
		 * End point of line.
		 */
		private final Point2D end;

		/**
		 * Pen used to draw the line.
		 */
		private final PenState penState;

		/**
		 * Constructs the line painter.
		 */
		private LinePainter(Point2D start, Point2D end, PenState penState) {
			this.start = start;
			this.end = end;
			this.penState = penState;
		}

		@Override
		public void paint(Graphics2D graphics) {
			graphics.setStroke(new BasicStroke((float) penState.width));
			graphics.setColor(penState.color);
			graphics.setPaint(null);
			graphics.draw(new Line2D.Double(start, end));
		}
	}

	/**
	 * The move-to animator.
	 */
	private class MoveToMethodAnimator implements WeightedAnimator {

		/**
		 * The start point of the movement.
		 */
		private final Point2D start;

		/**
		 * The end point of the movement.
		 */
		private final Point2D end;

		/**
		 * Difference of the X-coordinates.
		 */
		private final double dx;

		/**
		 * Difference of the Y-coordinates.
		 */
		private final double dy;

		/**
		 * State of the drawing pen.
		 */
		private final PenState penState;

		/**
		 * The drawing pane.
		 */
		private final Pane pane;

		/**
		 * Constructs the move-to animator.
		 * 
		 * @param start
		 *            the start point.
		 * @param end
		 *            the end point.
		 * @param penState
		 *            the immutable state of the drawing pen.
		 */
		public MoveToMethodAnimator(Point2D start, Point2D end, PenState penState) {
			this.start = start;
			this.end = end;
			this.dx = end.getX() - start.getX();
			this.dy = end.getY() - start.getY();
			this.penState = penState;
			this.pane = parentPane;
		}

		@Override
		public void animate(double fraction) {
			Point2D location = getLocation(fraction);
			internalSetPosition(location.getX(), location.getY(), true);

			if (penState.down) {
				pane.setOverlay(Turtle.this, new LinePainter(start, location, penState));
			}
		}

		@Override
		public long getWeight() {
			return Math.round(Math.sqrt(dx * dx + dy * dy));
		}

		/**
		 * Returns overlay painter that is draws result of the animator.
		 * 
		 * @return the overlay painter.
		 */
		public PanePainter getOverlay(double fraction) {
			if (penState.down) {
				return new LinePainter(start, getLocation(fraction), penState);
			} else {
				return null;
			}
		}

		/**
		 * Returns location that corresponds to given animation fraction.
		 * 
		 * @param fraction
		 *            the animation fraction.
		 * @return the location point.
		 */
		private Point2D getLocation(double fraction) {
			if (fraction <= 0) {
				return new Point2D.Double(start.getX(), start.getY());
			}

			if (fraction >= 1.0) {
				return new Point2D.Double(end.getX(), end.getY());
			}

			double currentX = start.getX() + fraction * dx;
			double currentY = start.getY() + fraction * dy;
			return new Point2D.Double(currentX, currentY);
		}
	}

	/**
	 * Composite step animator.
	 */
	private class StepMethodAnimator extends SequenceAnimator {

		/**
		 * Drawing pane where animation overlay is drawn.
		 */
		private final Pane drawingPane;

		/**
		 * Constructs the step animator.
		 */
		public StepMethodAnimator(Pane drawingPane, List<WeightedAnimator> animators) {
			super(animators);
			this.drawingPane = drawingPane;
		}

		@Override
		public void animate(double fraction) {
			AnimatorFraction animatorFraction = getAnimatorForFraction(fraction);

			for (int i = 0; i < animatorFraction.sequenceNumber; i++) {
				getAnimator(i).animate(1.0);
			}
			animatorFraction.animator.animate(animatorFraction.fraction);

			if (drawingPane != null) {
				PanePainter overlayPainter = getOverlay(animatorFraction);
				drawingPane.setOverlay(Turtle.this, overlayPainter);
			}
		}

		/**
		 * Creates animation overlay for given animation fraction.
		 * 
		 * @param fraction
		 *            the fraction of animation.
		 * @return the overlay painter or null, if there is no overlay.
		 */
		private CompositePanePainter getOverlay(double fraction) {
			return getOverlay(getAnimatorForFraction(fraction));
		}

		/**
		 * Creates animation overlay for given animation fraction.
		 * 
		 * @param animatorFraction
		 *            the animation fraction.
		 * @return the overlay painter or null, if there is no overlay.
		 */
		private CompositePanePainter getOverlay(AnimatorFraction animatorFraction) {
			ArrayList<PanePainter> painters = new ArrayList<>();
			for (int i = 0; i < animatorFraction.sequenceNumber; i++) {
				Animator animator = getAnimator(i);
				if (animator instanceof MoveToMethodAnimator) {
					PanePainter overlay = ((MoveToMethodAnimator) animator).getOverlay(1.0);
					if (overlay != null) {
						painters.add(overlay);
					}
				}
			}

			if (animatorFraction.animator instanceof MoveToMethodAnimator) {
				PanePainter overlay = ((MoveToMethodAnimator) animatorFraction.animator)
						.getOverlay(animatorFraction.fraction);
				if (overlay != null) {
					painters.add(overlay);
				}
			}

			if (painters.isEmpty()) {
				return null;
			} else {
				return new CompositePanePainter(painters);
			}
		}
	}

	/**
	 * Turn animator.
	 */
	private class TurnMethodAnimator extends TurnAnimator {

		/**
		 * Constructs turn animator.
		 * 
		 * @param startDirection
		 *            the initial direction in degrees when animation starts
		 * @param endDirection
		 *            the final direction in degrees after animation
		 * @param clockwise
		 *            the direction of rotation (turning).
		 */
		public TurnMethodAnimator(double startDirection, double endDirection, boolean clockwise) {
			super(startDirection, endDirection, clockwise);
		}

		@Override
		public void animate(double fraction) {
			internalSetDirection(computeDirection(fraction), true);
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Internal holder of pen state
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Immutable pen state.
	 */
	private static class PenState {
		/**
		 * Pen width.
		 */
		final double width;

		/**
		 * Pen color.
		 */
		final Color color;

		/**
		 * True, if the pen is down, false otherwise.
		 */
		final boolean down;

		/**
		 * Constructs the pen state.
		 * 
		 * @param turtle
		 *            the turtle whose state is stored.
		 */
		PenState(Turtle turtle) {
			this.width = turtle.penWidth;
			this.color = turtle.penColor;
			this.down = turtle.penDownState;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Turtle state fields
	// ---------------------------------------------------------------------------------------------------

	/**
	 * X-coordinate of the turtle's position
	 */
	private double x = 0;

	/**
	 * Y-coordinate of the turtle's position
	 */
	private double y = 0;

	/**
	 * Direction of turtle in grades
	 */
	private double direction = 0;

	/**
	 * Width of the turtle's pen
	 */
	private double penWidth = 1;

	/**
	 * Color of the turtle's pen
	 */
	private Color penColor = Color.black;

	/**
	 * Color used to fill polygons
	 */
	private Color fillColor = Color.orange;

	/**
	 * State of the turtle's pen
	 */
	private boolean penDownState = true;

	/**
	 * Name of the turtle
	 */
	private String name = null;

	/**
	 * Shape of the turtle
	 */
	private TurtleShape shape = null;

	/**
	 * Visibility of the turtle
	 */
	private boolean visible = true;

	/**
	 * Transparency of the shape - real number between 0 (no transparency) and 1
	 * (transparent).
	 */
	private double transparency = 0;

	/**
	 * Scale of the shape - a non negative number used as a multiplication
	 * scaling factor.
	 */
	private double scale = 1;

	/**
	 * Font used by the turtle to print texts
	 */
	private Font font = DEFAULT_FONT;

	/**
	 * List of points visited by the turtle that will form border of a filled
	 * polygon. If pointsOfPolygon is null, then no points are collecting.
	 */
	private ArrayList<Point2D> pointsOfPolygon = null;

	/**
	 * Pane in which the turtle is living.
	 */
	private Pane parentPane;

	/**
	 * Style how reaching the range border is handled
	 */
	private RangeStyle rangeStyle = RangeStyle.WINDOW;

	/**
	 * Rectangle range of valid tutle's positions. If range is null, the parent
	 * pane is considered as the range border.
	 */
	private Rectangle2D range = null;

	/**
	 * Index of the current view of the turtle's shape.
	 */
	private int viewIndex = 0;

	/**
	 * Index of the current animation frame of the turtle's shape.
	 */
	private int frameIndex = 0;

	/**
	 * Period in milliseconds in which animation frames are changed.
	 */
	private long frameDuration = 100;

	/**
	 * Setting of automatic turle's shape animation.
	 */
	private boolean animatedShape = true;

	/**
	 * Setting of automatic change of shape view according to turtle's
	 * direction.
	 */
	private boolean viewBoundToDirection = false;

	/**
	 * TickTimer utilized to animate the turtle's shape
	 */
	private TickTimer shapeAnimationTimer = null;

	/**
	 * Multiplication factor for move-turn animations. The factor determines
	 * duration of animation (smaller is faster). If the factor is 0, animations
	 * are disabled.
	 */
	private double moveTurnSpeed = 0;

	/**
	 * Currently active move-turn animation.
	 */
	private Animation moveTurnAnimation;

	/**
	 * Reference identification of the instance.
	 */
	protected final String referenceIdentification;

	/**
	 * Result of toString.
	 */
	private volatile String toStringResult;

	// ---------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs new turtle.
	 */
	public Turtle() {
		this(0, 0);
	}

	/**
	 * Constructs new turtle at a given position.
	 * 
	 * @param x
	 *            the X-coordinate of the turtle's position.
	 * @param y
	 *            the Y-coordinate of the turtle's position.
	 */
	public Turtle(double x, double y) {
		this(x, y, generateDefaultTurtleName());
	}

	/**
	 * Constructs new turtle at a given position and with a given name.
	 * 
	 * @param x
	 *            the X-coordinate of the turtle's position.
	 * @param y
	 *            the Y-coordinate of the turtle's position.
	 * @param name
	 *            the name for the turtle.
	 */
	public Turtle(double x, double y, String name) {
		referenceIdentification = JPAZUtilities.retrieveInternalId(super.toString());
		setPosition(x, y);
		setName(name);
		turtleCounter++;
		setShape(DEFAULT_TURTLE_SHAPE);
	}

	// ---------------------------------------------------------------------------------------------------
	// Getters and setters
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Gets the current x-coordinate of the turtle's position. The position is
	 * relative to the coordinate system of the parent pane.
	 * 
	 * @return the x-coordinate of the turtle's position.
	 */
	public double getX() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return x;
		}
	}

	/**
	 * Sets x-coordinate of the turtle's position. The position is relative to
	 * the coordinate system of the parent pane.
	 * 
	 * @param x
	 *            the desired x-coordinate.
	 */
	public void setX(double x) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setPosition(x, y);
		}
	}

	/**
	 * Gets the current y-coordinate of the turtle's position. The position is
	 * relative to the coordinate system of the parent pane.
	 * 
	 * @return the y-coordinate of the turtle's position.
	 */
	public double getY() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return y;
		}
	}

	/**
	 * Sets the y-coordinate of the turtle's position. The position is relative
	 * to the coordinate system of the parent pane.
	 * 
	 * @param y
	 *            the desired y-coordinate.
	 */
	public void setY(double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setPosition(x, y);
		}
	}

	/**
	 * Returns the current direction of the turtle. The degree 0 targets to top
	 * and the value of direction is clockwise increasing.
	 * 
	 * @return the current direction of the turtle in degrees.
	 */
	public double getDirection() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return direction;
		}
	}

	/**
	 * Sets the direction of the turtle. The degree 0 targets to top and the
	 * value of direction is clockwise increasing.
	 * 
	 * @param direction
	 *            the desired direction of the turtle in degrees.
	 */
	public void setDirection(double direction) {
		internalSetDirection(direction, false);
	}

	/**
	 * Internal implementation of the method {@link #setDirection(double)}.
	 * 
	 * @param direction
	 *            the desired direction of the turtle in degrees.
	 * @param inAnimation
	 *            true, if the method is executed as a part of animation.
	 */
	private void internalSetDirection(double direction, boolean inAnimation) {
		direction = JPAZUtilities.normalizeAngleInDegrees(direction);
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!inAnimation) {
				assertNoAnimation();
			}

			if (this.direction != direction) {
				this.direction = direction;

				if (viewBoundToDirection) {
					updateViewAccordingToDirection();
				} else {
					if (visible) {
						invalidateParent();
					}
				}
			}
		}
	}

	/**
	 * Gets the current width of the turtle's pen.
	 * 
	 * @return the width of the pen.
	 */
	public double getPenWidth() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return penWidth;
		}
	}

	/**
	 * Sets width of the turtle's pen.
	 * 
	 * @param penWidth
	 *            the desired width of the turtle's pen. In case of a negative
	 *            value, the value 0 is set.
	 */
	public void setPenWidth(double penWidth) {
		penWidth = Math.max(penWidth, 0);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.penWidth != penWidth) {
				this.penWidth = penWidth;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Gets the current color of the turtle's pen.
	 * 
	 * @return the color of the pen.
	 */
	public Color getPenColor() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return penColor;
		}
	}

	/**
	 * Sets the color of the turtle's pen.
	 * 
	 * @param penColor
	 *            the desired pen color.
	 * 
	 * @throws NullPointerException
	 *             if the desired pen color is null.
	 */
	public void setPenColor(Color penColor) {
		if (penColor == null) {
			throw new NullPointerException("Pen color cannot be null.");
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!penColor.equals(this.penColor)) {
				this.penColor = penColor;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Gets whether turtle's pen is down.
	 * 
	 * @return true, if the pen is down, false otherwise.
	 */
	public boolean isPenDown() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return penDownState;
		}
	}

	/**
	 * Sets the state of the turtle's pen.
	 * 
	 * @param penDownState
	 *            true for turtle's pen down, false otherwise.
	 */
	public void setPenDown(boolean penDownState) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.penDownState != penDownState) {
				this.penDownState = penDownState;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Gets whether the turtle (turtle's shape) is visible.
	 * 
	 * @return true, if the turtle is visible, false otherwise.
	 */
	public boolean isVisible() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return visible;
		}
	}

	/**
	 * Sets the visibility of the turtle.
	 * 
	 * @param visible
	 *            true for visible turtle, false for invisible turle
	 */
	public void setVisible(boolean visible) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.visible != visible) {
				this.visible = visible;
				updateAnimationTimer();
				invalidateParent();
			}
		}
	}

	/**
	 * Gets the current shape of the turtle.
	 * 
	 * @return the shape of this turtle.
	 */
	public TurtleShape getShape() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return shape;
		}
	}

	/**
	 * Sets new turtle's shape.
	 * 
	 * @param shape
	 *            the desired shape of the turtle. If set to null, the default
	 *            turtle shape is used.
	 */
	public void setShape(TurtleShape shape) {
		if (shape == null) {
			shape = DEFAULT_TURTLE_SHAPE;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((this.shape == null) || (!this.shape.equals(shape))) {
				this.shape = shape;

				frameIndex = 0;
				viewIndex = 0;
				setFrameDuration(this.shape.getFrameDuration());
				updateAnimationTimer();

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Gets the current transparency of the turtle's shape. The transparency is
	 * a real number between 0 (no transparency) and 1 (fully transparent).
	 * 
	 * @return the transparency of the turtle's shape.
	 */
	public double getTransparency() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return transparency;
		}
	}

	/**
	 * Sets the turtles's shape transparency. The transparency is a real number
	 * between 0 (no transparency) and 1 (fully transparent).
	 * 
	 * @param transparency
	 *            the desired shape transparency. If it is a number smaller than
	 *            0, the value 0 is set. If it is a number greater than 1, the
	 *            value 1 is set.
	 */
	public void setTransparency(double transparency) {
		transparency = Math.max(0, transparency);
		transparency = Math.min(transparency, 1);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.transparency != transparency) {
				this.transparency = transparency;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Sets the shape scale. The scale is a non negative number used as a
	 * multiplication factor for resizing the current turtle's shape.
	 * 
	 * @param scale
	 *            the desired shape scale.
	 */
	public void setScale(double scale) {
		scale = Math.abs(scale);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.scale != scale) {
				this.scale = scale;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Returns the shape scale. The scale is a non negative number used as a
	 * multiplication factor for resizing the current turtle's shape.
	 * 
	 * @return the scale.
	 */
	public double getScale() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return scale;
		}
	}

	/**
	 * Gets the turle's name.
	 * 
	 * @return the name of the turtle.
	 */
	public String getName() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return name;
		}
	}

	/**
	 * Sets the turtle's name.
	 * 
	 * @param name
	 *            the desired turtle name (String or null).
	 */
	public void setName(String name) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			boolean isChange;
			if (name == null) {
				isChange = (this.name != null);
			} else {
				isChange = !name.equals(this.name);
			}

			if (isChange) {
				this.name = name;

				if (visible) {
					invalidateParent();
				}

				if (name != null) {
					toStringResult = name + " (@" + referenceIdentification + ")";
				} else {
					toStringResult = super.toString();
				}
			}
		}
	}

	/**
	 * Gets the font used by the turtle for drawing strings.
	 * 
	 * @return the font used to print or null for the default font.
	 */
	public Font getFont() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return font;
		}
	}

	/**
	 * Sets the font used by the turtle for drawing strings.
	 * 
	 * @param font
	 *            the desired font
	 */
	public void setFont(Font font) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			boolean isChange;
			if (font == null) {
				isChange = (this.font != null);
			} else {
				isChange = !font.equals(this.font);
			}

			if (isChange) {
				this.font = font;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Gets whether the turtle is currently collecting points for drawing a
	 * filled polygon.
	 * 
	 * @return true, if the turtle has an open polygon, false otherwise.
	 */
	public boolean isPolygonOpen() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return (pointsOfPolygon != null);
		}
	}

	/**
	 * Gets the range style. The range style determines what happens if the
	 * turtle reaches the range border while doing a step.
	 * 
	 * @return the range style.
	 */
	public RangeStyle getRangeStyle() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return rangeStyle;
		}
	}

	/**
	 * Sets the range style. The range style determines what happens if the
	 * turtle reaches the range border while doing a step.
	 * 
	 * @param rangeStyle
	 *            the desired range style.
	 */
	public void setRangeStyle(RangeStyle rangeStyle) {
		if (rangeStyle == null) {
			rangeStyle = RangeStyle.WINDOW;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();

			if (!this.rangeStyle.equals(rangeStyle)) {
				this.rangeStyle = rangeStyle;
				setPosition(x, y);
			}
		}
	}

	/**
	 * Gets the turtle's range. The shape of range is always a rectangle. The
	 * turtle cannot be located out of range with exception of WINDOW range
	 * style.
	 * 
	 * @return the turtle's range or null, if no range is set.
	 */
	public Rectangle2D getRange() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (range == null) {
				return null;
			} else {
				Rectangle2D result = new Rectangle2D.Double();
				result.setRect(range);
				return result;
			}
		}
	}

	/**
	 * Sets the turtle's range. The shape of range is always a rectangle. The
	 * turtle cannot be located out of range with exception of WINDOW range
	 * style. If the range is null, the parent pane determines the range.
	 * 
	 * @param range
	 *            the desired range rectangle or null if the range is determined
	 *            by the parent pane.
	 */
	public void setRange(Rectangle2D range) {
		if (range != null) {
			Rectangle2D copyRange = new Rectangle2D.Double();
			copyRange.setRect(range);
			range = copyRange;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();

			boolean isChange;
			if (range == null) {
				isChange = (this.range != null);
			} else {
				isChange = !range.equals(this.range);
			}

			if (isChange) {
				this.range = range;
				setPosition(x, y);
			}
		}
	}

	/**
	 * Gets the color used to fill polygons.
	 * 
	 * @return the fill color.
	 */
	public Color getFillColor() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return fillColor;
		}
	}

	/**
	 * Sets the color used to fill polygons.
	 * 
	 * @param fillColor
	 *            the desired fill color.
	 * 
	 * @throws NullPointerException
	 *             if the color is set to null.
	 */
	public void setFillColor(Color fillColor) {
		if (fillColor == null) {
			throw new NullPointerException("Fill color cannot be null.");
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!fillColor.equals(this.fillColor)) {
				this.fillColor = fillColor;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Sets the speed of move and turn actions.
	 * 
	 * @param moveTurnSpeed
	 *            the speed of move and turn actions (smaller is faster), the
	 *            value 0 disabled animations during turn and move/step actions.
	 */
	public void setMoveTurnSpeed(double moveTurnSpeed) {
		moveTurnSpeed = Math.max(moveTurnSpeed, 0);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (JPAZUtilities.isHeadlessMode()) {
				this.moveTurnSpeed = 0;
			} else {
				this.moveTurnSpeed = moveTurnSpeed;
			}
		}
	}

	/**
	 * Returns the speed of move and turn actions.
	 * 
	 * @return the speed of move and turn actions.
	 * @see #setMoveTurnSpeed(double)
	 */
	public double getMoveTurnSpeed() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return this.moveTurnSpeed;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Moving actions and ranges
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Turns the turtle in a clockwise direction.
	 * 
	 * @param angle
	 *            angle in degrees.
	 */
	public void turn(double angle) {
		Animation animation = internalTurn(angle);
		if (animation != null) {
			animation.startAndWait();
		}
	}

	/**
	 * Internal method for turning the turtle in a clockwise direction.
	 * 
	 * @param angle
	 *            angle in degrees.
	 * 
	 * @return the animation of turn to be played.
	 */
	private Animation internalTurn(double angle) {
		if (angle == 0) {
			return null;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();

			// handle turn without animations
			if ((moveTurnSpeed <= 0) || (parentPane == null)) {
				setDirection(direction + angle);
				return null;
			}

			// turn with animations
			final double endDirection = direction + angle;
			TurnMethodAnimator animator = new TurnMethodAnimator(direction, endDirection, angle >= 0);
			Animation animation = new Animation(Math.round(animator.getAngleChange() * moveTurnSpeed), animator);
			animation.setFinalizer(new Runnable() {
				@Override
				public void run() {
					synchronized (JPAZUtilities.getJPAZLock()) {
						moveTurnAnimation = null;
						internalSetDirection(endDirection, false);
					}
				}
			});

			moveTurnAnimation = animation;
			return animation;
		}
	}

	/**
	 * Sets the position of the turtle. If the range style is not WINDOW and the
	 * new coordinates are out of range, the new coordinate are the closest
	 * location inside the range.
	 * 
	 * @param point
	 *            the desired location.
	 */
	public void setPosition(Point2D point) {
		setPosition(point.getX(), point.getY());
	}

	/**
	 * Sets the position of the turtle. If the range style is not WINDOW and the
	 * new coordinates are out of range, the new coordinate are the closest
	 * location inside the range.
	 * 
	 * @param x
	 *            the X-coordinate of the desired turtle position.
	 * @param y
	 *            the Y-coordinate of the desired turtle position.
	 */
	public void setPosition(double x, double y) {
		internalSetPosition(x, y, false);
		addPolygonPoint(x, y);
	}

	/**
	 * Internal method for setting position of the turtle.
	 * 
	 * @param x
	 *            the X-coordinate of the desired turtle position.
	 * @param y
	 *            the Y-coordinate of the desired turtle position.
	 * @param inAnimation
	 *            true, if the method is executed as a part of animation, false
	 *            otherwise.
	 */
	private void internalSetPosition(double x, double y, boolean inAnimation) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!inAnimation) {
				assertNoAnimation();
			}

			Point2D position = new Point2D.Double(x, y);
			applyLocationRestrictions(position);

			if ((this.x != position.getX()) || (this.y != position.getY())) {
				this.x = position.getX();
				this.y = position.getY();

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Applies location restrictions for given position that follows from active
	 * ranges and boundaries.
	 * 
	 * @param position
	 *            the position of turtle.
	 */
	private void applyLocationRestrictions(Point2D position) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			// update position to fit active range (only if the range style
			// differs from WINDOW)
			if (rangeStyle != RangeStyle.WINDOW) {
				// compute active range
				Rectangle2D activeRange = range;
				if ((activeRange == null) && (parentPane != null)) {
					activeRange = new Rectangle2D.Double(0, 0, parentPane.getWidth(), parentPane.getHeight());
				}

				// if x and y are not inside the range, change them to fit the
				// range
				if (activeRange != null) {
					if (!isInside(position.getX(), position.getY(), activeRange)) {
						double x = position.getX();
						double y = position.getY();
						x = Math.max(x, activeRange.getMinX());
						x = Math.min(x, activeRange.getMaxX());
						y = Math.max(y, activeRange.getMinY());
						y = Math.min(y, activeRange.getMaxY());
						position.setLocation(x, y);
					}
				}
			}
		}
	}

	/**
	 * Returns the current position of the turtle.
	 * 
	 * @return the position of the turtle.
	 */
	public Point2D getPosition() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return new Point2D.Double(x, y);
		}
	}

	/**
	 * Changes turtle's position to the center of the parent pane. If the turtle
	 * is not living in a pane, the turtle doesn't change its position.
	 */
	public void center() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (parentPane != null) {
				setPosition(parentPane.getWidth() / 2.0, parentPane.getHeight() / 2.0);
			}
		}
	}

	/**
	 * Sets the turtle's pen down.
	 */
	public void penDown() {
		setPenDown(true);
	}

	/**
	 * Sets the turtle's pen up.
	 */
	public void penUp() {
		setPenDown(false);
	}

	/**
	 * Moves the turtle to new position. A line from original position to the
	 * new position is drawn in case that the turtle's pen is down. If the range
	 * style is not WINDOW and the new coordinates are out of range, the new
	 * coordinate are the closest location inside the range.
	 * 
	 * @param x
	 *            the X-coordinate of the target position.
	 * @param y
	 *            the Y-coordinate of the target position.
	 */
	public void moveTo(double x, double y) {
		Animation animation = internalMoveTo(x, y);
		if (animation != null) {
			animation.startAndWait();
		}
	}

	/**
	 * Moves the turtle to new position. A line from original position to the
	 * new position is drawn in case that the turtle's pen is down. If the range
	 * style is not WINDOW and the new coordinates are out of range, the new
	 * coordinate are the closest location inside the range.
	 * 
	 * @param point
	 *            the target position.
	 */
	public void moveTo(Point2D point) {
		moveTo(point.getX(), point.getY());
	}

	/**
	 * Internal method that supports move-to operation with animations.
	 * 
	 * @param x
	 *            the X-coordinate of the target position.
	 * @param y
	 *            the Y-coordinate of the target position.
	 * @return the movement animation.
	 */
	private Animation internalMoveTo(double x, double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();

			final Point2D start = new Point2D.Double(this.x, this.y);
			final Point2D end = new Point2D.Double(x, y);
			applyLocationRestrictions(end);

			// handle move-to without animations
			if ((moveTurnSpeed <= 0) || (parentPane == null)) {
				if (penDownState && (parentPane != null)) {
					parentPane.draw(new Line2D.Double(start, end), new BasicStroke((float) penWidth), penColor, null);
				}

				internalSetPosition(end.getX(), end.getY(), false);
				addPolygonPoint(end.getX(), end.getY());
				return null;
			}

			// create animated move-to
			final PenState penState = new PenState(this);
			MoveToMethodAnimator animator = new MoveToMethodAnimator(start, end, penState);
			Animation animation = new Animation(Math.round(animator.getWeight() * moveTurnSpeed), animator);
			animation.setFinalizer(new Runnable() {
				@Override
				public void run() {
					synchronized (JPAZUtilities.getJPAZLock()) {
						moveTurnAnimation = null;
						parentPane.setOverlay(Turtle.this, null);
						if (penState.down) {
							parentPane.draw(new Line2D.Double(start, end), new BasicStroke((float) penState.width),
									penState.color, null);
						}
						internalSetPosition(end.getX(), end.getY(), false);
						addPolygonPoint(end.getX(), end.getY());
					}
				}
			});

			moveTurnAnimation = animation;
			return animation;
		}
	}

	/**
	 * Moves the turtle in the current direction. The path of the move depends
	 * on the current range style.
	 * 
	 * @param length
	 *            the length of the step.
	 */
	public void step(double length) {
		Animation animation = internalStep(length);
		if (animation != null) {
			animation.startAndWait();
		}
	}

	/**
	 * Internal implementation of the step method.
	 * 
	 * @param length
	 *            the length of the step.
	 * @return the animation to be played.
	 */
	private Animation internalStep(double length) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();

			// compute real range and range style
			Rectangle2D realRange = range;
			RangeStyle realRangeStyle = rangeStyle;

			if ((realRange == null) && (parentPane != null)) {
				realRange = new Rectangle2D.Double(0, 0, parentPane.getWidth(), parentPane.getHeight());
			}

			if (realRange == null) {
				realRangeStyle = RangeStyle.WINDOW;
			}

			// case of a simple move
			if (realRangeStyle == RangeStyle.WINDOW) {
				return internalMoveTo(x + length * JPAZUtilities.degreeCos(90 - direction),
						y - length * JPAZUtilities.degreeSin(90 - direction));
			}

			// -- otherwise, the step is more complicated

			// create list of animators that form the step
			ArrayList<WeightedAnimator> animators = null;
			PenState penState = null;
			if ((moveTurnSpeed > 0) && (parentPane != null)) {
				animators = new ArrayList<>();
				penState = new PenState(this);
			}

			// set "moving" variables
			double currentX = x;
			double currentY = y;
			double movingDirection = 90 - direction;
			double turtleDirection = direction;
			boolean stepBack = false;
			if (length < 0) {
				stepBack = true;
				length = -length;
				movingDirection += 180;
			}

			// while there is a positive distance to go, we just go
			while (length > 0) {
				movingDirection = JPAZUtilities.normalizeAngleInDegrees(movingDirection);

				// compute normalized coordinates of the move
				double xMove = JPAZUtilities.degreeCos(movingDirection);
				double yMove = JPAZUtilities.degreeSin(movingDirection);

				// identifier of the crossing range border
				int crossingBorder = 0; // 1, 2, 3, and 4 for top, right,
				// bottom, and left
				// length of the current step
				double stepLength = length;

				// end-point of the step
				double targetX = currentX + length * xMove;
				double targetY = currentY - length * yMove;

				// if the end-point is inside the range, we are done
				if (isInside(targetX, targetY, realRange)) {
					if (animators != null) {
						Point2D targetPoint = new Point2D.Double(targetX, targetY);
						applyLocationRestrictions(targetPoint);
						animators.add(new MoveToMethodAnimator(new Point2D.Double(currentX, currentY), targetPoint,
								penState));
						addPolygonPoint(targetX, targetY);
					} else {
						moveTo(targetX, targetY);
					}

					// if bounce mode (without animations), we change the
					// direction
					if ((realRangeStyle == RangeStyle.BOUNCE) && (animators == null)) {
						setDirection(stepBack ? 270 - movingDirection : 90 - movingDirection);
					}

					currentX = targetX;
					currentY = targetY;
					break;
				}

				// if the end-point is outside the range, we have to reduce
				// length of the step to finish on the range border
				if (yMove > 0) {
					double d = Math.abs((currentY - realRange.getMinY()) / yMove);
					if (d <= stepLength) {
						crossingBorder = 1;
						stepLength = d;
					}
				} else if (yMove < 0) {
					double d = Math.abs((realRange.getMaxY() - currentY) / yMove);
					if (d <= stepLength) {
						crossingBorder = 3;
						stepLength = d;
					}
				}

				if (xMove > 0) {
					double d = Math.abs((realRange.getMaxX() - currentX) / xMove);
					if (d <= stepLength) {
						crossingBorder = 2;
						stepLength = d;
					}
				} else if (xMove < 0) {
					double d = Math.abs((currentX - realRange.getMinX()) / xMove);
					if (d <= stepLength) {
						crossingBorder = 4;
						stepLength = d;
					}
				}

				targetX = currentX + stepLength * xMove;
				targetY = currentY - stepLength * yMove;

				targetX = Math.min(Math.max(targetX, realRange.getMinX()), realRange.getMaxX());
				targetY = Math.min(Math.max(targetY, realRange.getMinY()), realRange.getMaxY());

				if (animators != null) {
					Point2D targetPoint = new Point2D.Double(targetX, targetY);
					applyLocationRestrictions(targetPoint);
					animators.add(
							new MoveToMethodAnimator(new Point2D.Double(currentX, currentY), targetPoint, penState));
					addPolygonPoint(targetX, targetY);
				} else {
					moveTo(targetX, targetY);
				}
				length -= stepLength;

				// change position in case of the WRAP mode
				if ((realRangeStyle == RangeStyle.WRAP) && (crossingBorder != 0)) {
					if (crossingBorder == 1) {
						targetY = realRange.getMaxY();
					} else if (crossingBorder == 2) {
						targetX = realRange.getMinX();
					} else if (crossingBorder == 3) {
						targetY = realRange.getMinY();
					} else if (crossingBorder == 4) {
						targetX = realRange.getMaxX();
					}

					if (animators == null) {
						setPosition(targetX, targetY);
					} else {
						addPolygonPoint(targetX, targetY);
					}
				}

				// change moving direction in case of the BOUNCE mode
				if ((realRangeStyle == RangeStyle.BOUNCE) && (crossingBorder != 0)) {
					if ((crossingBorder == 1) || (crossingBorder == 3)) {
						movingDirection = -movingDirection;
					} else if ((crossingBorder == 2) || (crossingBorder == 4)) {
						movingDirection = 180 - movingDirection;
					}

					double newTurtleDirection = JPAZUtilities
							.normalizeAngleInDegrees(stepBack ? 270 - movingDirection : 90 - movingDirection);
					if ((turtleDirection != newTurtleDirection) && (animators != null)) {
						boolean clockwise = JPAZUtilities
								.normalizeAngleInDegrees(newTurtleDirection - turtleDirection) < 180;
						animators.add(new TurnMethodAnimator(turtleDirection, newTurtleDirection, clockwise));
					}

					turtleDirection = newTurtleDirection;
				}

				currentX = targetX;
				currentY = targetY;

				if (realRangeStyle == RangeStyle.FENCE) {
					break;
				}
			}

			// create step animation as composition of move-to and turn
			// animations
			if (animators != null) {
				final StepMethodAnimator stepAnimator = new StepMethodAnimator(parentPane, animators);
				Animation animation = new Animation(Math.round(stepAnimator.getWeight() * moveTurnSpeed), stepAnimator);

				final Point2D finalPoint = new Point2D.Double(currentX, currentY);
				final double finalDirection = turtleDirection;
				animation.setFinalizer(new Runnable() {
					@Override
					public void run() {
						synchronized (JPAZUtilities.getJPAZLock()) {
							moveTurnAnimation = null;

							parentPane.setOverlay(Turtle.this, null);
							internalSetDirection(finalDirection, false);
							internalSetPosition(finalPoint.getX(), finalPoint.getY(), false);

							PanePainter painter = stepAnimator.getOverlay(1.0);
							if (painter != null) {
								parentPane.paint(painter);
							}
						}
					}
				});

				moveTurnAnimation = animation;
				return animation;
			}
		}

		return null;
	}

	/**
	 * Computes distance from turtle to the specified location.
	 * 
	 * @param x
	 *            the x-coordinate of the location.
	 * @param y
	 *            the y-coordinate of the location.
	 * 
	 * @return the computed distance.
	 */
	public double distanceTo(double x, double y) {
		Point2D position = getPosition();
		return Math.sqrt(Math
				.abs((position.getX() - x) * (position.getX() - x) + (position.getY() - y) * (position.getY() - y)));
	}

	/**
	 * Computes distance from turtle to the specified location.
	 * 
	 * @param point
	 *            the location.
	 * 
	 * @return the computed distance.
	 */
	public double distanceTo(Point2D point) {
		return distanceTo(point.getX(), point.getY());
	}

	/**
	 * Computes the direction towards the specified location.
	 * 
	 * @param x
	 *            the x-coordinate of the location.
	 * @param y
	 *            the y-coordinate of the location.
	 * 
	 * @return the direction (absolute angle in degrees).
	 */
	public double directionTowards(double x, double y) {
		Point2D position = getPosition();

		double dx = x - position.getX();
		double dy = y - position.getY();
		double distance = Math.sqrt(Math
				.abs((position.getX() - x) * (position.getX() - x) + (position.getY() - y) * (position.getY() - y)));

		if (distance == 0) {
			return 0;
		}

		dx = dx / distance;
		dy = dy / distance;

		double acos = Math.toDegrees(Math.acos(dx));
		double result = dy > 0 ? 360 - acos : acos;
		result = (90 - result) % 360.0;
		if (result < 0) {
			result += 360;
		}

		return result;
	}

	/**
	 * Computes the direction towards the specified location.
	 * 
	 * @param point
	 *            the location.
	 * 
	 * @return the direction (absolute angle in degrees).
	 */
	public double directionTowards(Point2D point) {
		return directionTowards(point.getX(), point.getY());
	}

	/**
	 * Turns this turtle towards the specified location.
	 * 
	 * @param point
	 *            the location.
	 */
	public void turnTowards(Point2D point) {
		turnTowards(point.getX(), point.getY());
	}

	/**
	 * Turns this turtle towards the specified location.
	 * 
	 * @param x
	 *            the X-coordinate of the location.
	 * @param y
	 *            the Y-coordinate of the location.
	 */
	public void turnTowards(double x, double y) {
		Animation animation = internalTurnTowards(x, y);
		if (animation != null) {
			animation.startAndWait();
		}
	}

	/**
	 * Internally method for turning this turtle towards the specified location.
	 * 
	 * @param x
	 *            the X-coordinate of the location.
	 * @param y
	 *            the Y-coordinate of the location.
	 * @return the animation to be played.
	 */
	private Animation internalTurnTowards(double x, double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			double turnChange = JPAZUtilities.normalizeAngleInDegrees(directionTowards(x, y) - direction);
			if (turnChange > 180) {
				turnChange -= 360;
			}

			return internalTurn(turnChange);
		}
	}

	/**
	 * Sets direction of the turtle towards the specified location.
	 * 
	 * @param x
	 *            the X-coordinate of the location.
	 * @param y
	 *            the Y-coordinate of the location.
	 */
	public void setDirectionTowards(double x, double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setDirection(directionTowards(x, y));
		}
	}

	/**
	 * Sets direction of the turtle towards the specified location.
	 * 
	 * @param point
	 *            the location.
	 */
	public void setDirectionTowards(Point2D point) {
		setDirectionTowards(point.getX(), point.getY());
	}

	/**
	 * Adds point to polygon.
	 * 
	 * @param x
	 *            the X-coordinate of the polygon point.
	 * @param y
	 *            the Y-coordinate of the polygon point.
	 */
	private void addPolygonPoint(double x, double y) {
		if (pointsOfPolygon != null) {
			pointsOfPolygon.add(new Point2D.Double(x, y));
		}
	}

	/**
	 * Throws exception if there is an active animation.
	 */
	private void assertNoAnimation() {
		if (moveTurnAnimation != null) {
			throw new IllegalStateException("The turtle is animated.");
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Painting and printing actions
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Prints a text at turtle's position and in the turtle's direction.
	 * 
	 * @param message
	 *            the message to be printed.
	 */
	public void print(String message) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			if (parentPane != null) {
				parentPane.drawString(new Point2D.Double(x, y), direction, message, font, penColor, false);
			}
		}
	}

	/**
	 * Prints centered text at turtle's position and in the turtle's direction.
	 * 
	 * @param message
	 *            the message to be printed.
	 */
	public void printCenter(String message) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			if (parentPane != null) {
				parentPane.drawString(new Point2D.Double(x, y), direction, message, font, penColor, true);
			}
		}
	}

	/**
	 * Returns width of the message in pixels after printing this message using
	 * current turtle's font.
	 * 
	 * @param message
	 *            the message.
	 * 
	 * @return the width of message in pixels.
	 */
	public int textWidth(String message) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (parentPane == null) {
				return 0;
			}

			FontMetrics metrics = parentPane.getFontMetrics(font);
			return metrics.stringWidth(message);
		}
	}

	/**
	 * Returns height of the current turtle's font.
	 * 
	 * @return the height of current turtle's font.
	 */
	public int getTextHeight() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (parentPane == null) {
				return 0;
			}

			FontMetrics metrics = parentPane.getFontMetrics(font);
			return metrics.getHeight();
		}
	}

	/**
	 * Starts collecting the points of a polygon. The border of the polygon is
	 * determined future turtle's moves.
	 */
	public void openPolygon() {
		closePolygon();
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			pointsOfPolygon = new ArrayList<Point2D>();
			pointsOfPolygon.add(getPosition());
		}
	}

	/**
	 * Stops collecting the points of a polygon and paints a filled polygon
	 * whose border are determined by collected points.
	 */
	public void closePolygon() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			if (pointsOfPolygon == null) {
				return;
			}

			if (parentPane != null) {
				// prepare polygon shape
				Polygon polygon = new Polygon();
				for (Point2D point : pointsOfPolygon) {
					polygon.addPoint((int) Math.round(point.getX()), (int) Math.round(point.getY()));
				}

				parentPane.fill(polygon, new BasicStroke((float) penWidth), penColor, fillColor);
			}

			pointsOfPolygon = null;
		}
	}

	/**
	 * Paints a filled circle with specified radius.
	 * 
	 * @param radius
	 *            the radius of the circle.
	 */
	public void dot(double radius) {
		if (radius < 0) {
			radius = 0;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			if (parentPane != null) {
				parentPane.fill(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius),
						new BasicStroke((float) penWidth), penColor, fillColor);
			}
		}
	}

	/**
	 * Paints a circle with specified radius.
	 * 
	 * @param radius
	 *            the radius of the circle.
	 */
	public void circle(double radius) {
		if (radius < 0) {
			radius = 0;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			if (parentPane != null) {
				parentPane.draw(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius),
						new BasicStroke((float) penWidth), penColor, null);
			}
		}
	}

	/**
	 * Paints the shape to the parent pane.
	 */
	public void stamp() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();
			if (parentPane != null) {
				parentPane.paint(new PanePainter() {
					public void paint(Graphics2D graphics) {
						shape.paintTurtle(Turtle.this, graphics);
					}
				});
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Methods related to managing connection with the parent pane
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Paints the shape of turtle to a graphics at position relative to parent's
	 * coordinate system.
	 * 
	 * @param g
	 *            the graphics where the content is drawn.
	 */
	public void paintToPaneGraphics(Graphics2D g) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (visible && (shape != null)) {
				shape.paintTurtle(this, g);
			}
		}
	}

	/**
	 * Gets the pane to which this turtle belongs.
	 * 
	 * @return the parent pane.
	 */
	public Pane getPane() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return parentPane;
		}
	}

	/**
	 * Sets the parent pane, i.e., the containing pane.
	 * 
	 * @param newParentPane
	 *            the desired parent pane.
	 */
	public void setPane(Pane newParentPane) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			assertNoAnimation();

			if (parentPane != newParentPane) {
				// say former parent about going away
				if (parentPane != null) {
					parentPane.remove(this);
				}

				// change parent
				parentPane = newParentPane;

				// say new parent about joining
				if (newParentPane != null) {
					newParentPane.add(this);
				}

				updateAnimationTimer();
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Methods for automatic change of view according to the turtle's direction
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns whether view on shape is bound to turtle's direction.
	 * 
	 * @return true, if view is bound to directions, false otherwise.
	 */
	public boolean isViewBoundToDirection() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return viewBoundToDirection;
		}
	}

	/**
	 * Sets whether view on shape is bound to turtle's direction.
	 * 
	 * @param bound
	 *            desired binding state.
	 */
	public void setViewBoundToDirection(boolean bound) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.viewBoundToDirection != bound) {
				this.viewBoundToDirection = bound;

				if (bound) {
					updateViewAccordingToDirection();
				}

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Updates shape's view index according to current turtle's direction.
	 */
	private void updateViewAccordingToDirection() {
		int viewCount = shape.getViewCount();
		if (viewCount <= 1) {
			return;
		}

		double viewAngle = 360.0 / viewCount;
		double shiftedDirection = (this.direction + viewAngle / 2) % 360.0;
		setViewIndex((int) (shiftedDirection / viewAngle));
	}

	// ---------------------------------------------------------------------------------------------------
	// Methods for automatic animation of turtle shape
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns the index of the current view of the turtle shape. Views are
	 * indexed from 0.
	 * 
	 * @return the index of the current view.
	 */
	public int getViewIndex() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return viewIndex;
		}
	}

	/**
	 * Sets the index of the current view of the turtle shape. Views are indexed
	 * from 0.
	 * 
	 * @param viewIndex
	 *            the index of desired view on the turtle shape.
	 */
	public void setViewIndex(int viewIndex) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			int viewCount = shape.getViewCount();
			viewIndex = viewIndex % viewCount;
			if (viewIndex < 0) {
				viewIndex = viewCount + viewIndex;
			}

			if (this.viewIndex != viewIndex) {
				this.viewIndex = viewIndex;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Returns the number of views in the current turtle shape. Shape can
	 * consist of several shape views, e.g. for each direction of the turtle.
	 * 
	 * @return the number of views in the current turtle shape.
	 */
	public int getViewCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return shape.getViewCount();
		}
	}

	/**
	 * Returns the index of the current animation frame of the turtle's shape
	 * view. Animation frames are indexed from 0.
	 * 
	 * @return the index of the current animation frame.
	 */
	public int getFrameIndex() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameIndex;
		}
	}

	/**
	 * Sets the index of the current animation frame of the turtle's shape view.
	 * Animation frames are indexed from 0.
	 * 
	 * @param frameIndex
	 *            the index of desired frame in the current view on the turtle
	 *            shape.
	 */
	public void setFrameIndex(int frameIndex) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			int frameCount = shape.getFrameCount();
			frameIndex = frameIndex % frameCount;
			if (frameIndex < 0) {
				frameIndex = frameCount + frameIndex;
			}

			if (this.frameIndex != frameIndex) {
				this.frameIndex = frameIndex;

				if (visible) {
					invalidateParent();
				}
			}
		}
	}

	/**
	 * Returns the number of animation frames in the current turtle shape. Each
	 * view of turtle shape can consist of several animation frames.
	 * 
	 * @return the number of frames in the current turtle shape.
	 */
	public int getFrameCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return shape.getFrameCount();
		}
	}

	/**
	 * Returns the frame duration in milliseconds, i.e., the period between two
	 * changes of animation frames for shapes with multiple frames.
	 * 
	 * @return the frame duration in milliseconds.
	 */
	public long getFrameDuration() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameDuration;
		}
	}

	/**
	 * Sets the frame duration in milliseconds, i.e., the period between two
	 * changes of consecutive animation frames of the shape with multiple
	 * frames. A negative value and 0 will stop shape animation.
	 * 
	 * @param frameDuration
	 *            the desired frame duration in milliseconds.
	 */
	public void setFrameDuration(long frameDuration) {
		if (frameDuration < 0) {
			frameDuration = 0;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.frameDuration == frameDuration) {
				return;
			}

			this.frameDuration = frameDuration;
			updateAnimationTimer();
		}
	}

	/**
	 * Returns whether automatic animation of shapes is enabled.
	 * 
	 * @return true, if automatic animation of shapes is enabled, false
	 *         otherwise.
	 */
	public boolean isShapeAnimation() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return animatedShape;
		}
	}

	/**
	 * Sets the state of automatic animation of shapes.
	 * 
	 * @param enabled
	 *            true for enabling automatic animation, false for disabling.
	 */
	public void setShapeAnimation(boolean enabled) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.animatedShape == enabled) {
				return;
			}

			this.animatedShape = enabled;
			updateAnimationTimer();
		}
	}

	/**
	 * Updates animation timer.
	 */
	private void updateAnimationTimer() {
		boolean activeAnimation = animatedShape && (getFrameCount() > 1) && (frameDuration > 0) && (visible)
				&& (parentPane != null);

		if (activeAnimation) {
			// create animation timer, if necessary
			if (shapeAnimationTimer == null) {
				shapeAnimationTimer = new TickTimer(false) {
					@Override
					protected void onTick() {
						synchronized (JPAZUtilities.getJPAZLock()) {
							setFrameIndex(getFrameIndex() + 1);
						}
					}
				};
			}

			// activate timer and set tick period
			shapeAnimationTimer.setTickPeriod(frameDuration);
			shapeAnimationTimer.setEnabled(true);
		} else {
			// disable timer, if timer exists
			if (shapeAnimationTimer != null) {
				shapeAnimationTimer.setEnabled(false);
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Other useful methods
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns whether the given point is a point on the turtle's shape.
	 * 
	 * @param x
	 *            the x-coordinate of the point.
	 * @param y
	 *            the y-coordinate of the point.
	 * @return true, if the given point is lying on the turtle's shape, false
	 *         otherwise.
	 */
	public boolean containsInShape(double x, double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (shape != null) {
				return shape.isPointOfShape(this, x, y);
			} else {
				return false;
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Supporting methods
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Calls invalidate method of the parent pane.
	 */
	protected void invalidateParent() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (parentPane != null) {
				parentPane.invalidate();
			}
		}
	}

	@Override
	public String toString() {
		return toStringResult;
	}

	/**
	 * Generates the default turtle name for the last created turtle.
	 */
	private static String generateDefaultTurtleName() {
		return "Turtle" + (turtleCounter + 1);
	}

	/**
	 * Returns whether point lies inside rectangle.
	 * 
	 * @param x
	 *            the X-coordinate of the point.
	 * @param y
	 *            the Y-coordinate of the point.
	 * @param rectangle
	 *            the rectangle.
	 * @return true, if the point lies inside rectangle, false otherwise.
	 */
	private static boolean isInside(double x, double y, Rectangle2D rectangle) {
		return (rectangle.getMinX() <= x) && (x <= rectangle.getMaxX()) && (rectangle.getMinY() <= y)
				&& (y <= rectangle.getMaxY());
	}
}
