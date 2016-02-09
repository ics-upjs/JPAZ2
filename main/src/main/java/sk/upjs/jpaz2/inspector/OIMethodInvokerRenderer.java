package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * The renderer for OIMethodInvokerValue. It renders button for
 * opening a method invocation window.
 */
@SuppressWarnings("serial")
public class OIMethodInvokerRenderer extends JButton implements TableCellRenderer {
	/**
	 * Icon for execute button.
	 */
	private BufferedImage executeIcon;
	
	/**
	 * Constructs the renderer.
	 */
	public OIMethodInvokerRenderer() {
		// load icon
		try {
			executeIcon = ImageIO.read(this.getClass()
					.getResource("/sk/upjs/jpaz2/images/execute.png"));
		} catch (Exception e) {
			executeIcon = null;
		}
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return this;
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		// fill the background with gradient
		Color bgColor = getBackground();
		Color bgLighter = addLight(bgColor, 15);
		Color bgDarker = addLight(bgColor, -15);
		g2.setPaint(new GradientPaint(0, 0, bgLighter, 0, getHeight(), bgDarker));
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		// draw execute icon
		if (executeIcon != null)
			g2.drawImage(executeIcon, null, (getWidth() - executeIcon.getWidth()) / 2, (getHeight() - executeIcon.getHeight())/2);
		else
		{
			g2.setPaint(Color.BLACK);
			g2.setFont(g2.getFont().deriveFont(Font.BOLD));
			g2.drawString(">>>", 3, 14);
		}			
	}
	
	/**
	 * Computes lighter or darker color.
	 * @param c the basic color
	 * @param delta the added amount of light 
	 * @return new color
	 */
	private Color addLight(Color c, int delta) {
		int[] rgbColor = new int[] {c.getRed(), c.getGreen(), c.getBlue()};
		
		for (int i=0; i<3; i++) {
			rgbColor[i] += delta;
			if (rgbColor[i] < 0)
				rgbColor[i] = 0;
			
			if (rgbColor[i] > 255)
				rgbColor[i] = 255;
		}
		
		return new Color(rgbColor[0], rgbColor[1], rgbColor[2]);
	}
}
