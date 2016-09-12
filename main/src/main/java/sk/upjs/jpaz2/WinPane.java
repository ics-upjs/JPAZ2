package sk.upjs.jpaz2;

import java.awt.Graphics2D;
import javax.swing.*;

/**
 * WinPane represents a pane displayed in a windows.
 */
public class WinPane extends Pane {

	// ---------------------------------------------------------------------------------------------------
	// Instance variables
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Default title of the window
	 */
	public static final String JPAZ_DEFAULT_TITLE = "JPAZ Pane";

	/**
	 * Frame created for the purpose of visualizing the pane
	 */
	private JFrame frame;

	/**
	 * Frame component that is used to draw the content of this pane.
	 */
	private JPanel drawPanel;

	/**
	 * Frame title displayed in the frame border.
	 */
	private String frameTitle = JPAZ_DEFAULT_TITLE;

	/**
	 * Determines whether the pane's frame is resizable.
	 */
	private boolean frameResizable = true;

	// ---------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs a new pane inside a window with predefined size
	 */
	public WinPane() {
		this(300, 300);
	}

	/**
	 * Constructs a new pane inside a window.
	 * 
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 */
	public WinPane(int width, int height) {
		this(0, 0, width, height, JPAZ_DEFAULT_TITLE, false);
	}

	/**
	 * Constructs a new pane inside a window with a given title.
	 * 
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 * @param title
	 *            the title displayed in the frame's border
	 */
	public WinPane(int width, int height, String title) {
		this(0, 0, width, height, title, false);
	}

	/**
	 * Constructs a new pane inside a window at a given position.
	 * 
	 * @param x
	 *            the x-coordinate of the window top-left corner
	 * @param y
	 *            the y-coordinate of the window top-left corner
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 */
	public WinPane(int x, int y, int width, int height) {
		this(x, y, width, height, JPAZ_DEFAULT_TITLE);
	}

	/**
	 * Constructs a new pane inside a window at a given position and with a
	 * given title.
	 * 
	 * @param x
	 *            the x-coordinate of the window top-left corner
	 * @param y
	 *            the y-coordinate of the window top-left corner
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 * @param title
	 *            the title displayed in the frame's border
	 */
	public WinPane(int x, int y, int width, int height, String title) {
		this(x, y, width, height, title, true);
	}

	/**
	 * Constructs a new pane inside a window at a given position and with a
	 * given title.
	 * 
	 * @param x
	 *            the x-coordinate of the window top-left corner
	 * @param y
	 *            the y-coordinate of the window top-left corner
	 * @param width
	 *            the width of the pane
	 * @param height
	 *            the height of the pane
	 * @param title
	 *            the title displayed in the frame's border
	 * @param visible
	 *            the visibility of the constructed frame
	 */
	private WinPane(int x, int y, int width, int height, final String title, boolean visible) {
		// call constructor of the Pane. Position of the pane inside the frame
		// is (0, 0).
		super(0, 0, width, height);
		// no border for WinPanes
		setBorderWidth(0);
		// set frame title
		frameTitle = title;
	}

	// ---------------------------------------------------------------------------------------------------
	// Getters and setters
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Gets the title of containing frame.
	 * 
	 * @return the title of the containing frame, or an empty string ("") if
	 *         this frame doesn't have a title.
	 */
	public String getTitle() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameTitle;
		}
	}

	/**
	 * Sets the title for containing frame to the specified string.
	 * 
	 * @param title
	 *            the title to be displayed in the frame's border. A null value
	 *            is treated as an empty string, "".
	 */
	public void setTitle(final String title) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			frameTitle = title;

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					synchronized (JPAZUtilities.getJPAZLock()) {
						frame.setTitle(title);
					}
				}
			});
		}
	}

	/**
	 * Indicates whether this pane is resizable by the user.
	 * 
	 * @return true if the user can resize this pane; false otherwise.
	 */
	public boolean isResizable() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return frameResizable;
		}
	}

	/**
	 * Sets whether this pane is resizable by the user.
	 * 
	 * @param resizable
	 *            true if this pane is resizable; false otherwise.
	 */
	public void setResizable(final boolean resizable) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			frameResizable = resizable;

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					synchronized (JPAZUtilities.getJPAZLock()) {
						frame.setResizable(resizable);
					}
				}
			});
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Overrides of inherited methods
	// ---------------------------------------------------------------------------------------------------

	@Override
	public void setPosition(double x, double y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			super.setPosition(x, y);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					synchronized (JPAZUtilities.getJPAZLock()) {
						int xPosition = (int) Math.round(getX() - getXCenter());
						int yPosition = (int) Math.round(getY() - getYCenter());
						frame.setLocation(xPosition, yPosition);
					}
				}
			});
		}
	}

	@Override
	public void setRotation(double rotation) {
		throw new RuntimeException("WinPane cannot be rotated.");
	}

	@Override
	public Pane getPane() {
		return null;
	}

	@Override
	public void setPane(Pane newParentPane) {
		throw new RuntimeException("WinPane cannot have a parent.");
	}

	@Override
	public void paintToPaneGraphics(Graphics2D g) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			g.translate(-(getX() - getXCenter()), -(getY() - getYCenter()));
			super.paintToPaneGraphics(g);
		}
	}

	@Override
	public void invalidate() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			super.invalidate();

			if (drawPanel != null)
				drawPanel.repaint();
		}
	}

	@Override
	public String toString() {
		return "Win" + super.toString();
	}
}
