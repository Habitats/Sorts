package gui.view;

import sorts.Release;

public interface ReleaseTableModel {
	public void addData(Release releaase);

	public void clearData();

	public String getColumnName(int column);

	public Object getValueAt(int rowIndex, int columnIndex);

	public void setValueAt(Object newValue, int rowIndex, int columnIndex);

}
