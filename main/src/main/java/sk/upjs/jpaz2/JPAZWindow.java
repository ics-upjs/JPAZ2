package sk.upjs.jpaz2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.*;
import java.awt.image.*;

import javax.imageio.*;
import javax.swing.*;

/**
 * JPAZWindow represents a window that is able to display a pane. It is aimed to
 * be used as a main container to display panes.
 */
public class JPAZWindow {

	/**
	 * Underlying frame used to display panes.
	 */
	private JFrame frame;

	/**
	 * Panel displaying a bound pane.
	 */
	private JPAZPanel panel;

	/**
	 * Pane that is bound to the content of the underlying window.
	 */
	private Pane pane;

	/**
	 * Align mode for displaying pane.
	 */
	private boolean alignMode = false;

	/**
	 * Title of the underlying frame.
	 */
	private String title = "JPAZ Window";

	/**
	 * Width of the frame content.
	 */
	private int width = 300;

	/**
	 * Height of the frame content.
	 */
	private int height = 300;

	/**
	 * X-coordinate of the top-left corner of the underlying frame.
	 */
	private int x;

	/**
	 * Y-coordinate of the top-left corner of the underlying frame.
	 */
	private int y;

	/**
	 * Indicates whether underlying frame is resizable.
	 */
	private boolean resizable = false;

	/**
	 * Indicates that there is a request to center the frame.
	 */
	private boolean centerPending = true;

	/**
	 * Icon to be shown as a frame icon.
	 */
	private Object frameIcon;

	// ---------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs new window for displaying a pane.
	 */
	public JPAZWindow() {
		this(null);
	}

	/**
	 * Constructs new window for displaying a pane. The pane is aligned to the
	 * window (frame).
	 * 
	 * @param pane
	 *            the pane to be displayed in this window.
	 */
	public JPAZWindow(Pane pane) {
		this(pane, false);
	}

	/**
	 * Constructs new window for displaying a pane.
	 * 
	 * @param pane
	 *            the pane to be displayed in this window.
	 * @param alignMode
	 *            a boolean, true for aligning the pane is aligned to the window
	 *            (frame).
	 * 
	 */
	public JPAZWindow(Pane pane, boolean alignMode) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (pane != null) {
				width = pane.getWidth();
				height = pane.getHeight();
				this.pane = pane;
				this.alignMode = alignMode;
			}
		}
		createGUI();
	}

	// ---------------------------------------------------------------------------------------------------
	// Binding
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Binds the content of this window to a pane.
	 * 
	 * @param pane
	 *            the pane to be displayed in this window (frame).
	 */
	public void bindTo(Pane pane) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			bindTo(pane, alignMode);
		}
	}

	/**
	 * Binds the content of this window to a pane.
	 * 
	 * @param pane
	 *            the pane to be bound to this window.
	 * @param align
	 *            a boolean, true for aligning the bound pane to the window.
	 */
	public void bindTo(Pane pane, boolean align) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			this.pane = pane;
			this.alignMode = align;
			if (panel != null) {
				panel.bindTo(pane, alignMode);
			}
		}
	}

	/**
	 * Binds the content of this window to another pane with transition effect.
	 * 
	 * @param pane
	 *            the pane to be bound to this window.
	 * @param align
	 *            a boolean, true for aligning the bound pane to the window.
	 * @param transitionEffect
	 *            the transition effect to be applied.
	 * @param duration
	 *            the duration of the transition in milliseconds.
	 */
	public void rebindWithEffect(Pane pane, boolean align, TransitionEffect transitionEffect, long duration) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			this.pane = pane;
			this.alignMode = align;
			if (panel != null) {
				panel.rebindWithEffect(pane, align, transitionEffect, duration);
			}
		}
	}

	/**
	 * Binds the content of this window to another pane with transition effect.
	 * 
	 * @param pane
	 *            the pane to be bound to this window.
	 * @param transitionEffect
	 *            the transition effect to be applied.
	 * @param duration
	 *            the duration of the transition in milliseconds.
	 */
	public void rebindWithEffect(Pane pane, TransitionEffect transitionEffect, long duration) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			rebindWithEffect(pane, alignMode, transitionEffect, duration);
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Getters/Setters
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Returns the pane that is bound (displayed) to this window.
	 * 
	 * @return the bound pane.
	 */
	public Pane getPane() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return pane;
		}
	}

	/**
	 * Sets the pane that is bound to this window.
	 * 
	 * @param pane
	 *            the pane to be bound to this window.
	 */
	public void setPane(Pane pane) {
		synchronized (pane) {
			bindTo(pane);
		}
	}

	/**
	 * Returns whether the bound pane is aligned to the window.
	 * 
	 * @return true, if the bound pane is aligned to the window, false
	 *         otherwise.
	 */
	public boolean isAlignMode() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return alignMode;
		}
	}

	/**
	 * Sets whether the bound pane is aligned to the window.
	 * 
	 * @param alignMode
	 *            true, if the bound pane is aligned to the window, false
	 *            otherwise.
	 */
	public void setAlignMode(boolean alignMode) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.alignMode != alignMode) {
				this.alignMode = alignMode;
				if (panel != null)
					panel.setAlignMode(alignMode);
			}
		}
	}

	/**
	 * Returns the title of the window (frame).
	 * 
	 * @return the title of the window.
	 */
	public String getTitle() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return title;
		}
	}

	/**
	 * Sets the title of the window.
	 * 
	 * @param title
	 *            the desired title of the window.
	 */
	public void setTitle(String title) {
		if (title == null)
			title = "";

		synchronized (JPAZUtilities.getJPAZLock()) {
			if (!title.equals(this.title)) {
				this.title = title;
				if (frame != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							synchronized (JPAZUtilities.getJPAZLock()) {
								frame.setTitle(JPAZWindow.this.title);
							}
						}
					});
				}
			}
		}
	}

	/**
	 * Returns the width of the window.
	 * 
	 * @return the width of the window.
	 */
	public int getWidth() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return width;
		}
	}

	/**
	 * Sets the width of the window.
	 * 
	 * @param width
	 *            the desired width of the window.
	 */
	public void setWidth(int width) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setSize(width, height);
		}
	}

	/**
	 * Returns the height of the window.
	 * 
	 * @return the height of the window.
	 */
	public int getHeight() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return height;
		}
	}

	/**
	 * Sets the height of the window.
	 * 
	 * @param height
	 *            the desired height of the window.
	 */
	public void setHeight(int height) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setSize(width, height);
		}
	}

	/**
	 * Sets size of the window.
	 * 
	 * @param width
	 *            the desired width of the window.
	 * @param height
	 *            the desired height of the window.
	 */
	public void setSize(int width, int height) {
		if (width < 1)
			width = 1;

		if (height < 1)
			height = 1;

		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((this.width == width) && (this.height == height))
				return;

			this.width = width;
			this.height = height;

			if ((frame != null) && (panel != null))
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						synchronized (JPAZUtilities.getJPAZLock()) {
							panel.setSize(JPAZWindow.this.width, JPAZWindow.this.height);
							panel.setPreferredSize(new Dimension(JPAZWindow.this.width, JPAZWindow.this.height));
							frame.pack();
						}
					}
				});
		}
	}

	/**
	 * Returns the X-coordinate of the top-left corner of the window.
	 * 
	 * @return the X-coordinate of the top-left corner of the window.
	 */
	public int getX() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return x;
		}
	}

	/**
	 * Sets the X-coordinate of the top-left corner of the window.
	 * 
	 * @param x
	 *            the desired X-coordinate of the top-left corner of the window.
	 */
	public void setX(int x) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setLocation(x, y);
		}
	}

	/**
	 * Returns the Y-coordinate of the top-left corner of the window.
	 * 
	 * @return the Y-coordinate of the top-left corner of the window.
	 */
	public int getY() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return y;
		}
	}

	/**
	 * Sets the Y-coordinate of the top-left corner of the window.
	 * 
	 * @param y
	 *            the desired Y-coordinate of the top-left corner of the window.
	 */
	public void setY(int y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			setLocation(x, y);
		}
	}

	/**
	 * Sets the location of the top-left corner of the window.
	 * 
	 * @param x
	 *            the desired X-coordinate of the top-left corner of the window.
	 * @param y
	 *            the desired Y-coordinate of the top-left corner of the window.
	 */
	public void setLocation(int x, int y) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if ((this.y == y) && (this.x == x))
				return;

			this.y = y;
			this.x = x;
			centerPending = false;

			if (frame != null)
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						synchronized (JPAZUtilities.getJPAZLock()) {
							frame.setLocation(JPAZWindow.this.x, JPAZWindow.this.y);
						}
					}
				});
		}
	}

	/**
	 * Centers the window.
	 */
	public void center() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (frame != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						synchronized (JPAZUtilities.getJPAZLock()) {
							frame.setLocationRelativeTo(null);
						}
					}
				});
			} else {
				centerPending = true;
			}
		}
	}

	/**
	 * Returns whether the window (frame) is resizable.
	 * 
	 * @return true, if the window is resizable.
	 */
	public boolean isResizable() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			return resizable;
		}
	}

	/**
	 * Sets whether the window (frame) is resizable.
	 * 
	 * @param resizable
	 *            a boolean, true if the window is resizable, false otherwise.
	 */
	public void setResizable(boolean resizable) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (this.resizable != resizable) {
				this.resizable = resizable;

				if (frame != null)
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							frame.setResizable(JPAZWindow.this.resizable);
						}
					});
			}
		}
	}

	/**
	 * Sets the icon of the window.
	 * 
	 * @param image
	 *            the desired image to be shown as an icon of the window.
	 */
	public void setIconImage(Image image) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			frameIcon = image;

			if (frame != null)
				updateFrameIcon();
		}
	}

	/**
	 * Sets the icon of the window.
	 * 
	 * @param imageShape
	 *            the desired image shape to be shown as an icon of the window.
	 *            A frame with index 0 of the view with index 0 will be used.
	 */
	public void setIconImage(ImageShape imageShape) {
		synchronized (JPAZUtilities.getJPAZLock()) {
			frameIcon = imageShape;

			if (frame != null)
				updateFrameIcon();
		}
	}

	private void createGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUIInEDT();
			}
		});
	}

	private void createGUIInEDT() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			frame = new JFrame(title);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			frame.setLocation(x, y);
			frame.setSize(width, height);
			frame.setResizable(resizable);

			panel = new JPAZPanel(true);
			panel.setSize(width, height);
			panel.setPreferredSize(new Dimension(width, height));
			panel.setAlignMode(alignMode);
			frame.add(panel, BorderLayout.CENTER);

			if (pane != null)
				panel.bindTo(pane);

			// center frame if necessary
			if (centerPending) {
				frame.setLocationRelativeTo(null);
				centerPending = false;
			}

			frame.setVisible(true);
			frame.pack();

			panel.setRepackFrame(frame);

			x = frame.getX();
			y = frame.getY();
			frame.repaint();

			frame.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentMoved(ComponentEvent e) {
					synchronized (JPAZUtilities.getJPAZLock()) {
						x = frame.getX();
						y = frame.getY();
					}
				}
			});

			panel.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					synchronized (JPAZUtilities.getJPAZLock()) {
						width = panel.getWidth();
						height = panel.getHeight();
					}
				}
			});

			updateFrameIconInEDT();
			panel.requestFocusInWindow();
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Internal methods.
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Updates the frame icon.
	 */
	private void updateFrameIcon() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateFrameIconInEDT();
			}
		});
	}

	/**
	 * Sets icon for underlying JFrame. This method must be called in Swing's
	 * EDT.
	 */
	private void updateFrameIconInEDT() {
		synchronized (JPAZUtilities.getJPAZLock()) {
			if (frame == null)
				return;

			// set default icon, if no other is given
			if (frameIcon == null) {
				try {
					frame.setIconImage(ImageIO.read(this.getClass().getResource("/sk/upjs/jpaz2/images/jpazLogo.png")));
				} catch (Exception e) {
					// nothing to do
				}

				return;
			}

			if (frameIcon instanceof Image) {
				frame.setIconImage((Image) frameIcon);
				frameIcon = null;
				return;
			}

			if (frameIcon instanceof ImageShape) {
				ImageShape shape = (ImageShape) frameIcon;
				BufferedImage iconImage = new BufferedImage(shape.getWidth(), shape.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = iconImage.createGraphics();
				shape.paintShapeFrame(0, 0, g);
				g.dispose();
				frame.setIconImage(iconImage);
				frameIcon = null;
				return;
			}
		}
	}
}
