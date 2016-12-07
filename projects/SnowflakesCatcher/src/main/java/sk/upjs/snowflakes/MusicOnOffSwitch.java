package sk.upjs.snowflakes;

import java.awt.event.MouseEvent;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

/**
 * On/off switch with icon for controlling the music. This class demonstrates
 * how to create own "button" as an extension of the Pane class.
 */
public class MusicOnOffSwitch extends Pane {

	/**
	 * The turtle used to display the icon of this switch.
	 */
	private Turtle icon;

	/**
	 * The stage controlled by the switch.
	 */
	private Stage stage;

	/**
	 * Constructs a new music on/off switch
	 * 
	 * @param stage
	 *            the stage.
	 */
	public MusicOnOffSwitch(Stage stage) {
		this.stage = stage;
		setBorderWidth(0);
		setTransparentBackground(true);

		// create ImageShape.Builder for more detailed construction of the image
		// shape for turtle representing an icon
		ImageShape.Builder imageBuilder = new ImageShape.Builder("images", "music.png");
		imageBuilder.setViewCount(2);
		// create shape according to current setting of imageBuilder
		ImageShape iconShape = imageBuilder.createShape();
		// resize the pane according to size of the icon shape
		resize(iconShape.getWidth(), iconShape.getHeight());

		// create turtle representing the icon
		icon = new Turtle();
		icon.setShape(iconShape);
		add(icon);
		icon.center();
	}

	/**
	 * Updates the icon displayed in the switch in order to reflect current
	 * status of playing the music
	 */
	public void updateView() {
		if (isMusicOn()) {
			icon.setViewIndex(0);
		} else {
			icon.setViewIndex(1);
		}
	}

	/**
	 * Returns whether the music is on.
	 * 
	 * @return true, if the music is on, false otherwise.
	 */
	public boolean isMusicOn() {
		return stage.getBackgroundMusic().isPlaying();
	}

	/**
	 * Activates/deactivates the background music.
	 * 
	 * @param musicOn
	 *            a boolean, set true to play the background music or false to
	 *            stop the background music.
	 */
	public void setMusicOn(boolean musicOn) {
		stage.setMutedMusic(!musicOn);
		updateView();
	}

	@Override
	protected void onMouseReleased(int x, int y, MouseEvent detail) {
		// switch music after the left mouse button is released over this pane
		if (detail.getButton() == MouseEvent.BUTTON1) {
			setMusicOn(!isMusicOn());
		}
	}

	@Override
	protected boolean onCanClick(int x, int y) {
		// show hand only over the icon
		return icon.containsInShape(x, y);
	}
}
