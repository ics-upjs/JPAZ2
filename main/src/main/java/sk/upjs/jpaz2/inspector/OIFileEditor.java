package sk.upjs.jpaz2.inspector;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 * Editor for colors.
 */
@SuppressWarnings("serial")
class OIFileEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	File currentFile;
	JButton button;
	JFileChooser fileChooser;

	protected static final String EDIT = "edit";

	public OIFileEditor() {
		button = new JButton();
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			if (currentFile != null) {
				File parentDirectory = currentFile.getParentFile();
				if (parentDirectory != null) {
					fileChooser.setCurrentDirectory(parentDirectory);
				}

				fileChooser.setSelectedFile(currentFile);
			}

			if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(button)) {
				currentFile = fileChooser.getSelectedFile();
			}

			fireEditingStopped();
		}
	}

	public Object getCellEditorValue() {
		return currentFile;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		currentFile = (File) value;
		if (currentFile != null) {
			button.setText(currentFile.toString());
		} else {
			button.setText("");
		}
		return button;
	}
}
