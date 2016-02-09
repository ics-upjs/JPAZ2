package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/**
 * JPAZPanel is a Swing component that provides a view on a pane. The main aim
 * is to integrate JPAZ objects and drawing functionality within a swing
 * applications.
 */
@SuppressWarnings("serial")
public class JPAZPanel extends JPanel {
    /**
     * Pane that is contained in this panel
     */
    private Pane pane;

    /**
     * Indicates whether the pane is aligned in the panel. If aligned, any
     * change of panel size invokes resize of the bound pane and the pane is
     * always aligned in such a way that the top-left corner of the pane
     * corresponds to the top-left corner of this panel.
     */
    private boolean alignMode = false;

    /**
     * Indicates whether resize from pane is enabled.
     */
    private boolean resizeFromPaneEnabled = true;

    /**
     * Listener that listen to changes of the bound pane.
     */
    private PaneChangeListener paneChangeListener = null;

    /**
     * Backbuffer for the case of rotated bound pane, when drawing performance
     * is significantly slow.
     */
    private BufferedImage backbuffer = null;

    /**
     * JFrame that should be repacked in case of resize event from the bound
     * pane.
     */
    private JFrame repackFrame = null;

    /**
     * Implementation of active transition effect.
     */
    private TransitionEffect.Transition transition = null;

    /**
     * Cursor to be show when the point is clickable.
     */
    private static final Cursor CLICKABLE_CURSOR = new Cursor(Cursor.HAND_CURSOR);

    /**
     * Cursor to be shown when the point is not clickable.
     */
    private static final Cursor NOT_CLICKABLE_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

    // ---------------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------------

    /**
     * Creates a new JPAZPanel with a double buffer and a flow layout.
     */
    public JPAZPanel() {
	this((Pane) null);
    }

    /**
     * Creates a new JPAZPanel with a double buffer and a flow layout displaying
     * the specified pane. The pane is displayed in the align mode.
     * 
     * @param pane
     *            the pane to be displayed in this panel.
     */
    public JPAZPanel(Pane pane) {
	super();
	installListeners();
	bindTo(pane);
    }

    /**
     * Create a new buffered JPAZPanel with the specified layout manager.
     * 
     * @param layout
     *            the LayoutManager to use.
     */
    public JPAZPanel(LayoutManager layout) {
	super(layout);
	installListeners();
    }

    /**
     * Creates a new JPAZPanel with FlowLayout and the specified buffering
     * strategy.
     * 
     * @param isDoubleBuffered
     *            a boolean, true for double-buffering, which uses additional
     *            memory space to achieve fast, flicker-free updates.
     */
    public JPAZPanel(boolean isDoubleBuffered) {
	super(isDoubleBuffered);
	installListeners();
    }

    /**
     * Creates a new JPAZPanel with the specified layout manager and buffering
     * strategy.
     * 
     * @param layout
     *            the LayoutManager to use.
     * @param isDoubleBuffered
     *            a boolean, true for double-buffering, which uses additional
     *            memory space to achieve fast, flicker-free updates.
     */
    public JPAZPanel(LayoutManager layout, boolean isDoubleBuffered) {
	super(layout, isDoubleBuffered);
	installListeners();
    }

    // ---------------------------------------------------------------------------------------------------
    // Binding methods
    // ---------------------------------------------------------------------------------------------------

    /**
     * Binds this panel to a pane.
     * 
     * @param pane
     *            the pane to be bound to this panel.
     * @param align
     *            a boolean, true for aligning the bound pane to the panel.
     */
    public void bindTo(Pane pane, boolean align) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    if ((this.pane == pane) && (this.alignMode == align))
		return;

	    if ((this.pane == pane) && (this.alignMode != align)) {
		setAlignMode(align);
		return;
	    }

	    // stop transition effect (if it is running)
	    stopTransitionEffect();

	    // remove listener from previously bound pane
	    if (this.pane != null)
		this.pane.removePaneChangeListener(paneChangeListener);

	    this.alignMode = align;
	    this.pane = pane;

	    // add listener to new bound pane
	    if (this.pane != null) {
		this.pane.addPaneChangeListener(paneChangeListener);
		if (alignMode)
		    realignPane();
	    }

	    repaint();
	}
    }

    /**
     * Binds this panel to a pane.
     * 
     * @param pane
     *            the pane to be bound to this panel.
     */
    public void bindTo(Pane pane) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    bindTo(pane, alignMode);
	}
    }

    /**
     * Binds this panel to another pane with transition effect.
     * 
     * @param pane
     *            the pane to be bound to this panel.
     * @param align
     *            a boolean, true for aligning the bound pane to the panel.
     * @param transitionEffect
     *            the transition effect to be applied.
     * @param duration
     *            the duration of the transition in milliseconds.
     */
    public void rebindWithEffect(Pane pane, boolean align, TransitionEffect transitionEffect, long duration) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    stopTransitionEffect();
	    TransitionEffect.Transition newTransition = transitionEffect.createImplementation(this, duration);
	    bindTo(pane, align);
	    transition = newTransition;
	    repaint();
	}
    }

    /**
     * Binds this panel to another pane with transition effect.
     * 
     * @param pane
     *            the pane to be bound to this panel.
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

    /**
     * Stops the transition effect.
     */
    private void stopTransitionEffect() {
	if (transition != null) {
	    transition.stop();
	    transition = null;
	}
    }

    /**
     * Returns whether the bound pane is aligned to this panel. If aligned, any
     * change of panel size invokes resize of the bound pane and vice verse.
     * Moreover, the pane is always aligned in such a way that the top-left
     * corner of the pane corresponds to the top-left corner of this panel.
     * 
     * @return true, if the bound pane is aligned to this panel, false
     *         otherwise.
     */
    public boolean isAlignMode() {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    return alignMode;
	}
    }

    /**
     * Sets whether the bound pane is aligned to this panel. If aligned, any
     * change of panel size invokes resize of the bound pane and vice verse.
     * Moreover, the pane is always aligned in such a way that the top-left
     * corner of the pane corresponds to the top-left corner of this panel.
     * 
     * @param alignMode
     *            a boolean, true for aligning the bound pane to this panel.
     */
    public void setAlignMode(boolean alignMode) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    if (this.alignMode != alignMode) {
		this.alignMode = alignMode;

		if ((this.alignMode) && (this.pane != null))
		    realignPane();

		repaint();
	    }
	}
    }

    /**
     * Returns the pane that is bound to this panel.
     * 
     * @return the pane bound to this panel.
     */
    public Pane getPane() {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    return pane;
	}
    }

    /**
     * Sets the bound pane.
     * 
     * @param pane
     *            the desired pane to be bound to this panel.
     */
    public void setPane(Pane pane) {
	bindTo(pane);
    }

    // ---------------------------------------------------------------------------------------------------
    // Internal methods
    // ---------------------------------------------------------------------------------------------------

    /**
     * Realigns the pane. If repack frame is available, then this frame is
     * packed to match the size of the bound pane. Otherwise, the bound pane is
     * resized to match the size of this panel.
     */
    private void realignPane() {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		synchronized (JPAZUtilities.getJPAZLock()) {
		    if ((pane != null) && (JPAZPanel.this.alignMode)) {
			// check whether repack is required
			if ((pane.getWidth() == getWidth()) && (pane.getHeight() == getHeight()))
			    return;

			// resize/repack if necessary
			if (repackFrame != null) {
			    setPreferredSize(new Dimension(pane.getWidth(), pane.getHeight()));
			    repackFrame.pack();
			} else {
			    resizeFromPaneEnabled = false;
			    pane.resize(getWidth(), getHeight());
			    resizeFromPaneEnabled = true;
			}
		    }
		}
	    }
	});
    }

    /**
     * Sets the frame that should be packed after receiving resize from the
     * bound pane. This method is provided only for JPAZWindows.
     */
    void setRepackFrame(JFrame repackFrame) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    this.repackFrame = repackFrame;
	}
    }

    /**
     * Create and listener to receive events from the bound pane and GUI events.
     */
    private void installListeners() {
	// create listener for receiving event about changes of the bound pane
	paneChangeListener = new PaneChangeListener() {
	    public void paneResized(PaneChangeEvent e) {
		if (resizeFromPaneEnabled) {
		    resizeFromPane(e);
		    repaint();
		}
	    }

	    public void paneMoved(PaneChangeEvent e) {
		repaint();
	    }

	    public void paneInvalidated(PaneChangeEvent e) {
		repaint();
	    }

	    public void paneRotationChanged(PaneChangeEvent e) {
		repaint();
	    }
	};

	// create and install listener for receiving an event when the size of
	// the component has been changed
	this.addComponentListener(new ComponentAdapter() {
	    public void componentResized(ComponentEvent e) {
		int newWidth = JPAZPanel.this.getWidth();
		int newHeight = JPAZPanel.this.getHeight();
		synchronized (JPAZUtilities.getJPAZLock()) {
		    if ((pane != null) && (alignMode)) {
			resizeFromPaneEnabled = false;
			pane.resize(newWidth, newHeight);
			resizeFromPaneEnabled = true;
		    }
		}
	    }
	});

	// mouse events
	this.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		// click with left mouse key forces request to retrieve focus
		// (for keyboard events)
		if (e.getButton() == MouseEvent.BUTTON1) {
		    JPAZPanel.this.requestFocusInWindow();
		}

		fireMouseEventInBoundPane(MouseEvent.MOUSE_CLICKED, e);
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
		fireMouseEventInBoundPane(MouseEvent.MOUSE_PRESSED, e);
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
		fireMouseEventInBoundPane(MouseEvent.MOUSE_RELEASED, e);
	    }
	});

	// mouse motion events
	this.addMouseMotionListener(new MouseMotionAdapter() {
	    @Override
	    public void mouseMoved(MouseEvent e) {
		fireMouseEventInBoundPane(MouseEvent.MOUSE_MOVED, e);
		updateCursor(e);
	    }

	    @Override
	    public void mouseDragged(MouseEvent e) {
		fireMouseEventInBoundPane(MouseEvent.MOUSE_DRAGGED, e);
	    }
	});

	// keyboard event
	this.addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent e) {
		fireKeyEventInBoundPane(KeyEvent.KEY_PRESSED, e);
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
		fireKeyEventInBoundPane(KeyEvent.KEY_RELEASED, e);
	    }

	    @Override
	    public void keyTyped(KeyEvent e) {
		fireKeyEventInBoundPane(KeyEvent.KEY_TYPED, e);
	    }
	});
    }

    private void fireMouseEventInBoundPane(int type, MouseEvent e) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    if ((pane != null) && (transition == null)) {
		pane.fireMouseEvent(e.getX(), e.getY(), type, e, alignMode);
	    }
	}
    }

    private void fireKeyEventInBoundPane(int type, KeyEvent e) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    if ((pane != null) && (transition == null))
		pane.fireKeyEvent(type, e);
	}
    }

    /**
     * Updates the cursor in the case of "on mouse moved" event.
     * 
     * @param e
     *            the detail of event.
     */
    private void updateCursor(MouseEvent e) {
	synchronized (JPAZUtilities.getJPAZLock()) {
	    if (transition != null)
		return;

	    if (pane != null) {
		if (pane.canClick(e.getX(), e.getY(), !alignMode))
		    setCursor(CLICKABLE_CURSOR);
		else
		    setCursor(NOT_CLICKABLE_CURSOR);
	    }
	}
    }

    /**
     * Changes the preferred size of this panel after the size of the bound pane
     * changes.
     * 
     * @param e
     *            the event with information about change of the size of the
     *            bound pane.
     */
    private void resizeFromPane(PaneChangeEvent e) {
	Dimension preferredPanelSize;
	synchronized (JPAZUtilities.getJPAZLock()) {
	    if ((!alignMode) || (pane == null))
		return;

	    preferredPanelSize = new Dimension(pane.getWidth(), pane.getHeight());
	}

	final Dimension newPanelSize = preferredPanelSize;
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		setPreferredSize(newPanelSize);
		setSize(newPanelSize);
		synchronized (JPAZUtilities.getJPAZLock()) {
		    if (repackFrame != null)
			repackFrame.pack();
		    else
			revalidate();
		}
	    }
	});
    }

    @Override
    public void paint(Graphics g) {
	super.paint(g);

	synchronized (JPAZUtilities.getJPAZLock()) {
	    // in the case of active transition effect, use its rendering code
	    // to display the content of this panel
	    if (transition != null) {
		if (!transition.isCompleted()) {
		    transition.paintToPanel(pane, alignMode, (Graphics2D) g, getWidth(), getHeight());
		    return;
		} else {
		    transition = null;
		}
	    }

	    if (pane == null)
		return;

	    Graphics2D g2d = (Graphics2D) g;
	    if (alignMode) {
		backbuffer = null;
		pane.paintWithoutTransform(g2d);
	    } else {
		if (pane.getRotation() == 0) {
		    backbuffer = null;
		    pane.paintToPaneGraphics(g2d);
		} else {
		    // create backbuffer, if necessary
		    if ((backbuffer == null) || (backbuffer.getWidth() != getWidth())
			    || (backbuffer.getHeight() != getHeight())) {
			backbuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
		    }

		    // clean backbuffer and then draw the content
		    Graphics2D dbg = backbuffer.createGraphics();
		    dbg.setBackground(new Color(0, 0, 0, 0));
		    dbg.clearRect(0, 0, backbuffer.getWidth(), backbuffer.getHeight());
		    pane.paintToPaneGraphics(dbg);
		    dbg.dispose();

		    // draw backbuffer to the graphics
		    g2d.drawImage(backbuffer, null, 0, 0);
		}
	    }
	}
    }
}
