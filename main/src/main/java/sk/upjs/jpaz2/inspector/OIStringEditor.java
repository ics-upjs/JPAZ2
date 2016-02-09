package sk.upjs.jpaz2.inspector;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

/**
 * Editor for string values.
 */
@SuppressWarnings("serial")
class OIStringEditor extends DefaultCellEditor {
	public OIStringEditor() {
		super(new JTextField());
		setClickCountToStart(1);
	}
}
