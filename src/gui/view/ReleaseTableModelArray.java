package gui.view;

import java.util.List;
import javax.swing.table.AbstractTableModel;

import sorts.Release;

public class ReleaseTableModelArray extends AbstractTableModel implements ReleaseTableModel {
	private String[] columnNames = { "Name", "Title", "Rating", "Nationality", "Group" };
	private Object[][] data = new Object[3000][5];
	private int currentRow = 0;

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public void addData(Release rls) {
		synchronized (data) {
			data[currentRow][0] = rls.getName();
			data[currentRow][1] = rls.getTitle();
			data[currentRow][2] = rls.getRating();
			data[currentRow][3] = rls.getNationality();
			data[currentRow][4] = rls.getGroup();

			fireTableRowsInserted(currentRow, currentRow);
			currentRow++;
		}
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	public void setData(List<Release> releases) {
		data = new Object[releases.size()][5];
		int i = 0;
		for (Release release : releases) {
			data[i][0] = release.getName();
			data[i][1] = release.getTitle();
			data[i][2] = release.getRating();
			data[i][3] = release.getNationality();
			data[i][4] = release.getGroup();
			fireTableRowsInserted(i, i);
			i++;
		}
	}

	@Override
	public void clearData() {
		data = new String[3000][5];
	}
}
