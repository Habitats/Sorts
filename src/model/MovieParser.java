package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sorts.Controller;
import sorts.Release;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MovieParser implements Runnable {

	private final Release rls;
	private final Controller controller;

	public MovieParser(Release rls, Controller controller) {
		this.rls = rls;
		this.controller = controller;
	}

	@Override
	public void run() {
		if (getMovieInfoFromApi(rls) == null)
			getMovieInfoFromImdbManually(rls);
		controller.addRelease(rls);
	}

	private Release getMovieInfoFromApi(Release rls) {
		String url = String.format("http://imdbapi.org/?title=%s&type=json&plot=simple&episode=1&limit=1&yg=0&mt=none&lang=en-US&offset=&aka=simple&release=simple&business=0&tech=0", rls.getTitle().replaceAll("\\s", "%20"));
		// String raw = getSourceHttpConnFromImdbApi(url, 10);
		String raw = getSourceJSoup(url, 10).text();
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
			else
				nationality = "UNKNOWN";

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
		try {
			String directUrl = "http://www.imdb.com" + source.select("td.result_text a").first().attr("href");
			Document movieDoc = getSourceJSoup(directUrl, 10);
			name = movieDoc.getElementsByAttributeValue("itemprop", "name").first().text();
			rating = movieDoc.getElementsByAttributeValue("itemprop", "ratingValue").first().text();
			nationality = movieDoc.getElementsByAttributeValueMatching("href", "^/country/..\\?ref_=tt_dt_dt$").text();
			// count = movieDoc.getElementsByAttributeValue("itemprop",
			// "ratingCount").first().text();
		} catch (Exception e) {
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
