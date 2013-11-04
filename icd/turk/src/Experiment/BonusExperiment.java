//package Experiment;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//
//import com.mongodb.BasicDBObject;
//import com.mongodb.DB;
//import com.mongodb.DBCollection;
//import com.mongodb.DBCursor;
//import com.mongodb.DBObject;
//import com.mongodb.Mongo;
//
//public class BonusExperiment {
//	public static int exp = -1;
//	
//	public static void main(String[] args){
//		exp = 300;
//		LinkedList<MultipleQuestion> todo = getQuestions();
//		PostQuestions(todo);
//		getResults();
//	}
//	
//	public static void getQuestions() {
//		try {
//			Mongo mongo = new Mongo("localhost", 27017);
//			DB db = mongo.getDB("mydb");
//			DBCollection coll = db.getCollection("exp" + exp);
//
//			BasicDBObject query = new BasicDBObject("type", "doneQuestion");
//			DBCursor cursor = coll.find(query);
//			int i = 0;
//			while (cursor.hasNext() && i < 1) {
//				i++;
//				DBObject obj = cursor.next();
//				// String disease = (String) obj.get("disease");
//				String HITID = (String) obj.get("HITID");
//				DBObject locationObjs = (DBObject) obj.get("locationResults");
//				HashSet<String> locations = new HashSet<String>();
//				HashSet<String> rightLocations = new HashSet<String>();
//				Iterator<String> itr = locationObjs.keySet().iterator();
//				while (itr.hasNext()) {
//					String loc = itr.next();
//					locations.add(loc);
//					if ((int) locationObjs.get(loc) > 3) {
//						rightLocations.add(loc);
//					}
//				}
//				HashMap<String, HashSet<String>> result = Interface
//						.evaluateAssignmentsForMutiple(HITID, locations);
//				Iterator<String> itrWorker = result.keySet().iterator();
//				while (itrWorker.hasNext()) {
//					String workerId = itrWorker.next();
//					Worker cur = workers.get(workerId);
//					if (!workers.containsKey(workerId)) {
//						cur = new Worker(workerId);
//						workers.put(workerId, cur);
//					}
//					cur.numMutiple++;
//					HashSet<String> workerLoc = result.get(workerId);
//					cur.numLocation += locations.size();
//					Iterator<String> itrLoc = locations.iterator();
//					while (itrLoc.hasNext()) {
//						String loc = itrLoc.next();
//						if (workerLoc.contains(loc)
//								&& rightLocations.contains(loc)) {
//							cur.TPLocation++;
//						}
//						if (!workerLoc.contains(loc)
//								&& !rightLocations.contains(loc)) {
//							cur.TNLocation++;
//						}
//					}
//				}
//			}
//			cursor.close();
//			mongo.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//}
//
//class Bonus{
//	String workerId;
//	String assignId;
//	String HITId;
//	HashSet<String> locations;
//	public Bonus(String workerId, String assignId, String HITId, HashSet<String> locations){
//		this.workerId = workerId;
//		this.assignId = assignId;
//		this.HITId = HITId;
//		this.locations = locations;
//	}
//}
