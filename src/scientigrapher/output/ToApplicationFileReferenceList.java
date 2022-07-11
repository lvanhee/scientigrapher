package scientigrapher.output;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import cachingutils.advanced.failable.SuccessfulOutcome;
import cachingutils.advanced.localdatabase.AutofillLocalDatabase;
import cachingutils.advanced.localdatabase.LocalDatabaseImpl;
import cachingutils.impl.SplittedFileBasedCache;
import scientigrapher.datagathering.ScopusSearcher;
import scientigrapher.input.TextUtils;
import scientigrapher.input.references.Reference;
import webscrapping.RobotBasedPageReader;
import webscrapping.robot.RobotUtils;

public class ToApplicationFileReferenceList {

	private enum Mode{JOURNALS_FFT, BOOKS_FFT, CONFERENCE_FFT,
		ALL_JOURNALS_LATEX_ITEMIZE, ALL_INTERNATIONAL_BOOK_CHAPTERS_LATEX_ITEMIZE, ALL_CONFERENCES_LATEX_ITEMIZE}
	
	

	private static final Mode CURRENT_MODE = Mode.ALL_CONFERENCES_LATEX_ITEMIZE;

	public static void main(String[] args)
	{
		/*Set<Reference> scopusReferences = ScopusSearcher.translateIntoReference(ScopusSearcher.getAllScopusIdReferencesFrom("53985383600"));

		System.out.println(scopusReferences);*/


		Set<Reference> allReferences = Reference.referencesFromBibFile(new File(args[0]));

		Set<Reference> selectedReferences = 
				allReferences.stream().filter(x->isSelectedReference(x)).collect(Collectors.toSet());
		Set<Reference> sortedReferences = new TreeSet<>(getReferenceSortingOrder());
		sortedReferences.addAll(selectedReferences);

		exportStandardizedOutput(sortedReferences);
	}

	private static void exportStandardizedOutput(Set<Reference> sortedReferences) {
		//pre-compute
		sortedReferences.stream().forEach(x->{x.getBestLinkToPaper(); getStandardizedOutputFor(x);});

		int index = 1;

		if(CURRENT_MODE==Mode.ALL_JOURNALS_LATEX_ITEMIZE||CURRENT_MODE==Mode.ALL_INTERNATIONAL_BOOK_CHAPTERS_LATEX_ITEMIZE
				||CURRENT_MODE==Mode.ALL_CONFERENCES_LATEX_ITEMIZE)
		{
			String res = "\\begin{itemize}";

			res+=sortedReferences.stream().map(x->"\\item "+getStandardizedOutputFor(x)).reduce("", (x,y)->x+"\n"+y);

			res+="\n\\end{itemize}";
			System.out.println(res);
			return;
		}
		boolean previousStartedByBold = false;


		if(CURRENT_MODE==Mode.JOURNALS_FFT|| CURRENT_MODE==Mode.BOOKS_FFT || CURRENT_MODE==Mode.CONFERENCE_FFT)
		{
			System.out.println("Start typing");
			/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/


			for(Reference r: sortedReferences)
			{
				String linkToBestPaper ="";
				if(r.getBestLinkToPaper()!=null)
					linkToBestPaper = r.getBestLinkToPaper().toString();
				String toPrint = getStandardizedOutputFor(r);


				String start = toPrint.substring(0,toPrint.indexOf("Vanhée, L."));
				toPrint = toPrint.substring(start.length());


				String boldVanhee = toPrint.substring(0, new String("Vanhée, L.").length());
				toPrint = toPrint.substring(boldVanhee.length());

				String otherAuthors = toPrint.substring(0, toPrint.indexOf(";")+1);
				toPrint = toPrint.substring(otherAuthors.length());

				String title = toPrint.substring(0, toPrint.indexOf(";"));
				toPrint = toPrint.substring(title.length());



				try {
					Thread.sleep(500);


					if(index==1) {
						RobotUtils.typeString("1.");
						RobotUtils.typeString(" ");

					}
					index++;

					if(previousStartedByBold)
					{
						RobotUtils.setBold();
						/*	System.out.println("Previous started by bold and current start is empty");
					Thread.sleep(5000);*/

						Thread.sleep(200);
					}


					//word set the start to bold by default if the previous line of the itemize is bold

					RobotUtils.typeString(start);

					Thread.sleep(200);

					//	if(startByBold&&start.isEmpty())
					RobotUtils.setBold();

					Thread.sleep(100);
					RobotUtils.typeString(boldVanhee);

					Thread.sleep(100);

					RobotUtils.setBold();

					Thread.sleep(100);

					RobotUtils.typeString(otherAuthors);

					Thread.sleep(100);
					RobotUtils.setItalics();
					Thread.sleep(100);
					RobotUtils.typeString(title);
					Thread.sleep(100);
					RobotUtils.setItalics();
					Thread.sleep(100);
					RobotUtils.typeString(toPrint+" (link)");
					Thread.sleep(100);
					if(!linkToBestPaper.isBlank())
					{
						RobotUtils.setShiftOn();
						Thread.sleep(100);
						RobotUtils.pressLeft(6);
						Thread.sleep(100);
						RobotUtils.setShiftOff();
						Thread.sleep(100);
						RobotUtils.control("k");
						Thread.sleep(700);
						RobotUtils.pasteText(linkToBestPaper);
						Thread.sleep(100);
						RobotUtils.enter();
					}

					RobotUtils.enter();

					System.out.println(toPrint);

					previousStartedByBold = start.isEmpty();

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return;
		}
		throw new Error();
	}

	private static String getStandardizedOutputFor(Reference r) {
		String res = "";
		if(CURRENT_MODE==Mode.JOURNALS_FFT)
		{
			res += 
					r.getAuthors()+"; "+r.getTitle()+"; "+r.getJournal()+", Volume: "
							+r.getVolume()+"";
			if(r.hasPages())res+=", Pages: "+r.getPages();
			res+=", ("+r.getYear()+"); Citations: "+r.getNumberOfCitationsFromGoogleScholar()
			+", ISI: "+r.getISI()+", Norwegian list: "+r.getLevelInNorwegianRegister();

			return res;
		}
		if(CURRENT_MODE==Mode.BOOKS_FFT)
		{
			res+=
					r.getAuthors()+"; "+r.getTitle()+" ("+r.getYear()+")"+ "; In "+r.getBookTitle()+", Pages: "+r.getPages()+
					", Publisher: "+r.getPublisher()+
					"; Citations: "+r.getNumberOfCitationsFromGoogleScholar();
			return res;
		}

		if(CURRENT_MODE==Mode.CONFERENCE_FFT)
		{
			res+=
					r.getAuthors()+"; "+r.getTitle()+" ("+r.getYear()+")"+ "; In "+r.getGenericVenue()+", Pages: "+r.getPages()+
					", Publisher: "+r.getPublisher()+
					"; Citations: "+r.getNumberOfCitationsFromGoogleScholar()+", CORE: "+r.getCoreRanking()+", ERA: "+r.getEraRanking()
					+", Qualis: "+r.getQualisRanking();
			return res;
		}

		if(CURRENT_MODE==Mode.ALL_JOURNALS_LATEX_ITEMIZE)
		{
			res+=r.getAuthors()+" ("+r.getYear()+"). "+r.getTitle()+". \\textit{"+r.getGenericVenue()+"}; "+r.getVolume()+", "+r.getPages()
			+"; \\href{"+r.getUrl()+"}{URL}; "+getImpactMetricsInText(r,CURRENT_MODE)+"; "+getCitationsInText(r,CURRENT_MODE);
			return res;
		}
		
		if(CURRENT_MODE==Mode.ALL_CONFERENCES_LATEX_ITEMIZE)
		{
			res+=
					r.getAuthors()+"; ("+r.getYear()+") "+r.getTitle()+ ". In \\textit{"+r.getGenericVenue()+"}; pp. "+r.getPages();
			if(r.hasPublisher())
				res+="; Publisher: "+r.getPublisher();
			
			res+="; \\href{"+r.getUrl()+"}{URL}"+
					"; Citations: "+r.getNumberOfCitationsFromGoogleScholar();
			return res;
		}
		throw new Error();
	}

	private static String getCitationsInText(Reference r, Mode currentMode) {
		return "Citations: "+r.getNumberOfCitationsFromGoogleScholar();
	}

	private static String getImpactMetricsInText(Reference r, Mode currentMode) {
		if(CURRENT_MODE==Mode.ALL_JOURNALS_LATEX_ITEMIZE)
		{
			return "Impact factor: "+r.getISI();
		}
		throw new Error();
	}

	private static Comparator<Reference> getReferenceSortingOrder() {
		return (x,y)->{
			if(x.getYear()!=y.getYear())
				return -Integer.compare(x.getYear(), y.getYear());
			return x.toString().compareTo(y.toString());
		};
	}

	private static boolean isSelectedReference(Reference x) {
		if(CURRENT_MODE==Mode.ALL_JOURNALS_LATEX_ITEMIZE)
			return x.isJournal();
		
		if(CURRENT_MODE==Mode.ALL_INTERNATIONAL_BOOK_CHAPTERS_LATEX_ITEMIZE)
			return x.isBookChapter();
		
		if(CURRENT_MODE==Mode.ALL_CONFERENCES_LATEX_ITEMIZE)
			return x.isConferencePaper();
		
		if(CURRENT_MODE==Mode.JOURNALS_FFT)
			return x.isJournal()&&x.getYear()>=2015;
			else if(CURRENT_MODE==Mode.BOOKS_FFT)
				return x.isBookChapter()&&x.getYear()>=2015;
				else if(CURRENT_MODE==Mode.CONFERENCE_FFT)
					return x.isConferencePaper()&&x.getYear()>=2015;
					throw new Error();
	}
}
