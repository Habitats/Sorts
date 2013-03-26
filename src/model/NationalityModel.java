package model;

import gui.view.NationalitySorter;
import gui.view.NationalitySorter.ReleaseTable;
import gui.view.SortsWindow;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.text.BadLocationException;
import sorts.Controller;
import sorts.Release;

public class NationalityModel extends SortsModel implements Runnable {

	private List<Release> releases;
	private final Controller controller;
	private NationalitySorter view;

	public NationalityModel(Controller controller, SortsWindow sortsWindow) {
		this.controller = controller;
		this.view = (NationalitySorter) sortsWindow;
	}

	public void analyze() {
		new Thread(new NationalityModel(controller, view)).start();
	}

	private void recursiveWalk(Path fromPath) {
		releases = Collections.synchronizedList(new ArrayList<Release>());
		try {
			Files.walkFileTree(fromPath, new ReleaseVisitor(fromPath, this));
			// latch.await();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Path getToPath() {
		return Paths.get(view.getToField().getText());
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

	private class NationalityParser implements Runnable {

		private final Release rls;

		public NationalityParser(Release rls) {
			this.rls = rls;
		}

		@Override
		public void run() {
			if (getMovieInfoFromApi(rls) == null)
				getMovieInfoFromImdbManually(rls);
			NationalityModel.this.addRelease(rls);
		}

		private Release getMovieInfoFromApi(Release rls) {
			String url = String.format("http://imdbapi.org/?title=%s&type=json&plot=simple&episode=1&limit=1&yg=0&mt=none&lang=en-US&offset=&aka=simple&release=simple&business=0&tech=0", rls.getTitle().replaceAll("\\s", "%20"));
			String raw = getSourceHttpConnFromImdbApi(url, 10);
			String nationality = "";
			String rating = "";
			if (raw.contains("\"code\":404"))
				return null;
			HashMap<String, Object> json = toJson(raw);
			if (json != null) {
				if (json.containsKey("country"))
					nationality = ((ArrayList<String>) json.get("country")).get(0);
				else if (json.containsKey("language"))
					nationality = ((ArrayList<String>) json.get("language")).get(0);

				if (json.containsKey("rating"))
					rating = Double.toString((double) json.get("rating"));

			} else
				nationality = "UNKNOWN";
			rls.setNationality(nationality).setRating(rating);
			// log(rls);
			return rls;
		}

		private Release getMovieInfoFromImdbManually(Release rls) {
			String imdbSearchUrl = String.format("http://www.imdb.com/find?q=%s&s=all", rls.getTitle().replaceAll("\\s", "+"));
			Document source = getSourceJSoup(imdbSearchUrl, 10);
			Elements movies = source.select("td.result_text a");
			String rating = "";
			String name = "";
			String count = "";
			String nationality = "";
			Document movieDoc = null;

			for (Element movie : movies) {
				String url = String.format("http://www.imdb.com%s", movie.attr("href"));
				movieDoc = getSourceJSoup(url, 10);
				try {
					name = movieDoc.getElementsByAttributeValue("itemprop", "name").first().text();
					rating = movieDoc.getElementsByAttributeValue("itemprop", "ratingValue").first().text();
					nationality = movieDoc.getElementsByAttributeValueMatching("href", "^/country/..\\?ref_=tt_dt_dt$").text();
					// count = movieDoc.getElementsByAttributeValue("itemprop",
					// "ratingCount").first().text();
				} catch (Exception e) {
				}
			}
			return rls.setNationality(nationality).setCorrectedTitle(name).setRating(rating);
		}

		private HashMap<String, Object> toJson(String str) {
			HashMap<String, Object> json = null;
			try {
				ObjectMapper mapper = new ObjectMapper();
				str = (String) str.subSequence(1, str.length() - 1);

				json = mapper.readValue(str, HashMap.class);
				// System.out.println("SUCCESS: " + str);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("FAIL: " + str);
			}
			return json;
		}

		private String getSourceHttpConnFromImdbApi(String str, int retries) {
			URL url;
			HttpURLConnection urlConn = null;
			String source = "";
			try {
				url = new URL(str.replaceAll("\\s", "%20"));
				urlConn = (HttpURLConnection) url.openConnection();
				urlConn.setRequestProperty("User-Agent", "Mozilla/5.0");
				urlConn.setConnectTimeout(15 * 1000);
				urlConn.setReadTimeout(15 * 1000);
				if (urlConn.getResponseMessage() == null)
					System.out.println("null response: " + url);

				try (BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()))) {
					String line;
					while ((line = in.readLine()) != null) {
						source += line;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				controller.log(String.format("NOT FOUND%s: %s ", (retries != -1) ? " # " + retries : "", str));
				try {
					Thread.sleep((long) (10000 * Math.random()));
				} catch (InterruptedException e1) {
				}
				getSourceHttpConnFromImdbApi(str, --retries);
			}
			return source;
		}

		private Document getSourceJSoup(String url, int retries) {
			if (retries == -1)
				retries = 10;
			Document source = null;
			try {
				source = Jsoup.connect(url).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(15 * 1000).get();
			} catch (IOException e) {
				e.printStackTrace();
				if (retries >= 0) {
					controller.log(String.format("NOT FOUND%s: %s ", (retries != -1) ? " # " + retries : "", url));
					try {
						Thread.sleep((long) (10000 * Math.random()));
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					getSourceJSoup(url, --retries);
				}
			}
			return source;
		}
	}

	private class ReleaseVisitor extends SimpleFileVisitor<Path> {

		private final Path fromPath;
		private final NationalityModel controller;

		public ReleaseVisitor(Path fromPath, NationalityModel nationalityModel) {
			this.fromPath = fromPath;
			this.controller = nationalityModel;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if (dir.getFileName().toString().startsWith("_") && !dir.equals(fromPath) || dir.getFileName().toString().contains(" "))
				return FileVisitResult.SKIP_SUBTREE;
			else if (dir.getFileName().toString().contains("-")) {
				Release rls = new Release(dir.getFileName().toString(), dir);
				releases.add(rls);
				new Thread(new NationalityParser(rls)).start();
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			// if (dir.equals(fromPath)) {
			// try {
			// boolean done = false;
			// while (!done && releases.size() > 0) {
			// for (Release rls : releases) {
			// done = true;
			// if (rls.getNationality() == null) {
			// done = false;
			// }
			// Thread.sleep(3);
			// }
			// }
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// addReleases(releases);
			// }
			return FileVisitResult.CONTINUE;
		}
	}

	public void addReleases(List<Release> rls) {
		((ReleaseTable) view.getReleaseTable()).addReleases(rls);
	}

	public void addRelease(Release rls) {
		((ReleaseTable) view.getReleaseTable()).addRelease(rls);
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

	@Override
	public void setView(SortsWindow view) {
		this.view = (NationalitySorter) view;
	}

	@Override
	public void run() {
		recursiveWalk(Paths.get(view.getFromField().getText()));
	}
}
