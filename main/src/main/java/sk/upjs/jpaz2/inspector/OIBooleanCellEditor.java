package sk.upjs.jpaz2.inspector;

import java.awt.Component;

import javax.swing.*;

/**
 * The ComboBox editor for boolean values.
 */
@SuppressWarnings("serial")
public class OIBooleanCellEditor extends DefaultCellEditor {

	/**
	 * Constructs the editor.
	 * 
	 * @param nullAllowed
	 *            true, if null is possible value
	 */
	@SuppressWarnings("unchecked")
	public OIBooleanCellEditor(boolean nullAllowed) {
		super(new JComboBox<Object>());
		JComboBox<Boolean> component = (JComboBox<Boolean>) getComponent();
		if (nullAllowed)
			component.addItem(null);

		component.addItem(new Boolean(true));
		component.addItem(new Boolean(false));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JComboBox<Boolean> result = (JComboBox<Boolean>)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		result.setSelectedItem(value);
		return result;
	}
}
