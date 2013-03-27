package sorts;

import java.nio.file.Path;

public class Release {
	private String name;
	private Path path;
	private String group;
	private String nationality;
	private String title = "";
	private String rating;
	private String correctedTitle;

	public Release(String name, Path file) {
		group = name.split("-")[name.split("-").length - 1];
		this.name = name;
		this.path = file;
		String[] rlsArr = name.split("[-\\._\\s]");
		for (String string : rlsArr) {
			if (string.toLowerCase().matches("(^\\d{4}$|^720p$|^1080p$|^dvdrip$|^ws$|^hdtv$)"))
				break;
			title += string + " ";
		}
	}

	public Release(String name, String rating, String nationality) {
		this.name = name;
		this.rating = rating;
		this.nationality = nationality;
	}

	public Release(String name) {
		this(name, null);
	}

	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public Path getPath() {
		return path;
	}

	public Release setNationality(String nationality) {
		this.nationality = nationality;
		return this;
	}

	public String getNationality() {
		return nationality;
	}

	public String getTitle() {
		return title;
	}

	public String getCorrectedTitle() {
		return correctedTitle;
	}

	public String getRating() {
		return rating;
	}

	public Release setRating(String rating) {
		this.rating = rating;
		return this;
	}

	public Release setCorrectedTitle(String correctedTitle) {
		this.correctedTitle = correctedTitle;
		return this;
	}

	@Override
	public String toString() {
		return String.format("Name: %s - Title: %s - Nationality: %s", name, title, nationality);
	}
}
