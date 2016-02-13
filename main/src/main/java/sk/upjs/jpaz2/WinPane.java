package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.event.*;

import javax.imageio.*;
import javax.swing.*;

/**
 * WinPane represents a pane displayed in a windows.
 */
public class WinPane extends Pane {

	/**
	 * Cursor to be show when the point is clickable.
	 */
	private static final Cursor CLICKABLE_CURSOR = new Cursor(Cursor.HAND_CURSOR);

	/**
	 * Cursor to be shown when the point is not clickable.
	 */
	private static final Cursor NOT_CLICKABLE_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

	/**
	 * Internal class extending the JPanel. The paint method of this class asks
	 * the pane to draw its content in the panel.
	 */
	@SuppressWarnings("serial")
	private class DrawPanel extends JPanel {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintToPaneGraphics((Graphics2D) g);
		}
	}

	/**
	 * Time in miliseconds specifying how long are processing of events is
	 * delayed after construction of the WinPane object. The reason for delay is
	 * that events can occur even before the object extending the WinPane class
	 * is completely constructed. Locking cannot be used for beginners.
	 */
	private static final long EVENT_HANDLING_DELAY = 500;

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

	/**
	 * Time, when construction of the object was completed
	 */
	private long creationTime;

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
		centerGUIFrame();
		showFrame();
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
		centerGUIFrame();
		showFrame();
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

		// construct GUI visualizing the pane
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUI();
			}
		});

		if (visible)
			showFrame();
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
	public void resize(int newWidth, int newHeight) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			super.resize(newWidth, newHeight);

			if (drawPanel != null)
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						synchronized (JPAZUtilities.getJPAZLock()) {
							// preferred size of draw panel is always set to
							// last known
							// size of the pane (this is to recognize, whether
							// component resize is
							// caused by resize request or user activity
							drawPanel.setPreferredSize(new Dimension(getWidth(), getHeight()));
							drawPanel.setSize(drawPanel.getPreferredSize());
							frame.pack();
							drawPanel.repaint();
						}
					}
				});
		}
	}

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

	// ---------------------------------------------------------------------------------------------------
	// User interface and event handlers
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Creates the GUI frame and initializes its content
	 */
	private void createGUI() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			// set time, when creation was started
			creationTime = System.currentTimeMillis();

			frame = new JFrame(frameTitle);

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());

			drawPanel = new DrawPanel();
			drawPanel.setDoubleBuffered(true);
			drawPanel.setLocation(0, 0);
			drawPanel.setSize(getWidth(), getHeight());
			drawPanel.setPreferredSize(new Dimension(getWidth(), getHeight()));

			frame.add(drawPanel, BorderLayout.CENTER);
			frame.pack();
			frame.setLocation((int) Math.round(getX() - getXCenter()), (int) Math.round(getY() - getYCenter()));
			frame.setVisible(false);
			frame.setResizable(frameResizable);

			// set icon
			try {
				frame.setIconImage(ImageIO.read(this.getClass().getResource("/sk/upjs/jpaz2/images/jpazLogo.png")));
			} catch (Exception e) {
				// nothing to do
			}

			createAndInstallHandlers();
		}
	}

	/**
	 * Centers the GUI frame
	 */
	private void centerGUIFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized (JPAZUtilities.getJPAZLock()) {
					frame.setLocationRelativeTo(null);
				}
			}
		});
	}

	/**
	 * Shows the GUI frame.
	 */
	private void showFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized (JPAZUtilities.getJPAZLock()) {
					frame.setVisible(true);
				}
			}
		});
	}

	/**
	 * Creates and install handlers for user events
	 */
	private void createAndInstallHandlers() {
		// frame moved event
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				synchronized (JPAZUtilities.getJPAZLock()) {
					WinPane.super.setPosition(frame.getX() + getXCenter(), frame.getY() + getYCenter());
				}
			}
		});

		// resize event
		drawPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				synchronized (JPAZUtilities.getJPAZLock()) {
					// preferred size of draw panel is always set to last known
					// size of the pane (this is to recognize, whether component
					// resize is
					// caused by resize request or user activity

					// if user activity, then resize request
					if (!drawPanel.getPreferredSize().equals(drawPanel.getSize())) {
						drawPanel.setPreferredSize(new Dimension(getWidth(), getHeight()));
						WinPane.super.resize(drawPanel.getWidth(), drawPanel.getHeight());
					}
				}
			}
		});

		// mouse events
		drawPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				fireMouseEventWithTransform(MouseEvent.MOUSE_CLICKED, e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				fireMouseEventWithTransform(MouseEvent.MOUSE_PRESSED, e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				fireMouseEventWithTransform(MouseEvent.MOUSE_RELEASED, e);
			}
		});

		// mouse motion events
		drawPanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				fireMouseEventWithTransform(MouseEvent.MOUSE_MOVED, e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				fireMouseEventWithTransform(MouseEvent.MOUSE_DRAGGED, e);
			}
		});

		// keyboard event
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				synchronized (JPAZUtilities.getJPAZLock()) {
					if (acceptEvents())
						fireKeyEvent(KeyEvent.KEY_PRESSED, e);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				synchronized (JPAZUtilities.getJPAZLock()) {
					if (acceptEvents())
						fireKeyEvent(KeyEvent.KEY_RELEASED, e);

				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
				synchronized (JPAZUtilities.getJPAZLock()) {
					if (acceptEvents())
						fireKeyEvent(KeyEvent.KEY_TYPED, e);
				}
			}
		});
	}

	/**
	 * Returns whether processing of user GUI events is allowed.
	 */
	private boolean acceptEvents() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (creationTime == 0)
				return false;

			return (System.currentTimeMillis() - creationTime > EVENT_HANDLING_DELAY);
		}
	}

	/**
	 * Fires a mouse event.
	 */
	private void fireMouseEventWithTransform(int type, MouseEvent e) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (acceptEvents()) {
				fireMouseEvent(e.getX(), e.getY(), type, e, true);

				// update cursor after the "on mouse moved" event
				if (type == MouseEvent.MOUSE_MOVED) {
					if (canClick(e.getX(), e.getY(), false))
						drawPanel.setCursor(CLICKABLE_CURSOR);
					else
						drawPanel.setCursor(NOT_CLICKABLE_CURSOR);
				}
			}
		}
	}
}
