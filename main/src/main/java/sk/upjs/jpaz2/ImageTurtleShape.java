package sk.upjs.jpaz2;

import java.io.File;
import java.net.URL;

/**
 * The painter of a turtle shape based on an image. This class is provided only
 * for backward compatibility.
 */
public class ImageTurtleShape extends ImageShape {

	public ImageTurtleShape(URL imageURL, double xCenter, double yCenter) {
		super(imageURL, xCenter, yCenter);
	}

	public ImageTurtleShape(URL imageURL) {
		super(imageURL);
	}

	public ImageTurtleShape(File imageFile, double xCenter, double yCenter) {
		super(imageFile, xCenter, yCenter);
	}

	public ImageTurtleShape(File imageFile) {
		super(imageFile);
	}

	public ImageTurtleShape(String imageLocation, double xCenter, double yCenter) {
		super(imageLocation, xCenter, yCenter);
	}

	public ImageTurtleShape(String imageLocation) {
		super(imageLocation);
	}

	public ImageTurtleShape(String packageName, String fileName) {
		super(packageName, fileName);
	}
}
