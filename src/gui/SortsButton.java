package gui;

import gui.view.OrganizeButtonListener;

import java.awt.Dimension;
import javax.swing.JButton;

public class SortsButton extends JButton {
	private final ButtonType type;
	private final SortsField field;

	public enum ButtonType {
		SORT, FILECHOOSER, ANALYZE
	}

	public SortsButton(String string, ButtonType type, OrganizeButtonListener listener) {
		this(string, type, listener, null);
	}

	public SortsButton(String string, ButtonType type, OrganizeButtonListener listener, SortsField field) {
		super(string);
		this.type = type;
		setMaximumSize(new Dimension(60, 20));
//		setMinimumSize(new Dimension(40, 20));
//		setPreferredSize(new Dimension(40, 20));
		addActionListener(listener);
		this.field = field;

	}

	public SortsField getField() {
		return field;
	}

	public ButtonType getType() {
		return type;
	}
}
