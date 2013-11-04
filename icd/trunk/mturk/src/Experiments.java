import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Experiments {
	public static HashSet<OtherQuestion> otherQuestions = new HashSet<OtherQuestion>();
	public static HashSet<MultipleQuestion> multipleQuestions = new HashSet<MultipleQuestion>();
	public static HashSet<BinaryQuestion> binaryQuestions = new HashSet<BinaryQuestion>();
	public static HashMap<String, HashMap<String, Answer>> binaryResults = new HashMap<String, HashMap<String, Answer>>();

	public static void readOtherThanQuestion() {
		try {
			String path = "otherThanExp/OtherThanQues.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				String locationsS = in.readLine();
				String locationStrings[] = locationsS.split(",");
				HashSet<String> locations = new HashSet<String>();
				for (int i = 0; i < locationStrings.length; i++) {
					locations.add(locationStrings[i]);
				}
				String definition = in.readLine();
				String wiki = in.readLine();

				OtherQuestion ques = new OtherQuestion(disease, locations,
						definition, wiki);
				otherQuestions.add(ques);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readBinaryQuestion() {
		try {
			String path = "otherThanExp/BinaryQues.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				String location = in.readLine();
				String definition = in.readLine();
				String wiki = in.readLine();
				BinaryQuestion ques = new BinaryQuestion(disease, location,
						definition, wiki);
				binaryQuestions.add(ques);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readMultipleQuestion() {
		try {
			String path = "otherThanExp/MultipleQues.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				String locationsS = in.readLine();
				String locationStrings[] = locationsS.split(",");
				HashSet<String> locations = new HashSet<String>();
				for (int i = 0; i < locationStrings.length; i++) {
					locations.add(locationStrings[i]);
				}
				String definition = in.readLine();
				String wiki = in.readLine();

				MultipleQuestion ques = new MultipleQuestion(disease,
						locations, definition, wiki);
				multipleQuestions.add(ques);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readBinaryQuestionFromMultiple() {
		try {
			String path = "otherThanExp/MultipleQues.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				String locationsS = in.readLine();
				String locationStrings[] = locationsS.split(",");
				HashSet<String> locations = new HashSet<String>();
				for (int i = 0; i < locationStrings.length; i++) {
					locations.add(locationStrings[i]);
				}
				String definition = in.readLine();
				String wiki = in.readLine();

				Iterator<String> itr = locations.iterator();
				while (itr.hasNext()) {
					BinaryQuestion ques = new BinaryQuestion(disease,
							itr.next(), definition, wiki);
					binaryQuestions.add(ques);
				}
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeOtherThanQuestion() {
		try {
			FileWriter fstream = new FileWriter(
					"otherThanExp/otherThanLogs.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<OtherQuestion> itr = otherQuestions.iterator();
			while (itr.hasNext()) {
				OtherQuestion question = itr.next();
				out.write(question.disease);
				out.newLine();
				out.write(question.locations.toString());
				out.newLine();
				out.write(question.HITID);
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeBinaryQuestion() {
		try {
			FileWriter fstream = new FileWriter("otherThanExp/binaryLogs.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<BinaryQuestion> itr = binaryQuestions.iterator();
			while (itr.hasNext()) {
				BinaryQuestion question = itr.next();
				out.write(question.disease);
				out.newLine();
				out.write(question.location);
				out.newLine();
				try{
					out.write(question.HITID);
				} catch (Exception e) {
					e.printStackTrace();
				}
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeMultipleQuestion() {
		try {
			FileWriter fstream = new FileWriter("otherThanExp/multipleLogs.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<MultipleQuestion> itr = multipleQuestions.iterator();
			while (itr.hasNext()) {
				MultipleQuestion question = itr.next();
				out.write(question.disease);
				out.newLine();
				out.write(question.locations.toString());
				out.newLine();
				out.write(question.HITID);
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void postMultipleQuestions() {
		Iterator<MultipleQuestion> itr = multipleQuestions.iterator();
		while (itr.hasNext()) {
			MultipleQuestion ques = itr.next();
			ques.postQuestion();
		}
	}

	public static void postBinaryQuestions() {
		Iterator<BinaryQuestion> itr = binaryQuestions.iterator();
		while (itr.hasNext()) {
			BinaryQuestion ques = itr.next();
			ques.postQuestion();
		}
	}

	public static void postOtherThanQuestions() {
		Iterator<OtherQuestion> itr = otherQuestions.iterator();
		while (itr.hasNext()) {
			OtherQuestion ques = itr.next();
			ques.postQuestion();
		}
	}

	public static void getMultipleResults() {
		try {
			String path = "otherThanExp/multipleLogs.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				String locationsS = in.readLine();
				String locationStrings[] = locationsS.split(",");
				HashSet<String> locations = new HashSet<String>();
				for (int i = 0; i < locationStrings.length; i++) {
					if (i != locationStrings.length - 1) {
						locations.add(locationStrings[i].substring(1));
					} else {
						locations.add(locationStrings[i].substring(1,
								locationStrings[i].length() - 1));
					}
				}
				String HITID = in.readLine();
				System.out.println("disease:"+disease);
				Interface.getAssignmentsForMutiple(HITID, locations);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printBinaryResults() {
		Iterator<String> itrDisease = binaryResults.keySet().iterator();
		while (itrDisease.hasNext()) {
			String disease = itrDisease.next();
			HashMap<String, Answer> locationResults = binaryResults
					.get(disease);
			Iterator<String> itrLocation = locationResults.keySet().iterator();
			System.out.println("Disease: " + disease);
			while (itrLocation.hasNext()) {
				String location = itrLocation.next();
				Answer ans = locationResults.get(location);
				System.out.println("Location:" + location + " yes count: "
						+ ans.yes + "  total:" + ans.total);
			}
			System.out.println();
		}
	}

	public static void getBinaryResultsFromMultiple() {
		try {
			String path = "otherThanExp/binaryLogs.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine;
				String location = in.readLine();
				String HITID = in.readLine();
				Answer answer = Interface.getAssignmentsForHIT(HITID);
				if (binaryResults.containsKey(disease)) {
					binaryResults.get(disease).put(location, answer);
				} else {
					HashMap<String, Answer> locationResults = new HashMap<String, Answer>();
					locationResults.put(location, answer);
					binaryResults.put(disease, locationResults);
				}
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		try {
			Hierarchy
					.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Interface.LocationParentChild = Hierarchy.LocationParentChild;
		Interface.readWiki();

//		 readBinaryQuestionFromMultiple();
//		 postBinaryQuestions();
//		 writeBinaryQuestion();

		// readOtherThanQuestion();
		// postOtherThanQuestions();
		// writeOtherThanQuestion();

		 readMultipleQuestion();
		 postMultipleQuestions();
		 writeMultipleQuestion();

//		getMultipleResults();

//		 getBinaryResultsFromMultiple();
//		 printBinaryResults();

		// readBinaryQuestion();
		// postBinaryQuestions();
		// writeBinaryQuestion();
	}
}

class OtherQuestion {
	public String disease;
	public HashSet<String> locations;
	public String definition;
	public String wiki;
	public String HITID;

	public OtherQuestion(String disease, HashSet<String> locations,
			String definition, String wiki) {
		this.disease = disease;
		this.locations = locations;
		this.definition = definition;
		this.wiki = wiki;
	}

	public String postQuestion() {
		String HITID = Interface.createOtherThanQuestion(disease, locations,
				definition, wiki);
		this.HITID = HITID;
		return HITID;
	}
}

class MultipleQuestion {
	public String disease;
	public HashSet<String> locations;
	public String definition;
	public String wiki;
	public String HITID;

	public MultipleQuestion(String disease, HashSet<String> locations,
			String definition, String wiki) {
		this.disease = disease;
		this.locations = locations;
		this.definition = definition;
		this.wiki = wiki;
	}

	public String postQuestion() {
		String ls[] = (String[]) locations.toArray(new String[0]);
		String HITID = Interface.createMultipleQuestion(disease, ls,
				definition, wiki);
		this.HITID = HITID;
		return HITID;
	}
}

class BinaryQuestion {
	public String disease;
	public String location;
	public String definition;
	public String wiki;
	public String HITID;

	public BinaryQuestion(String disease, String location, String definition,
			String wiki) {
		this.disease = disease;
		this.location = location;
		this.definition = definition;
		this.wiki = wiki;
	}

	public String postQuestion() {
		String HITID = Interface.createBinaryQuestion(disease, location,
				definition, wiki);
		this.HITID = HITID;
		return HITID;
	}
}