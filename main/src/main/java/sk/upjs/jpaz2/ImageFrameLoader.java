package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

/**
 * Internal class that realizes loading of animated gifs. Class implementation
 * is based on https://forums.oracle.com/forums/thread.jspa?threadID=1269862.
 */
class ImageFrameLoader {
    /**
     * List of loaded image frames.
     */
    private ArrayList<BufferedImage> frames = null;

    /**
     * List of delays in milliseconds between consecutive frames.
     */
    private ArrayList<Long> delays = null;

    // ---------------------------------------------------------------------------------------------------
    // Variables for loading animated gifs
    // ---------------------------------------------------------------------------------------------------

    // information obtained from source file/url.
    private Image[] images;
    private short[] durations; // hundredths of a second
    private short[] xOffsets;
    private short[] yOffsets;
    private Disposal[] disposalMethods;

    private static final int DEFAULT_DURATION = 100;

    // The background color of the global pallete in the GIF file is not
    // used. It is here merely in case you want to change the implementation
    // of RESTORE_TO_BACKGROUND disposal method.
    @SuppressWarnings("unused")
    private Color backgroundColor;

    /**
     * The GIF Frame disposal methods
     */
    private static enum Disposal {
	/** Treated the same as DO_NOT_DISPOSE */
	UNSPECIFIED("none", "undefinedDisposalMethod4", "undefinedDisposalMethod5", "undefinedDisposalMethod6",
		"undefinedDisposalMethod7"),
	/**
	 * Any pixels not covered by the next frame continues to display.
	 * However, even if the last frame is marked as DO_NOT_DISPOSE, the area
	 * will be cleared when the animation starts over.
	 */
	DO_NOT_DISPOSE("doNotDispose"),

	/**
	 * There exists two interpretations of this dispose method. One
	 * interpretation is that the current frame is restored to a background
	 * color before rendering the next frame. The second interpretation is
	 * that the frame is entirely cleared allowing the background of the web
	 * browser to show. The later interpretation is used in this case.
	 */
	RESTORE_TO_BACKGROUND("restoreToBackgroundColor"),

	/**
	 * Restores the src to the previous frame marked by UNSPECIFIED or
	 * DO_NOT_DISPOSE
	 */
	RESTORE_TO_PREVIOUS("restoreToPrevious");

	private String[] gifMetaNames;

	private Disposal(String... gifMetaNames) {
	    this.gifMetaNames = gifMetaNames;
	}

	public static Disposal disposalForString(String s) {
	    for (Disposal d : Disposal.values()) {
		for (String validName : d.gifMetaNames) {
		    if (validName.equals(s)) {
			return d;
		    }
		}
	    }
	    return UNSPECIFIED;
	}
    }

    /**
     * Loads all the frames in an src from the given ImageInputStream.
     * Furthermore, if the src references a GIF file then information on frame
     * durations, offsets, and disposal methods will be extracted (and stored).
     * The src stream is not closed at the end of the method. It is the duty of
     * the caller to close it if they so wish.
     */
    private void loadFromStream(ImageInputStream imageStream) throws IOException {
	// obtain an appropriate src reader
	java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);

	ImageReader reader = null;
	while (readers.hasNext()) {
	    reader = readers.next();

	    String metaFormat = reader.getOriginatingProvider().getNativeImageMetadataFormatName();
	    if ("gif".equalsIgnoreCase(reader.getFormatName()) && !("javax_imageio_gif_image_1.0".equals(metaFormat))) {
		continue;
	    } else {
		break;
	    }
	}

	if (reader == null)
	    throw new RuntimeException("Cannot read image format!");

	try {
	    boolean isGif = reader.getFormatName().equalsIgnoreCase("gif");

	    reader.setInput(imageStream, false, !isGif);

	    // before we get to the frames, determine if there is a background
	    // color
	    IIOMetadata globalMeta = reader.getStreamMetadata();
	    if ((globalMeta != null)
		    && ("javax_imageio_gif_stream_1.0".equals(globalMeta.getNativeMetadataFormatName()))) {

		IIOMetadataNode root = (IIOMetadataNode) globalMeta.getAsTree("javax_imageio_gif_stream_1.0");

		IIOMetadataNode colorTable = (IIOMetadataNode) root.getElementsByTagName("GlobalColorTable").item(0);

		if (colorTable != null) {
		    String bgIndex = colorTable.getAttribute("backgroundColorIndex");

		    IIOMetadataNode colorEntry = (IIOMetadataNode) colorTable.getFirstChild();
		    while (colorEntry != null) {
			if (colorEntry.getAttribute("index").equals(bgIndex)) {
			    int red = Integer.parseInt(colorEntry.getAttribute("red"));
			    int green = Integer.parseInt(colorEntry.getAttribute("green"));
			    int blue = Integer.parseInt(colorEntry.getAttribute("blue"));

			    backgroundColor = new java.awt.Color(red, green, blue);
			    break;
			}

			colorEntry = (IIOMetadataNode) colorEntry.getNextSibling();
		    }
		}
	    }

	    // now we read the images, delay times, offsets and disposal methods
	    ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
	    ArrayList<Integer> delays = new ArrayList<Integer>();
	    ArrayList<Integer> lOffsets = new ArrayList<Integer>();
	    ArrayList<Integer> tOffsets = new ArrayList<Integer>();
	    ArrayList<Disposal> disposals = new ArrayList<Disposal>();

	    boolean unkownMetaFormat = false;
	    for (int index = 0;; index++) {
		try {
		    // read a frame and its metadata
		    javax.imageio.IIOImage frame = reader.readAll(index, null);

		    // add the frame to the list
		    frames.add((BufferedImage) frame.getRenderedImage());

		    if (unkownMetaFormat)
			continue;

		    // obtain src metadata
		    javax.imageio.metadata.IIOMetadata meta = frame.getMetadata();

		    IIOMetadataNode imgRootNode = null;
		    try {
			imgRootNode = (IIOMetadataNode) meta.getAsTree("javax_imageio_gif_image_1.0");
		    } catch (IllegalArgumentException e) {
			// unkown metadata format, can't do anyting about this
			unkownMetaFormat = true;
			continue;
		    }

		    IIOMetadataNode gce = (IIOMetadataNode) imgRootNode.getElementsByTagName("GraphicControlExtension")
			    .item(0);

		    delays.add(Integer.parseInt(gce.getAttribute("delayTime")));
		    disposals.add(Disposal.disposalForString(gce.getAttribute("disposalMethod")));

		    IIOMetadataNode imgDescr = (IIOMetadataNode) imgRootNode.getElementsByTagName("ImageDescriptor")
			    .item(0);

		    lOffsets.add(Integer.parseInt(imgDescr.getAttribute("imageLeftPosition")));
		    tOffsets.add(Integer.parseInt(imgDescr.getAttribute("imageTopPosition")));
		} catch (IndexOutOfBoundsException e) {
		    break;
		}
	    }

	    // copy the source information into their respective arrays
	    if (!frames.isEmpty()) {
		images = frames.toArray(new BufferedImage[0]);
	    }
	    if (!delays.isEmpty()) {
		durations = new short[delays.size()];
		int i = 0;
		for (int duration : delays)
		    durations[i++] = (short) (duration == 0 ? DEFAULT_DURATION : duration);
	    }
	    if (!lOffsets.isEmpty()) {
		xOffsets = new short[lOffsets.size()];
		int i = 0;
		for (int offset : lOffsets)
		    xOffsets[i++] = (short) offset;
	    }
	    if (!tOffsets.isEmpty()) {
		yOffsets = new short[tOffsets.size()];
		int i = 0;
		for (int offset : tOffsets)
		    yOffsets[i++] = (short) offset;
	    }
	    if (!disposals.isEmpty()) {
		disposalMethods = disposals.toArray(new Disposal[0]);
	    }
	} finally {
	    reader.dispose();
	}
    }

    /**
     * Processes frames of a gif image according to metadata records.
     */
    private void processGifFrames() {
	// compute image size
	int width = 0;
	int height = 0;

	for (int i = 0; i < images.length; i++) {
	    width = Math.max(width, images[i].getWidth(null) + xOffsets[i]);
	    height = Math.max(height, images[i].getHeight(null) + yOffsets[i]);
	}

	// construct gif frames
	frames = new ArrayList<BufferedImage>();
	for (int i = 0; i < images.length; i++) {
	    BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
	    Graphics2D g2 = frame.createGraphics();

	    // prepare background according to disposal method of the previous
	    // frame
	    if (i > 0) {
		Disposal disposalMethod = disposalMethods[i - 1];
		if (disposalMethod.equals(Disposal.DO_NOT_DISPOSE)) {
		    g2.drawImage(frames.get(i - 1), 0, 0, null);
		} else if (disposalMethod.equals(Disposal.RESTORE_TO_PREVIOUS)) {
		    int disposeToIdx = -1;
		    for (int prevIdx = i - 2; prevIdx >= 0; prevIdx--)
			if (disposalMethods[prevIdx].equals(Disposal.UNSPECIFIED)
				|| disposalMethods[prevIdx].equals(Disposal.DO_NOT_DISPOSE)) {
			    disposeToIdx = prevIdx;
			    break;
			}

		    if (disposeToIdx >= 0)
			g2.drawImage(frames.get(disposeToIdx), 0, 0, null);
		}
	    }

	    // draw content
	    g2.drawImage(images[i], xOffsets[i], yOffsets[i], null);
	    g2.dispose();

	    // add created frame to the list of generated frames
	    frames.add(frame);
	}

	// compute delays
	delays = new ArrayList<Long>();
	for (int i = 0; i < durations.length; i++)
	    delays.add((long) (durations[i] * 10));
    }

    /**
     * Processes a single frame image.
     */
    private void processSingleFrame() {
	frames = new ArrayList<BufferedImage>();
	frames.add(toBufferedImage(images[0]));
	delays = new ArrayList<Long>();
	delays.add(0L);
    }

    /**
     * Transforms an image to a BufferedImage.
     */
    private BufferedImage toBufferedImage(Image image) {
	if (image instanceof BufferedImage)
	    return (BufferedImage) image;

	BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null),
		BufferedImage.TYPE_INT_ARGB_PRE);
	Graphics2D g2 = result.createGraphics();
	g2.drawImage(image, 0, 0, null);
	g2.dispose();
	return result;
    }

    /**
     * Loads image frames from input stream
     * 
     * @param imageStream
     *            the stream with image
     */
    public void loadFramesFromStream(ImageInputStream imageStream) {
	try {
	    loadFromStream(imageStream);

	    // check whether at least one image has been loaded
	    if ((images == null) || (images.length == 0))
		throw new RuntimeException("No image has been found.");

	    // check availability of metadata
	    boolean gifMetadata = (durations != null) && (xOffsets != null) && (yOffsets != null)
		    && (disposalMethods != null);

	    // process input
	    if ((images.length > 1) && (gifMetadata))
		processGifFrames();
	    else
		processSingleFrame();

	} catch (Exception e) {
	    frames = null;
	    delays = null;
	    throw new RuntimeException("Cannot load image frames.", e);
	} finally {
	    images = null;
	    durations = null;
	    xOffsets = null;
	    yOffsets = null;
	    disposalMethods = null;
	}
    }

    /**
     * Returns list of loaded image frames.
     * 
     * @return a list of loaded image frames
     */
    public java.util.List<BufferedImage> getFrames() {
	if (frames == null)
	    return null;

	return new ArrayList<BufferedImage>(frames);
    }

    /**
     * Returns list of durations for all image frames. Durations are in
     * milliseconds.
     * 
     * @return a list with durations of frames
     */
    public java.util.List<Long> getDurations() {
	if (delays == null)
	    return null;

	return new ArrayList<Long>(delays);
    }
}
