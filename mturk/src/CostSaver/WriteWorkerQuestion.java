package CostSaver;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import Experiment.Interface;
import Experiment.Worker;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class WriteWorkerQuestion {
	public static int exp = 2011;
	
	public static void main(String[] args) {
		checkMultipleQuestions();
	}
	
	public static void checkMultipleQuestions() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type", "doneQuestion");
			DBCursor cursor = coll.find(query);
			int i = 0;
			while (cursor.hasNext() && i < 2000) {
				i++;
				DBObject obj = cursor.next();
				// String disease = (String) obj.get("disease");
				String HITID = (String) obj.get("HITID");
				DBObject locationObjs = (DBObject) obj.get("locationResults");
				HashSet<String> locations = new HashSet<String>();
				HashSet<String> rightLocations = new HashSet<String>();
				Iterator<String> itr = locationObjs.keySet().iterator();
				while (itr.hasNext()) {
					String loc = itr.next();
					locations.add(loc);
					if ((int) locationObjs.get(loc) > 4) {
						rightLocations.add(loc);
					}
				}
				HashMap<String, HashSet<String>> result = Interface
						.evaluateAssignmentsForMutiple(HITID, locations);
				
				Iterator<String> itrWorker = result.keySet().iterator();
				while (itrWorker.hasNext()) {
					String workerId = itrWorker.next();
					HashSet<String> workerLoc = result.get(workerId);
					String locString = "";
					Iterator<String> itr2 = workerLoc.iterator();
					if(itr2.hasNext()) {
						String loc = itr2.next();
						if(loc.matches("all")) {
							System.out.println();
						}
						locString=loc;
					}
					while(itr2.hasNext()) {
						String loc = itr2.next();
						if(loc.matches("all")) {
							System.out.println();
						}
						locString = locString + "!"+ loc;
					}
					
					writeAnswer(HITID, workerId, locString);
				}
				
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
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void writeAnswer(String HITID, String workerID, String locString){
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject ques = new BasicDBObject("type", "workerAnswer").append("HITID", HITID).append(
					"workerID", workerID).append("locString", locString);
			coll.insert(ques);
			mongo.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
