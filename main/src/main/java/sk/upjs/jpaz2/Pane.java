package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * The Pane represents a rectangle pane with graphical content and ability to
 * host (provide home) for pane objects like other panes, turtles, etc. It
 * provides the methods for basic handling of mouse and keyboard events.
 */
public class Pane implements PaneObject {

	/**
	 * The transparent color, i.e., color with alpha channel set to 0.
	 */
	private static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

	/**
	 * Ordering of mouse buttons.
	 */
	private static final int[] MOUSE_BUTTONS = { MouseEvent.BUTTON1, MouseEvent.BUTTON2, MouseEvent.BUTTON3 };

	/**
	 * Width of the pane in pixels.
	 */
	private int width = 0;
	/**
	 * Height of the pane in pixels.
	 */
	private int height = 0;

	/**
	 * X-coordinate of the pane location.
	 */
	private double x = 0;

	/**
	 * Y-coordinate of the pane location.
	 */
	private double y = 0;

	/**
	 * Coordinates of location of this pane rounded as integers (for
	 * efficiency).
	 */
	private Point roundedLocation = new Point(0, 0);

	/**
	 * Coordinates of center of this pane rounded as integers (for efficiency).
	 */
	private Point roundedCenter = new Point(0, 0);

	/**
	 * The X-coordinate of the center of this pane.
	 */
	private double xCenter = 0;

	/**
	 * The Y-coordinate of the center of this pane.
	 */
	private double yCenter = 0;

	/**
	 * Rotation of the pane in degrees. 0 is the normal pane orientation.
	 */
	private double rotation = 0;

	/**
	 * Transform for transforming parent's pane coordinates to this pane
	 * coordinates.
	 */
	private AffineTransform coordinatesTransform = null;

	/**
	 * Border width. The border is drawn over the content of the pane, i.e., it
	 * can hide a part of the pane content.
	 */
	private int borderWidth = 1;

	/**
	 * Color of the border.
	 */
	private Color borderColor = Color.black;

	/**
	 * Transparency of the pane - real number between 0 (no transparency) and 1
	 * (transparent).
	 */
	private double transparency = 0;

	/**
	 * Background color of the pane.
	 */
	private Color backgroundColor = Color.WHITE;

	/**
	 * Determines whether the pane is drawn with transparent background (pane is
	 * like a glass pane).
	 */
	private boolean transparentBackground = false;

	/**
	 * Composite that reflects current value of transparency. It is used for
	 * rendering the pane.
	 */
	private AlphaComposite drawingComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

	/**
	 * Image that stores content of the pane.
	 */
	private BufferedImage content = null;

	/**
	 * Image that stores content of the pane together with all pane's objects.
	 */
	private BufferedImage backBuffer = null;

	/**
	 * Map of drawable overlays.
	 */
	private final Map<Object, PanePainter> overlays = new HashMap<>();

	/**
	 * True, if the antialiasing is on, false otherwise.
	 */
	private boolean antialiased = true;

	/**
	 * Indicates that content stored in the backBuffer is no longer valid.
	 */
	private boolean invalidated = true;

	/**
	 * Determines whether the pane is transparent for mouse events, i.e., any
	 * mouse event is forwarded also to pane under this pane.
	 */
	private boolean mouseTransparent = true;

	/**
	 * Indicates for each mouse button whether the button is currently hold
	 * (pressed).
	 */
	private final boolean[] holdMouseButtons = new boolean[3];

	/**
	 * X-coordinate of the last known position of mouse cursor over the pane.
	 */
	private int lastMouseX;

	/**
	 * Y-coordinate of the last known position of mouse cursor over the pane.
	 */
	private int lastMouseY;

	/**
	 * Last known mouse event.
	 */
	private MouseEvent lastMouseEvent;

	/**
	 * Parent pane which is this pane located on.
	 */
	private Pane parentPane = null;

	/**
	 * List of all objects living on the pane. With increasing index of an
	 * object decreases z-index of the object, i.e., last object is in front of
	 * all other objects.
	 */
	private final java.util.List<PaneObject> children = new ArrayList<PaneObject>();

	/**
	 * List of all children turtles living on the pane. This list is only a
	 * sublist of the children list. It is maintained due to performance
	 * reasons.
	 */
	private final java.util.List<Turtle> turtles = new ArrayList<Turtle>();

	/**
	 * List of all children panes living on the pane. This list is only a
	 * sublist of the children list. It is maintained due to performance
	 * reasons.
	 */
	private final java.util.List<Pane> panes = new ArrayList<Pane>();

	/**
	 * List of registered listeners that listen to changes in this pane.
	 */
	private final java.util.List<PaneChangeListener> changeListeners = new ArrayList<PaneChangeListener>();

	/**
	 * TickTimer that invokes ticks for this pane.
	 */
	private final TickTimer tickTimer;

	/**
	 * Manager of key events that is responsible for correct handling of key
	 * events independently of OS.
	 */
	private final KeyEventManager keyEventManager;

	/**
	 * Indicates whether turtles should be centered when added to the pane.
	 */
	private boolean turtleCentering = true;
	
	/**
	 * Simple name of the class.
	 */
	private final String className;

	/**
	 * Reference identification of the instance.
	 */
	protected final String referenceIdentification;

	// ---------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs a pane with predefined size.
	 */
	public Pane() {
		this(100, 100);
	}

	/**
	 * Constructs a pane at position [0, 0] with center in its top-left corner.
	 * 
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 */
	public Pane(int width, int height) {
		this(0, 0, width, height);
	}

	/**
	 * Constructs a pane at a given position. Center of the pane is set to its
	 * top-left corner.
	 * 
	 * @param x
	 *            the x-coordinate of the top-left corner of the pane
	 * @param y
	 *            the y-coordinate of the top-left corner of the pane
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 */
	public Pane(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;

		// set identifications
		referenceIdentification = JPAZUtilities.retrieveInternalId(super.toString());
		if (this.getClass() != null) {
			className = this.getClass().getSimpleName();
		} else {
			className = "Pane";
		}

		// lock headless mode
		JPAZUtilities.lockHeadlessMode();

		// prepare tick timer
		tickTimer = new TickTimer(this.toString()) {
			protected void onTick() {
				Pane.this.onTick();
			};
		};

		// prepare manager of key events
		keyEventManager = new KeyEventManager() {
			protected void fireKeyEvent(int type, KeyEvent evt) {
				Pane.this.processKeyEvent(type, evt);
			}
		};

		resize(width, height);
	}

	// ---------------------------------------------------------------------------------------------------
	// Dimensions and location
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Gets the current width of this pane.
	 * 
	 * @return the with of this pane.
	 */
	public int getWidth() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return width;
		}
	}

	/**
	 * Sets new width of this pane. The change of the width resizes this pane.
	 * 
	 * @param width
	 *            the desired width of the pane.
	 */
	public void setWidth(int width) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			resize(width, height);
		}
	}

	/**
	 * Gets the current height of this pane.
	 * 
	 * @return the height of this pane.
	 */
	public int getHeight() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return height;
		}
	}

	/**
	 * Sets new height of this pane. The change of the height resizes this pane.
	 * 
	 * @param height
	 *            the desired height of the pane.
	 */
	public void setHeight(int height) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			resize(width, height);
		}
	}

	/**
	 * Gets the x-coordinate of this pane relative to the parent pane. The
	 * position of the pane is determined by the position of its center.
	 * 
	 * @return the x-coordinate of the pane.
	 */
	public double getX() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return x;
		}
	}

	/**
	 * Sets the new x-coordinate of this pane. The position of the pane is
	 * determined by the position of its center.
	 * 
	 * @param x
	 *            the desired x-coordinate of the pane.
	 */
	public void setX(double x) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setPosition(x, y);
		}
	}

	/**
	 * Gets the y-coordinate of this pane relative to the parent pane. The
	 * position of the pane is determined by the position of its center.
	 * 
	 * @return the y-coordinate of the pane.
	 */
	public double getY() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return y;
		}
	}

	/**
	 * Sets the new y-coordinate of this pane. The position of the pane is
	 * determined by the position of its center.
	 * 
	 * @param y
	 *            the desired y-coordinate of the pane.
	 */
	public void setY(double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setPosition(x, y);
		}
	}

	/**
	 * Gets the x-coordinate of the center of this pane. The location of center
	 * is specified relative to coordinates of this pane.
	 * 
	 * @return the x-coordinate of the pane's center.
	 */
	public double getXCenter() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return xCenter;
		}
	}

	/**
	 * Sets the x-coordinate of the center of this pane. The location of the
	 * center is specified with respect to coordinate system of this pane.
	 * 
	 * @param xCenter
	 *            the desired x-coordinate of the pane's center.
	 */
	public void setXCenter(double xCenter) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setCenter(xCenter, yCenter);
		}
	}

	/**
	 * Gets the y-coordinate of the center of this pane. The location of center
	 * is specified relative to coordinates of this pane.
	 * 
	 * @return the y-coordinate of the pane's center.
	 */
	public double getYCenter() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return yCenter;
		}
	}

	/**
	 * Sets the y-coordinate of the center of this pane. The location of the
	 * center is specified with respect to coordinate system of this pane.
	 * 
	 * @param yCenter
	 *            the desired y-coordinate of the pane's center.
	 */
	public void setYCenter(double yCenter) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setCenter(xCenter, yCenter);
		}
	}

	/**
	 * Sets the center of this pane. The location of the center is specified
	 * relative to coordinate system of this pane.
	 * 
	 * @param center
	 *            the desired coordinates of the pane's center.
	 */
	public void setCenter(Point2D center) {
		setCenter(center.getX(), center.getY());
	}

	/**
	 * Gets the coordinates of the pane's center. The location of center is
	 * specified relative to coordinates of this pane.
	 * 
	 * @return the coordinates of the pane's center.
	 */
	public Point2D getCenter() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return new Point2D.Double(xCenter, yCenter);
		}
	}

	/**
	 * Gets the rotation angle of this pane in degrees.
	 * 
	 * @return the rotation angle in degrees.
	 */
	public double getRotation() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return rotation;
		}
	}

	/**
	 * Sets the rotation angle of this pane. The pane is rotated with respect to
	 * position of its center.
	 * 
	 * @param rotation
	 *            the desired rotation angle in degrees.
	 */
	public void setRotation(double rotation) {
		rotation = JPAZUtilities.normalizeAngleInDegrees(rotation);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.rotation != rotation) {
				this.rotation = rotation;

				// notify parent about the change (parent's visual content is
				// not valid)
				if (parentPane != null) {
					parentPane.invalidate();
				}

				recalculateCoordinatesTransform();

				// notify change listeners
				if (!changeListeners.isEmpty()) {
					PaneChangeEvent e = new PaneChangeEvent(this);
					for (PaneChangeListener l : changeListeners) {
						l.paneRotationChanged(e);
					}
				}
			}
		}
	}

	/**
	 * Resizes the pane.
	 * 
	 * @param newWidth
	 *            desired width of the pane.
	 * @param newHeight
	 *            desired height of the pane.
	 */
	public void resize(int newWidth, int newHeight) {
		newWidth = Math.max(newWidth, 1);
		newHeight = Math.max(newHeight, 1);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((this.width == newWidth) && (this.height == newHeight)) {
				return;
			}

			this.width = newWidth;
			this.height = newHeight;
			resizePaneImages(newWidth, newHeight);

			// notify change listeners
			if (!changeListeners.isEmpty()) {
				PaneChangeEvent e = new PaneChangeEvent(this);
				for (PaneChangeListener l : changeListeners) {
					l.paneResized(e);
				}
			}
		}
	}

	/**
	 * Changes the position of the pane. The new position of the pane is
	 * determined by coordinates of its center relative to the coordinate system
	 * of the parent pane.
	 * 
	 * @param x
	 *            the desired x-coordinate.
	 * @param y
	 *            the desired y-coordinate.
	 */
	public void setPosition(double x, double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((x != this.x) || (y != this.y)) {
				this.x = x;
				this.y = y;

				int roundedX = (int) Math.round(x);
				int roundedY = (int) Math.round(y);

				// check whether there is a real visual change of position
				if ((roundedX != roundedLocation.x) || (roundedY != roundedLocation.y)) {
					roundedLocation.x = roundedX;
					roundedLocation.y = roundedY;

					// notify parent about the change (parent's visual content
					// is not valid)
					if (parentPane != null) {
						parentPane.invalidate();
					}
				}

				recalculateCoordinatesTransform();

				// notify change listeners
				if (!changeListeners.isEmpty()) {
					PaneChangeEvent e = new PaneChangeEvent(this);
					for (PaneChangeListener l : changeListeners) {
						l.paneMoved(e);
					}
				}
			}
		}
	}

	/**
	 * Changes the location of the pane. The new position of the pane is
	 * determined by coordinates of its center relative to the coordinate system
	 * of the parent pane.
	 * 
	 * @param position
	 *            the desired position of this pane.
	 */
	public void setPosition(Point2D position) {
		setPosition(position.getX(), position.getY());
	}

	/**
	 * Returns the position of this pane. The position of the pane is determined
	 * by coordinates of its center relative to the coordinate system of the
	 * parent pane.
	 * 
	 * @return the position of this pane.
	 */
	public Point2D getPosition() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return new Point2D.Double(x, y);
		}
	}

	/**
	 * Changes the location of center of this pane. The location of center is
	 * specified relative to coordinate system of this pane. Change of the
	 * center does not move the pane, only position of the pane is recomputed.
	 * 
	 * @param xCenter
	 *            the desired x-coordinate of the center.
	 * @param yCenter
	 *            the desired y-coordinate of the center.
	 */
	public void setCenter(double xCenter, double yCenter) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((xCenter != this.xCenter) || (yCenter != this.yCenter)) {
				double dx = xCenter - this.xCenter;
				double dy = yCenter - this.yCenter;

				this.xCenter = xCenter;
				this.yCenter = yCenter;
				roundedCenter.x = (int) Math.round(xCenter);
				roundedCenter.y = (int) Math.round(yCenter);

				x += dx;
				y += dy;
				roundedLocation.x = (int) Math.round(x);
				roundedLocation.y = (int) Math.round(y);

				recalculateCoordinatesTransform();
			}
		}
	}

	/**
	 * Returns whether the pane contains a point at given coordinates. The
	 * coordinates are expressed with respect to parent pane's coordinate
	 * system.
	 * 
	 * @param x
	 *            the x-coordinate of the point.
	 * @param y
	 *            the y-coordinate of the point.
	 * @return true, if the point is covered (contained) by the pane, false
	 *         otherwise.
	 */
	public boolean containsPoint(int x, int y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			Point transformedPoint = transformCoordinates(x, y);
			return (transformedPoint.x >= 0) && (transformedPoint.y >= 0) && (transformedPoint.x < width)
					&& (transformedPoint.y < height);
		}
	}

	/**
	 * Transforms coordinates in the parent's pane to coordinates of this pane.
	 * 
	 * @return the transformed coordinates.
	 */
	private Point transformCoordinates(int x, int y) {
		if (coordinatesTransform != null) {
			Point2D point = new Point2D.Double(x, y);
			point = coordinatesTransform.transform(point, null);
			return new Point((int) Math.round(point.getX()), (int) Math.round(point.getY()));
		} else {
			return new Point(x - roundedLocation.x + roundedCenter.x, y - roundedLocation.y + roundedCenter.y);
		}
	}

	/**
	 * Updates coordinates transform used to transform parent's pane coordinates
	 * to coordinates in this pane.
	 */
	private void recalculateCoordinatesTransform() {
		if (rotation == 0) {
			coordinatesTransform = null;
			return;
		}

		coordinatesTransform = new AffineTransform();
		coordinatesTransform.translate(xCenter, yCenter);
		coordinatesTransform.rotate(-Math.toRadians(rotation));
		coordinatesTransform.translate(-x, -y);
	}

	// ---------------------------------------------------------------------------------------------------
	// Border style
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Gets the current border with (in pixels). The border is drawn over the
	 * pane content, i.e., it is an internal pane border.
	 * 
	 * @return the border width.
	 */
	public int getBorderWidth() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return borderWidth;
		}
	}

	/**
	 * Sets new border with. The border is drawn over the pane content, i.e., it
	 * is an internal pane border.
	 * 
	 * @param borderWidth
	 *            the desired border with in pixels.
	 */
	public void setBorderWidth(int borderWidth) {
		if (borderWidth < 0)
			borderWidth = 0;

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.borderWidth != borderWidth) {
				this.borderWidth = borderWidth;
				invalidate();
			}
		}
	}

	/**
	 * Gets the current border color. The border is drawn over the pane content,
	 * i.e., it is an internal pane border.
	 * 
	 * @return the border color.
	 */
	public Color getBorderColor() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return borderColor;
		}
	}

	/**
	 * Sets the new border color. The border is drawn over the pane content,
	 * i.e., it is an internal pane border.
	 * 
	 * @param borderColor
	 *            the desired color of the border. If it is set to null, no
	 *            border is drawn.
	 */
	public void setBorderColor(Color borderColor) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			boolean isChange;
			if (borderColor == null) {
				isChange = (this.borderColor != null);
			} else {
				isChange = !borderColor.equals(this.borderColor);
			}

			if (isChange) {
				this.borderColor = borderColor;
				invalidate();
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Visual style
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Gets the current transparency of the pane. The transparency is a real
	 * number between 0 (no transparency) and 1 (fully transparent).
	 * 
	 * @return the transparency.
	 */
	public double getTransparency() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return transparency;
		}
	}

	/**
	 * Sets the pane's transparency. The transparency is a real number between 0
	 * (no transparency) and 1 (fully transparent).
	 * 
	 * @param transparency
	 *            the desired pane's transparency. If it is a number smaller
	 *            than 0, the value 0 is set. If it is a number greater than 1,
	 *            the value 1 is set.
	 */
	public void setTransparency(double transparency) {
		transparency = Math.max(0, transparency);
		transparency = Math.min(transparency, 1);

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.transparency != transparency) {
				this.transparency = transparency;
				drawingComposite = drawingComposite.derive((float) (1.0 - this.transparency));
				invalidate();
			}
		}
	}

	/**
	 * Gets the pane's background color.
	 * 
	 * @return the background color.
	 */
	public Color getBackgroundColor() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return backgroundColor;
		}
	}

	/**
	 * Sets the pane's background color.
	 * 
	 * @param backgroundColor
	 *            the desired pane's background color. If set to null, the
	 *            pane's background is transparent.
	 */
	public void setBackgroundColor(Color backgroundColor) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			boolean isChange;
			if (backgroundColor == null) {
				isChange = (this.backgroundColor != null);
			} else {
				isChange = !backgroundColor.equals(this.backgroundColor);
			}

			if (isChange) {
				this.backgroundColor = backgroundColor;
				invalidate();
			}
		}
	}

	/**
	 * Returns whether drawing the pane's background is disabled.
	 * 
	 * @return true, if the drawing the pane's background is disabled, i.e., the
	 *         pane's background is transparent; false otherwise.
	 */
	public boolean isTransparentBackground() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return transparentBackground;
		}
	}

	/**
	 * Sets whether the pane's background is drawn, i.e., whether pane's
	 * background is transparent.
	 * 
	 * @param transparentBackground
	 *            true for transparent (not drawn) background
	 */
	public void setTransparentBackground(boolean transparentBackground) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.transparentBackground != transparentBackground) {
				this.transparentBackground = transparentBackground;
				invalidate();
			}
		}
	}

	/**
	 * Gets whether the drawing actions are antialiased.
	 * 
	 * @return true if the antialiasing is enabled, false otherwise.
	 */
	public boolean isAntialiased() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return antialiased;
		}
	}

	/**
	 * Sets whether the drawing actions are antialiased.
	 * 
	 * @param antialiased
	 *            true for drawing with antialiasing, false for drawing without
	 *            anitialiasing.
	 */
	public void setAntialiased(boolean antialiased) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			this.antialiased = antialiased;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Rendering
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Forces the repaint of the pane's backbuffer if necessary. In case that
	 * there was no change of the pane's content and visual presentations of all
	 * objects living on the pane, i.e., the pane was not invalidated, no
	 * repaint is realized.
	 */
	private void repaintBackBuffer() {
		// if content of the back buffer is still valid, we don't need to
		// repaint it
		if (!invalidated) {
			return;
		}

		// create graphics for accessing back buffer
		Graphics2D dbg = backBuffer.createGraphics();

		// prepare the background
		if (transparentBackground || (backgroundColor == null)) {
			dbg.setBackground(TRANSPARENT_COLOR);
		} else {
			dbg.setBackground(backgroundColor);
		}

		dbg.clearRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());

		// paint content of pane
		dbg.drawImage(content, null, 0, 0);

		// paint overlays
		for (PanePainter overlay : overlays.values()) {
			Graphics2D g2d = (Graphics2D) dbg.create();
			if (antialiased) {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			overlay.paint(g2d);
			g2d.dispose();
		}

		// draw objects on the pane
		for (PaneObject o : children) {
			Graphics2D g2d = (Graphics2D) dbg.create();
			o.paintToPaneGraphics(g2d);
			g2d.dispose();
		}

		// draw border
		if ((borderWidth > 0) && (borderColor != null)) {
			dbg.setPaint(borderColor);
			dbg.fillRect(0, 0, width, borderWidth);
			dbg.fillRect(0, 0, borderWidth, height);
			dbg.fillRect(width - borderWidth, 0, borderWidth, height);
			dbg.fillRect(0, height - borderWidth, width, borderWidth);
		}

		dbg.dispose();
		invalidated = false;
	}

	/**
	 * Paints the pane to a graphics at position relative to parent's coordinate
	 * system.
	 * 
	 * @param g
	 *            the graphics where the content is drawn.
	 */
	public void paintToPaneGraphics(Graphics2D g) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			repaintBackBuffer();

			g.setComposite(drawingComposite);
			if (coordinatesTransform != null) {
				g.translate(x, y);
				g.rotate(Math.toRadians(rotation));
				g.translate(-xCenter, -yCenter);
				g.drawImage(backBuffer, null, 0, 0);
			} else {
				g.drawImage(backBuffer, null, roundedLocation.x - roundedCenter.x, roundedLocation.y - roundedCenter.y);
			}
		}
	}

	/**
	 * Paints the content of this pane to given graphic at 0, 0 coordinates.
	 * 
	 * @param g
	 *            the graphics where the content is drawn.
	 */
	void paintWithoutTransform(Graphics2D g) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			repaintBackBuffer();
			g.drawImage(backBuffer, null, 0, 0);
		}
	}

	/**
	 * Marks that the content of the pane is no longer valid.
	 */
	public void invalidate() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			invalidated = true;

			// notify the parent that its content is also invalidated
			if (parentPane != null) {
				parentPane.invalidate();
			}

			// notify change listeners
			if (!changeListeners.isEmpty()) {
				PaneChangeEvent e = new PaneChangeEvent(this);
				for (PaneChangeListener l : changeListeners) {
					l.paneInvalidated(e);
				}
			}
		}
	}

	/**
	 * Resizes all underlying images and data-structures in order to fit new
	 * dimension of the pane
	 * 
	 * @param newWidth
	 *            new width of the pane
	 * @param newHeight
	 *            new height of the pane
	 */
	private void resizePaneImages(int newWidth, int newHeight) {
		// create new images for storing pane content and backbuffer
		backBuffer = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB_PRE);
		BufferedImage newContent = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB_PRE);

		// draw old content to new content
		if (content != null) {
			Graphics2D g = newContent.createGraphics();
			g.drawImage(content, null, 0, 0);
			g.dispose();
		}

		content = newContent;
		invalidate();
	}

	/**
	 * Cleans the graphical content of the pane.
	 */
	public void clear() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			Graphics2D g = content.createGraphics();
			g.setColor(Color.white);
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0, 0, content.getWidth(), content.getHeight());
			g.dispose();
			invalidate();
		}
	}

	/**
	 * Sets overlay associated with given object. The method is internal method.
	 * 
	 * @param object
	 *            an object to which the overlay is associated, it is used as
	 *            identifier of the painter.
	 * @param painter
	 *            the painter of the overlay.
	 */
	void setOverlay(Object object, PanePainter painter) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (object == null) {
				return;
			}

			if (painter == null) {
				overlays.remove(object);
			} else {
				overlays.put(object, painter);
			}

			invalidate();
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Management of child objects
	// ---------------------------------------------------------------------------------------------------
	
	/**
	 * Gets the current parent pane of this pane. If this pane don't have a
	 * parent, null is returned.
	 * 
	 * @return the pane in which this pane is contained.
	 */
	public Pane getPane() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return parentPane;
		}
	}

	/**
	 * Sets the parent pane of this pane. First, the pane is removed from its
	 * current parent pane. Later, it is added to the new parent pane.
	 * 
	 * @param newParentPane
	 *            the new parent pane
	 */
	public void setPane(Pane newParentPane) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (parentPane != newParentPane) {
				// say former parent about going away
				if (parentPane != null) {
					parentPane.remove(this);
				}

				// if there will be no parent, we execute detach actions.
				if (newParentPane == null) {
					detach();
				}

				// change parent
				parentPane = newParentPane;

				// say new parent about joing
				if (newParentPane != null) {
					newParentPane.add(this);
				}
			}
		}
	}

	/**
	 * Adds the new pane object (Pane, Turtle, etc.) on the pane.
	 * 
	 * @param o
	 *            the object adding to the pane. If null, nothing is done.
	 */
	public void add(PaneObject o) {
		if (o == null)
			return;

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!children.contains(o)) {
				children.add(o);

				// update special sublists
				if (o instanceof Turtle) {
					turtles.add((Turtle) o);
				}

				if (o instanceof Pane) {
					panes.add((Pane) o);
				}

				// try to add this pane as a parent of the PaneObject referenced
				// by o
				try {
					o.setPane(this);
				} catch (RuntimeException e) {
					children.remove(o);
					turtles.remove(o);
					panes.remove(o);
					throw e;
				}

				// center turtles (if feature enabled)
				if (isTurtleCentering() && (o instanceof Turtle)) {
					((Turtle) o).center();
				}
				
				invalidate();
			}
		}
	}
	
	/**
	 * Returns whether turtles are centered when added to this pane.
	 * 
	 * @return true, if the turtles are centered, false otherwise.
	 */
	public boolean isTurtleCentering() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return turtleCentering;
		}
	}

	/**
	 * Sets whether turtles are centered when added to this pane.
	 * 
	 * @param turtleCentering
	 *            true, to enabled centering, false to disable.
	 */
	public void setTurtleCentering(boolean turtleCentering) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			this.turtleCentering = turtleCentering;
		}
	}

	/**
	 * Removes a pane object from the pane. If the object is not living in this
	 * pane, nothing is done.
	 * 
	 * @param o
	 *            the removing pane object
	 */
	public void remove(PaneObject o) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (children.remove(o)) {

				// update special sublists
				if (o instanceof Turtle) {
					turtles.remove((Turtle) o);
				}

				if (o instanceof Pane) {
					panes.remove((Pane) o);
				}

				o.setPane(null);
				invalidate();
			}
		}
	}

	/**
	 * Moves a pane object to front of all other pane objects.
	 * 
	 * @param o
	 *            the pane object that will be moved
	 */
	public void bringToFront(PaneObject o) {
		if (o == null) {
			return;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!children.contains(o)) {
				throw new RuntimeException("Object is not living in this pane.");
			}

			children.remove(o);
			children.add(o);

			// update special sublists
			if (o instanceof Turtle) {
				turtles.remove((Turtle) o);
				turtles.add((Turtle) o);
			}

			if (o instanceof Pane) {
				panes.remove((Pane) o);
				panes.add((Pane) o);
			}

			invalidate();
		}
	}

	/**
	 * Moves a pane object to back of all other pane objects.
	 * 
	 * @param o
	 *            the pane object that will be moved
	 */
	public void bringToBack(PaneObject o) {
		if (o == null) {
			return;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!children.contains(o))
				throw new RuntimeException("Object is not living in this pane.");

			children.remove(o);
			children.add(0, o);

			// update special sublists
			if (o instanceof Turtle) {
				turtles.remove((Turtle) o);
				turtles.add(0, (Turtle) o);
			}

			if (o instanceof Pane) {
				panes.remove((Pane) o);
				panes.add(0, (Pane) o);
			}

			invalidate();
		}
	}

	/**
	 * Moves a pane object to front of another pane object.
	 * 
	 * @param o
	 *            the pane object that will be moved
	 * 
	 * @param location
	 *            the pane object to front of which the object o will be moved
	 */
	public void bringToFrontOf(PaneObject o, PaneObject location) {
		if ((o == null) || (o == location)) {
			return;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!children.contains(o)) {
				throw new RuntimeException("Object is not living in this pane.");
			}

			if (!children.contains(location)) {
				throw new RuntimeException("Location object is not living in this pane.");
			}

			children.remove(o);
			children.add(children.indexOf(location) + 1, o);

			// update special sublists
			if (o instanceof Turtle) {
				turtles.clear();
				for (PaneObject po : children) {
					if (po instanceof Turtle) {
						turtles.add((Turtle) po);
					}
				}
			}

			if (o instanceof Pane) {
				panes.clear();
				for (PaneObject po : children) {
					if (po instanceof Pane) {
						panes.add((Pane) po);
					}
				}
			}

			invalidate();
		}
	}

	/**
	 * Moves a pane object to back of another pane object.
	 * 
	 * @param o
	 *            the pane object that will be moved
	 * 
	 * @param location
	 *            the pane object to back of which the object o will be moved
	 */
	public void bringToBackOf(PaneObject o, PaneObject location) {
		if ((o == null) || (o == location)) {
			return;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!children.contains(o)) {
				throw new RuntimeException("Object is not living in this pane.");
			}

			if (!children.contains(location)) {
				throw new RuntimeException("Location object is not living in this pane.");
			}

			children.remove(o);
			children.add(children.indexOf(location), o);

			// update special sublists
			if (o instanceof Turtle) {
				turtles.clear();
				for (PaneObject po : children) {
					if (po instanceof Turtle) {
						turtles.add((Turtle) po);
					}
				}
			}

			if (o instanceof Pane) {
				panes.clear();
				for (PaneObject po : children) {
					if (po instanceof Pane) {
						panes.add((Pane) po);
					}
				}
			}

			invalidate();
		}
	}

	/**
	 * Gets an array of turtles currently living in this pane.
	 * 
	 * @return the array referencing all turtles in this pane.
	 */
	public Turtle[] getTurtles() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return turtles.toArray(new Turtle[turtles.size()]);
		}
	}

	/**
	 * Gets the number of turtles currently living in this pane.
	 * 
	 * @return the number of turtles in this pane.
	 */
	public int getTurtleCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return turtles.size();
		}
	}

	/**
	 * Gets the turtle associated with the specified index.
	 * 
	 * @param index
	 *            the position of the turtle.
	 * @return a turtle that is associated with the specified index.
	 */
	public Turtle getTurtle(int index) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return turtles.get(index);
		}
	}

	/**
	 * Gets an array of children panes currently living in this pane.
	 * 
	 * @return the array referencing all panes in this pane.
	 */
	public Pane[] getPanes() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return panes.toArray(new Pane[0]);
		}
	}

	/**
	 * Gets the number of children panes currently living in this pane.
	 * 
	 * @return the number of panes in this pane.
	 */
	public int getPaneCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return panes.size();
		}
	}

	/**
	 * Gets the pane associated with the specified index.
	 * 
	 * @param index
	 *            the position of the pane.
	 * @return a pane that is associated with the specified index.
	 */
	public Pane getPane(int index) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return panes.get(index);
		}
	}

	/**
	 * Gets an array of all children object currently living in this pane.
	 * 
	 * @return the array referencing all pane objects in this pane.
	 */
	public PaneObject[] getPaneObjects() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return children.toArray(new PaneObject[0]);
		}
	}

	/**
	 * Gets the number of children objects currently living in this pane.
	 * 
	 * @return the number of pane objects in this panes.
	 */
	public int getObjectCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return children.size();
		}
	}

	/**
	 * Gets the pane object associated with the specified index.
	 * 
	 * @param index
	 *            the position of the pane object.
	 * @return a pane object that is associated with the specified index.
	 */
	public PaneObject getPaneObject(int index) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return children.get(index);
		}
	}

	/**
	 * Internal method that executes detaching actions. The method causes that
	 * all events are closed by appropriated finalization events.
	 */
	void detach() {
		clearMouseEvents();
		keyEventManager.releasePressedKeys();
	}

	// ---------------------------------------------------------------------------------------------------
	// Event handling based on overridden methods
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns whether the pane is transparent for mouse events. If the pane is
	 * transparent for mouse events the any mouse event is forwarded also to
	 * panes under this pane.
	 * 
	 * @return true, if the pane is mouse transparent, false otherwise.
	 */
	public boolean isMouseTransparent() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return mouseTransparent;
		}
	}

	/**
	 * Sets transparency of the pane for mouse events. If the pane is
	 * transparent for mouse events the any mouse event is forwarded also to
	 * panes under this pane.
	 * 
	 * @param mouseTransparent
	 *            true for mouse transparent pane, false otherwise.
	 */
	public void setMouseTransparent(boolean mouseTransparent) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			this.mouseTransparent = mouseTransparent;
		}
	}

	/**
	 * Processes a mouse event. The event is broadcasted to all panes at a given
	 * position, until a mouse non-transparent pane is reached.
	 * 
	 * @param x
	 *            the x-coordinate of the mouse event with respect to the parent
	 *            pane
	 * @param y
	 *            the y-coordinate of the mouse event with respect to the parent
	 *            pane
	 * @param type
	 *            the type of the event
	 * @param detail
	 *            the additional information about the event
	 * 
	 * @param transformed
	 *            a boolean, true if coordinates x and y are in coordinates
	 *            system of this pane.
	 */
	void fireMouseEvent(int x, int y, int type, MouseEvent detail, boolean transformed) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			// transform parent's pane coordinates to local coordinates (if
			// necessary)
			if (!transformed) {
				Point localPoint = transformCoordinates(x, y);
				x = localPoint.x;
				y = localPoint.y;
			}

			lastMouseX = x;
			lastMouseY = y;
			lastMouseEvent = detail;

			// update array that stores which mouse buttons are hold
			int buttonIdx = -1;
			for (int i = 0; i < MOUSE_BUTTONS.length; i++) {
				if (detail.getButton() == MOUSE_BUTTONS[i]) {
					buttonIdx = i;
					break;
				}
			}

			if (buttonIdx >= 0) {
				if (type == MouseEvent.MOUSE_PRESSED) {
					// stop event fire in case of invalid state (pressing of a
					// button that is already pressed)
					if (holdMouseButtons[buttonIdx]) {
						return;
					}

					holdMouseButtons[buttonIdx] = true;
				} else if ((type == MouseEvent.MOUSE_RELEASED)) {
					// stop event fire in case of invalid state (release of a
					// button that is already released)
					if (!holdMouseButtons[buttonIdx]) {
						return;
					}

					holdMouseButtons[buttonIdx] = false;
				}
			}

			// broadcast the event through all mouse transparent children at a
			// given position
			int index = panes.size() - 1;
			boolean fireAllowed = true;
			// use a copy of panes in case of reorder caused by event handler
			ArrayList<Pane> copyOfPanes = new ArrayList<Pane>(panes);
			while (index >= 0) {
				Pane childPane = copyOfPanes.get(index);
				boolean eventWanted = childPane.mouseEventWanted(type, buttonIdx);

				if (fireAllowed && (!eventWanted)) {
					if (childPane.containsPoint(x, y)) {
						childPane.fireMouseEvent(x, y, type, detail, false);
						if (!childPane.isMouseTransparent()) {
							fireAllowed = false;
						}
					}
				}

				if (eventWanted) {
					childPane.fireMouseEvent(x, y, type, detail, false);
				}

				index--;
			}

			// if no other child pane processed the event, this pane processes
			// the event
			if (index < 0) {
				String calledMethodName = "";
				try {
					if (type == MouseEvent.MOUSE_CLICKED) {
						calledMethodName = "onMouseClicked";
						onMouseClicked(x, y, detail);
					} else if (type == MouseEvent.MOUSE_DRAGGED) {
						calledMethodName = "onMouseDragged";
						onMouseDragged(x, y, detail);
					} else if (type == MouseEvent.MOUSE_MOVED) {
						calledMethodName = "onMouseMoved";
						onMouseMoved(x, y, detail);
					} else if (type == MouseEvent.MOUSE_PRESSED) {
						calledMethodName = "onMousePressed";
						onMousePressed(x, y, detail);
					} else if (type == MouseEvent.MOUSE_RELEASED) {
						calledMethodName = "onMouseReleased";
						onMouseReleased(x, y, detail);
					}
				} catch (Exception e) {
					System.err.println("Catched an exception in the " + calledMethodName + " method of "
							+ this.toString() + ": " + e);
				}
			}
		}
	}

	/**
	 * Emulates and fires mouse release events for mouse buttons that are hold.
	 */
	private void clearMouseEvents() {
		int lmx = lastMouseX;
		int lmy = lastMouseY;
		MouseEvent lme = lastMouseEvent;
		if (lme == null) {
			return;
		}

		for (int i = 0; i < holdMouseButtons.length; i++) {
			if (holdMouseButtons[i]) {
				MouseEvent event = new MouseEvent(lme.getComponent(), MouseEvent.MOUSE_RELEASED,
						System.currentTimeMillis(), lme.getModifiers(), lme.getX(), lme.getY(), 1, false,
						MOUSE_BUTTONS[i]);
				fireMouseEvent(lmx, lmy, MouseEvent.MOUSE_RELEASED, event, true);
			}
		}
	}

	/**
	 * Returns whether a mouse event of given type and related to a given mouse
	 * button should be received by this pane.
	 * 
	 * @type type the type of a mouse event.
	 * @param buttonIdx
	 *            the index of mouse button related to a mouse event. This is a
	 *            number 0, 1 or 2, or a negative number, if event is not
	 *            related to any mouse button.
	 * 
	 * @return true, if the event should by received by this pane.
	 */
	private boolean mouseEventWanted(int type, int buttonIdx) {
		// if a mouse button is released, and we remember that it was pressed
		// over this pane, we want to receive the event
		if ((type == MouseEvent.MOUSE_RELEASED) && (buttonIdx >= 0) && (holdMouseButtons[buttonIdx])) {
			return true;
		}

		// mouse dragged is wanted whenever a mouse button was pressed over this
		// pane
		if ((type == MouseEvent.MOUSE_DRAGGED) && (holdMouseButtons[0] || holdMouseButtons[1] || holdMouseButtons[2])) {
			return true;
		}

		return false;
	}

	/**
	 * Sets the period in which key pressed events are generated for key codes
	 * that are hold.
	 * 
	 * @param keyRepeatPeriod
	 *            the period in milliseconds. 0 for default strategy (defined by
	 *            OS or JRE).
	 */
	public void setKeyRepeatPeriod(long keyRepeatPeriod) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			keyEventManager.setRepeatPeriod(keyRepeatPeriod);
		}
	}

	/**
	 * Returns the period in which key pressed events are generated for key
	 * codes that are hold.
	 * 
	 * @return the period in milliseconds or 0 for default strategy (defined by
	 *         OS or JRE).
	 */
	public long getKeyRepeatPeriod() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return keyEventManager.getRepeatPeriod();
		}
	}

	/**
	 * Sets the period in which key pressed events are generated for given key
	 * code when it is hold.
	 * 
	 * @param keyCode
	 *            the key code.
	 * @param keyRepeatPeriod
	 *            the period in milliseconds. 0 for default strategy (defined by
	 *            OS or JRE).
	 */
	public void setKeyRepeatPeriod(int keyCode, long keyRepeatPeriod) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			keyEventManager.setRepeatPeriod(keyCode, keyRepeatPeriod);
		}
	}

	/**
	 * Returns the period in which key pressed events are generated for given
	 * key code when it is hold.
	 * 
	 * @param keyCode
	 *            the key code
	 * @return the period in milliseconds or 0 for default strategy (defined by
	 *         OS or JRE).
	 */
	public long getKeyRepeatPeriod(int keyCode) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return keyEventManager.getRepeatPeriod(keyCode);
		}
	}

	/**
	 * Receives a key event.
	 * 
	 * @param type
	 *            the type of the event
	 * @param detail
	 *            the detailed information about the event
	 */
	void fireKeyEvent(int type, KeyEvent detail) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			keyEventManager.processKeyEvent(type, detail);
		}
	}

	/**
	 * Processes a key event. The event is broadcasted to all child panes.
	 * 
	 * @param type
	 *            the type of the event
	 * @param detail
	 *            the detailed information about the event
	 */
	private void processKeyEvent(int type, KeyEvent detail) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			// invoke a proper onKeyXYZ method
			if (type == KeyEvent.KEY_PRESSED) {
				try {
					onKeyPressed(detail);
				} catch (Exception e) {
					System.err.println(
							"Catched an exception in the onKeyPressed method of " + this.toString() + ": " + e);
				}
			} else if (type == KeyEvent.KEY_RELEASED) {
				try {
					onKeyReleased(detail);
				} catch (Exception e) {
					System.err.println(
							"Catched an exception in the onKeyReleased method of " + this.toString() + ": " + e);
				}
			} else if (type == KeyEvent.KEY_TYPED) {
				try {
					onKeyTyped(detail);
				} catch (Exception e) {
					System.err.println(
							"Catched an exception in the onKeyPressed method of " + this.toString() + ": " + e);
				}
			}

			// broadcast the event to all child panes
			for (Pane childPane : panes) {
				childPane.fireKeyEvent(type, detail);
			}
		}
	}

	/**
	 * Called on mouse clicked event. The method is invoked in the Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the mouse click with respect to pane
	 *            coordinate system.
	 * @param y
	 *            the Y-coordinate of the mouse click with respect to pane
	 *            coordinate system.
	 * @param detail
	 *            the additional information about the event.
	 */
	protected void onMouseClicked(int x, int y, MouseEvent detail) {
		// Handling code
	}

	/**
	 * Called on mouse moved event. The method is invoked in the Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the mouse with respect to pane coordinate
	 *            system
	 * @param y
	 *            the Y-coordinate of the mouse with respect to pane coordinate
	 *            system
	 * @param detail
	 *            the additional information about the event
	 */
	protected void onMouseMoved(int x, int y, MouseEvent detail) {
		// Handling code
	}

	/**
	 * Called on mouse dragged event. The method is invoked in the Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the mouse with respect to pane coordinate
	 *            system.
	 * @param y
	 *            the Y-coordinate of the mouse with respect to pane coordinate
	 *            system.
	 * @param detail
	 *            the additional information about the event.
	 */
	protected void onMouseDragged(int x, int y, MouseEvent detail) {
		// Handling code
	}

	/**
	 * Called on mouse pressed event. The method is invoked in the Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the mouse with respect to pane coordinate
	 *            system.
	 * @param y
	 *            the Y-coordinate of the mouse with respect to pane coordinate
	 *            system.
	 * @param detail
	 *            the additional information about the event.
	 */
	protected void onMousePressed(int x, int y, MouseEvent detail) {
		// Handling code
	}

	/**
	 * Called on mouse released event. The method is invoked in the Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the mouse with respect to pane coordinate
	 *            system.
	 * @param y
	 *            the Y-coordinate of the mouse with respect to pane coordinate
	 *            system.
	 * @param detail
	 *            the additional information about the event.
	 */
	protected void onMouseReleased(int x, int y, MouseEvent detail) {
		// Handling code
	}

	/**
	 * Called on key pressed event. The method is invoked in the Swing's EDT.
	 * 
	 * @param e
	 *            the additional information about the event.
	 */
	protected void onKeyPressed(KeyEvent e) {
		// Handling code
	}

	/**
	 * Called on key released event. The method is invoked in the Swing's EDT.
	 * 
	 * @param e
	 *            the additional information about the event.
	 */
	protected void onKeyReleased(KeyEvent e) {
		// Handling code
	}

	/**
	 * Called on key typed event. The method is invoked in the Swing's EDT.
	 * 
	 * @param e
	 *            the additional information about the event.
	 */
	protected void onKeyTyped(KeyEvent e) {
		// Handling code
	}

	/**
	 * Returns whether a point at given location is clickable, i.e., a hand
	 * mouse cursor should be shown over this point. This method can be called
	 * only from Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the location with respect to specified
	 *            coordinate system.
	 * @param y
	 *            the Y-coordinate of the location with respect to specified
	 *            coordinate system.
	 * 
	 * @param inParentCoordinates
	 *            a boolean that determines coordinate system of given
	 *            coordinates. True, if the coordinates are in coordinate system
	 *            of the parent pane, false, if the coordinates are in
	 *            coordinate system of this pane.
	 * 
	 * @return true, if a point at given location can be clicked (i.e., a hand
	 *         mouse cursor should be displayed), false otherwise.
	 */
	public boolean canClick(int x, int y, boolean inParentCoordinates) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (inParentCoordinates) {
				Point localPoint = transformCoordinates(x, y);
				x = localPoint.x;
				y = localPoint.y;
			}

			return onCanClick(x, y);
		}
	}

	/**
	 * Called when querying whether a point at given location can be clicked.
	 * This method is called in order to display an appropriate mouse cursor. If
	 * this method is not overridden, it returns whether at given location is an
	 * object that is clickable considering all panes to be mouse transparent.
	 * The method is invoked in the Swing's EDT.
	 * 
	 * @param x
	 *            the X-coordinate of the location with respect to pane
	 *            coordinate system.
	 * @param y
	 *            the Y-coordinate of the location with respect to pane
	 *            coordinate system.
	 * 
	 * @return true, if a point at given location can be clicked (i.e., a hand
	 *         mouse cursor should be displayed), false otherwise.
	 */
	protected boolean onCanClick(int x, int y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (panes.isEmpty()) {
				return false;
			}

			ArrayList<Pane> copyOfPanes = new ArrayList<Pane>(panes);
			int index = copyOfPanes.size() - 1;
			while (index >= 0) {
				Pane childPane = copyOfPanes.get(index);
				if (childPane.containsPoint(x, y) && childPane.canClick(x, y, true)) {
					return true;
				}

				index--;
			}

			return false;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Drawing methods for turtles
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Draws the shape to the pane.
	 * 
	 * @param shape
	 *            the shape to be drawn
	 * @param stroke
	 *            the stroke used to draw the shape
	 * @param color
	 *            the color used to draw the shape
	 * @param paint
	 *            the color used to paint the shape
	 */
	public void draw(Shape shape, Stroke stroke, Color color, Paint paint) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			Graphics2D g2 = content.createGraphics();

			if (antialiased) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

			// set stroke, color, and paint
			g2.setStroke(stroke);
			g2.setColor(color);
			g2.setPaint(paint);

			// draw the shape
			if (shape != null) {
				g2.draw(shape);
			}

			g2.dispose();
			invalidate();
		}
	}

	/**
	 * Fills the shape to the pane.
	 * 
	 * @param shape
	 *            the shape to be drawn
	 * @param stroke
	 *            the stroke used to draw the shape
	 * @param color
	 *            the color used to draw the shape
	 * @param paint
	 *            the color used to paint the shape
	 */
	public void fill(Shape shape, Stroke stroke, Color color, Paint paint) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			Graphics2D g2 = content.createGraphics();

			if (antialiased) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

			// set stroke, color, and paint
			g2.setStroke(stroke);
			g2.setColor(color);
			g2.setPaint(paint);

			// draw the shape
			if (shape != null) {
				g2.fill(shape);
			}

			g2.dispose();
			invalidate();
		}
	}

	/**
	 * Prints the text message. This method is only for internal use within
	 * JPAZ.
	 * 
	 * @param position
	 *            the position of the message
	 * @param direction
	 *            the direction of the message. The direction is defined in
	 *            degrees in the same way as the turtles' directions.
	 * @param message
	 *            the message which will be printed
	 * @param font
	 *            the font used to print the message
	 * @param color
	 *            the color used to print the message
	 * 
	 * @param centerAtPosition
	 *            a boolean, true for centering the text at given position,
	 *            false otherwise.
	 */
	void drawString(Point2D position, double direction, String message, Font font, Color color,
			boolean centerAtPosition) {
		if (message == null) {
			return;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			Graphics2D g2 = content.createGraphics();

			if (antialiased) {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

			g2.translate(position.getX(), position.getY());
			g2.rotate(Math.toRadians(270 + direction));

			// set font and color
			if (font != null) {
				g2.setFont(font);
			}

			if (color != null) {
				g2.setColor(color);
			}

			// draw message
			if (centerAtPosition) {
				FontMetrics fm = g2.getFontMetrics();
				Rectangle2D bounds = fm.getStringBounds(message, g2);
				g2.drawString(message, -(int) (bounds.getWidth() / 2), fm.getHeight() / 2 - fm.getDescent());
			} else {
				g2.drawString(message, 0, 0);
			}
			g2.dispose();
			invalidate();
		}
	}

	/**
	 * Realizes painting to the pane's by a paint painter. This method is only
	 * for internal use within JPAZ.
	 * 
	 * @param painter
	 *            the paint painter
	 */
	void paint(PanePainter painter) {
		if (painter == null) {
			return;
		}

		synchronized (JPAZUtilities.getJPAZLock()) {
			Graphics2D g = content.createGraphics();
			if (antialiased) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			painter.paint(g);
			g.dispose();
			invalidate();
		}
	}

	/**
	 * Returns font metrics for the specified font. This method is only for
	 * internal use within JPAZ.
	 * 
	 * @param font
	 *            the specified font.
	 * @return font metrics for the specified font.
	 */
	FontMetrics getFontMetrics(Font font) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			Graphics2D g2 = content.createGraphics();
			FontMetrics result = g2.getFontMetrics((font == null) ? g2.getFont() : font);
			g2.dispose();
			return result;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Ticking support
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Gets the time period (in milliseconds) in which the onTick method is
	 * executed. The value 0 indicates disabled periodical execution of the
	 * onTick method.
	 * 
	 * @return the tick period in milliseconds.
	 */
	public long getTickPeriod() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return tickTimer.getTickPeriod();
		}
	}

	/**
	 * Sets the time period (in milliseconds) in which the onTick method is
	 * executed. The value 0 indicates disabled periodical execution of the
	 * onTick method.
	 * 
	 * @param tickPeriod
	 *            the tick period in milliseconds.
	 */
	public void setTickPeriod(long tickPeriod) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			tickTimer.setTickPeriod(tickPeriod);
		}
	}

	/**
	 * Called periodically by the pane. The period of calling is determined by
	 * the property tickPeriod.
	 */
	protected void onTick() {
		// Handling code
	}

	// ---------------------------------------------------------------------------------------------------
	// PaneChange events handling
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Adds the specified pane change listener to receive change events from
	 * this pane. If listener l is null, no exception is thrown and no action is
	 * performed.
	 * 
	 * @param l
	 *            the pane change listener
	 */
	void addPaneChangeListener(PaneChangeListener l) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!changeListeners.contains(l)) {
				changeListeners.add(l);
			}
		}
	}

	/**
	 * Removes the specified pane change listener receiving change events from
	 * this pane. If listener l is null, no exception is thrown and no action is
	 * performed.
	 * 
	 * @param l
	 *            the pane change listener
	 */
	void removePaneChangeListener(PaneChangeListener l) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			changeListeners.remove(l);
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Other
	// ---------------------------------------------------------------------------------------------------

	@Override
	public String toString() {
		return className + " (@" + referenceIdentification + ")";
	}

	/**
	 * Stores picture of the pane to an image file.
	 * 
	 * @param filename
	 *            the name of the output image file.
	 * @return true, if the picture has been saved, false otherwise.
	 */
	public boolean savePicture(String filename) {
		File file = new File(filename.trim());
		int dotSeparator = filename.lastIndexOf('.');
		if (dotSeparator < 0) {
			throw new RuntimeException("Invalid filename (no filename extension).");
		}
		String format = filename.substring(dotSeparator + 1).toLowerCase();

		BufferedImage bufferedImage;
		synchronized (JPAZUtilities.getJPAZLock()) {
			repaintBackBuffer();

			bufferedImage = new BufferedImage(backBuffer.getWidth(), backBuffer.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.drawImage(backBuffer, null, 0, 0);
			g2d.dispose();
		}

		try {
			return ImageIO.write(bufferedImage, format, file);
		} catch (Exception e) {
			return false;
		}
	}
}
