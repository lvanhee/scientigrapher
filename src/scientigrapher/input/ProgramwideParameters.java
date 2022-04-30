package scientigrapher.input;

import java.io.File;
import java.nio.file.Paths;

public class ProgramwideParameters {

	private static final File DATABASE_LOCATION = new File("../databases/scientigrapher/agent_decision_making");
	public static final File REFERENCE_BIB_FILE = new File(DATABASE_LOCATION.getAbsolutePath()+"/bibliography.bib");
	public static final File PDF_FOLDER = Paths.get(DATABASE_LOCATION.getAbsolutePath()+"/pdfs").toFile();
	public static final File GOOGLE_SCHOLAR_ENTRY_FOLDER = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/google_scholar_search_of_references");
	public static final File UNPAYWALL_FOUND_ENTRIES = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/found_unpaywall_entries.txt");
	public static final File UNPAYWALL_UNFOUND_ENTRIES = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/unfound_unpaywall_entries.txt");
	public static final File FAILED_TO_LOAD_PDF_BY_ALL_MEANS = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/failed_to_load_pdf_by_all_means.txt");
	public static final File CHROME_DOWNLOAD_FOLDER = new File("C:\\Users\\loisv\\Downloads");
	public static final File REFERENCE_TO_WORKING_PDF_URL_DATABASE = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/reference_to_working_pdf_links.txt");
	public static final File WORKING_PDF_URL_DATABASE = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/working_links_database.txt");
	public static final File TXT_OF_PDF_CACHE = new File(DATABASE_LOCATION.getAbsolutePath()+"/cache/txt_of_pdf/");

}
