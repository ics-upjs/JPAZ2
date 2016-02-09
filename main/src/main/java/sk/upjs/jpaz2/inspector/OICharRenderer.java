package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer for character values.
 */
@SuppressWarnings("serial")
class OICharRenderer extends DefaultTableCellRenderer {
	
	/**
	 * List of descriptions for special characters 
	 */
	private static Map<Character, String> specialCharsDescription = new HashMap<Character, String>();
	
	static {
		// Initialization of special descriptions
		specialCharsDescription.put('\b', "\\b (\\u0008: backspace BS)");
		specialCharsDescription.put('\t', "\\t (\\u0009: horizontal tab HT)");
		specialCharsDescription.put('\n', "\\n (\\u000A: linefeed LF)");
		specialCharsDescription.put('\f', "\\f (\\u000C: form feed FF)");
		specialCharsDescription.put('\r', "\\r (\\u000D: carriage return CR)");
		specialCharsDescription.put(' ',  "  (\\u0020: space)");
	}
	
	/**
	 * Constructs the renderer
	 */
	public OICharRenderer() {
		setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
		setFont(getFont().deriveFont(0));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value == null) {
			setValue("");
			return this;
		}
		
		char charValue = (Character)value;	
		if (specialCharsDescription.containsKey(charValue))
			setValue(specialCharsDescription.get(charValue));
		else
			setValue(charValue + " (" + toUnicodeEscapeSequence(charValue) + ")");
		
		return this;
	}
	
	/**
	 * Translates character value to its unicode escape sequence.
	 * @param charValue character value to be translated
	 * @return a string containing unicode escape sequence
	 */
	private static String toUnicodeEscapeSequence(char charValue) {
		String hexNumber = Integer.toHexString(charValue).toUpperCase();
		while (hexNumber.length() < 4)
			hexNumber = '0' + hexNumber;
		
		return "\\u" + hexNumber;
	}
}
