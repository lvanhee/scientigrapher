package scientigrapher.model.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import scientigrapher.input.references.Reference;

public class AsjcInterdisciplinaryScore {
	
	public static Set<String> allIntermediateCodes =
			Arrays.asList(
					"AGRI",
					"ARTS",
					"BIOC",
					"BUSI",
					 "CENG",
					 "CHEM",
					"COMP",
					 "DECI",
					 "EART",
					"ECON",
					 "ENER",
				 "ENGI",
					 "ENVI",
					"IMMU",
					"MATE",
				 "MATH",
					 "MEDI",
					"NEUR",
					"NURS",
					"PHAR",
					 "PHYS",
					 "PSYC",
					"SOCI",
					 "VETE",
					 "DENT",
					 "HEAL"
					).stream().collect(Collectors.toSet());

	private final Map<String, Long>asjcCodesOccurrenceMap;
	public AsjcInterdisciplinaryScore(Map<String, Long> asjcCodesFrequencyMap) {
		this.asjcCodesOccurrenceMap = asjcCodesFrequencyMap;		
	}

	public static AsjcInterdisciplinaryScore newInstance(Map<String, Long> asjcCodesFrequencyMap) {
		return new AsjcInterdisciplinaryScore(asjcCodesFrequencyMap);
	}




	private static String getAsjcCodeFromName(String name) {
		switch(name) {
		case "General" : return "1000";
		case "Agricultural and Biological Sciences (all)" : return "1100";
		case "Agricultural and Biological Sciences (miscellaneous)" : return "1101";
		case "Agronomy and Crop Science" : return "1102";
		case "Animal Science and Zoology" : return "1103";
		case "Aquatic Science" : return "1104";
		case "Ecology, Evolution, Behavior and Systematics" : return "1105";
		case "Food Science" : return "1106";
		case "Forestry" : return "1107";
		case "Horticulture" : return "1108";
		case "Insect Science" : return "1109";
		case "Plant Science" : return "1110";
		case "Soil Science" : return "1111";
		case "Arts and Humanities(all)" : return "1200";
		case "Arts and Humanities (miscellaneous)" : return "1201";
		case "History" : return "1202";
		case "Language and Linguistics" : return "1203";
		case "Archaeology" : return "1204";
		case "Classics" : return "1205";
		case "Conservation" : return "1206";
		case "History and Philosophy of Science" : return "1207";
		case "Literature and Literary Theory" : return "1208";
		case "Museology" : return "1209";
		case "Music" : return "1210";
		case "Philosophy" : return "1211";
		case "Religious studies" : return "1212";
		case "Visual Arts and Performing Arts" : return "1213";
		case "Biochemistry, Genetics and Molecular Biology(all)" : return "1300";
		case "Biochemistry, Genetics and Molecular Biology (miscellaneous)" : return "1301";
		case "Ageing" : return "1302";
		case "Biochemistry" : return "1303";
		case "Biophysics" : return "1304";
		case "Biotechnology" : return "1305";
		case "Cancer Research" : return "1306";
		case "Cell Biology" : return "1307";
		case "Clinical Biochemistry" : return "1308";
		case "Developmental Biology" : return "1309";
		case "Endocrinology" : return "1310";
		case "Genetics" : return "1311";
		case "Molecular Biology" : return "1312";
		case "Molecular Medicine" : return "1313";
		case "Physiology" : return "1314";
		case "Structural Biology" : return "1315";
		case "Business, Management and Accounting(all)                  " : return "1400";
		case "Business, Management and Accounting (miscellaneous)" : return "1401";
		case "Accounting" : return "1402";
		case "Business and International Management" : return "1403";
		case "Management Information Systems" : return "1404";
		case "Management of Technology and Innovation" : return "1405";
		case "Marketing" : return "1406";
		case "Organizational Behavior and Human Resource Management" : return "1407";
		case "Strategy and Management" : return "1408";
		case "Tourism, Leisure and Hospitality Management" : return "1409";
		case "Industrial relations" : return "1410";
		case "Chemical Engineering(all)" : return "1500";
		case "Chemical Engineering (miscellaneous)" : return "1501";
		case "Bioengineering" : return "1502";
		case "Catalysis" : return "1503";
		case "Chemical Health and Safety" : return "1504";
		case "Colloid and Surface Chemistry" : return "1505";
		case "Filtration and Separation" : return "1506";
		case "Fluid Flow and Transfer Processes" : return "1507";
		case "Process Chemistry and Technology" : return "1508";
		case "Chemistry(all)" : return "1600";
		case "Chemistry (miscellaneous)" : return "1601";
		case "Analytical Chemistry" : return "1602";
		case "Electrochemistry" : return "1603";
		case "Inorganic Chemistry" : return "1604";
		case "Organic Chemistry" : return "1605";
		case "Physical and Theoretical Chemistry" : return "1606";
		case "Spectroscopy" : return "1607";
		case "Computer Science (all)" : return "1700";
		case "Computer Science (miscellaneous)" : return "1701";
		case "Artificial Intelligence" : return "1702";
		case "Computational Theory and Mathematics" : return "1703";
		case "Computer Graphics and Computer-Aided Design" : return "1704";
		case "Computer Networks and Communications" : return "1705";
		case "Computer Science Applications" : return "1706";
		case "Computer Vision and Pattern Recognition" : return "1707";
		case "Hardware and Architecture" : return "1708";
		case "Human-Computer Interaction" : return "1709";
		case "Information Systems" : return "1710";
		case "Signal Processing" : return "1711";
		case "Software" : return "1712";
		case "Decision Sciences(all)" : return "1800";
		case "Decision Sciences (miscellaneous)" : return "1801";
		case "Information Systems and Management" : return "1802";
		case "Management Science and Operations Research" : return "1803";
		case "Statistics, Probability and Uncertainty" : return "1804";
		case "Earth and Planetary Sciences(all)" : return "1900";
		case "Earth and Planetary Sciences (miscellaneous)" : return "1901";
		case "Atmospheric Science" : return "1902";
		case "Computers in Earth Sciences" : return "1903";
		case "Earth-Surface Processes" : return "1904";
		case "Economic Geology" : return "1905";
		case "Geochemistry and Petrology" : return "1906";
		case "Geology" : return "1907";
		case "Geophysics" : return "1908";
		case "Geotechnical Engineering and Engineering Geology" : return "1909";
		case "Oceanography" : return "1910";
		case "Palaeontology" : return "1911";
		case "Space and Planetary Science" : return "1912";
		case "Stratigraphy" : return "1913";
		case "Economics, Econometrics and Finance(all)" : return "2000";
		case "Economics, Econometrics and Finance (miscellaneous)" : return "2001";
		case "Economics and Econometrics" : return "2002";
		case "Finance" : return "2003";
		case "Energy(all)" : return "2100";
		case "Energy (miscellaneous)" : return "2101";
		case "Energy Engineering and Power Technology" : return "2102";
		case "Fuel Technology" : return "2103";
		case "Nuclear Energy and Engineering" : return "2104";
		case "Renewable Energy, Sustainability and the Environment" : return "2105";
		case "Engineering(all)" : return "2200";
		case "Engineering (miscellaneous)" : return "2201";
		case "Aerospace Engineering" : return "2202";
		case "Automotive Engineering" : return "2203";
		case "Biomedical Engineering" : return "2204";
		case "Civil and Structural Engineering" : return "2205";
		case "Computational Mechanics" : return "2206";
		case "Control and Systems Engineering" : return "2207";
		case "Electrical and Electronic Engineering" : return "2208";
		case "Industrial and Manufacturing Engineering" : return "2209";
		case "Mechanical Engineering" : return "2210";
		case "Mechanics of Materials" : return "2211";
		case "Ocean Engineering" : return "2212";
		case "Safety, Risk, Reliability and Quality" : return "2213";
		case "Media Technology" : return "2214";
		case "Building and Construction" : return "2215";
		case "Architecture" : return "2216";
		case "Environmental Science(all)" : return "2300";
		case "Environmental Science (miscellaneous)" : return "2301";
		case "Ecological Modelling" : return "2302";
		case "Ecology" : return "2303";
		case "Environmental Chemistry" : return "2304";
		case "Environmental Engineering" : return "2305";
		case "Global and Planetary Change" : return "2306";
		case "Health, Toxicology and Mutagenesis" : return "2307";
		case "Management, Monitoring, Policy and Law" : return "2308";
		case "Nature and Landscape Conservation" : return "2309";
		case "Pollution" : return "2310";
		case "Waste Management and Disposal" : return "2311";
		case "Water Science and Technology" : return "2312";
		case "Immunology and Microbiology(all)" : return "2400";
		case "Immunology and Microbiology (miscellaneous)" : return "2401";
		case "Applied Microbiology and Biotechnology" : return "2402";
		case "Immunology" : return "2403";
		case "Microbiology" : return "2404";
		case "Parasitology" : return "2405";
		case "Virology" : return "2406";
		case "Materials Science(all)" : return "2500";
		case "Materials Science (miscellaneous)" : return "2501";
		case "Biomaterials" : return "2502";
		case "Ceramics and Composites" : return "2503";
		case "Electronic, Optical and Magnetic Materials" : return "2504";
		case "Materials Chemistry" : return "2505";
		case "Metals and Alloys" : return "2506";
		case "Polymers and Plastics" : return "2507";
		case "Surfaces, Coatings and Films" : return "2508";
		case "Mathematics (all)" : return "2600";
		case "Mathematics (miscellaneous)" : return "2601";
		case "Algebra and Number Theory" : return "2602";
		case "Analysis" : return "2603";
		case "Applied Mathematics" : return "2604";
		case "Computational Mathematics" : return "2605";
		case "Control and Optimization" : return "2606";
		case "Discrete Mathematics and Combinatorics" : return "2607";
		case "Geometry and Topology" : return "2608";
		case "Logic" : return "2609";
		case "Mathematical Physics" : return "2610";
		case "Modelling and Simulation" : return "2611";
		case "Numerical Analysis" : return "2612";
		case "Statistics and Probability" : return "2613";
		case "Theoretical Computer Science" : return "2614";
		case "Medicine (all)" : return "2700";
		case "Medicine (miscellaneous)" : return "2701";
		case "Anatomy" : return "2702";
		case "Anesthesiology and Pain Medicine" : return "2703";
		case "Biochemistry, medical" : return "2704";
		case "Cardiology and Cardiovascular Medicine" : return "2705";
		case "Critical Care and Intensive Care Medicine" : return "2706";
		case "Complementary and alternative medicine" : return "2707";
		case "Dermatology" : return "2708";
		case "Drug guides" : return "2709";
		case "Embryology" : return "2710";
		case "Emergency Medicine" : return "2711";
		case "Endocrinology, Diabetes and Metabolism" : return "2712";
		case "Epidemiology" : return "2713";
		case "Family Practice" : return "2714";
		case "Gastroenterology" : return "2715";
		case "Genetics(clinical)" : return "2716";
		case "Geriatrics and Gerontology" : return "2717";
		case "Health Informatics" : return "2718";
		case "Health Policy" : return "2719";
		case "Hematology" : return "2720";
		case "Hepatology" : return "2721";
		case "Histology" : return "2722";
		case "Immunology and Allergy" : return "2723";
		case "Internal Medicine" : return "2724";
		case "Infectious Diseases" : return "2725";
		case "Microbiology (medical)" : return "2726";
		case "Nephrology" : return "2727";
		case "Clinical Neurology" : return "2728";
		case "Obstetrics and Gynaecology" : return "2729";
		case "Oncology" : return "2730";
		case "Ophthalmology" : return "2731";
		case "Orthopedics and Sports Medicine" : return "2732";
		case "Otorhinolaryngology" : return "2733";
		case "Pathology and Forensic Medicine" : return "2734";
		case "Pediatrics, Perinatology, and Child Health" : return "2735";
		case "Pharmacology (medical)" : return "2736";
		case "Physiology (medical)" : return "2737";
		case "Psychiatry and Mental health" : return "2738";
		case "Public Health, Environmental and Occupational Health" : return "2739";
		case "Pulmonary and Respiratory Medicine" : return "2740";
		case "Radiology Nuclear Medicine and imaging" : return "2741";
		case "Rehabilitation" : return "2742";
		case "Reproductive Medicine" : return "2743";
		case "Reviews and References, Medical" : return "2744";
		case "Rheumatology" : return "2745";
		case "Surgery" : return "2746";
		case "Transplantation" : return "2747";
		case "Urology" : return "2748";
		case "Neuroscience(all)" : return "2800";
		case "Neuroscience (miscellaneous)" : return "2801";
		case "Behavioral Neuroscience" : return "2802";
		case "Biological Psychiatry" : return "2803";
		case "Cellular and Molecular Neuroscience" : return "2804";
		case "Cognitive Neuroscience" : return "2805";
		case "Developmental Neuroscience" : return "2806";
		case "Endocrine and Autonomic Systems" : return "2807";
		case "Neurology" : return "2808";
		case "Sensory Systems" : return "2809";
		case "Nursing(all)                                                        " : return "2900";
		case "Nursing (miscellaneous)" : return "2901";
		case "Advanced and Specialised Nursing" : return "2902";
		case "Assessment and Diagnosis" : return "2903";
		case "Care Planning" : return "2904";
		case "Community and Home Care" : return "2905";
		case "Critical Care" : return "2906";
		case "Emergency" : return "2907";
		case "Fundamentals and skills" : return "2908";
		case "Gerontology" : return "2909";
		case "Issues, ethics and legal aspects" : return "2910";
		case "Leadership and Management" : return "2911";
		case "LPN and LVN" : return "2912";
		case "Maternity and Midwifery" : return "2913";
		case "Medical–Surgical" : return "2914";
		case "Nurse Assisting" : return "2915";
		case "Nutrition and Dietetics" : return "2916";
		case "Oncology(nursing)" : return "2917";
		case "Pathophysiology" : return "2918";
		case "Pediatrics" : return "2919";
		case "Pharmacology (nursing)" : return "2920";
		case "Phychiatric Mental Health" : return "2921";
		case "Research and Theory" : return "2922";
		case "Review and Exam Preparation" : return "2923";
		case "Pharmacology, Toxicology and Pharmaceutics(all)      " : return "3000";
		case "Pharmacology, Toxicology and Pharmaceutics (miscellaneous)" : return "3001";
		case "Drug Discovery" : return "3002";
		case "Pharmaceutical Science" : return "3003";
		case "Pharmacology" : return "3004";
		case "Toxicology" : return "3005";
		case "Physics and Astronomy(all)" : return "3100";
		case "Physics and Astronomy (miscellaneous)" : return "3101";
		case "Acoustics and Ultrasonics" : return "3102";
		case "Astronomy and Astrophysics" : return "3103";
		case "Condensed Matter Physics" : return "3104";
		case "Instrumentation" : return "3105";
		case "Nuclear and High Energy Physics" : return "3106";
		case "Atomic and Molecular Physics, and Optics" : return "3107";
		case "Radiation" : return "3108";
		case "Statistical and Nonlinear Physics" : return "3109";
		case "Surfaces and Interfaces" : return "3110";
		case "Psychology(all)" : return "3200";
		case "Psychology (miscellaneous)" : return "3201";
		case "Applied Psychology" : return "3202";
		case "Clinical Psychology" : return "3203";
		case "Developmental and Educational Psychology" : return "3204";
		case "Experimental and Cognitive Psychology" : return "3205";
		case "Neuropsychology and Physiological Psychology" : return "3206";
		case "Social Psychology" : return "3207";
		case "Social Sciences(all)" : return "3300";
		case "Social Sciences (miscellaneous)" : return "3301";
		case "Development" : return "3303";
		case "Education" : return "3304";
		case "Geography, Planning and Development" : return "3305";
		case "Health(social science)" : return "3306";
		case "Human Factors and Ergonomics" : return "3307";
		case "Law" : return "3308";
		case "Library and Information Sciences" : return "3309";
		case "Linguistics and Language" : return "3310";
		case "Safety Research" : return "3311";
		case "Sociology and Political Science" : return "3312";
		case "Transportation" : return "3313";
		case "Anthropology" : return "3314";
		case "Communication" : return "3315";
		case "Cultural Studies" : return "3316";
		case "Demography" : return "3317";
		case "Gender Studies" : return "3318";
		case "Life-span and Life-course Studies" : return "3319";
		case "Political Science and International Relations" : return "3320";
		case "Public Administration" : return "3321";
		case "Urban Studies" : return "3322";
		case "veterinary(all)" : return "3400";
		case "veterinary (miscalleneous)" : return "3401";
		case "Equine" : return "3402";
		case "Food Animals" : return "3403";
		case "Small Animals" : return "3404";
		case "Dentistry(all)" : return "3500";
		case "Dentistry (miscellaneous)" : return "3501";
		case "Dental Assisting" : return "3502";
		case "Dental Hygiene" : return "3503";
		case "Oral Surgery" : return "3504";
		case "Orthodontics" : return "3505";
		case "Periodontics" : return "3506";
		case "Health Professions(all)" : return "3600";
		case "Health Professions (miscellaneous)" : return "3601";
		case "Chiropractics" : return "3602";
		case "Complementary and Manual Therapy" : return "3603";
		case "Emergency Medical Services" : return "3604";
		case "Health Information Management" : return "3605";
		case "Medical Assisting and Transcription" : return "3606";
		case "Medical Laboratory Technology" : return "3607";
		case "Medical Terminology" : return "3608";
		case "Occupational Therapy" : return "3609";
		case "Optometry" : return "3610";
		case "Pharmacy" : return "3611";
		case "Physical Therapy, Sports Therapy and Rehabilitation" : return "3612";
		case "Podiatry" : return "3613";
		case "Radiological and Ultrasound Technology" : return "3614";
		case "Respiratory Care" : return "3615";
		case "Speech and Hearing" : return "3616";
		default:throw new Error();
		}
	}

	private static String getExtendedSubjectAreaCode(String s) {
		switch(s) {
		case "1000": return "General";
		case "1100": return "Agricultural and Biological Sciences (all)";
		case "1101": return "Agricultural and Biological Sciences (miscellaneous)";
		case "1102": return "Agronomy and Crop Science";
		case "1103": return "Animal Science and Zoology";
		case "1104": return "Aquatic Science";
		case "1105": return "Ecology, Evolution, Behavior and Systematics";
		case "1106": return "Food Science";
		case "1107": return "Forestry";
		case "1108": return "Horticulture";
		case "1109": return "Insect Science";
		case "1110": return "Plant Science";
		case "1111": return "Soil Science";
		case "1200": return "Arts and Humanities(all)";
		case "1201": return "Arts and Humanities (miscellaneous)";
		case "1202": return "History";
		case "1203": return "Language and Linguistics";
		case "1204": return "Archaeology";
		case "1205": return "Classics";
		case "1206": return "Conservation";
		case "1207": return "History and Philosophy of Science";
		case "1208": return "Literature and Literary Theory";
		case "1209": return "Museology";
		case "1210": return "Music";
		case "1211": return "Philosophy";
		case "1212": return "Religious studies";
		case "1213": return "Visual Arts and Performing Arts";
		case "1300": return "Biochemistry, Genetics and Molecular Biology(all)";
		case "1301": return "Biochemistry, Genetics and Molecular Biology (miscellaneous)";
		case "1302": return "Ageing";
		case "1303": return "Biochemistry";
		case "1304": return "Biophysics";
		case "1305": return "Biotechnology";
		case "1306": return "Cancer Research";
		case "1307": return "Cell Biology";
		case "1308": return "Clinical Biochemistry";
		case "1309": return "Developmental Biology";
		case "1310": return "Endocrinology";
		case "1311": return "Genetics";
		case "1312": return "Molecular Biology";
		case "1313": return "Molecular Medicine";
		case "1314": return "Physiology";
		case "1315": return "Structural Biology";
		case "1400": return "Business, Management and Accounting(all)                    ";
		case "1401": return "Business, Management and Accounting (miscellaneous)";
		case "1402": return "Accounting";
		case "1403": return "Business and International Management";
		case "1404": return "Management Information Systems";
		case "1405": return "Management of Technology and Innovation";
		case "1406": return "Marketing";
		case "1407": return "Organizational Behavior and Human Resource Management";
		case "1408": return "Strategy and Management";
		case "1409": return "Tourism, Leisure and Hospitality Management";
		case "1410": return "Industrial relations";
		case "1500": return "Chemical Engineering(all)";
		case "1501": return "Chemical Engineering (miscellaneous)";
		case "1502": return "Bioengineering";
		case "1503": return "Catalysis";
		case "1504": return "Chemical Health and Safety";
		case "1505": return "Colloid and Surface Chemistry";
		case "1506": return "Filtration and Separation";
		case "1507": return "Fluid Flow and Transfer Processes";
		case "1508": return "Process Chemistry and Technology";
		case "1600": return "Chemistry(all)";
		case "1601": return "Chemistry (miscellaneous)";
		case "1602": return "Analytical Chemistry";
		case "1603": return "Electrochemistry";
		case "1604": return "Inorganic Chemistry";
		case "1605": return "Organic Chemistry";
		case "1606": return "Physical and Theoretical Chemistry";
		case "1607": return "Spectroscopy";
		case "1700": return "Computer Science (all)";
		case "1701": return "Computer Science (miscellaneous)";
		case "1702": return "Artificial Intelligence";
		case "1703": return "Computational Theory and Mathematics";
		case "1704": return "Computer Graphics and Computer-Aided Design";
		case "1705": return "Computer Networks and Communications";
		case "1706": return "Computer Science Applications";
		case "1707": return "Computer Vision and Pattern Recognition";
		case "1708": return "Hardware and Architecture";
		case "1709": return "Human-Computer Interaction";
		case "1710": return "Information Systems";
		case "1711": return "Signal Processing";
		case "1712": return "Software";
		case "1800": return "Decision Sciences(all)";
		case "1801": return "Decision Sciences (miscellaneous)";
		case "1802": return "Information Systems and Management";
		case "1803": return "Management Science and Operations Research";
		case "1804": return "Statistics, Probability and Uncertainty";
		case "1900": return "Earth and Planetary Sciences(all)";
		case "1901": return "Earth and Planetary Sciences (miscellaneous)";
		case "1902": return "Atmospheric Science";
		case "1903": return "Computers in Earth Sciences";
		case "1904": return "Earth-Surface Processes";
		case "1905": return "Economic Geology";
		case "1906": return "Geochemistry and Petrology";
		case "1907": return "Geology";
		case "1908": return "Geophysics";
		case "1909": return "Geotechnical Engineering and Engineering Geology";
		case "1910": return "Oceanography";
		case "1911": return "Palaeontology";
		case "1912": return "Space and Planetary Science";
		case "1913": return "Stratigraphy";
		case "2000": return "Economics, Econometrics and Finance(all)";
		case "2001": return "Economics, Econometrics and Finance (miscellaneous)";
		case "2002": return "Economics and Econometrics";
		case "2003": return "Finance";
		case "2100": return "Energy(all)";
		case "2101": return "Energy (miscellaneous)";
		case "2102": return "Energy Engineering and Power Technology";
		case "2103": return "Fuel Technology";
		case "2104": return "Nuclear Energy and Engineering";
		case "2105": return "Renewable Energy, Sustainability and the Environment";
		case "2200": return "Engineering(all)";
		case "2201": return "Engineering (miscellaneous)";
		case "2202": return "Aerospace Engineering";
		case "2203": return "Automotive Engineering";
		case "2204": return "Biomedical Engineering";
		case "2205": return "Civil and Structural Engineering";
		case "2206": return "Computational Mechanics";
		case "2207": return "Control and Systems Engineering";
		case "2208": return "Electrical and Electronic Engineering";
		case "2209": return "Industrial and Manufacturing Engineering";
		case "2210": return "Mechanical Engineering";
		case "2211": return "Mechanics of Materials";
		case "2212": return "Ocean Engineering";
		case "2213": return "Safety, Risk, Reliability and Quality";
		case "2214": return "Media Technology";
		case "2215": return "Building and Construction";
		case "2216": return "Architecture ";
		case "2300": return "Environmental Science(all)";
		case "2301": return "Environmental Science (miscellaneous)";
		case "2302": return "Ecological Modelling";
		case "2303": return "Ecology";
		case "2304": return "Environmental Chemistry";
		case "2305": return "Environmental Engineering";
		case "2306": return "Global and Planetary Change";
		case "2307": return "Health, Toxicology and Mutagenesis";
		case "2308": return "Management, Monitoring, Policy and Law";
		case "2309": return "Nature and Landscape Conservation";
		case "2310": return "Pollution";
		case "2311": return "Waste Management and Disposal";
		case "2312": return "Water Science and Technology";
		case "2400": return "Immunology and Microbiology(all)";
		case "2401": return "Immunology and Microbiology (miscellaneous) ";
		case "2402": return "Applied Microbiology and Biotechnology";
		case "2403": return "Immunology";
		case "2404": return "Microbiology";
		case "2405": return "Parasitology";
		case "2406": return "Virology";
		case "2500": return "Materials Science(all)";
		case "2501": return "Materials Science (miscellaneous)";
		case "2502": return "Biomaterials";
		case "2503": return "Ceramics and Composites";
		case "2504": return "Electronic, Optical and Magnetic Materials";
		case "2505": return "Materials Chemistry";
		case "2506": return "Metals and Alloys";
		case "2507": return "Polymers and Plastics";
		case "2508": return "Surfaces, Coatings and Films";
		case "2600": return "Mathematics (all)";
		case "2601": return "Mathematics (miscellaneous)";
		case "2602": return "Algebra and Number Theory";
		case "2603": return "Analysis";
		case "2604": return "Applied Mathematics";
		case "2605": return "Computational Mathematics";
		case "2606": return "Control and Optimization";
		case "2607": return "Discrete Mathematics and Combinatorics";
		case "2608": return "Geometry and Topology";
		case "2609": return "Logic";
		case "2610": return "Mathematical Physics";
		case "2611": return "Modelling and Simulation";
		case "2612": return "Numerical Analysis";
		case "2613": return "Statistics and Probability";
		case "2614": return "Theoretical Computer Science";
		case "2700": return "Medicine (all)";
		case "2701": return "Medicine (miscellaneous)";
		case "2702": return "Anatomy";
		case "2703": return "Anesthesiology and Pain Medicine";
		case "2704": return "Biochemistry, medical";
		case "2705": return "Cardiology and Cardiovascular Medicine";
		case "2706": return "Critical Care and Intensive Care Medicine";
		case "2707": return "Complementary and alternative medicine";
		case "2708": return "Dermatology";
		case "2709": return "Drug guides";
		case "2710": return "Embryology";
		case "2711": return "Emergency Medicine";
		case "2712": return "Endocrinology, Diabetes and Metabolism";
		case "2713": return "Epidemiology";
		case "2714": return "Family Practice";
		case "2715": return "Gastroenterology";
		case "2716": return "Genetics(clinical)";
		case "2717": return "Geriatrics and Gerontology";
		case "2718": return "Health Informatics";
		case "2719": return "Health Policy";
		case "2720": return "Hematology";
		case "2721": return "Hepatology";
		case "2722": return "Histology";
		case "2723": return "Immunology and Allergy";
		case "2724": return "Internal Medicine";
		case "2725": return "Infectious Diseases";
		case "2726": return "Microbiology (medical)";
		case "2727": return "Nephrology";
		case "2728": return "Clinical Neurology";
		case "2729": return "Obstetrics and Gynaecology";
		case "2730": return "Oncology";
		case "2731": return "Ophthalmology";
		case "2732": return "Orthopedics and Sports Medicine";
		case "2733": return "Otorhinolaryngology";
		case "2734": return "Pathology and Forensic Medicine";
		case "2735": return "Pediatrics, Perinatology, and Child Health";
		case "2736": return "Pharmacology (medical)";
		case "2737": return "Physiology (medical)";
		case "2738": return "Psychiatry and Mental health";
		case "2739": return "Public Health, Environmental and Occupational Health";
		case "2740": return "Pulmonary and Respiratory Medicine";
		case "2741": return "Radiology Nuclear Medicine and imaging";
		case "2742": return "Rehabilitation";
		case "2743": return "Reproductive Medicine";
		case "2744": return "Reviews and References, Medical";
		case "2745": return "Rheumatology";
		case "2746": return "Surgery";
		case "2747": return "Transplantation";
		case "2748": return "Urology";
		case "2800": return "Neuroscience(all)";
		case "2801": return "Neuroscience (miscellaneous)";
		case "2802": return "Behavioral Neuroscience";
		case "2803": return "Biological Psychiatry";
		case "2804": return "Cellular and Molecular Neuroscience";
		case "2805": return "Cognitive Neuroscience";
		case "2806": return "Developmental Neuroscience";
		case "2807": return "Endocrine and Autonomic Systems";
		case "2808": return "Neurology";
		case "2809": return "Sensory Systems";
		case "2900": return "Nursing(all)                                                          ";
		case "2901": return "Nursing (miscellaneous)";
		case "2902": return "Advanced and Specialised Nursing";
		case "2903": return "Assessment and Diagnosis";
		case "2904": return "Care Planning";
		case "2905": return "Community and Home Care";
		case "2906": return "Critical Care";
		case "2907": return "Emergency";
		case "2908": return "Fundamentals and skills";
		case "2909": return "Gerontology";
		case "2910": return "Issues, ethics and legal aspects";
		case "2911": return "Leadership and Management";
		case "2912": return "LPN and LVN";
		case "2913": return "Maternity and Midwifery";
		case "2914": return "Medical–Surgical";
		case "2915": return "Nurse Assisting";
		case "2916": return "Nutrition and Dietetics";
		case "2917": return "Oncology(nursing)";
		case "2918": return "Pathophysiology";
		case "2919": return "Pediatrics";
		case "2920": return "Pharmacology (nursing)";
		case "2921": return "Phychiatric Mental Health";
		case "2922": return "Research and Theory";
		case "2923": return "Review and Exam Preparation";
		case "3000": return "Pharmacology, Toxicology and Pharmaceutics(all)        ";
		case "3001": return "Pharmacology, Toxicology and Pharmaceutics (miscellaneous)";
		case "3002": return "Drug Discovery";
		case "3003": return "Pharmaceutical Science";
		case "3004": return "Pharmacology";
		case "3005": return "Toxicology";
		case "3100": return "Physics and Astronomy(all)";
		case "3101": return "Physics and Astronomy (miscellaneous)";
		case "3102": return "Acoustics and Ultrasonics";
		case "3103": return "Astronomy and Astrophysics";
		case "3104": return "Condensed Matter Physics";
		case "3105": return "Instrumentation";
		case "3106": return "Nuclear and High Energy Physics";
		case "3107": return "Atomic and Molecular Physics, and Optics";
		case "3108": return "Radiation";
		case "3109": return "Statistical and Nonlinear Physics";
		case "3110": return "Surfaces and Interfaces";
		case "3200": return "Psychology(all)";
		case "3201": return "Psychology (miscellaneous)";
		case "3202": return "Applied Psychology";
		case "3203": return "Clinical Psychology";
		case "3204": return "Developmental and Educational Psychology";
		case "3205": return "Experimental and Cognitive Psychology";
		case "3206": return "Neuropsychology and Physiological Psychology";
		case "3207": return "Social Psychology";
		case "3300": return "Social Sciences(all)";
		case "3301": return "Social Sciences (miscellaneous)";
		case "3302": return "Archaeology";
		case "3303": return "Development";
		case "3304": return "Education";
		case "3305": return "Geography, Planning and Development";
		case "3306": return "Health(social science)";
		case "3307": return "Human Factors and Ergonomics";
		case "3308": return "Law";
		case "3309": return "Library and Information Sciences";
		case "3310": return "Linguistics and Language";
		case "3311": return "Safety Research";
		case "3312": return "Sociology and Political Science";
		case "3313": return "Transportation";
		case "3314": return "Anthropology";
		case "3315": return "Communication";
		case "3316": return "Cultural Studies";
		case "3317": return "Demography";
		case "3318": return "Gender Studies";
		case "3319": return "Life-span and Life-course Studies";
		case "3320": return "Political Science and International Relations";
		case "3321": return "Public Administration";
		case "3322": return "Urban Studies";
		case "3400": return "veterinary(all)";
		case "3401": return "veterinary (miscalleneous)";
		case "3402": return "Equine";
		case "3403": return "Food Animals";
		case "3404": return "Small Animals";
		case "3500": return "Dentistry(all)";
		case "3501": return "Dentistry (miscellaneous)";
		case "3502": return "Dental Assisting";
		case "3503": return "Dental Hygiene";
		case "3504": return "Oral Surgery";
		case "3505": return "Orthodontics";
		case "3506": return "Periodontics";
		case "3600": return "Health Professions(all)";
		case "3601": return "Health Professions (miscellaneous)";
		case "3602": return "Chiropractics";
		case "3603": return "Complementary and Manual Therapy";
		case "3604": return "Emergency Medical Services";
		case "3605": return "Health Information Management";
		case "3606": return "Medical Assisting and Transcription";
		case "3607": return "Medical Laboratory Technology";
		case "3608": return "Medical Terminology";
		case "3609": return "Occupational Therapy";
		case "3610": return "Optometry";
		case "3611": return "Pharmacy";
		case "3612": return "Physical Therapy, Sports Therapy and Rehabilitation";
		case "3613": return "Podiatry";
		case "3614": return "Radiological and Ultrasound Technology";
		case "3615": return "Respiratory Care";
		case "3616": return "Speech and Hearing";
		default:throw new Error();
		}
	}


	//https://service.elsevier.com/app/answers/detail/a_id/15181/supporthub/scopus/
	private static String getIntermediateSubjectAreaCodeFromAsjcCode(String s) {
		if(s.startsWith("10"))return "MULT";
		if(s.startsWith("11"))return "AGRI";
		if(s.startsWith("12"))return "ARTS";
		if(s.startsWith("13"))return "BIOC";
		if(s.startsWith("14"))return "BUSI";
		if(s.startsWith("15"))return "CENG";
		if(s.startsWith("16"))return "CHEM";
		if(s.startsWith("17"))return "COMP";
		if(s.startsWith("18"))return "DECI";
		if(s.startsWith("19"))return "EART";
		if(s.startsWith("20"))return "ECON";
		if(s.startsWith("21"))return "ENER";
		if(s.startsWith("22")) return "ENGI";
		if(s.startsWith("23")) return "ENVI";
		if(s.startsWith("24")) return "IMMU";
		if(s.startsWith("25")) return "MATE";
		if(s.startsWith("26")) return "MATH";
		if(s.startsWith("27")) return "MEDI";
		if(s.startsWith("28")) return "NEUR";
		if(s.startsWith("29")) return "NURS";
		if(s.startsWith("30")) return "PHAR";
		if(s.startsWith("31")) return "PHYS";
		if(s.startsWith("32")) return "PSYC";
		if(s.startsWith("33"))return "SOCI";
		if(s.startsWith("34"))return "VETE";
		if(s.startsWith("35"))return "DENT";
		if(s.startsWith("36"))return "HEAL";
		throw new Error();
	}

	private static String getAreaIntermediateCode(String in) {
		switch(in)
		{
		case "Agricultural and Biological Sciences": return "AGRI";
		case "Arts and Humanities": return "ARTS";
		case "Biochemistry, Genetics, and Molecular Biology": return "BIOC";
		case "Business, Management, and Accounting": return "BUSI";
		case "Chemical Engineering": return "CENG";
		case "Chemistry": return "CHEM";
		case "Computer Science": return "COMP";
		case "Decision Sciences": return "DECI";
		case "Dentistry": return "DENT";
		case "Earth and Planetary Sciences": return "EART";
		case "Economics, Econometrics, and Finance": return "ECON";
		case "Energy": return "ENER";
		case "Engineering": return "ENGI";
		case "Environmental Science": return "ENVI";
		case "Health Professions": return "HEAL";
		case "Immunology and Microbiology": return "IMMU";
		case "Material Science": return "MATE";
		case "Mathematics": return "MATH";
		case "Medicine": return "MEDI";
		case "Multidisciplinary": return "MULT";
		case "Neuroscience": return "NEUR";
		case "Nursing": return "NURS";
		case "Pharmacology, Toxicology, and Pharmaceutics": return "PHAR";
		case "Physics and Astronomy": return "PHYS";
		case "Psychology": return "PSYC";
		case "Social Sciences": return "SOCI";
		case "Veterinary": return "VETE";
		default: throw new Error();
		}
	}

	private static String getAreaIntermediateName(String in) {
		if(isDetailedAreaCode(in))
			return getAreaIntermediateName(getIntermediateSubjectAreaCodeFromAsjcCode(in));
		switch(in)
		{
		case "AGRI": return "Agricultural and Biological Sciences";
		case "ARTS": return "Arts and Humanities";
		case "BIOC": return "Biochemistry, Genetics, and Molecular Biology";
		case "BUSI": return "Business, Management, and Accounting";
		case "CENG": return "Chemical Engineering";
		case "CHEM": return "Chemistry";
		case "COMP": return "Computer Science";
		case "DECI": return "Decision Sciences";
		case "DENT": return "Dentistry";
		case "EART": return "Earth and Planetary Sciences";
		case "ECON": return "Economics, Econometrics, and Finance";
		case "ENER": return "Energy";
		case "ENGI": return "Engineering";
		case "ENVI": return "Environmental Science";
		case "HEAL": return "Health Professions";
		case "IMMU": return "Immunology and Microbiology";
		case "MATE": return "Material Science";
		case "MATH": return "Mathematics";
		case "MEDI": return "Medicine";
		case "MULT": return "Multidisciplinary";
		case "NEUR": return "Neuroscience";
		case "NURS": return "Nursing";
		case "PHAR": return "Pharmacology, Toxicology, and Pharmaceutics";
		case "PHYS": return "Physics and Astronomy";
		case "PSYC": return "Psychology";
		case "SOCI": return "Social Sciences";
		case "VETE": return "Veterinary";
		default:throw new Error();
		}

	}

	private static boolean isDetailedAreaCode(String in) {
		return in.length()==4 && in.chars().allMatch(Character::isDigit);
	}

	public static AsjcInterdisciplinaryScore newInstance(List<Reference> allCitedPapers) {
		List<String> distributionOfAllEntries = allCitedPapers.stream()
				.map(x->x.getAsjcCodes())
				.reduce(new ArrayList(), (x,y)->{x.addAll(y); return x;});			

		Map<String, Long> asjcCodesFrequencyMap = distributionOfAllEntries
				.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		return newInstance(asjcCodesFrequencyMap);
	}

	//https://service.elsevier.com/app/answers/detail/a_id/15181/supporthub/scopus/
	private static String getGlobalSubjectAreaCode(String s) {
		if(isDetailedAreaCode(s))
			s = getIntermediateSubjectAreaCodeFromAsjcCode(s);
		
		if(s.startsWith("MULT")) return "Multidisciplinary";
		if(s.startsWith("AGRI")) return "Life Sciences";
		if(s.startsWith("ARTS")) return "Social Sciences & Humanities";
		if(s.startsWith("BIOC")) return "Life Sciences";
		if(s.startsWith("BUSI")) return "Social Sciences & Humanities";
		if(s.startsWith("CENG")) return "Physical Sciences";
		if(s.startsWith("CHEM")) return "Physical Sciences";
		if(s.startsWith("COMP")) return "Physical Sciences";
		if(s.startsWith("DECI")) return "Social Sciences & Humanities";
		if(s.startsWith("EART")) return "Physical Sciences";
		if(s.startsWith("ECON")) return "Social Sciences & Humanities";
		if(s.startsWith("ENER")) return "Physical Sciences";
		if(s.startsWith("ENGI")) return "Physical Sciences";
		if(s.startsWith("ENVI")) return "Physical Sciences";
		if(s.startsWith("IMMU")) return "Life Sciences";
		if(s.startsWith("MATH")) return "Physical Sciences";
		if(s.startsWith("MEDI")) return "Health Sciences";
		if(s.startsWith("NEUR")) return "Life Sciences";
		if(s.startsWith("PHAR")) return "Life Sciences";
		if(s.startsWith("PHYS")) return "Physical Sciences";
		if(s.startsWith("MATE")) return "Physical Sciences";
		if(s.startsWith("NURS")) return "Health Sciences";
		if(s.startsWith("PSYC")) return "Social Sciences & Humanities";
		if(s.startsWith("SOCI")) return "Social Sciences & Humanities";
		if(s.startsWith("VETE")) return "Health Sciences";
		if(s.startsWith("DENT")) return "Health Sciences";
		if(s.startsWith("HEAL")) return "Health Sciences";
		throw new Error();
	}

	public static String toExcel(AsjcInterdisciplinaryScore... scores) {

		Set<String> allInput = new TreeSet<>(
				(x,y)->
				{
					String globalCategoryX = getGlobalSubjectAreaCode(x);
					String globalCategoryY = getGlobalSubjectAreaCode(y);
					if(globalCategoryX.equals(globalCategoryY))
						return x.compareTo(y);
					return globalCategoryX.compareTo(globalCategoryY);
				}
				);
		
		allInput.addAll(allIntermediateCodes);

		return allInput.stream()
				.map(x->
				{
					String res = getAreaIntermediateName(x)+"\t"+x;
					for(AsjcInterdisciplinaryScore sc:scores)
					{
						long totalExtended = 0;
						if(sc.getIntermediaryLevelScores().containsKey(x))
							totalExtended = sc.getIntermediaryLevelScores().get(x);
						//	if(allExtendedAreas2.containsKey(x))
						//		totalExtended2 = allExtendedAreas2.get(x).values().stream().reduce(0, (z,y)->z+y);

						res+="\t"+totalExtended;//+"\t"+totalExtended2;
					}
					return res;
				}
						).reduce("",(x,y)->x+"\n"+y);

	}

	private Map<String, Long> getIntermediaryLevelScores() {
		Map<String, Long> total = new HashMap<>();
		for(String specCode : asjcCodesOccurrenceMap.keySet())
		{
			String interm = getIntermediateSubjectAreaCodeFromAsjcCode(specCode);
			if(!total.containsKey(interm))
				total.put(interm, 0l);
			total.put(interm, total.get(interm)+asjcCodesOccurrenceMap.get(specCode));
		}
		return total;
	}

	public static AsjcInterdisciplinaryScore newInstance(Reference res) {
		return newInstance(Arrays.asList(res));
	}

	public Set<String> getAllIntermediateSubjectAreaCodes() {
		return asjcCodesOccurrenceMap.keySet()
				.stream().map(x->getIntermediateSubjectAreaCodeFromAsjcCode(x))
				.collect(Collectors.toSet());
		
	}

	public static List<String> getIntermediateScores(List<String> asjcCodes) {
		return asjcCodes.stream().map(x->getIntermediateSubjectAreaCodeFromAsjcCode(x))
				.collect(Collectors.toList());
	}
	
	
}
