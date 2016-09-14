package sk.upjs.jpaz2.inspector;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.event.*;
import java.awt.*;
import java.text.*;

/**
 * Implements a cell editor that uses a formatted text field to edit Integer
 * values.
 */
@SuppressWarnings("serial")
class OINumberEditor extends DefaultCellEditor {

	/**
	 * Number formatter.
	 */
	private static class NumberFormatter extends DefaultFormatter {
		/**
		 * Class of the number
		 */
		Class<?> numberClass;

		/**
		 * Constructs the NumberFormatter.
		 * 
		 * @param numberClass
		 *            the class of result value
		 */
		public NumberFormatter(Class<?> numberClass) {
			this.numberClass = numberClass;
			setOverwriteMode(false);
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			if (text != null)
				text = text.trim();

			// empty text is treaded as null value
			if ("".equals(text) || (text == null)) {
				if (numberClass.isPrimitive())
					throw new ParseException(
							"For primitive values the empty string is not allowed.",
							0);
				else
					return null;
			}

			try {
				// solve transformation to floating point number
				if (float.class.equals(numberClass)
						|| Float.class.equals(numberClass))
					return Float.parseFloat(text);

				if (double.class.equals(numberClass)
						|| Double.class.equals(numberClass))
					return Double.parseDouble(text);

				// solve nonfloating point number
				long value = Long.parseLong(text);

				// long value
				if (long.class.equals(numberClass)
						|| Long.class.equals(numberClass))
					return new Long(value);

				// int value
				if (int.class.equals(numberClass)
						|| Integer.class.equals(numberClass)) {
					if ((value < Integer.MIN_VALUE)
							|| (value > Integer.MAX_VALUE))
						throw new ParseException(
								"Integer value is out of valid range.", 0);

					return new Integer((int) value);
				}

				// byte value
				if (byte.class.equals(numberClass)
						|| Byte.class.equals(numberClass)) {
					if ((value < Byte.MIN_VALUE) || (value > Byte.MAX_VALUE))
						throw new ParseException(
								"Byte value is out of valid range.", 0);

					return new Byte((byte) value);
				}

				return null;

			} catch (Exception e) {
				throw new ParseException("Parsing " + text + " to "
						+ numberClass.getSimpleName() + " failed.", 0);
			}
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value == null)
				return "";
			else
				return value.toString();
		}
	}

	/**
	 * Field for editing the value
	 */
	private JFormattedTextField ftf;

	/**
	 * Type of edited value
	 */
	private Class<?> type;

	/**
	 * Constructs the editor for numeric cells.
	 * 
	 * @param type
	 *            the type of
	 */
	public OINumberEditor(Class<?> type) {
		super(new JFormattedTextField());
		ftf = (JFormattedTextField) getComponent();
		this.type = type;

		// install number formatter
		ftf.setFormatterFactory(new DefaultFormatterFactory(
				new NumberFormatter(type)));

		// set GUI behaviour of text field
		ftf.setValue(null);
		ftf.setHorizontalAlignment(JTextField.LEADING);
		ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

		// set that one click on cell is enough for editing
		setClickCountToStart(1);

		// special handling code for ENTER
		ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"check");
		ftf.getActionMap().put("check", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (!ftf.isEditValid()) {
					if (userSaysRevert()) {
						ftf.postActionEvent();
					}
				} else
					try {
						ftf.commitEdit();
						ftf.postActionEvent();
					} catch (java.text.ParseException exc) {
						// nothing to do
					}
			}
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JFormattedTextField ftf = (JFormattedTextField) super
				.getTableCellEditorComponent(table, value, isSelected, row,
						column);
		ftf.setValue(value);
		return ftf;
	}

	@Override
	public Object getCellEditorValue() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		return ftf.getValue();
	}

	@Override
	public boolean stopCellEditing() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		if (ftf.isEditValid()) {
			try {
				ftf.commitEdit();
			} catch (java.text.ParseException exc) {
				// nothing to do
			}
		} else {
			if (!userSaysRevert())
				return false;
		}
		return super.stopCellEditing();
	}

	/**
	 * Asks the user whether to continue edit or rever the value.
	 */
	private boolean userSaysRevert() {
		ftf.selectAll();
		Object[] options = { "Edit", "Revert" };
		int answer = JOptionPane.showOptionDialog(SwingUtilities
				.getWindowAncestor(ftf),
				"The value must be a valid value for the type "
						+ this.type.getSimpleName() + ".\n"
						+ "You can either continue editing "
						+ "or revert to the last valid value.",
				"Invalid Text Entered", JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, options[1]);

		if (answer == 1) {
			ftf.setValue(ftf.getValue());
			return true;
		}
		return false;
	}
}
