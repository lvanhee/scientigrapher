package scientigrapher.input;

import java.io.File;
import java.nio.file.Paths;

public class ProgramwideParameters {

	public static final File REFERENCE_BIB_FILE = new File("data/input_bibliography.bib");
	public static final File PDF_FOLDER = Paths.get("data/pdfs").toFile();
	public static final File GOOGLE_SCHOLAR_ENTRY_FOLDER = new File("data/cache/google_scholar_info_about_references");
	public static final File UNPAYWALL_FOUND_ENTRIES = new File("data/cache/found_unpaywall_entries.txt");
	public static final File UNPAYWALL_UNFOUND_ENTRIES = new File("data/cache/unfound_unpaywall_entries.txt");
	public static final File FAILED_TO_LOAD_PDF_BY_ALL_MEANS = new File("data/cache/failed_to_load_pdf_by_all_means.txt");
	public static final File GET_CHROME_DOWNLOAD_FOLDER = new File("C:\\Users\\loisv\\Downloads\\ChromeDownload");

}
