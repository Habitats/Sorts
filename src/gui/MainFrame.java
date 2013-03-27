package gui;

import gui.view.MovieView;
import gui.view.SortsWindow;
import gui.view.SortsWindow.SortsWindowType;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.util.HashMap;

import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.UIManager;

public class MainFrame extends JFrame {

	private MovieView nationalityView;
	private HashMap<SortsWindowType, SortsWindow> views;
	private int frameWidth = 900;
	private int frameHeight = 600;

	public MainFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		views = new HashMap<SortsWindowType, SortsWindow>();
		nationalityView = new MovieView(this);
		JLayeredPane layeredPane = buildLayeredPane();
		getContentPane().add(layeredPane);

		buildFrame(this);
	}

	private JLayeredPane buildLayeredPane() {
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setBackground(Color.black);

		// ADD COMPONENTS
		layeredPane.add(nationalityView, new Integer(16));

		// SET BOUNDS ON EVERY COMPONENT ADDED DIRECTLY TO A LAYER
		nationalityView.setBounds(0, 0, frameWidth, frameHeight);

		layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));
		layeredPane.setOpaque(true);

		return layeredPane;
	}

	public HashMap<SortsWindowType, SortsWindow> getViews() {
		return views;
	}

	public void addView(SortsWindow view) {
		views.put(view.getType(), view);
	}

	private void buildFrame(JFrame frame) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(getRootPane());
		frame.setVisible(true);
		frame.setExtendedState(Frame.NORMAL);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		nationalityView.setBounds(0, 0, getWidth() - 7, getHeight() - 30);
		revalidate();
	}
}
