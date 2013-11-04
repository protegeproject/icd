package CostSaver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import Experiment.Worker;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class exp {
	public static HashMap<String, Worker> workers = new HashMap<String, Worker>();
	public static double P_all = 0.707;
	public static double P_none = 0.0405;
	public static double P_other = 0.107;
	public static double P_loc = 0.692;
	public static int trainning_exp = 2020;
	public static int test_exp=2011;

	public static double all_cutoff = 0.9;
	public static double all_cutoff_end = 0.4;
//	public static double all_cutoff_low = 0.2;
	public static double other_cutoff = 0.3;
	public static double other_cutoff_end = 0.15;
	public static double other_cutoff_low = 0.05;
	public static double none_cutoff = 0.8;
	public static double none_cutoff_end = 0.5;
//	public static double none_cutoff_low = 0.2;
	public static double loc_cutoff = 0.85;
	public static double loc_cutoff_low = 0.05;
	public static double loc_cutoff_end = 0.5;
	
	public static void main(String[] args) {
		learn();
		simulate();
	}

	public static void simulate() {
		int responses = 0;
		int questions = 0;
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + test_exp);

			BasicDBObject query = new BasicDBObject("type", "doneQuestion");
			DBCursor cursor = coll.find(query);
			int i = 0;
			while (cursor.hasNext() && i < 1000 && questions<100) {
				i++;
				questions++;
				System.out.println("question id:"+questions);
				DBObject obj = cursor.next();
				String disease = (String) obj.get("disease");
				System.out.print(disease + ":");
				String HITID = (String) obj.get("HITID");
				HashMap<String, Double> P_locs = new HashMap<String, Double>();
				DBObject locationObjs = (DBObject) obj.get("locationResults");
				Iterator<String> itr = locationObjs.keySet().iterator();
				while (itr.hasNext()) {
					String loc = itr.next();
					System.out.print(loc + "!");
					P_locs.put(loc, P_loc);
				}
				System.out.println();
				boolean all_question = false;
				if ((int) obj.get("other") == -1) {
					all_question = true;
				}

				if ((int) obj.get("other") >= 3) {
					System.out.println("other");
				} else if ((int) obj.get("all") >= 4) {
					System.out.println("all");
				} else if ((int) obj.get("none") >= 6) {
					System.out.println("none");
				} else {
					locationObjs = (DBObject) obj.get("locationResults");
					Iterator<String> itr2 = locationObjs.keySet().iterator();
					while (itr2.hasNext()) {
						String loc = itr2.next();
						if ((int) locationObjs.get(loc) > 4) {
							System.out.print(loc + "!");
						}
					}
					System.out.println();
				}

				BasicDBObject answer_query = new BasicDBObject("type",
						"workerAnswer").append("HITID", HITID);
				DBCursor cursor_answer = coll.find(answer_query);
				double P_all_cur = P_all;
				double P_none_cur = P_none;
				double P_other_cur = P_other;

				int valid_num = 0;
				int j;
				for (j = 0; j < 10; j++) {
					DBObject answer = cursor_answer.next();
					String workerID = (String) answer.get("workerID");
					double worker_p = workers.get(workerID).getRate();
					if (worker_p < 0.55) {
						continue;
					}
					responses++;
					valid_num++;
					String locString = (String) answer.get("locString");
					String locAnswers[] = locString.split("!");
					HashSet<String> votes = new HashSet<String>();
					for (int k = 0; k < locAnswers.length; k++) {
						String vote = locAnswers[k];
						votes.add(vote);
					}
					if (all_question) {
						if (votes.contains("all")) {
							P_all_cur = P_all_cur
									* worker_p
									/ (P_all_cur * worker_p + (1 - P_all_cur)
											* (1 - worker_p));
						} else {
							P_all_cur = P_all_cur
									* (1 - worker_p)
									/ (P_all_cur * (1 - worker_p) + (1 - P_all_cur)
											* worker_p);
						}
						if (votes.contains("none")) {
							P_none_cur = P_none_cur
									* worker_p
									/ (P_none_cur * worker_p + (1 - P_none_cur)
											* (1 - worker_p));
						} else {
							P_none_cur = P_none_cur
									* (1 - worker_p)
									/ (P_none_cur * (1 - worker_p) + (1 - P_none_cur)
											* worker_p);
						}
					} else {
						if (votes.contains("other")) {
							P_other_cur = P_other_cur
									* worker_p
									/ (P_other_cur * worker_p + (1 - P_other_cur)
											* (1 - worker_p));
						} else {
							P_other_cur = P_other_cur
									* (1 - worker_p)
									/ (P_other_cur * (1 - worker_p) + (1 - P_other_cur)
											* worker_p);
						}
					}

					int done = 0;
					int pos = 0;
					Iterator<String> loc_itr = P_locs.keySet().iterator();
					String results = "";
					while (loc_itr.hasNext()) {
						String loc = loc_itr.next();
						double P_loc = P_locs.get(loc);
						if (votes.contains(loc)) {
							P_loc = P_loc
									* worker_p
									/ (P_loc * worker_p + (1 - P_loc)
											* (1 - P_loc));
						} else {
							P_loc = P_loc
									* (1 - worker_p)
									/ (P_loc * (1 - worker_p) + (1 - P_loc)
											* worker_p);
						}
						P_locs.put(loc, P_loc);
						if(P_loc>loc_cutoff){
							results = results+loc+ "!";
							done++;
							pos++;
						} else if (P_loc<loc_cutoff_low){
							done++;
						}
					}
					
					if (P_all_cur > all_cutoff) {
						System.out.println("all. stop at " + valid_num);
						System.out.println("P_all_cur:" + P_all_cur
								+ " P_none_cur:" + P_none_cur);
						System.out.println(P_locs);
						break;
					}
					if (P_none_cur > none_cutoff) {
						System.out.println("none. stop at " + valid_num);
						System.out.println("P_all_cur:" + P_all_cur
								+ " P_none_cur:" + P_none_cur);
						System.out.println(P_locs);
						break;
					}
					
					if (P_other_cur > other_cutoff) {
						System.out.println("other. stop at " + valid_num);
						System.out.println("P_other_cur:" + P_other_cur);
						System.out.println(P_locs);
						break;
					}

					if (P_other_cur > other_cutoff/2&&valid_num>=2) {
						System.out.println("other. stop at " + valid_num);
						System.out.println("P_other_cur:" + P_other_cur);
						System.out.println(P_locs);
						break;
					}
					
					if(done==P_locs.size()&&(P_other_cur<other_cutoff_low||P_other_cur==P_other)){
						if(results.matches("")){
							results = "none";
						}
						if(pos==P_locs.size()&&all_question){
							System.out.println("all stop at "+valid_num);
						} else {
							System.out.println(results+" stop at "+valid_num);
						}
						System.out.println("P_all_cur:" + P_all_cur
								+ " P_none_cur:" + P_none_cur+"P_other_cur:" + P_other_cur);
						System.out.println(P_locs);
						break;
					}
					
				}
				//

				if (j == 10) {
					if (P_all_cur > all_cutoff_end) {
						System.out.print("all.");
						System.out.println(" stop at 10");
						System.out.println("P_all_cur:" + P_all_cur
								+ " P_none_cur:" + P_none_cur +" P_other_cur:"+P_other_cur);
						System.out.println(P_locs);
						System.out.println();
						continue;
					}
					if (P_none_cur > none_cutoff_end) {
						System.out.print("none.");
						System.out.println(" stop at 10");
						System.out.println("P_all_cur:" + P_all_cur
								+ " P_none_cur:" + P_none_cur +" P_other_cur:"+P_other_cur);
						System.out.println(P_locs);
						System.out.println();
						continue;
					}
					
					if (P_other_cur > other_cutoff_end) {
						System.out.print("other.");
						System.out.println(" stop at 10");
						System.out.println("P_all_cur:" + P_all_cur
								+ " P_none_cur:" + P_none_cur +" P_other_cur:"+P_other_cur);
						System.out.println(P_locs);
						System.out.println();
						continue;
					}
//					if(disease.matches("Abscess of upper eyelid"))
//						System.out.println();
					Iterator<String> loc_itr = P_locs.keySet().iterator();
					while (loc_itr.hasNext()) {
						String loc = loc_itr.next();
						double P_loc = P_locs.get(loc);
						if(P_loc>loc_cutoff_end){
							System.out.print(loc+"!");
						}
					}
					System.out.println(" stop at 10");
					System.out.println("P_all_cur:" + P_all_cur
							+ " P_none_cur:" + P_none_cur +" P_other_cur:"+P_other_cur);
					System.out.println(P_locs);
				}
				System.out.println();
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("number of questions : "+questions);
		System.out.println("average responses: "+(double)responses/questions);
	}

	public static void learn() {
		checkMultipleQuestions();
		learnPiors();
		// printAccuracy();
	}

	public static void learnPiors() {
		int all_count = 0;
		int none_count = 0;
		int other_count = 0;
		int loc_count = 0;
		double all_pos = 0;
		double none_pos = 0;
		double other_pos = 0;
		double loc_pos = 0;

		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + trainning_exp);

			BasicDBObject query = new BasicDBObject("type", "doneQuestion");
			DBCursor cursor = coll.find(query);
			int i = 0;
			while (cursor.hasNext() && i < 2000) {
				i++;
				DBObject obj = cursor.next();

				if ((int) obj.get("all") == -1) {
					other_count++;
					if ((int) obj.get("other") >= 3) {
						other_pos++;
					}
				}
				if ((int) obj.get("other") == -1) {
					all_count++;
					none_count++;
					if ((int) obj.get("all") >= 4) {
						all_pos++;
					}
					if ((int) obj.get("none") >= 6) {
						none_pos++;
					}
				}

				DBObject locationObjs = (DBObject) obj.get("locationResults");
				loc_count += locationObjs.keySet().size();
				HashSet<String> locations = new HashSet<String>();
				Iterator<String> itr = locationObjs.keySet().iterator();
				while (itr.hasNext()) {
					String loc = itr.next();
					locations.add(loc);
					if ((int) locationObjs.get(loc) > 4) {
						loc_pos++;
					}
				}
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("all:" + all_pos / all_count);
		System.out.println("other:" + other_pos / other_count);
		System.out.println("none:" + none_pos / none_count);
		System.out.println("loc:" + loc_pos / loc_count);
	}

	public static void printAccuracy() {
		System.out.println(workers.size());
		int mem = 0;
		Iterator<String> itr = workers.keySet().iterator();
		while (itr.hasNext()) {
			String id = itr.next();
			Worker cur = workers.get(id);
			if (cur.numMutiple > 0) {
				mem++;
				System.out.print(cur.id);
				System.out.print(" " + cur.getAccuracy());
				System.out.print(" " + cur.numMutiple);
				System.out.println();
			}
		}
		System.out.println("total number of workers:" + mem);
	}

	public static void checkMultipleQuestions() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + test_exp);

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

				BasicDBObject query_answer = new BasicDBObject("type",
						"workerAnswer").append("HITID", HITID);
				DBCursor cursor2 = coll.find(query_answer);
				while (cursor2.hasNext()) {
					DBObject workerAnswer = cursor2.next();
					String workerId = (String) workerAnswer.get("workerID");
					String locString = (String) workerAnswer.get("locString");
					String locs[] = locString.split("!");
					HashSet<String> workerLoc = new HashSet<String>();
					for (int j = 0; j < locs.length; j++) {
						String loc = locs[j];
						workerLoc.add(loc);
					}
					Worker cur = workers.get(workerId);
					if (!workers.containsKey(workerId)) {
						cur = new Worker(workerId);
						workers.put(workerId, cur);
					}
					cur.numMutiple++;
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
