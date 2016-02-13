package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.geom.*;

/**
 * The painter of default turtle shape - triangle. The color of the triangle is
 * determined by the current turtle's pen color.
 */
public class DefaultTurtleShape implements TurtleShape {
	private static final double angleTg = 0.4142135623730950488016887242097;

	/**
	 * Size of the triangle forming the turtle shape
	 */
	private int size;

	/**
	 * Polygon that defines the default shape
	 */
	private Polygon shapePolygon;

	/**
	 * Constructs the basic turtle shape (triangle).
	 */
	public DefaultTurtleShape() {
		this(5);
	}

	/**
	 * Constructs the basic turtle shape (triangle) with defined length of the
	 * side.
	 * 
	 * @param size
	 *            the length of the triangle side
	 */
	public DefaultTurtleShape(int size) {
		this.size = size;

		int offset = (int) Math.round(size / (3 * angleTg));
		shapePolygon = new Polygon();
		shapePolygon.addPoint(-size, offset);
		shapePolygon.addPoint(size, offset);
		shapePolygon.addPoint(0, -2 * offset);
	}

	public void paintTurtle(Turtle t, Graphics2D g) {
		if ((t == null) || (g == null))
			return;

		synchronized (JPAZUtilities.getJPAZLock()) {
			// check transparency, if it is 1, we don't draw the shape
			double shapeTransparency = t.getTransparency();
			if (shapeTransparency == 1)
				return;

			double scale = t.getScale();
			if (scale == 0)
				return;

			// prepare graphics for shape rendering
			g.translate(t.getX(), t.getY());
			if (!t.isViewBoundToDirection())
				g.rotate(Math.toRadians(t.getDirection()));

			// scaling
			if (scale != 1)
				g.scale(scale, scale);

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setPaint(t.getPenColor());

			// draw the triangle shape using actual shape transparency
			if (shapeTransparency != 0) {
				Composite oldCmp = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - shapeTransparency)));
				g.fill(shapePolygon);
				g.setComposite(oldCmp);
			} else {
				g.fill(shapePolygon);
			}
		}
	}

	public int getViewCount() {
		return 1;
	}

	public int getFrameCount() {
		return 1;
	}

	public boolean isPointOfShape(Turtle t, double x, double y) {
		if (t == null)
			return false;

		synchronized (JPAZUtilities.getJPAZLock()) {
			double scale = t.getScale();
			if (scale == 0)
				return false;

			// transform
			AffineTransform transform = new AffineTransform();
			if (!t.isViewBoundToDirection())
				transform.rotate(Math.toRadians(-t.getDirection()));
			transform.translate(-t.getX(), -t.getY());
			Point2D translatedPoint = new Point2D.Double();
			transform.transform(new Point2D.Double(x, y), translatedPoint);

			// scaling
			if (scale != 1)
				translatedPoint.setLocation(translatedPoint.getX() / scale, translatedPoint.getY() / scale);

			return shapePolygon.contains(translatedPoint);
		}
	}

	public long getFrameDuration() {
		return 0;
	}

	/**
	 * Returns the size of the shape.
	 * 
	 * @return the size of the shape
	 */
	public int getSize() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return size;
		}
	}
}
