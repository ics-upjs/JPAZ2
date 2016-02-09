package sk.upjs.jpaz2.inspector;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * The extension of TableModel adapted for purposes of OITable.
 */
@SuppressWarnings("serial")
abstract class OITableModel extends AbstractTableModel {
	/**
	 * Name of the first column
	 */
	private String columnName1 = "";
	
	/**
	 * Name of the second column
	 */
	private String columnName2 = "";
	
	/**
	 * Map that associates an additional info for each row
	 */
	protected ArrayList<Object> rowBinding = new ArrayList<Object>();
	
	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return rowBinding.size();
	}
	
	@Override
	public String getColumnName(int colIndex) {
		if (colIndex == 0)
			return columnName1;

		if (colIndex == 1)
			return columnName2;

		return null;
	}
	
	/**
	 * Sets column names
	 * @param columnName1 the name for the first column
	 * @param columnName2 the name for the second column
	 */
	public void setColumnNames(String columnName1, String columnName2) {
		this.columnName1 = columnName1;
		this.columnName2 = columnName2;
	}
	
	/**
	 * Gets the cell type of a specified cell
	 * @param rowIndex the row index of the cell
	 * @param colIndex the column index of the cell
	 */
	public Class<?> getCellType(int rowIndex, int colIndex) {
		Object value = getValueAt(rowIndex, colIndex);
		return (value != null) ? value.getClass() : null;
	}
	
	/**
	 * Returns whether a specified cell is read-only 
	 * @param rowIndex the row index of the cell
	 * @param colIndex the col index of the cell
	 * @return true, if the specified cell is read-only, false otherwise
	 */
	public boolean isReadOnlyCell(int rowIndex, int colIndex) {
		return true;
	}
	
	protected void mouseClicked(MouseEvent e, int rowIndex, int colIndex) {
		
	}
}
