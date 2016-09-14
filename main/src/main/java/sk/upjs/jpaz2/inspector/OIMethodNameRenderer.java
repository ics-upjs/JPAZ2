package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * The renderer for OIMethodName. It renders the method name and its signature.
 */
@SuppressWarnings("serial")
class OIMethodNameRenderer extends DefaultTableCellRenderer {
	
	/**
	 * Constructs the renderer displaying method names.
	 */
	public OIMethodNameRenderer() {
		setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Method method = ((OIMethodNameValue)value).method;
		// set method name
		setValue(method.getName());
		
		// prepare and set tool tip text displaying complete method signature
		StringBuffer toolTipText = new StringBuffer();
		if (Modifier.isStatic(method.getModifiers()))
			toolTipText.append("static ");
		toolTipText.append(method.getReturnType().getSimpleName() + " " + method.getName() + "(");
		boolean firstParameter = true;
		for (Class<?> parameterClass : method.getParameterTypes())
		{
			if (firstParameter)
				firstParameter = false;
			else
				toolTipText.append(", "); 
			
			toolTipText.append(parameterClass.getSimpleName());
		}
		toolTipText.append(')');
		setToolTipText(toolTipText.toString());
		
		// for static method uses italic
		if (Modifier.isStatic(method.getModifiers()))
			setFont(getFont().deriveFont(Font.BOLD | Font.ITALIC));
		else
			setFont(getFont().deriveFont(Font.BOLD));
		
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.LIGHT_GRAY);
		//g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
		g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()-1);
	}
}