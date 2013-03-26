package gui.view;

import gui.GBC;
import gui.MainFrame;
import gui.SortsButton;
import gui.SortsField;
import gui.SortsLabel;
import gui.SortsButton.ButtonType;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import sorts.Release;

import model.NationalityModel;
import model.SortsModel;

public class NationalitySorter extends SortsWindow {
	private NationalityModel model;
	private JTextPane textPane;
	private SortsField fromField;
	private SortsField toField;
	private JTable releaseTable;

	public NationalitySorter(MainFrame mainFrame) {
		setType(SortsWindowType.NATIONALITY_SORTER);
		mainFrame.addView(this);
		setLayout(new GridBagLayout());

		fromField = new SortsField();
		toField = new SortsField();

		OrganizeButtonListener listener = new NationalityButtonListener();
		SortsButton browseButtonFrom = new SortsButton("browse", ButtonType.FILECHOOSER, listener, fromField);
		SortsButton browseButtonTo = new SortsButton("browse", ButtonType.FILECHOOSER, listener, toField);
		SortsButton analyzeButton = new SortsButton("analyze", ButtonType.ANALYZE, listener);
		SortsButton sortButton = new SortsButton("execute sort", ButtonType.SORT, listener);

		SortsLabel fromLabel = new SortsLabel("from");
		SortsLabel toLabel = new SortsLabel("to");

		add(fromLabel, new GBC(0, 2));
		add(toLabel, new GBC(0, 4));

		add(fromField, new GBC(1, 2).setWeight(1, 0).setSpan(8, 1));
		add(toField, new GBC(1, 4).setWeight(1, 0).setSpan(8, 1));

		add(browseButtonFrom, new GBC(9, 2).setAnchor(GridBagConstraints.EAST));
		add(browseButtonTo, new GBC(9, 4).setAnchor(GridBagConstraints.EAST));

		add(analyzeButton, new GBC(8, 20).setSpan(1, 1));
		add(sortButton, new GBC(9, 20).setSpan(1, 1));

		textPane = new JTextPane();
		releaseTable = new ReleaseTable();
		// JScrollPane scrollPane = new JScrollPane(textPane);
		JScrollPane scrollPane = new JScrollPane(releaseTable);
		add(scrollPane, new GBC(0, 10).setWeight(1, 1).setSpan(10, 1));
	}

	public class ReleaseTable extends JTable {
		public ReleaseTable() {
			super(new ReleaseTableModel());
			setFillsViewportHeight(true);
			setAutoCreateRowSorter(true);
			// setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			getModel().addTableModelListener(this);

			getColumnModel().getColumn(0).setMinWidth(100);
			getColumnModel().getColumn(1).setPreferredWidth(5);
			getColumnModel().getColumn(2).setPreferredWidth(5);
			getColumnModel().getColumn(3).setPreferredWidth(5);
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			super.tableChanged(e);
		}

		public void addReleases(List<Release> releases) {
			((ReleaseTableModel) getModel()).setData(releases);
		}

		public void addRelease(Release rls) {
			((ReleaseTableModel) getModel()).addData(rls);
		}
	}

	public JTable getReleaseTable() {
		return releaseTable;
	}

	private class ReleaseTableModel extends AbstractTableModel {
		private String[] columnNames = { "Name", "Title", "Rating", "Nationality", "Group" };
		private Object[][] data = new Object[1000][5];
		private int currentRow = 0;

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

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
	}

	private class NationalityButtonListener extends OrganizeButtonListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (((SortsButton) e.getSource()).getType()) {
			case ANALYZE:
				model.analyze();
				break;
			case FILECHOOSER:
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int value = fileChooser.showOpenDialog(((Component) e.getSource()).getParent());
				if (value == JFileChooser.APPROVE_OPTION) {
					((SortsButton) e.getSource()).getField().setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
				break;
			case SORT:
				// model.nationalitySort(releases, fromPath);

				break;
			}
		}
	}

	public JTextPane getTextPane() {
		return textPane;
	}

	public SortsField getFromField() {
		return fromField;
	}

	public SortsField getToField() {
		return toField;
	}

	@Override
	public void setModel(SortsModel model) {
		this.model = (NationalityModel) model;
	}
}
