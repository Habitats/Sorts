package sorts;

import gui.MainFrame;
import gui.view.SortsWindow.SortsWindowType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import model.MovieModel;
import model.SortsModel;

public class Controller {
	private DatabaseQueries dbQueries;
	// private final Path root = Paths.get("D:\\_TEST");
	private SortsModel nationalityModel;

	public Controller() {

		MainFrame mainFrame = new MainFrame();
		nationalityModel = new MovieModel(this, mainFrame.getViews().get(SortsWindowType.NATIONALITY_SORTER));
		mainFrame.getViews().get(SortsWindowType.NATIONALITY_SORTER).setModel(nationalityModel);

		// DatabaseConnection dbConn = new DatabaseConnection();
		// dbQueries = new DatabaseQueries(dbConn);
	}

	public void addFromFile(File file) {
		BufferedInputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			Scanner scanner = new Scanner(in);
			while (scanner.hasNextLine()) {
				String next = scanner.nextLine();
				dbQueries.addSingle(next);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void addRelease(Release rls) {
		((MovieModel) nationalityModel).addRelease(rls);
	}

	public synchronized void log(String str) {
		((MovieModel) nationalityModel).addLine(str);
		System.out.println(str);
	}

}
