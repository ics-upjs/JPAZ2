package sk.upjs.jpaz2;

/**
 *	An event which indicates that a change of the pane occurred.
 */
class PaneChangeEvent {

	/**
	 * Pane where the change occurred.
	 */
	private Pane source;

	/**
	 * Creates a new PaneChangeEvent object containing details of the event that occurred.
	 * @param source the pane that initiated the event
	 */
	public PaneChangeEvent(Pane source) {
		this.source = source;
	}

	/**
	 * Returns the pane where the change occurred.
	 * @return a pane where the change occurred
	 */
	public Pane getSource() {
		return source;
	}
	
}
