package scientigrapher.input;

public class Reference {
	
	private final String doi;
	private final String title;

	public Reference(String title2, String doi2) {
		this.doi = doi2;
		this.title = title2;
	}

	public boolean hasDoi() {
		return doi != null;
	}

	public String getDoi() {
		return doi;
	}

	public String getTitle() {
		return title;
	}

	public static Reference newInstance(String title2, String doi2) {
		return new Reference(title2,doi2);
	}

}
