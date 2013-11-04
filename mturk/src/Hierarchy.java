import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Hierarchy {
	public static HashMap<String, HashSet<String>> ICDParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, HashSet<String>> ICDChildParent = new HashMap<String, HashSet<String>>();
	public static String ICDRoot;

	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String> LocationChildParent = new HashMap<String, String>();
	public static String LocationRoot;

	public static HashMap<String, String> diseaseName = new HashMap<String, String>();
	public static HashMap<String, String> diseaseDefinition = new HashMap<String, String>();
	public static HashMap<String, String> diseaseWikiLink = new HashMap<String, String>();

	// write to disk
	public static HashMap<String, HashMap<String, Integer>> diseaseLocation = new HashMap<String, HashMap<String, Integer>>();
	public static HashMap<String, HashSet<String>> diseaseLocationFinal = new HashMap<String, HashSet<String>>();
	public static HashMap<String, Question> diseaseCurQues = new HashMap<String, Question>();

	// public static HashMap<String, String> curDisLocation = new
	// HashMap<String, String>();
	// public static HashMap<String, String> diseaseType = new HashMap<String,
	// String>();

	public static void main(String agrs[]) throws Exception {
		readHierarchyData();
		ICDRoot = "http://who.int/icd#B35";
		LocationRoot = "root";
		//iniHierarchyWork();
		HierarchyWork();
		System.out.println();
	}

	public static void HierarchyWork() {
		readData();
		Iterator<String> itr = diseaseCurQues.keySet().iterator();
		while (itr.hasNext()) {
			Question cur = diseaseCurQues.get(itr.next());
			Answer ans = Interface.getAssignmentsForHIT(cur.HITID);
			if (ans.total != 10) {
				continue;
			}
			if (cur.type.matches("binary")) {

				if (ans.yes > 5) {
					getBinaryYes(cur);
				} else if (ans.yes < 4) {
					getBinaryNo(cur);
				} else {
					getBinaryMore(cur);
					System.out.println("Binary Problem: " + cur.disease + " "
							+ cur.location);
				}

			} else if (cur.type.matches("other")) {
				if (ans.yes > 6) {
					getOtherYes(cur);
				} else if (ans.yes <= 4) {
					startNewDiseases(cur.disease);
				} else {
					System.out.println("Other Problem: " + cur.disease + " "
							+ cur.location);
				}

			} else if (cur.type.matches("same")) {

				if (ans.yes > 6) {
					startNewDiseases(cur.disease);
				} else if (ans.yes <= 4) {
					String HITID = Interface.createBinaryQuestion(diseaseName.get(cur.disease),
							cur.location, diseaseDefinition.get(cur.disease),
							diseaseWikiLink.get(cur.disease));
					diseaseCurQues.put(cur.disease, new Question(cur.disease,
							cur.location, HITID));
				} else {
					System.out.println("Other Problem: " + cur.disease + " "
							+ cur.location);
				}

			}
		}
		writeData();
	}

	private static void getBinaryMore(Question question){
		String HITID = Interface.createBinaryQuestion(diseaseName.get(question.disease),
				question.location, diseaseDefinition.get(question.disease),
				diseaseWikiLink.get(question.disease));
		diseaseCurQues.put(question.disease, new Question(question.disease,
				question.location, HITID));
	}
	private static void startNewDiseases(String disease) {
		if (!ICDParentChild.containsKey(disease)) {
			return;
		}
		Iterator<String> itr = ICDParentChild.get(disease).iterator();
		while (itr.hasNext()) {
			String cur = itr.next();
			startNewDisease(cur, disease);
		}
	}

	private static void startNewDisease(String disease, String parent) {
		String HITID = Interface.createSameQuestion(diseaseName.get(disease),
				diseaseLocationFinal.get(disease),
				diseaseDefinition.get(disease), diseaseWikiLink.get(disease),
				diseaseName.get(parent));
		diseaseCurQues.put(disease, new Question("same", disease,
				diseaseLocationFinal.get(disease), HITID));
	}

	public static void getOtherYes(Question question) {
		String nextLocation = getNextLocation(question.location,
				question.disease);
		String HITID = Interface.createBinaryQuestion(diseaseName.get(question.disease),
				nextLocation, diseaseDefinition.get(question.disease),
				diseaseWikiLink.get(question.disease));
		diseaseCurQues.put(question.disease, new Question(question.disease,
				nextLocation, HITID));
	}

	private static String getNextLocation(String location, String disease) {
		Set<String> siblings = LocationParentChild.get(LocationChildParent
				.get(location));
		HashMap<String, Integer> locations = diseaseLocation.get(disease);
		Iterator<String> itr = siblings.iterator();
		String nextLocation = null;
		while (itr.hasNext()) {
			String loc = itr.next();
			if (!locations.containsKey(loc)) {
				nextLocation = loc;
				break;
			}
		}
		if (nextLocation == null) {
			nextLocation = getNextLocation(LocationChildParent.get(location),
					disease);
		}
		return nextLocation;
	}

	public static void getBinaryNo(Question question) {
		if(diseaseLocation.containsKey(question.disease)){
			HashMap<String, Integer> locations = diseaseLocation
					.get(question.disease);
			locations.put(question.location, 0);
		} else {
			HashMap<String, Integer> locations = new HashMap<String, Integer>();
			locations.put(question.location, 0);
			diseaseLocation.put(question.disease, locations);
		}

		
		String nextLocation = getNextLocation(question.locations.iterator().next(),
				question.disease);

		if (!LocationParentChild
				.get(LocationChildParent.get(question.location)).contains(
						nextLocation)
				&& !diseaseLocationFinal.containsKey(question.disease)) {
			System.out.println("Problem: " + question.disease + " "
					+ question.location);
			return;
		}
		String HITID = Interface.createBinaryQuestion(diseaseName.get(question.disease),
				nextLocation, diseaseDefinition.get(question.disease),
				diseaseWikiLink.get(question.disease));
		diseaseCurQues.put(question.disease, new Question(question.disease,
				nextLocation, HITID));
	}

	public static void getBinaryYes(Question question) {
		if(diseaseLocation.containsKey(question.disease)){
			HashMap<String, Integer> locations = diseaseLocation
					.get(question.disease);
			locations.put(question.location, 1);
		} else {
			HashMap<String, Integer> locations = new HashMap<String, Integer>();
			locations.put(question.location, 1);
			diseaseLocation.put(question.disease, locations);
		}

		String location = checkfound(question);
		if (location != null) {
			if (diseaseLocationFinal.containsKey(question.disease)) {
				diseaseLocationFinal.get(question.disease).add(location);
			} else {
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(location);
				diseaseLocationFinal.put(question.disease, tmp);
			}

			String HITID = Interface.createOtherThanQuestion(diseaseName.get(question.disease),
					diseaseLocationFinal.get(question.disease),
					diseaseDefinition.get(question.disease),
					diseaseWikiLink.get(question.disease));
			diseaseCurQues.put(question.disease,
					new Question("other", question.disease,
							diseaseLocationFinal.get(question.disease), HITID));
			// if (other != null) {
			// String HITID = Interface.createBinaryQuestion(question.disease,
			// other, diseaseDefinition.get(question.disease),
			// diseaseWikiLink.get(question.disease));
			// diseaseCurQues.put(question.disease, new Question(
			// question.disease, other, HITID));
			// } else {
			// Iterator<String> itr =
			// ICDParentChild.get(question.disease).iterator();
			// while(itr.hasNext()){
			// findLocation(itr.next());
			// }
			// }
		} else {
			String nextLocation = LocationParentChild.get(question.location)
					.iterator().next();
			String HITID = Interface.createBinaryQuestion(diseaseName.get(question.disease),
					nextLocation, diseaseDefinition.get(question.disease),
					diseaseWikiLink.get(question.disease));
			diseaseCurQues.put(question.disease, new Question(question.disease,
					nextLocation, HITID));
		}
	}

	private static String checkfound(Question question) {
		if (!LocationParentChild.containsKey(question.location)) {
			return question.location;
		}
		Set<String> siblings = LocationParentChild.get(LocationChildParent
				.get(question.location));
		boolean found = true;
		HashMap<String, Integer> locations = diseaseLocation
				.get(question.disease);
		Iterator<String> itr = siblings.iterator();
		while (itr.hasNext()) {
			String location = itr.next();
			if (!locations.containsKey(location)) {
				found = false;
			} else if (locations.get(location) == 0) {
				found = false;
			}
		}
		if (found) {
			return LocationChildParent.get(question.location);
		}
		return null;
	}

	public static void readHierarchyData() throws Exception {
		readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/ICDStrcuture.txt");
		readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		readDefAndWiki("/Users/Yun/Dropbox/medical/workspace/turk/DiseaseDefiAndWiki.txt");
	}

	public static void iniHierarchyWork() {
		findTopLocation(ICDRoot);
//		Set<String> diseases = ICDParentChild.get(ICDRoot);
//		Iterator<String> itr = diseases.iterator();
//		while (itr.hasNext()) {
//			String topDisease = itr.next();
//			findTopLocation(topDisease);
//		}
		writeData();
	}

	public static void findTopLocation(String topDisease) {
		String location = LocationParentChild.get(LocationRoot).iterator()
				.next();
		String id = Interface.createBinaryQuestion(diseaseName.get(topDisease),
				location, diseaseDefinition.get(topDisease),
				diseaseWikiLink.get(topDisease));
		diseaseCurQues.put(topDisease, new Question(topDisease, location, id));
	}

	public static void readDefAndWiki(String path) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(path));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			String disease = strLine;
			strLine = in.readLine();
			diseaseName.put(disease, strLine);
			strLine = in.readLine();
			diseaseDefinition.put(disease, strLine);
			strLine = in.readLine();
			diseaseWikiLink.put(disease, strLine);
			in.readLine();
		}
		in.close();
	}

	public static void readLocationHierarchy(String path) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(path));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			String parent = strLine;
			strLine = in.readLine();
			String[] children = strLine.split("!");
			for (int i = 0; i < children.length; i++) {
				if (LocationParentChild.containsKey(parent)) {
					Set<String> tmp = LocationParentChild.get(parent);
					tmp.add(children[i]);
				} else {
					HashSet<String> tmp = new HashSet<String>();
					tmp.add(children[i]);
					LocationParentChild.put(parent, tmp);
				}

				LocationChildParent.put(children[i], parent);
			}
			in.readLine();
		}
		in.close();
	}

	public static void readICDHierarchy(String path) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(path));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			String parent = strLine;
			strLine = in.readLine();
			String[] children = strLine.split(" ");
			for (int i = 0; i < children.length; i++) {
				if (ICDParentChild.containsKey(parent)) {
					Set<String> tmp = ICDParentChild.get(parent);
					tmp.add(children[i]);
				} else {
					HashSet<String> tmp = new HashSet<String>();
					tmp.add(children[i]);
					ICDParentChild.put(parent, tmp);
				}

				if (ICDChildParent.containsKey(children[i])) {
					Set<String> tmp = ICDChildParent.get(children[i]);
					tmp.add(parent);
				} else {
					HashSet<String> tmp = new HashSet<String>();
					tmp.add(parent);
					ICDChildParent.put(children[i], tmp);
				}
			}
			in.readLine();
		}
		in.close();
	}

	public static void readData() {
		readDiseaseLocation();
		readDiseaseLocationFinal();
		readDiseaseCurQues();
		Interface.readLog();
	}

	public static void writeData() {
		writeDiseaseLocation();
		writeDiseaseLocationFinal();
		writeDiseaseCurQues();
		Interface.writeLog();
	}

	private static void readDiseaseCurQues() {
		try {
			String path = "/Users/Yun/Dropbox/medical/workspace/turk/DiseaseCurQues.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				strLine = in.readLine();
				String items[] = strLine.split("!");
				String location = items[1];
				String HITID = items[2];
				String type = items[3];
				HashSet<String> locations = new HashSet<String>();
				locations.add(location);
				Question question = new Question(type, disease, locations,
						HITID);
				diseaseCurQues.put(disease, question);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeDiseaseCurQues() {
		try {
			FileWriter fstream = new FileWriter("DiseaseCurQues.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<String> itr = diseaseCurQues.keySet().iterator();
			while (itr.hasNext()) {
				String disease = itr.next();
				out.write(disease);
				out.newLine();
				Question question = diseaseCurQues.get(disease);
				out.write(question.disease + "!");
				out.write(question.location + "!");
				out.write(question.HITID + "!");
				out.write(question.type);
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readDiseaseLocationFinal() {
		try {
			String path = "/Users/Yun/Dropbox/medical/workspace/turk/DiseaseLocationFinal.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				HashSet<String> curLocations = new HashSet<String>();
				strLine = in.readLine();
				String items[] = strLine.split("!");
				for (int i = 0; i < items.length - 1; i++) {
					String location = items[i];
					curLocations.add(location);
				}
				diseaseLocationFinal.put(disease, curLocations);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeDiseaseLocationFinal() {
		try {
			FileWriter fstream = new FileWriter("DiseaseLocationFinal.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<String> itr = diseaseLocationFinal.keySet().iterator();
			while (itr.hasNext()) {
				String disease = itr.next();
				out.write(disease);
				out.newLine();
				Iterator<String> itrLocation = diseaseLocationFinal
						.get(disease).iterator();
				while (itrLocation.hasNext()) {
					String location = itrLocation.next();
					out.write(location + "!");
				}
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readDiseaseLocation() {
		try {
			String path = "/Users/Yun/Dropbox/medical/workspace/turk/diseaseLocation.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				HashMap<String, Integer> curLocations = new HashMap<String, Integer>();
				strLine = in.readLine();
				//strLine = "1";
				//int size = Integer.parseInt(strLine);
				double size = Double.parseDouble(strLine);
				for (int i = 0; i < size; i++) {
					strLine = in.readLine();
					String items[] = strLine.split("!");
					String location = items[0];
					int type = Integer.parseInt(items[1]);
					curLocations.put(location, type);
				}
				diseaseLocation.put(disease, curLocations);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeDiseaseLocation() {
		try {
			FileWriter fstream = new FileWriter("diseaseLocation.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<String> itr = diseaseLocation.keySet().iterator();
			while (itr.hasNext()) {
				String disease = itr.next();
				out.write(disease);
				out.newLine();
				int size = diseaseLocation.get(disease).size();
				out.write(size+"");
				out.newLine();
				Iterator<String> itrLocation = diseaseLocation.get(disease)
						.keySet().iterator();
				while (itrLocation.hasNext()) {
					String location = itrLocation.next();
					out.write(location + "!"
							+ diseaseLocation.get(disease).get(location));
					out.newLine();
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Question {
	String disease;
	String location;
	String HITID;
	String type;
	HashSet<String> locations;
	Answer answer;

	public Question(String disease, String location, String HITID) {
		this.disease = disease;
		this.location = location;
		this.HITID = HITID;
		type = "binary";
	}

	public Question(String type, String disease, HashSet<String> locations,
			String HITID) {
		this.disease = disease;
		this.locations = locations;
		this.location = locations.iterator().next();
		this.HITID = HITID;
		this.type = type;
	}
}
