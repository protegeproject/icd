package Experiment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class WorkerBehavior {
	public static long totalTime;
	public static long totalQuestion;
	public static int timeDistribution[] = new int[61];

	public static HashMap<String, Worker> workers = new HashMap<String, Worker>();
	public static int exp;
	public static MyRequesterService service = new MyRequesterService(
			new PropertiesClientConfig(
					"/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));

	public static void main(String[] args) {
		exp = 2020;
		checkMultipleQuestions();
		printAccuracy();
		System.out.println("average time:" + totalTime / totalQuestion);
//		notifyWorkers();
	}

	public static void notifyWorkers() {
		Iterator<String> itr = workers.keySet().iterator();
		while (itr.hasNext()) {
			Worker worker = workers.get(itr.next());
			if (worker.getAccuracy() > 0.7) {
				String workerId[] = { worker.id };
				service.notifyWorkers(
						"hi",
						"Thank you very much for finishing medical related HITs for yunlou. Your accuracy rate is high. I just created a new accout 'Stanford Biomedical Informatics Research' and I will post same kind of question with the new account. Looking forward to working with you. Best wishes.",
						workerId);
				System.out.println("successfully notified "+ worker.id );
			}

		}
	}

	public static void printAccuracy() {
		int distribution[] = new int[50];
		System.out.println(workers.size());
		int mem = 0;
		Iterator<String> itr = workers.keySet().iterator();
		while (itr.hasNext()) {
			String id = itr.next();
			Worker cur = workers.get(id);
			if (cur.numMutiple > 0) {
				mem++;
				int index = (int) (cur.getAccuracy() / 0.02);
				distribution[index]++;
				System.out.print(cur.id);
				System.out.print(" " + cur.getAccuracy());
				System.out.print(" " + cur.numMutiple);
				System.out.println();
			}
		}
		System.out
				.println("total number of workers finished more than 3 questions:"
						+ mem);
		for (int i = 0; i < distribution.length; i++) {
			System.out.print("," + distribution[i]);
		}
		System.out.println();
		System.out.println("time distribution:");
		for (int i = 0; i < timeDistribution.length; i++) {
			System.out.print("," + timeDistribution[i]);
		}
		System.out.println();
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
					Worker cur = workers.get(workerId);
					if (!workers.containsKey(workerId)) {
						cur = new Worker(workerId);
						workers.put(workerId, cur);
					}
					cur.numMutiple++;
					HashSet<String> workerLoc = result.get(workerId);
					cur.numLocation += locations.size();
					Iterator<String> itrLoc = locations.iterator();
					while (itrLoc.hasNext()) {
						String loc = itrLoc.next();
						if (workerLoc.contains(loc)
								&& rightLocations.contains(loc)) {
							cur.TPLocation++;
						}
						if (!workerLoc.contains(loc)
								&& !rightLocations.contains(loc)) {
							cur.TNLocation++;
						}
					}
				}
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
