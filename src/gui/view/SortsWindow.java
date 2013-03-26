package gui.view;

import javax.swing.JPanel;

import model.SortsModel;

public abstract class SortsWindow extends JPanel {
	public enum SortsWindowType {
		NATIONALITY_SORTER,
	}

	private SortsWindowType type;

	public abstract void setModel(SortsModel model);

	protected void setType(SortsWindowType type) {
		this.type = type;
	}

	public SortsWindowType getType() {
		return type;
	}

}
