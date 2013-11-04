package Exp3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

public class Hierarchy2 {
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
	public static HashSet<MultipleQuestion> diseaseCurQuesWithOther = new HashSet<MultipleQuestion>();

	public static double totalCost;
	public static int totalMulQuestion;
	public static int totalSameQuestion;
	public static int totalOtherQuestion;
	public static int totalCurDisease;
	public static int allCutOff = 4;
	public static int optionCutOff = 5;
	public static int otherCutOff = 4;
	public static int noneCutOff = 6;

	public static void main(String agrs[]) throws Exception {
		run();
	}

	public static void run() {
		ICDParentChild = new HashMap<String, HashSet<String>>();
		ICDChildParent = new HashMap<String, HashSet<String>>();

		LocationParentChild = new HashMap<String, HashSet<String>>();
		LocationChildParent = new HashMap<String, String>();
		diseaseDefinition = new HashMap<String, String>();
		diseaseWikiLink = new HashMap<String, String>();

		diseaseLocation = new HashMap<String, HashMap<String, Integer>>();
		diseaseLocationFinal = new HashMap<String, HashSet<String>>();
		diseaseCurQues = new HashSet<MultipleQuestion>();
		diseaseCurQuesWithOther = new HashSet<MultipleQuestion>();

		totalCost = 0;
		exp = 2020;// 2002, 2011
		readHierarchyData();
		Interface.readWiki();
		Interface.LocationParentChild = LocationParentChild;

//		ICDRoot = "Inflammatory disorders of eyelid";
//		writeDisease(ICDRoot);
//		postQues(ICDRoot, LocationParentChild.get("body regions"), false);
//		postQues(ICDRoot, LocationParentChild.get("body organs"), false);
//
//		ICDRoot = "Acquired disorders of eyelashes";
//		writeDisease(ICDRoot);
//		postQues(ICDRoot, LocationParentChild.get("body regions"), false);
//		postQues(ICDRoot, LocationParentChild.get("body organs"), false);
//
//		ICDRoot = "Disorders of lips";
//		writeDisease(ICDRoot);
//		postQues(ICDRoot, LocationParentChild.get("body regions"), false);
//		postQues(ICDRoot, LocationParentChild.get("body organs"), false);
//
//		ICDRoot = "Disturbances of oral epithelium";
//		writeDisease(ICDRoot);
//		postQues(ICDRoot, LocationParentChild.get("body regions"), false);
//		postQues(ICDRoot, LocationParentChild.get("body organs"), false);
//
//		ICDRoot = "Lichen planus and lichenoid reactions of oral mucosa";
//		writeDisease(ICDRoot);
//		postQues(ICDRoot, LocationParentChild.get("body regions"), false);
//		postQues(ICDRoot, LocationParentChild.get("body organs"), false);
//
//		ICDRoot = "Non-infective erosive and ulcerative disorders of oral mucosa";
//		writeDisease(ICDRoot);
//		postQues(ICDRoot, LocationParentChild.get("body regions"), false);
//		postQues(ICDRoot, LocationParentChild.get("body organs"), false);

		readDiseaseLocation();
		readDiseaseCurQues();

		LocationHierarchy();
		DiseasesHierarchy();
		System.out.println("total cost:" + totalCost);
		System.out.println("done");
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

	public static void writeDisease(String disease) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject ques = new BasicDBObject("type", "curDisease")
					.append("disease", disease);
			coll.insert(ques);

			// BasicDBObject obj = new BasicDBObject("type", "diseaseLocation")
			// .append("disease", disease)
			// .append("location", "body regions").append("result", 1);
			// coll.insert(obj);
			//
			// obj = new BasicDBObject("type", "diseaseLocation")
			// .append("disease", disease)
			// .append("location", "body organs").append("result", 1);
			// coll.insert(obj);

			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void DiseasesHierarchy() {
		startNextDisease(true);
		otherQuestions();
		startNextDisease(false);
	}

	public static void calculateBonus(
			HashMap<String, HashMap<String, HashSet<String>>> userVotes,
			HashSet<String> rightAnswers) {
		Iterator<String> itr = userVotes.keySet().iterator();
		rightAnswers.remove("all");
		while (itr.hasNext()) {
			String workerID = itr.next();
			HashMap<String, HashSet<String>> questions = userVotes
					.get(workerID);
			Iterator<String> itrAssign = questions.keySet().iterator();
			while (itrAssign.hasNext()) {
				String assignmentID = itrAssign.next();
				HashSet<String> answer = questions.get(assignmentID);
				if (answer.containsAll(rightAnswers)
						&& rightAnswers.containsAll(answer)) {
					Bonus bonus = new Bonus(workerID, assignmentID, 0.02);
					bonus.writeBonus(exp);
				}
			}
		}
	}

	public static void otherQuestions() {
		Iterator<MultipleQuestion> itr = diseaseCurQuesWithOther.iterator();
		while (itr.hasNext()) {
			MultipleQuestion cur = itr.next();
			System.out.println();
			System.out.println("other multiple-choice question disease:"
					+ cur.disease);
			System.out.println(cur.locations);
			if (cur.disease.matches("Loose anagen syndrome")
					|| cur.disease.matches("Familial premature canities")
					|| cur.disease.matches("Pili torti")) {
				System.out.println();
			}
			MultipleAnswer output = Interface.getAssignmentsForMutiple(
					cur.HITID, cur.locations);

			HashMap<String, Integer> ans = output.locVotes;
			if (ans.get("total") != 10) {
				continue;
			}

			int total = ans.get("total");
			int none = ans.get("none");
			if (!ans.containsKey("other")) {
				System.out.println();
			}
			int other = ans.get("other");
			ans.remove("total");
			ans.remove("none");
			ans.remove("all");
			ans.remove("other");
			cur.removeFromCurList(exp);
			cur.locResults = ans;
			cur.total = total;
			cur.other = other;
			cur.none = none;
			cur.writeFinishedQues(exp);

			HashSet<String> rightAnswers = new HashSet<String>();
			Iterator<String> itrLoc = ans.keySet().iterator();
			while (itrLoc.hasNext()) {
				String loc = itrLoc.next();
				if (ans.get(loc) >= optionCutOff) {
					if (!LocationParentChild.containsKey(loc)
							|| LocationParentChild.get(loc).size() == 0) {
						update(cur.disease, loc, 1);
					}
					rightAnswers.add(loc);
					if (LocationParentChild.containsKey(loc)
							&& cur.other < otherCutOff) {
						HashSet<String> locations = new HashSet<String>(
								LocationParentChild.get(loc));
						postQues(cur.disease, locations, false);
					}
				} else {
					update(cur.disease, loc, 0);
				}
			}

			calculateBonus(output.userVotes, rightAnswers);

			if (cur.other >= otherCutOff) {
				postQues(cur.disease, LocationParentChild.get("body regions"),
						false);
				postQues(cur.disease, LocationParentChild.get("body organs"),
						false);

				System.out.println("start multiple question : " + cur.disease);
				System.out.println();
			}
			// } else {
			// postQuestions(cur);//
			// }
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
				if (diseaseLocationFinal.containsKey(nextParent)
						&& diseaseLocationFinal.get(nextParent).size() != 0
						&& !diseaseLocationFinal.get(nextParent).contains(
								"body regions")) {
					HashSet<String> locations = diseaseLocationFinal
							.get(nextParent);
					locations.add("other");

					String def = diseaseDefinition.get(nextDisease);
					if (def == null) {
						def = "none";
					}
					String wiki = diseaseWikiLink.get(nextDisease);
					if (wiki == null) {
						wiki = "none";
					}
					MultipleQuestion cur = new MultipleQuestion(nextDisease,
							locations, def, wiki);
					cur.other = 0;
					cur.fromParent = true;
					cur.postQuestion();
					cur.writeCurQues(exp);
					System.out.println("other multiple-choice question : "
							+ nextDisease);
					System.out.println(diseaseLocationFinal.get(nextParent));
					System.out.println();
				} else {
					postQues(nextDisease,
							LocationParentChild.get("body regions"), false);
					postQues(nextDisease,
							LocationParentChild.get("body organs"), false);
					System.out.println("start multiple-choice question : "
							+ nextDisease);
					System.out.println(LocationParentChild.get("body regions")
							+ " " + LocationParentChild.get("body organs"));
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
				// ques = new BasicDBObject("type",
				// "curTestSameQuestion").append(
				// "disease", disease);
				// if (coll.count(ques) != 0) {
				// continue;
				// }
				// ques = new BasicDBObject("type", "curOtherQuestion").append(
				// "disease", disease);
				// if (coll.count(ques) != 0) {
				// continue;
				// }
				finalizeDiseaseLocations(disease);

				// if (finalize) {
				// finalizeDiseaseLocations(disease);
				// } else {
				// removeLocation(disease);
				// }
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

			// Iterator<String> itr = locations.iterator();
			// HashSet<String> ignoreList = new HashSet<String>();
			// while (itr.hasNext()) {
			// String location = itr.next();
			//
			// if (ignoreList.contains(location)) {
			// continue;
			// }
			//
			// HashSet<String> children = LocationParentChild.get(location);
			// if (children == null || children.size() == 0) {
			// finalLocations.add(location);
			// } else if (locations.containsAll(children)) {
			// finalLocations.add(location);
			// finalLocations.removeAll(children);
			// ignoreList.addAll(children);
			// }
			// }
			finalLocations = new HashSet<String>(locations);
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
			System.out.println();
			System.out.println(cur.disease);
			MultipleAnswer output = Interface.getAssignmentsForMutiple(
					cur.HITID, cur.locations);
			HashMap<String, Integer> ans = output.locVotes;
			// if(ans.get("total") ==0)
			// cur.removeFromCurList(exp);
			if (ans.get("total") != 10) {
				continue;
			}
			int total = ans.get("total");
			int none = ans.get("none");
			int all = ans.get("all");
			ans.remove("total");
			ans.remove("none");
			ans.remove("all");
			cur.removeFromCurList(exp);
			cur.locResults = ans;
			cur.total = total;
			cur.none = none;
			cur.all = all;
			cur.writeFinishedQues(exp);

			HashSet<String> rightAnswers = new HashSet<String>();
			if (cur.all >= allCutOff) {
				Iterator<String> itrLoc = ans.keySet().iterator();
				String child = itrLoc.next();
				rightAnswers.add(child);
				String parent = LocationChildParent.get(child);
				update(cur.disease, parent, 1);
				while (itrLoc.hasNext()) {
					String loc = itrLoc.next();
					rightAnswers.add(loc);
				}
			} else if (cur.none >= noneCutOff) {
				Iterator<String> itrLoc = ans.keySet().iterator();
				while (itrLoc.hasNext()) {
					String loc = itrLoc.next();
					update(cur.disease, loc, 0);
				}
			} else {
				Iterator<String> itrLoc = ans.keySet().iterator();
				while (itrLoc.hasNext()) {
					String loc = itrLoc.next();
					if (ans.get(loc) >= optionCutOff) {
						if (!LocationParentChild.containsKey(loc)
								|| LocationParentChild.get(loc).size() == 0) {
							update(cur.disease, loc, 1);
						}
						rightAnswers.add(loc);
						if (LocationParentChild.containsKey(loc)) {
							HashSet<String> locations = new HashSet<String>(
									LocationParentChild.get(loc));
							postQues(cur.disease, locations, false);
						}
					} else {
						update(cur.disease, loc, 0);
					}
				}
				// postQuestions(cur);
			}
			calculateBonus(output.userVotes, rightAnswers);
		}
	}

	// public static void postQuestions(MultipleQuestion cur) {
	// String parent = cur.locations.iterator().next();
	// parent = LocationChildParent.get(parent);
	// HashMap<String, Integer> results = diseaseLocation.get(cur.disease);
	// Iterator<String> itrNext = LocationParentChild.get(parent)
	// .iterator();
	// while (itrNext.hasNext()) {
	// String loc = itrNext.next();
	// if (results.get(loc) == 1
	// && LocationParentChild.containsKey(loc)) {
	// HashSet<String> locations = new HashSet<String>(
	// LocationParentChild.get(loc));
	// postQues(cur.disease, locations, false);
	// }
	// }
	// }

	public static void postQues(String disease, HashSet<String> locations,
			boolean fromParent) {
		while (locations.size() > 20) {
			HashSet<String> curLocations = new HashSet<String>();
			Iterator<String> itr = locations.iterator();
			for (int i = 0; i < 6; i++) {
				curLocations.add(itr.next());
			}
			String def = diseaseDefinition.get(disease);
			if (def == null) {
				def = "none";
			}
			String wiki = diseaseWikiLink.get(disease);
			if (wiki == null) {
				wiki = "none";
			}
			MultipleQuestion cur = new MultipleQuestion(disease, curLocations,
					def, wiki);
			cur.postQuestion();
			cur.writeCurQues(exp);
			locations.removeAll(curLocations);
		}
		String def = diseaseDefinition.get(disease);
		if (def == null) {
			def = "none";
		}
		String wiki = diseaseWikiLink.get(disease);
		if (wiki == null) {
			wiki = "none";
		}

		MultipleQuestion cur = new MultipleQuestion(disease, locations, def,
				wiki);
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

	public static void readHierarchyData() {
		readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/SampleICDStrcuture3.txt");
		readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct2.txt");
		readDefAndWiki("/Users/Yun/Dropbox/medical/workspace/turk/DiseaseDefiAndWiki.txt");
	}

	public static void readDefAndWiki(String path) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));

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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readLocationHierarchy(String path) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String parent = strLine;
				strLine = in.readLine();
				String[] children = strLine.split("!");
				// if(children.length>7){
				// System.out.println(parent+children.length);
				// }
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				String def = diseaseDefinition.get(disease);
				if (def == null) {
					def = "none";
				}
				String wiki = diseaseWikiLink.get(disease);
				if (wiki == null) {
					wiki = "none";
				}
				Integer other = (Integer) obj.get("other");
				if (other != -1 && !locSet.contains("other")) {
					locSet.add("other");
				}

				MultipleQuestion cur = new MultipleQuestion(disease, locSet,
						def, wiki);
				if (fromP != null) {
					cur.fromParent = true;
				}
				cur.HITID = HITID;

				if (other == -1) {
					diseaseCurQues.add(cur);
				} else {
					diseaseCurQuesWithOther.add(cur);
				}
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