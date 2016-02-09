package sk.upjs.jpaz2.inspector;

import java.awt.*;
import javax.swing.*;

/**
 * Editor for enumaration types
 *
 */
@SuppressWarnings("serial")
class OIEnumEditor extends DefaultCellEditor {
	/**
	 * Specified enum class (preferred before value class)
	 */
	private Class<?> enumClass;
	
	/**
	 * Constructs universal enum editor.
	 */
	public OIEnumEditor() {
		super(new JComboBox<Object>());
	}
	
	public OIEnumEditor(Class<?> enumClass) {
		this();
		this.enumClass = enumClass; 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JComboBox<Object> comboBox = (JComboBox<Object>)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		
		// list all possible enum values for this enum class
		comboBox.removeAllItems();
		
		Class<?> currentEnumClass = enumClass;
		if ((enumClass == null) && (value != null))
			currentEnumClass = value.getClass();
		
		if (currentEnumClass == null)
			return comboBox;
		
		for (Object enumConstant: currentEnumClass.getEnumConstants())
			comboBox.addItem(enumConstant);
		
		if (value != null)
			comboBox.setSelectedItem(value);
		else
			comboBox.setSelectedIndex(0);
		
		return comboBox;
	}
}
