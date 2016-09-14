package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

// Code based on: http://java.sun.com/docs/books/tutorial/uiswing/examples/components/TableDialogEditDemoProject/src/components/ColorEditor.java
/**
 * Editor for colors.
 */
@SuppressWarnings("serial")
class OIColorEditor extends AbstractCellEditor implements
		TableCellEditor, ActionListener {
	
	Color currentColor;
	JButton button;
	JColorChooser colorChooser;
	JDialog dialog;
	protected static final String EDIT = "edit";

	public OIColorEditor() {
		button = new JButton() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2 = (Graphics2D) g;
				if (getBackground() != null) {
					g2.setPaint(getBackground());
					g2.fillRect(2, 2, getWidth() - 4, getHeight() - 3);
					g2.setColor(Color.black);
					g2.drawRect(1, 1, getWidth() - 3, getHeight() - 2);
				} else {
					g2.setColor(Color.black);
					g2.drawRect(1, 1, getWidth() - 3, getHeight() - 2);
					g2.drawLine(1, 1, getWidth() - 2, getHeight() - 2);
					g2.drawLine(1, getHeight() - 2, getWidth() - 2, 1);
				}
			}
		};

		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);

		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button, "Color Chooser", true,
				colorChooser, this, null);
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);
			fireEditingStopped();
		} else {
			currentColor = colorChooser.getColor();
		}
	}

	public Object getCellEditorValue() {
		return currentColor;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		currentColor = (Color) value;
		return button;
	}
}
