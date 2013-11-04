package Experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class TestHierarchy2 {
	static int exp = -1;
	public static HashMap<String, HashSet<String>> ICDParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, HashSet<String>> ICDChildParent = new HashMap<String, HashSet<String>>();
	public static String ICDRoot;

	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String> LocationChildParent = new HashMap<String, String>();
	public static String LocationRoot;

	// public static HashMap<String, String> diseaseName = new HashMap<String,
	// String>();
	public static HashMap<String, String> diseaseDefinition = new HashMap<String, String>();
	public static HashMap<String, String> diseaseWikiLink = new HashMap<String, String>();

	// write to disk
	public static HashMap<String, HashMap<String, Integer>> diseaseLocation = new HashMap<String, HashMap<String, Integer>>();
	public static HashMap<String, HashSet<String>> diseaseLocationFinal = new HashMap<String, HashSet<String>>();
	public static HashSet<MultipleQuestion> diseaseCurQues = new HashSet<MultipleQuestion>();
	public static HashSet<TestTransitionQuestion> diseaseTestSameCurQues = new HashSet<TestTransitionQuestion>();
	// public static HashSet<OtherQuestion> diseaseOtherCurQues = new
	// HashSet<OtherQuestion>();

	public static double totalCost;
	public static int totalMulQuestion;
	public static int totalSameQuestion;
	public static int totalOtherQuestion;
	public static int totalCurDisease;

	public static void main(String agrs[]) throws Exception {
		totalCost = 0;
		exp = 602;
		readHierarchyData();
		Interface.readWiki();
		Interface.LocationParentChild = TestHierarchy2.LocationParentChild;
		ICDRoot = "Rosacea and related disorders";
		LocationRoot = "root";

		// fixDB();
		// writeDisease(ICDRoot);
		// LocationRoot = "root";
		
//		 postQues("Papulopustular rosacea", LocationParentChild.get("Face"), false);
//		 postQues("Rosacea fulminans", LocationParentChild.get("Face"), false);
//		 postQues("Rhinophyma", LocationParentChild.get("Face"), false);

		readData();
		LocationHierarchy();
//		DiseasesHierarchy();
		System.out.println("total cost:" + totalCost);
		System.out.println("done");
	}

	public static void fixDB() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			HashSet<String> todo = new HashSet<String>();

			todo.add("Apocrine sweat disorders");
			todo.add("Alopecia...");
			todo.add("Lipoedematous alopecia");
			todo.add("Alopecia areata");
			todo.add("Weathered hair");
			todo.add("Acquired changes in hair colour");
			todo.add("Hirsutism and syndromes with hirsutism");
			todo.add("Hypertrichosis of eyelid");
			todo.add("Male pattern hair loss");
			todo.add("Female pattern hair loss (androgenetic)");

			Iterator<String> itr = todo.iterator();
			while (itr.hasNext()) {
				String disease = itr.next();
				BasicDBObject ques = new BasicDBObject("type",
						"doneOtherQuestion").append("disease", disease);
				DBCursor cursor = coll.find(ques);
				DBObject output = cursor.iterator().next();
				BasicDBObject newInput = new BasicDBObject("type",
						"curOtherQuestion").append("disease", disease);
				newInput.append("HITID", output.get("HITID"));
				newInput.append("locations", output.get("locations"));
				newInput.append("diseaseParent", output.get("diseaseParent"));
				coll.insert(newInput);
				newInput = new BasicDBObject("type", "curDisease").append(
						"disease", disease);
				coll.insert(newInput);
				// coll.remove(ques);
			}

			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void removeLocation(String location) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject ques = new BasicDBObject("type", "curDisease")
					.append("disease", location);
			coll.remove(ques);
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeDisease(String location) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject ques = new BasicDBObject("type", "curDisease")
					.append("disease", location);
			coll.insert(ques);
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void DiseasesHierarchy() {
		startNextDisease(true);
		sameQuestions();
		startNextDisease(false);
	}

	public static void sameQuestions() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			Iterator<TestTransitionQuestion> itr = diseaseTestSameCurQues
					.iterator();
			while (itr.hasNext()) {
				TestTransitionQuestion cur = itr.next();
				System.out.println("same question disease:" + cur.diseaseC);
				System.out.println(cur.locationsP);
				TestAnswer ans = Interface.getAssignmentsForTestHIT(cur.HITID,
						"hehe");

				if (ans.total != 10) {
					continue;
				}

				cur.removeFromCurList(exp);
				cur.same = ans.same;
				cur.other = ans.other;
				cur.part = ans.part;
				cur.total = ans.total;
				cur.writeFinishedQues(exp);

				if (cur.other > 4) {
					LocationRoot = "root";
					postQues(cur.diseaseC, LocationParentChild.get(LocationRoot),
							true);
					postQues(cur.diseaseC, LocationParentChild.get("Body Systems"),
							true);
					postQues(cur.diseaseC, LocationParentChild.get("Body Regions"),
							true);
					postQues(cur.diseaseC, LocationParentChild.get("Body Organ"),
							true);

					System.out.println("start multiple question : "
							+ cur.diseaseC);
					System.out.println(LocationParentChild.get(LocationRoot));
					System.out.println();
					writeDisease(cur.diseaseC);
				} else if (cur.part > 3) {
					HashSet<String> locations = new HashSet<String>(
							cur.locationsP);
					postQues(cur.diseaseC, locations, true);
					System.out.println("start multiple question : "
							+ cur.diseaseC);
					System.out.println(locations);
					System.out.println();
					writeDisease(cur.diseaseC);
				} else {
					diseaseLocationFinal.put(cur.diseaseC, cur.locationsP);
					String finalLocationString = "";
					if (cur.locationsP.size() > 0) {
						Iterator<String> itrFinal = cur.locationsP.iterator();
						finalLocationString = itrFinal.next();
						while (itrFinal.hasNext()) {
							finalLocationString = finalLocationString + "!"
									+ itrFinal.next();
						}
					}
					BasicDBObject finalLocationsObj = new BasicDBObject("type",
							"diseaseFinalLocation").append("disease",
							cur.diseaseC).append("location",
							finalLocationString);
					coll.insert(finalLocationsObj);
				}

			}
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void startNextDisease(boolean finalize) {
		HashSet<String> parents = findNextDiseases(finalize);
		Iterator<String> itr = parents.iterator();
		while (itr.hasNext()) {
			String nextParent = itr.next();
			HashSet<String> nextDiseases = ICDParentChild.get(nextParent);
			Iterator<String> itrNextDiseases = nextDiseases.iterator();

			while (itrNextDiseases.hasNext()) {
				String nextDisease = itrNextDiseases.next();
				writeDisease(nextDisease);
				if (diseaseLocationFinal.get(nextParent).size() != 0) {
					TestTransitionQuestion cur = new TestTransitionQuestion(
							nextParent, diseaseLocationFinal.get(nextParent),
							nextDisease, diseaseDefinition.get(nextDisease),
							diseaseWikiLink.get(nextDisease));
					cur.postQuestion();
					cur.writeCurQues(exp);
					System.out.println("test same question : " + nextDisease);
					System.out.println(diseaseLocationFinal.get(nextParent));
					System.out.println();
				} else {
					LocationRoot = "root";
					postQues(nextDisease, LocationParentChild.get(LocationRoot),
							false);
					postQues(nextDisease, LocationParentChild.get("Body Systems"),
							false);
					postQues(nextDisease, LocationParentChild.get("Body Regions"),
							false);
					postQues(nextDisease, LocationParentChild.get("Body Organ"),
							false);
					System.out.println("start multiple question : "
							+ nextDisease);
					System.out.println(LocationParentChild.get(LocationRoot));
					System.out.println();
				}
			}
		}
	}

	public static HashSet<String> findNextDiseases(boolean finalize) {
		HashSet<String> output = new HashSet<String>();
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject query = new BasicDBObject("type", "curDisease");
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String disease = (String) obj.get("disease");
				BasicDBObject ques = new BasicDBObject("type", "curQuestion")
						.append("disease", disease);
				if (coll.count(ques) != 0) {
					continue;
				}
				ques = new BasicDBObject("type", "curTestSameQuestion").append(
						"disease", disease);
				if (coll.count(ques) != 0) {
					continue;
				}
				// ques = new BasicDBObject("type", "curOtherQuestion").append(
				// "disease", disease);
				// if (coll.count(ques) != 0) {
				// continue;
				// }
				if (finalize) {
					finalizeDiseaseLocations(disease);
				} else {
					removeLocation(disease);
				}
				output.add(disease);
			}
			cursor.close();
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public static void finalizeDiseaseLocations(String disease) {
		HashSet<String> finalLocations = new HashSet<String>();
		HashSet<String> locations = new HashSet<String>();
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject toRemove = new BasicDBObject("type", "curDisease")
					.append("disease", disease);
			coll.remove(toRemove);
			BasicDBObject query = new BasicDBObject("type", "diseaseLocation")
					.append("disease", disease).append("result", 1);
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String location = (String) obj.get("location");
				locations.add(location);
			}
			cursor.close();

			Iterator<String> itr = locations.iterator();
			HashSet<String> ignoreList = new HashSet<String>();
			while (itr.hasNext()) {
				String location = itr.next();
				if (ignoreList.contains(location)) {
					continue;
				}

				HashSet<String> children = LocationParentChild.get(location);
				if (children == null || children.size() == 0) {
					finalLocations.add(location);
				} else if (locations.containsAll(children)) {
					finalLocations.add(location);
					finalLocations.removeAll(children);
					ignoreList.addAll(children);
				}
			}
			diseaseLocationFinal.put(disease, finalLocations);
			String finalLocationString = "";
			if (finalLocations.size() > 0) {
				Iterator<String> itrFinal = finalLocations.iterator();
				finalLocationString = itrFinal.next();
				while (itrFinal.hasNext()) {
					finalLocationString = finalLocationString + "!"
							+ itrFinal.next();
				}
			}
			BasicDBObject finalLocationsObj = new BasicDBObject("type",
					"diseaseFinalLocation").append("disease", disease).append(
					"location", finalLocationString);
			coll.insert(finalLocationsObj);
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void LocationHierarchy() {
		Iterator<MultipleQuestion> itr = diseaseCurQues.iterator();
		while (itr.hasNext()) {
			MultipleQuestion cur = itr.next();
			HashMap<String, Integer> ans = Interface.getAssignmentsForMutiple(
					cur.HITID, cur.locations);
			if (ans.get("total") != 10) {
				continue;
			}
			int total = ans.get("total");
			int none = ans.get("none");
			ans.remove("total");
			ans.remove("none");
			cur.removeFromCurList(exp);
			cur.locResults = ans;
			cur.total = total;
			cur.none = none;
			cur.writeFinishedQues(exp);

			Iterator<String> itrLoc = ans.keySet().iterator();
			while (itrLoc.hasNext()) {
				String loc = itrLoc.next();
				if (ans.get(loc) > 3) {
					update(cur.disease, loc, 1);
				} else {
					update(cur.disease, loc, 0);
				}
			}
			if (cur.fromParent) {
				Iterator<String> itrNext = cur.locations.iterator();
				HashMap<String, Integer> results = diseaseLocation
						.get(cur.disease);
				while (itrNext.hasNext()) {
					String loc = itrNext.next();
					if (results.get(loc) == 1
							&& LocationParentChild.containsKey(loc)) {
						HashSet<String> locations = new HashSet<String>(
								LocationParentChild.get(loc));
						postQues(cur.disease, locations, false);
					}
				}
			} else if (moreQues(cur)) {
				String parent = cur.locations.iterator().next();
				parent = LocationChildParent.get(parent);
				HashMap<String, Integer> results = diseaseLocation
						.get(cur.disease);
				Iterator<String> itrNext = LocationParentChild.get(parent)
						.iterator();
				while (itrNext.hasNext()) {
					String loc = itrNext.next();
					if (results.get(loc) == 1
							&& LocationParentChild.containsKey(loc)) {
						HashSet<String> locations = new HashSet<String>(
								LocationParentChild.get(loc));
						postQues(cur.disease, locations, false);
					}
				}
			}
		}
	}

	public static boolean moreQues(MultipleQuestion ques) {
		String parent = ques.locations.iterator().next();
		parent = LocationChildParent.get(parent);
		HashMap<String, Integer> results = diseaseLocation.get(ques.disease);
		Set<String> siblings = LocationParentChild.get(parent);
		if (!results.keySet().containsAll(siblings)) {
			return false;
		}
		Iterator<String> itr = siblings.iterator();
		while (itr.hasNext()) {
			String loc = itr.next();
			if (results.get(loc) == 0) {
				return true;
			}
		}
		return false;
	}

	public static void postQues(String disease, HashSet<String> locations,
			boolean fromParent) {
		if (disease.matches("Trichostasis spinulosa")) {
			System.out.println();
		}
		while (locations.size() > 7) {
			HashSet<String> curLocations = new HashSet<String>();
			Iterator<String> itr = locations.iterator();
			for (int i = 0; i < 5; i++) {
				curLocations.add(itr.next());
			}
			MultipleQuestion cur = new MultipleQuestion(disease, curLocations,
					diseaseDefinition.get(disease),
					diseaseWikiLink.get(disease));
			cur.postQuestion();
			cur.writeCurQues(exp);
			locations.removeAll(curLocations);
		}
		MultipleQuestion cur = new MultipleQuestion(disease, locations,
				diseaseDefinition.get(disease), diseaseWikiLink.get(disease));
		cur.fromParent = fromParent;
		cur.postQuestion();
		cur.writeCurQues(exp);
	}

	public static void update(String disease, String location, int result) {
		if (diseaseLocation.containsKey(disease)) {
			HashMap<String, Integer> locations = diseaseLocation.get(disease);
			locations.put(location, result);
		} else {
			HashMap<String, Integer> locations = new HashMap<String, Integer>();
			locations.put(location, result);
			diseaseLocation.put(disease, locations);
		}
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject obj = new BasicDBObject("type", "diseaseLocation")
					.append("disease", disease).append("location", location)
					.append("result", result);
			coll.insert(obj);
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readHierarchyData() throws Exception {
		readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/ICDStrcuture.txt");
		readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure2.txt");
		readDefAndWiki("/Users/Yun/Dropbox/medical/workspace/turk/DiseaseDefiAndWiki.txt");
	}

	public static void readDefAndWiki(String path) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(path));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			String disease = strLine;
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

	public static void readICDHierarchy(String path) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));

			String strLine;
			while ((strLine = in.readLine()) != null) {
				String parent = strLine;
				strLine = in.readLine();
				if (strLine.length() == 0) {
					ICDParentChild.put(parent, new HashSet<String>());
					in.readLine();
					continue;
				}
				String[] children = strLine.split("!");
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
						if (children[i].length() == 0) {
							System.out.println();
						}
						ICDChildParent.put(children[i], tmp);
					}
				}
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readData() {
		readDiseaseLocation();
		readDiseaseCurQues();
		readSameCurQues();
		// readOtherCurQues();
	}

	private static void readSameCurQues() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type",
					"curTestSameQuestion");
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String diseaseC = (String) obj.get("disease");
				String diseaseP = (String) obj.get("diseaseParent");
				String HITID = (String) obj.get("HITID");
				String locString = (String) obj.get("locations");
				String locations[] = locString.split("!");
				HashSet<String> locSet = new HashSet<String>();
				for (int i = 0; i < locations.length; i++) {
					locSet.add(locations[i]);
				}

				TestTransitionQuestion cur = new TestTransitionQuestion(
						diseaseP, locSet, diseaseC,
						diseaseDefinition.get(diseaseC),
						diseaseWikiLink.get(diseaseC));
				cur.HITID = HITID;
				diseaseTestSameCurQues.add(cur);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readDiseaseCurQues() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type", "curQuestion");
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String disease = (String) obj.get("disease");
				String HITID = (String) obj.get("HITID");
				String fromP = (String) obj.get("fromParent");
				String locString = (String) obj.get("locations");
				String locations[] = locString.split("!");
				HashSet<String> locSet = new HashSet<String>();
				for (int i = 0; i < locations.length; i++) {
					locSet.add(locations[i]);
				}
				MultipleQuestion cur = new MultipleQuestion(disease, locSet,
						diseaseDefinition.get(disease),
						diseaseWikiLink.get(disease));
				if (fromP != null) {
					cur.fromParent = true;
				}
				cur.HITID = HITID;
				diseaseCurQues.add(cur);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readDiseaseLocation() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject query = new BasicDBObject("type", "diseaseLocation");
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String disease = (String) obj.get("disease");
				String location = (String) obj.get("location");
				int result = (int) obj.get("result");
				if (diseaseLocation.containsKey(disease)) {
					HashMap<String, Integer> curLocations = diseaseLocation
							.get(disease);
					curLocations.put(location, result);
				} else {
					HashMap<String, Integer> curLocations = new HashMap<String, Integer>();
					curLocations.put(location, result);
					diseaseLocation.put(disease, curLocations);
				}
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}