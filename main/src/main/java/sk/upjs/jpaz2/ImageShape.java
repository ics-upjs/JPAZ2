package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/**
 * The painter of a turtle shape based on an image.
 */
public class ImageShape implements TurtleShape {

	// ---------------------------------------------------------------------------------------------------
	// Image shape builder
	// ---------------------------------------------------------------------------------------------------

	/**
	 * The builder of ImageShape class instances. This class is not thread-safe.
	 */
	public static class Builder {

		/**
		 * URL of resource containing the shape image.
		 */
		private URL imageURL;

		/**
		 * Center of the image.
		 */
		private Point2D center = null;

		/**
		 * Number of views.
		 */
		private int viewCount = 1;

		/**
		 * Number of animation frames.
		 */
		private int frameCount = 1;

		/**
		 * Animation delay in milliseconds.
		 */
		private Long frameDuration;

		/**
		 * Whether animation frames are in rows, i.e., located horizontally
		 */
		private boolean framesInRows = true;

		/**
		 * Whether the color of the pixel in the top-left corner is replaced by
		 * transparent color.
		 */
		private boolean transparentTopLeft = false;

		/**
		 * Setting whether transparent pixels of the shape image are excluded
		 * from the shape (in sense of the isPointOfShape method).
		 */
		private boolean transparentExcludedFromShape = true;

		/**
		 * Constructs a builder of a shape from image with given URL.
		 * 
		 * @param imageURL
		 *            the URL of the image file
		 */
		public Builder(URL imageURL) {
			this.imageURL = imageURL;
		}

		/**
		 * Constructs a builder of a shape from an image file.
		 * 
		 * @param imageFile
		 *            the image file
		 */
		public Builder(File imageFile) {
			try {
				imageURL = imageFile.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(
						"Parameter imageFile does not contain valid name of an image file (" + e.getMessage() + ")");
			}
		}

		/**
		 * Constructs a builder of a shape from a file at the given location
		 * (resource or file). If location string starts with /, it is first
		 * considered to be a resource location. If resource localization
		 * failed, location string is considered as a file
		 * 
		 * @param imageLocation
		 *            the string with location of an image (file or resource)
		 */
		public Builder(String imageLocation) {
			// if imageLocationString starts with /, then try to locate image in
			// resources
			if (imageLocation.startsWith("/")) {
				try {
					imageURL = JPAZUtilities.getResourceAsURL(imageLocation);
				} catch (Exception e) {
					imageURL = null;
				}
			}

			// if there is an URL, we are done
			if (imageURL != null)
				return;

			// try to locate the image considering imageLocation to be a
			// filename
			try {
				File imageFile = new File(imageLocation);
				if (imageFile.exists()) {
					imageURL = imageFile.toURI().toURL();
				}
			} catch (Exception e) {
				imageURL = null;
			}

			if (imageURL == null)
				throw new RuntimeException(
						"Parameter imageLocation (" + imageLocation + ") does not refer to an image file");
		}

		/**
		 * Constructs a builder of a shape from a resource located in specified
		 * package and given filename.
		 * 
		 * @param packageName
		 *            the package where resource file is searched
		 * @param fileName
		 *            the filename (in the package given by parameter
		 *            packageName)
		 */
		public Builder(String packageName, String fileName) {
			if (packageName == null)
				packageName = "";

			String resourceLocation = '/' + packageName.trim().replace('.', '/') + '/' + fileName.trim();
			if (resourceLocation.startsWith("//"))
				resourceLocation = resourceLocation.substring(1);

			try {
				imageURL = JPAZUtilities.getResourceAsURL(resourceLocation);
			} catch (Exception e) {
				throw new RuntimeException("Specified resource was not found.");
			}

			if (imageURL == null)
				throw new RuntimeException("Specified resource was not found.");
		}

		/**
		 * Sets the position of shape center.
		 * 
		 * @param x
		 *            the X-coordinate of the shape center
		 * @param y
		 *            the Y-coordinate of the shape center
		 * 
		 * @return a reference to this object
		 */
		public Builder setShapeCenter(double x, double y) {
			center = new Point2D.Double(x, y);
			return this;
		}

		/**
		 * Returns position of the shape center or null if it is not defined.
		 * 
		 * @return the defined position of the shape center
		 */
		public Point2D getShapeCenter() {
			if (center != null)
				return new Point2D.Double(center.getX(), center.getY());
			else
				return null;
		}

		/**
		 * Sets the number of animation frames in the image.
		 * 
		 * @param frameCount
		 *            the number of animation frames
		 * @return a reference to this object
		 */
		public Builder setFrameCount(int frameCount) {
			if (frameCount < 1)
				frameCount = 1;

			this.frameCount = frameCount;
			return this;
		}

		/**
		 * Returns the number of animation frames.
		 * 
		 * @return the number of animation frames.
		 */
		public int getFrameCount() {
			return frameCount;
		}

		/**
		 * Sets the number of views in the image.
		 * 
		 * @param viewCount
		 *            the number of shape views
		 * @return a reference to this object
		 */
		public Builder setViewCount(int viewCount) {
			if (viewCount < 1)
				viewCount = 1;

			this.viewCount = viewCount;
			return this;
		}

		/**
		 * Returns the number of views in the image.
		 * 
		 * @return the number of views that are available in this image shape.
		 */
		public int getViewCount() {
			return viewCount;
		}

		/**
		 * Sets the frame duration in milliseconds for views with multiple
		 * frames.
		 * 
		 * @param frameDuration
		 *            the frame duration in milliseconds
		 * 
		 * @return a reference to this object
		 */
		public Builder setFrameDuration(long frameDuration) {
			if (frameDuration < 0)
				frameDuration = 0;

			this.frameDuration = frameDuration;
			return this;
		}

		/**
		 * Returns the frame duration in milliseconds for views with multiple
		 * frames.
		 * 
		 * @return the frame duration in milliseconds
		 */
		public Long getFrameDuration() {
			return frameDuration;
		}

		/**
		 * Returns URL of the image.
		 * 
		 * @return URL of the image
		 */
		public URL getURL() {
			return imageURL;
		}

		/**
		 * Sets orientation of animation frames in the input image.
		 * 
		 * @param framesInRows
		 *            true, if frames of a view are placed horizontally, false,
		 *            if vertically
		 * @return a reference to this object
		 */
		public Builder setFramesInRows(boolean framesInRows) {
			this.framesInRows = framesInRows;
			return this;
		}

		/**
		 * Returns orientation of animation frames in the input image.
		 * 
		 * @return true, if animation frames of a view are placed horizontally,
		 *         false, if vertically
		 */
		public boolean areFramesInRows() {
			return framesInRows;
		}

		/**
		 * Creates an instance of the ImageTurtleShape according to current
		 * setting of this builder.
		 * 
		 * @return a new instance of the ImageTurtleShape class
		 */
		public ImageShape createShape() {
			return new ImageShape(this);
		}

		/**
		 * Returns whether color of the pixel in the top-left corner of the
		 * image is considered as a transparent color.
		 * 
		 * @return true, if recoloring will be applied, false otherwise
		 */
		public boolean isTopLeftToTransparentColor() {
			return transparentTopLeft;
		}

		/**
		 * Sets whether color of the pixel in the top-left corner of the image
		 * is considered as a transparent color.
		 * 
		 * @param recolor
		 *            true, if recoloring has to be applied, false otherwise
		 * @return a reference to this object
		 */
		public Builder setTopLeftToTransparentColor(boolean recolor) {
			transparentTopLeft = recolor;
			return this;
		}

		/**
		 * Returns whether transparent pixels of the shape image are considered
		 * as excluded from the shape in sense of the isPointOfShape method.
		 * 
		 * @return true, if transparent pixels are excluded from the shape,
		 *         false otherwise.
		 */
		public boolean isTransparentExcludedFromShape() {
			return transparentExcludedFromShape;
		}

		/**
		 * SetsReturns whether transparent pixels of the shape image are
		 * considered as excluded from the shape in sense of the isPointOfShape
		 * method.
		 * 
		 * @param excluded
		 *            true, if transparent pixels are excluded from the shape,
		 *            false otherwise.
		 * @return a reference to this object
		 */
		public Builder setTransparentExcludedFromShape(boolean excluded) {
			transparentExcludedFromShape = excluded;
			return this;
		}

	}

	// ---------------------------------------------------------------------------------------------------
	// Instance variables
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Default duration of one animation frame in milliseconds.
	 */
	private static final long DEFAULT_FRAME_DURATION = 500;

	/**
	 * Animation frames.
	 */
	private BufferedImage frames[][];

	/**
	 * X-coordinate of the center of the image.
	 */
	private double xCenter;

	/**
	 * Y-coordinate of the center of the image.
	 */
	private double yCenter;

	/**
	 * Setting whether transparent pixels of the shape image are excluded from
	 * the shape (in sense of the isPointOfShape method).
	 */
	private boolean transparentExcludedFromShape;

	/**
	 * Number of views of the shape.
	 */
	private int viewCount;

	/**
	 * Number of animation frames of the shape.
	 */
	private int frameCount;

	/**
	 * Frame duration in milliseconds for images with multiple (animated)
	 * frames.
	 */
	private long frameDuration;

	/**
	 * Width of the shape frame
	 */
	private int frameWidth;

	/**
	 * Height of the shape frame
	 */
	private int frameHeight;

	// ---------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs a turtle shape according to builder settings.
	 */
	private ImageShape(Builder builder) {
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from an image.
	 * 
	 * @param imageURL
	 *            the URL of an image with the shape
	 * @param xCenter
	 *            the X-coordinate of the shape center
	 * @param yCenter
	 *            the Y-coordinate of the shape center
	 */
	public ImageShape(URL imageURL, double xCenter, double yCenter) {
		Builder builder = new Builder(imageURL);
		builder.setShapeCenter(xCenter, yCenter);
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from an image. The center of the shape is set
	 * to the center of the image.
	 * 
	 * @param imageURL
	 *            the URL of an image with the shape
	 */
	public ImageShape(URL imageURL) {
		Builder builder = new Builder(imageURL);
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from an image file.
	 * 
	 * @param imageFile
	 *            file with image for the shape
	 * @param xCenter
	 *            the X-coordinate of the shape center
	 * @param yCenter
	 *            the Y-coordinate of the shape center
	 */
	public ImageShape(File imageFile, double xCenter, double yCenter) {
		Builder builder = new Builder(imageFile);
		builder.setShapeCenter(xCenter, yCenter);
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from an image file. The center of the shape is
	 * set to the center of the image.
	 * 
	 * @param imageFile
	 *            file with the image for the shape
	 */
	public ImageShape(File imageFile) {
		Builder builder = new Builder(imageFile);
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from image at a specified location. If location
	 * string starts with /, it is first considered to be a resource location.
	 * If resource localization failed, location string is considered as a file
	 * name.
	 * 
	 * @param imageLocation
	 *            the string with location of an image (file or resource)
	 * @param xCenter
	 *            the X-coordinate of the shape center
	 * @param yCenter
	 *            the Y-coordinate of the shape center
	 */
	public ImageShape(String imageLocation, double xCenter, double yCenter) {
		Builder builder = new Builder(imageLocation);
		builder.setShapeCenter(xCenter, yCenter);
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from image at a specified location. If location
	 * string starts with /, it is first considered to be a resource location.
	 * If resource localization failed, location string is considered as a file
	 * name. The center of the shape is set to the center of the image.
	 * 
	 * @param imageLocation
	 *            the string with location of an image (file or resource)
	 */
	public ImageShape(String imageLocation) {
		Builder builder = new Builder(imageLocation);
		constructShapeFromBuilder(builder);
	}

	/**
	 * Constructs a turtle shape from an image resource located in specified
	 * package and a given filename.The center of the shape is set to the center
	 * of the image.
	 * 
	 * @param packageName
	 *            the package where resource file is searched
	 * @param fileName
	 *            the filename (in the package given by parameter packageName)
	 */
	public ImageShape(String packageName, String fileName) {
		Builder builder = new Builder(packageName, fileName);
		constructShapeFromBuilder(builder);
	}

	// ---------------------------------------------------------------------------------------------------
	// Internal construction methods
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Loads the shape according to builder settings.
	 * 
	 * @param builder
	 */
	private void constructShapeFromBuilder(Builder builder) {
		frameDuration = DEFAULT_FRAME_DURATION;
		java.util.List<BufferedImage> images = null;
		try {
			ImageInputStream imageStream = null;
			try {
				imageStream = ImageIO.createImageInputStream(builder.getURL().openStream());

				ImageFrameLoader frameLoader = new ImageFrameLoader();
				frameLoader.loadFramesFromStream(imageStream);
				images = frameLoader.getFrames();

				// compute average duration
				java.util.List<Long> durations = frameLoader.getDurations();
				if ((durations != null) && (!durations.isEmpty())) {
					long totalDuration = 0;
					for (long duration : durations)
						totalDuration += duration;

					frameDuration = totalDuration / durations.size();
				}
			} finally {
				if (imageStream != null) {
					imageStream.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Loading of the image file with URL " + builder.getURL().toString() + " failed.");
		}

		if ((images == null) || (images.isEmpty()))
			throw new RuntimeException("Image file cannot be decoded using available image readers.");

		// if the input image is already an animated image, we compute shape
		// parameters
		if (images.size() > 1) {
			if (builder.areFramesInRows()) {
				viewCount = 1;
				frameCount = images.size();
			} else {
				viewCount = images.size();
				frameCount = 1;
			}

			frameWidth = images.get(0).getWidth();
			frameHeight = images.get(0).getHeight();

			frames = new BufferedImage[viewCount][frameCount];

			int frameIdx = 0;
			int viewIdx = 0;
			for (int i = 0; i < images.size(); i++) {
				// store frame
				if (builder.isTopLeftToTransparentColor())
					frames[viewIdx][frameIdx] = replaceTopLeftColorToTransparent(images.get(i));
				else
					frames[viewIdx][frameIdx] = images.get(i);

				// update indices
				if (builder.areFramesInRows())
					frameIdx++;
				else
					viewIdx++;
			}
		} else {
			viewCount = builder.getViewCount();
			frameCount = builder.getFrameCount();
			BufferedImage fullImage = images.get(0);

			if (builder.isTopLeftToTransparentColor())
				fullImage = replaceTopLeftColorToTransparent(fullImage);

			frames = splitToFrames(fullImage, viewCount, frameCount, builder.areFramesInRows());
			frameWidth = frames[0][0].getWidth();
			frameHeight = frames[0][0].getHeight();
		}

		// set shape center
		if (builder.getShapeCenter() == null) {
			xCenter = frameWidth / 2.0;
			yCenter = frameHeight / 2.0;
		} else {
			Point2D center = builder.getShapeCenter();
			xCenter = center.getX();
			yCenter = center.getY();
		}

		// set user defined animation delay
		if (builder.getFrameDuration() != null)
			frameDuration = builder.getFrameDuration();

		// if there is only one frame, set frame duration to 0
		if (frameCount == 0)
			frameDuration = 0;

		// exclusion of transparent pixels
		transparentExcludedFromShape = builder.isTransparentExcludedFromShape();
	}

	/**
	 * Cuts the input image into frames grid.
	 */
	private BufferedImage[][] splitToFrames(BufferedImage image, int viewCount, int frameCount, boolean framesInRow) {
		int tilesXCount = (framesInRow) ? frameCount : viewCount;
		int tilesYCount = (framesInRow) ? viewCount : frameCount;

		// check divisibility
		if ((image.getWidth() % tilesXCount != 0) || (image.getHeight() % tilesYCount != 0))
			throw new RuntimeException("Image dimensions are not divisible by given number of views or frames.");

		// compute tile size
		int tileWidth = image.getWidth() / tilesXCount;
		int tileHeight = image.getHeight() / tilesYCount;

		// read tiles
		BufferedImage[][] result = new BufferedImage[viewCount][frameCount];
		for (int x = 0; x < tilesXCount; x++)
			for (int y = 0; y < tilesYCount; y++) {
				BufferedImage tile = image.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
				if (framesInRow)
					result[y][x] = tile;
				else
					result[x][y] = tile;
			}

		return result;
	}

	/**
	 * Replaces color of the pixel in the top-left corner by transparent color
	 */
	private BufferedImage replaceTopLeftColorToTransparent(BufferedImage image) {
		final long topLeftColor = image.getRGB(0, 0) | 0xFF000000;

		ImageFilter filter = new RGBImageFilter() {
			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == topLeftColor)
					return 0x00FFFFFF & rgb;
				else
					return rgb;
			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		Image trasparentImage = Toolkit.getDefaultToolkit().createImage(ip);

		BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = result.createGraphics();
		g2.drawImage(trasparentImage, 0, 0, null);
		g2.dispose();

		return result;
	}

	// ---------------------------------------------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns the width of each frame in this shape.
	 * 
	 * @return the width of the shape.
	 */
	public int getWidth() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameWidth;
		}
	}

	/**
	 * Returns the height of each frame in this shape.
	 * 
	 * @return the height of the shape.
	 */
	public int getHeight() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameHeight;
		}
	}

	/**
	 * Returns coordinates of the shape center.
	 * 
	 * @return coordinates of the shape center.
	 */
	public Point2D getCenter() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return new Point2D.Double(xCenter, yCenter);
		}
	}

	/**
	 * Returns the X-coordinate of the shape center.
	 * 
	 * @return the X-coordinate of the shape center.
	 */
	public double getXCenter() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return xCenter;
		}
	}

	/**
	 * Returns the Y-coordinate of the shape center.
	 * 
	 * @return the Y-coordinate of the shape center.
	 */
	public double getYCenter() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return yCenter;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Painting and implementation methods of the TurtleShape interface
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Paints shape of a turtle to a given graphics.
	 * 
	 * @param t
	 *            the turtle whose shape is drawn.
	 * @param g
	 *            the graphic where the shape is drawn.
	 */
	public void paintTurtle(Turtle t, Graphics2D g) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			// check transparency, if it is 1, we don't draw the shape
			double shapeTransparency = t.getTransparency();
			if (shapeTransparency == 1)
				return;

			// compute rescaled size
			double scale = t.getScale();
			int scaledFrameWidth = (int) Math.round(frameWidth * scale);
			int scaledFrameHeight = (int) Math.round(frameHeight * scale);
			boolean rescaleNeeded = (scaledFrameWidth != frameWidth) || (scaledFrameHeight != frameHeight);

			// translate and rotate graphics
			g.translate(t.getX(), t.getY());
			if (!t.isViewBoundToDirection())
				g.rotate(Math.toRadians(t.getDirection()));

			// reflect the current transparency
			Composite oldCmp = null;
			if (shapeTransparency != 0) {
				oldCmp = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - shapeTransparency)));
			}

			// draw image with desired scale
			BufferedImage currentFrame = frames[t.getViewIndex()][t.getFrameIndex()];
			if (rescaleNeeded) {
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
				g.translate(-xCenter * scale, -yCenter * scale);
				g.drawImage(currentFrame, 0, 0, scaledFrameWidth, scaledFrameHeight, null);
			} else {
				g.translate(-xCenter, -yCenter);
				g.drawImage(currentFrame, null, 0, 0);
			}

			if (shapeTransparency != 0)
				g.setComposite(oldCmp);
		}
	}

	/**
	 * Paints a frame of the shape image to a given graphics. This is an
	 * internal JPAZ method for use of other classes.
	 * 
	 * @param viewIdx
	 *            the index of view to be drawn.
	 * @param frameIdx
	 *            the index of frame to be drawn.
	 * @param g
	 *            the graphic where the shape is drawn.
	 */
	void paintShapeFrame(int viewIdx, int frameIdx, Graphics2D g) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((viewIdx < 0) || (frameIdx < 0) || (viewIdx >= viewCount) || (frameIdx >= frameCount))
				return;

			g.drawImage(frames[viewIdx][frameIdx], null, 0, 0);
		}
	}

	public int getViewCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return viewCount;
		}
	}

	public int getFrameCount() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameCount;
		}
	}

	public boolean isPointOfShape(Turtle t, double x, double y) {
		if (t == null)
			return false;

		synchronized (JPAZUtilities.getJPAZLock()) {
			double scale = t.getScale();
			if (scale == 0)
				return false;

			// translate pane coordinates to shape coordinates
			AffineTransform transform = new AffineTransform();
			transform.translate(xCenter * scale, yCenter * scale);
			if (!t.isViewBoundToDirection())
				transform.rotate(Math.toRadians(-t.getDirection()));
			transform.translate(-t.getX(), -t.getY());
			Point2D translatedPoint = new Point2D.Double();
			transform.transform(new Point2D.Double(x, y), translatedPoint);

			// rescale coordinates
			int xInShape = (int) Math.round(translatedPoint.getX() / scale);
			int yInShape = (int) Math.round(translatedPoint.getY() / scale);

			// check whether the computed point is inside frame
			if ((xInShape < 0) || (yInShape < 0) || (xInShape >= frameWidth) || (yInShape >= frameHeight))
				return false;

			// if necessary, check transparency of the computed pixel in the
			// frame
			if (transparentExcludedFromShape) {
				if ((frames[t.getViewIndex()][t.getFrameIndex()].getRGB(xInShape, yInShape) & 0xFF000000) == 0)
					return false;
			}

			return true;
		}
	}

	public long getFrameDuration() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameDuration;
		}
	}
}
