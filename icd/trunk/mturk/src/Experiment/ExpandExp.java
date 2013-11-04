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

public class ExpandExp {
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

	public static double totalCost;

	public static void main(String[] args) {
		Hierarchy.totalCost = 0;
		exp = 233; //previous 204, 214, 221,stuck?  230
		readHierarchyData();
		Interface.readWiki();
		Interface.LocationParentChild = LocationParentChild;
//		ICDRoot = "Trichostasis spinulosa";
//		LocationRoot = "root";
//		postQues(ICDRoot,LocationParentChild.get(LocationRoot),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Systems"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Regions"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Organ"),false);
//		
//		ICDRoot = "Alopecia areata of eyelashes";
//		LocationRoot = "root";
//		postQues(ICDRoot,LocationParentChild.get(LocationRoot),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Systems"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Regions"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Organ"),false);
//		
//		ICDRoot = "Drug-induced hair colour change";
//		LocationRoot = "root";
//		postQues(ICDRoot,LocationParentChild.get(LocationRoot),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Systems"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Regions"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Organ"),false);
//		
//		ICDRoot = "Heterotopic sebaceous glands";
//		LocationRoot = "root";
//		postQues(ICDRoot,LocationParentChild.get(LocationRoot),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Systems"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Regions"),false);
//		postQues(ICDRoot,LocationParentChild.get("Body Organ"),false);
		
		readData();
		LocationHierarchy();
//		finalizeDiseaseLocations("Disorders of hair and the hair follicle");
		System.out.println("total cost:" + Hierarchy.totalCost);
		System.out.println("done");
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

			if (cur.disease.matches("Trichostasis spinulosa")) {
				System.out.println();
			}
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

	public static void readData() {
		readDiseaseLocation();
		readDiseaseCurQues();
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

	public static void readHierarchyData() {
		readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/ICDStrcuture.txt");
		try {
			readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure2.txt");
			readDefAndWiki("/Users/Yun/Dropbox/medical/workspace/turk/DiseaseDefiAndWiki.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
