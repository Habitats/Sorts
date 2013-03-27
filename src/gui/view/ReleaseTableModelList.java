package gui.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import sorts.Release;

public class ReleaseTableModelList extends AbstractTableModel implements ReleaseTableModel {
	private String[] columnNames = { "Name", "Title", "Rating", "Nationality", "Group" };
	private List<List<String>> data = Collections.synchronizedList(new ArrayList<List<String>>());

	@Override
	public synchronized void addData(final Release rls) {
		synchronized (data) {
			data.add(new ArrayList<String>() {
				{
					add(rls.getName());
					add(rls.getTitle());
					add(rls.getRating());
					add(rls.getNationality());
					add(rls.getGroup());
				}
			});
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).get(columnIndex);
	}

	@Override
	public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
		data.get(rowIndex).add(columnIndex, (String) newValue);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	@Override
	public void clearData() {
		data.clear();
	}
}
