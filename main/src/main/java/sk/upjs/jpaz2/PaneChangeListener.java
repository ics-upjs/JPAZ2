package sk.upjs.jpaz2;

/**
 * The listener interface for receiving change events of a pane.
 */
interface PaneChangeListener {

    /**
     * Invoked when a pane is resized.
     */
    void paneResized(PaneChangeEvent e);

    /**
     * Invoked when a pane is moved.
     */
    void paneMoved(PaneChangeEvent e);

    /**
     * Invoked when the graphical content of a pane is invalidated.
     */
    void paneInvalidated(PaneChangeEvent e);

    /**
     * Invoked when a pane rotation is changed.
     */
    void paneRotationChanged(PaneChangeEvent e);
}
