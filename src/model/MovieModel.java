package model;

import gui.view.MovieView;
import gui.view.MovieView.ReleaseTable;
import gui.view.SortsWindow;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.text.BadLocationException;
import sorts.Controller;
import sorts.Release;

public class MovieModel extends SortsModel {

	private final Controller controller;
	private MovieView view;
	private ArrayList<Thread> searchThreads;
	private int releasesCount;

	public MovieModel(Controller controller, SortsWindow sortsWindow) {
		searchThreads = new ArrayList<Thread>();
		this.controller = controller;
		this.view = (MovieView) sortsWindow;
	}

	public void analyze() {
		releasesCount = 0;
		for (Thread searchThread : searchThreads) {
			searchThread.interrupt();
		}
		view.clearTable();
		String fromStr = view.getFromField().getText();
		Path from = (fromStr.contains("\"") ? Paths.get(fromStr.split("\"")[1]) : Paths.get(fromStr));
		if (Files.isDirectory(from))
			recursiveWalk(from);
		else if (from.getFileName().toString().endsWith(".txt"))
			scanFromFile(from);
	}

	private void scanFromFile(final Path from) {
		Thread searchThread = new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<Release> releases = new ArrayList<Release>();
				try (BufferedReader in = Files.newBufferedReader(from, Charset.defaultCharset())) {
					String line;
					while ((line = in.readLine()) != null)
						releases.add(new Release(line));
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (Release rls : releases) {
					new Thread(new MovieParser(rls, controller)).start();
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		searchThreads.add(searchThread);
		searchThread.start();
	}

	public void sort() {
	}

	private void recursiveWalk(final Path fromPath) {
		Thread searchThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Files.walkFileTree(fromPath, new ReleaseVisitor(fromPath, controller));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		searchThreads.add(searchThread);
		searchThread.start();
	}

	public void nationalitySort(List<Release> releases, Path fromPath) {
		Path toPath = (getToPath().toString().equals("")) ? fromPath : getToPath();

		for (Release release : releases) {
			Path newPath = toPath.resolve("_" + release.getNationality().toUpperCase().replaceAll("I", "i").replaceAll("\\s", "."));
			controller.log("Moving " + release.getPath() + " --> " + newPath);
			try {
				Files.walkFileTree(release.getPath(), new ReleaseMover(release.getPath().getParent(), newPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Path getToPath() {
		return Paths.get(view.getToField().getText());
	}

	public void addRelease(Release rls) {
		((ReleaseTable) view.getReleaseTable()).addRelease(rls);
		view.getReleasesCount().setText(Integer.toString(++releasesCount));
	}

	public void addLine(String str) {
		if (str.replaceAll("\\s", "").length() == 0)
			return;

		try {
			view.getTextPane().getDocument().insertString(view.getTextPane().getText().length(), "\n" + str, null);
		} catch (BadLocationException e) {
		}
		view.getTextPane().setCaretPosition(view.getTextPane().getDocument().getLength());
	}

	/**
	 * finds all releases recursively, starting at a given root
	 */
	private class ReleaseVisitor extends SimpleFileVisitor<Path> {

		private final Path fromPath;
		private final Controller controller;

		public ReleaseVisitor(Path fromPath, Controller controller) {
			this.fromPath = fromPath;
			this.controller = controller;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.getFileName().toString().startsWith("_") && !dir.equals(fromPath) || dir.getFileName().toString().contains(" "))
				return FileVisitResult.SKIP_SUBTREE;
			else if (dir.getFileName().toString().contains("-")) {
				Release rls = new Release(dir.getFileName().toString(), dir);
				new Thread(new MovieParser(rls, controller)).start();
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					return FileVisitResult.TERMINATE;
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	/**
	 * sorts releases into specified dirs
	 */
	private class ReleaseMover extends SimpleFileVisitor<Path> {
		private final Path newRoot;
		private final Path oldRoot;

		public ReleaseMover(Path oldRoot, Path newRoot) {
			this.oldRoot = oldRoot;
			this.newRoot = newRoot;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Path oldRoot2 = oldRoot.relativize(newRoot).resolve(oldRoot.relativize(file));
			Path oldRoot3 = oldRoot.resolve(oldRoot2);
			// log("Moving: " + file + " --> " + oldRoot3);
			Files.createDirectories(oldRoot3.getParent());
			Files.move(file, oldRoot3, StandardCopyOption.ATOMIC_MOVE);
			return FileVisitResult.CONTINUE;
		}
	}

	@Override
	public void setView(SortsWindow view) {
		this.view = (MovieView) view;
	}

}
