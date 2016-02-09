package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * An extension of JTable for displaying 2 row property - value tables.
 */
@SuppressWarnings("serial")
class OITable extends JTable {

	/**
	 * Table model for special needs of OI
	 */
	private OITableModel tableModel;

	/**
	 * Manager object containing a list of supported class type and providing
	 * appropriate cell renderers and editor for each supported class type.
	 */
	private OIClassSupporter classSupporter;

	/**
	 * Colors for striped rows
	 */
	private Color[] rowColors = new Color[0];

	/**
	 * Constructs an object inspector table for viewing and presenting values.
	 * 
	 * @param dm
	 *            the OI table model
	 * @param cs
	 *            the manager of supported class types
	 */
	public OITable(OITableModel dm, OIClassSupporter cs) {
		super(dm);
		tableModel = dm;
		classSupporter = cs;

		if (tableModel == null)
			throw new RuntimeException("TableModel cannot be null.");

		if (classSupporter == null)
			throw new RuntimeException("ClassSupported cannot be null.");

		installMouseHandlers();

		// UI settings
		this.setShowGrid(false);
		this.setIntercellSpacing(new Dimension(0, 0));
		this.setRowHeight(20);
		this.getTableHeader().setReorderingAllowed(false);
	}

	/**
	 * Gets the row colors used to paint stripped table.
	 */
	public Color[] getRowColors() {
		return rowColors.clone();
	}

	/**
	 * Sets the row colors used to paint stripped table.
	 * 
	 * @param rowColors
	 *            the colors used to draw stripes
	 */
	public void setRowColors(Color[] rowColors) {
		if (rowColors != null)
			this.rowColors = rowColors.clone();
		else
			this.rowColors = new Color[0];
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		Object value = getValueAt(row, column);
		if (value instanceof UnknownValue)
			return classSupporter.getTableCellRenderer(UnknownValue.class);
		else
			return classSupporter.getTableCellRenderer(tableModel.getCellType(row,
				column));
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		Class<?> cellClass = tableModel.getCellType(row, column);
		boolean isReadOnlyCell = tableModel.isReadOnlyCell(row, column);
		return classSupporter.getTableCellEditor(cellClass, isReadOnlyCell);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row,
			int column) {
		Component rComponent = super.prepareRenderer(renderer, row, column);

		if (rComponent == null)
			return null;

		// change the background color of the component in order to get stripped
		// table
		Color bgColor = getBackground();

		if (rowColors.length != 0) {
			bgColor = rowColors[row % rowColors.length];
			if (bgColor == null)
				bgColor = getBackground();
		}

		rComponent.setBackground(bgColor);
		return rComponent;
	}

	/**
	 * Installs mouse handlers
	 */
	private void installMouseHandlers() {
		// handler propagating mouse clicks to the table model
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int column = columnAtPoint(e.getPoint());
				tableModel.mouseClicked(e, row, column);
			}
		});
	}
}
