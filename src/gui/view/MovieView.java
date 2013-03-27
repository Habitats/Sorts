package gui.view;

import gui.GBC;
import gui.GBC.Align;
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

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import sorts.Release;

import model.MovieModel;
import model.SortsModel;

public class MovieView extends SortsWindow {
	private MovieModel model;
	private JTextPane textPane;
	private SortsField fromField;
	private SortsField toField;
	private JTable releaseTable;
	private JCheckBox autoScroll;
	private JScrollPane scrollPane;
	private SortsLabel releasesCount;

	public MovieView(MainFrame mainFrame) {
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

		SortsLabel autoScrollLabel = new SortsLabel("auto scroll:");
		autoScroll = new JCheckBox();

		SortsLabel releasesCountLabel = new SortsLabel("releases:");
		releasesCount = new SortsLabel("0");

		add(fromLabel, new GBC(0, 2, Align.LEFT_TOP));
		add(toLabel, new GBC(0, 4, Align.LEFT));

		add(fromField, new GBC(1, 2, Align.MID_TOP).setWeight(1, 0).setSpan(8, 1));
		add(toField, new GBC(1, 4, Align.MID).setWeight(1, 0).setSpan(8, 1));

		add(browseButtonFrom, new GBC(9, 2, Align.RIGHT_TOP).setAnchor(GridBagConstraints.EAST));
		add(browseButtonTo, new GBC(9, 4, Align.RIGHT).setAnchor(GridBagConstraints.EAST));

		add(autoScrollLabel, new GBC(0, 20, Align.LEFT_BOTTOM));
		add(autoScroll, new GBC(1, 20, Align.MID_BOTTOM));

		add(releasesCountLabel, new GBC(2, 20, Align.MID_BOTTOM));
		add(releasesCount, new GBC(3, 20, Align.MID_BOTTOM));

		add(analyzeButton, new GBC(8, 20, Align.MID_BOTTOM).setSpan(1, 1).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.EAST));
		add(sortButton, new GBC(9, 20, Align.RIGHT_BOTTOM).setSpan(1, 1).setFill(GridBagConstraints.NONE).setAnchor(GridBagConstraints.EAST));

		textPane = new JTextPane();
		releaseTable = new ReleaseTable();
		scrollPane = new JScrollPane(releaseTable);
		add(scrollPane, new GBC(0, 10, Align.FULL_WIDTH).setWeight(1, 1).setSpan(10, 1));
	}

	public class ReleaseTable extends JTable {
		public ReleaseTable() {
			// super(new ReleaseTableModelArray());
			super(new ReleaseTableModelList());
			setFillsViewportHeight(true);
			setAutoCreateRowSorter(true);
			// setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			getModel().addTableModelListener(this);

			getColumnModel().getColumn(0).setMinWidth(400);

			getColumnModel().getColumn(1).setPreferredWidth(200);
			getColumnModel().getColumn(1).setMinWidth(50);

			getColumnModel().getColumn(2).setMaxWidth(50);
			getColumnModel().getColumn(2).setMinWidth(50);

			getColumnModel().getColumn(3).setPreferredWidth(60);
			getColumnModel().getColumn(3).setMinWidth(60);

			getColumnModel().getColumn(4).setPreferredWidth(50);
			getColumnModel().getColumn(4).setMinWidth(50);
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			try {
				// not sure what happens here... but prob some swing anti-concurrency bullshit
				super.tableChanged(e);
			} catch (Exception e2) {
			}
			if (autoScroll.isSelected())
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
					}
				});
		}

		public void addRelease(Release rls) {
			((ReleaseTableModel) getModel()).addData(rls);
		}
	}

	public JTable getReleaseTable() {
		return releaseTable;
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
				fileChooser.setCurrentDirectory(new File(fromField.getText()));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int value = fileChooser.showOpenDialog(((Component) e.getSource()).getParent());
				if (value == JFileChooser.APPROVE_OPTION) {
					((SortsButton) e.getSource()).getField().setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
				break;
			case SORT:
				model.sort();

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

	public SortsLabel getReleasesCount() {
		return releasesCount;
	}

	@Override
	public void setModel(SortsModel model) {
		this.model = (MovieModel) model;
	}

	public void clearTable() {
		((ReleaseTableModel) releaseTable.getModel()).clearData();
	}
}
