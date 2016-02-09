package sk.upjs.jpaz2.inspector;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.event.*;
import java.awt.*;
import java.text.*;

/**
 * Implements a cell editor that uses a formatted text field to edit Character
 * values.
 */
@SuppressWarnings("serial")
public class OICharEditor extends DefaultCellEditor {

	/**
	 * Character formatter.
	 */
	private static class CharacterFormatter extends DefaultFormatter {
		/**
		 * Constructs the CharacterFormatter.
		 */
		public CharacterFormatter() {
			setOverwriteMode(false);
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			if (text != null)
				text = text.trim();

			if ((text == null) || (text.length() == 0))
				return null;

			// there is no need to check presence of escape sequences in one
			// character inputs
			if (text.length() == 1)
				return text.charAt(0);

			// check start of a java-like character escape sequence starting
			// with backslash
			if (text.charAt(0) == '\\') {
				text = text.substring(1).toLowerCase();

				// check special single character escape sequences
				if (text.length() == 1) {
					switch (text.charAt(0)) {
					case 's':
						return ' ';
					case 'b':
						return '\b';
					case 't':
						return '\t';
					case 'n':
						return '\n';
					case 'f':
						return '\f';
					case 'r':
						return '\r';
					case '\"':
						return '\"';
					case '\'':
						return '\'';
					case '\\':
						return '\\';
					}
					throw new ParseException(
							"Parsing \\"
									+ text
									+ " to a character failed. Unsupported escape sequence occured.",
							0);
				}

				// check unicode escape sequences
				if (text.charAt(0) == 'u') {
					try {
						return (char) Integer.parseInt(text.substring(1), 16);
					} catch (Exception e) {
						throw new ParseException(
								"Parsing "
										+ text
										+ " as a hexadecimal number of a unicode character failed.",
								0);
					}
				}

				// check for special space sequence
				if ("space".equals(text))
					return ' ';

				// if the escape sequence starts with a digit, an octal sequence
				// is expected
				if (Character.isDigit(text.charAt(0))) {
					try {
						return (char) Integer.parseInt(text, 8);
					} catch (Exception e) {
						throw new ParseException(
								"Parsing \\"
										+ text
										+ "as an octal number of a unicode character failed.",
								0);
					}
				}

				// if this point is reached, an unknown escape sequence is
				// detected
				throw new ParseException(
						"Parsing \\"
								+ text
								+ " to a character failed. Unsupported escape sequence occured.",
						0);
			}

			// check start of a pascal-like character code
			if (text.charAt(0) == '#') {
				try {
					return (char) Integer.parseInt(text.substring(1));
				} catch (Exception e) {
					throw new ParseException("Parsing #" + text
							+ " as a number of a unicode character failed.", 0);
				}
			}

			// check special escape sequence "space" denoting the space
			// character (u0020)
			if ("space".equals(text))
				return ' ';

			throw new ParseException("Parsing " + text
					+ " as a unicode character failed.", 0);
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value == null)
				return "";
			else {
				char charValue;
				try {
					charValue = (Character) value;
				} catch (Exception e) {
					return "";
				}

				// first, check the special values with standardized escape
				// sequences
				switch (charValue) {
				case ' ':
					return "space";
				case '\b':
					return "\\b";
				case '\t':
					return "\\t";
				case '\n':
					return "\\n";
				case '\f':
					return "\\f";
				case '\r':
					return "\\r";
				}

				// non-displayable characters are displayed as escape sequences
				if ((charValue < 32)
						|| (charValue == 127)
						|| (!(new JFormattedTextField()).getFont().canDisplay(
								charValue)))
					return toUnicodeEscapeSequence(charValue);
				else
					return Character.toString(charValue);
			}
		}

		/**
		 * Translates character value to its unicode escape sequence.
		 * 
		 * @param charValue
		 *            character value to be translated
		 * @return a string containing unicode escape sequence
		 */
		private static String toUnicodeEscapeSequence(char charValue) {
			String hexNumber = Integer.toHexString(charValue).toUpperCase();
			while (hexNumber.length() < 4)
				hexNumber = '0' + hexNumber;

			return "\\u" + hexNumber;
		}
	}

	/**
	 * Field for editing the value
	 */
	private JFormattedTextField ftf;

	/**
	 * Constructs the editor for cells with a character.
	 */
	public OICharEditor() {
		super(new JFormattedTextField());
		ftf = (JFormattedTextField) getComponent();

		// install character formatter
		ftf.setFormatterFactory(new DefaultFormatterFactory(
				new CharacterFormatter()));

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
		int answer = JOptionPane.showOptionDialog(
				SwingUtilities.getWindowAncestor(ftf),
				"The value must be a unicode character or an escape sequence.\n"
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
