package CostSaver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.math3.distribution.NormalDistribution;

import Experiment.Worker;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class expWithNetwork2 {
	public static HashMap<String, Worker> workers = new HashMap<String, Worker>();
	public static double P_all = 0.707;
	public static double P_none = 0.0405;
	public static double P_other = 0.107;
	public static double P_loc = 0.692;
	public static int trainning_exp = 2020;
	public static int test_exp = 2011;//
	public static GradientAscent gradient_ascent = new GradientAscent();
	public static double cutoff = 0.5;
	public static double response_square = 0;

	public static double budget = 667;

	public static void main(String[] args) {
		learn();
		simulate();
	}

	public static void simulate() {
		int responses = 0;
		int questions = 0;
		int diseaseNum = 0;
		int total_disease = 71;
		int otherNum = 0;
		int allNum = 0;
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + test_exp);// test_exp

			BasicDBObject query = new BasicDBObject("type",
					"diseaseFinalLocation");
			int i = 0;
			Reinforcement reinforce = new Reinforcement();
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				if (budget < 0) {
					break;
				}
				DBObject disease_obj = cursor.next();
				String disease = (String) disease_obj.get("disease");
				System.out.println(disease);
				diseaseNum++;
				query = new BasicDBObject("type", "doneQuestion").append(
						"disease", disease);
				cutoff = reinforce.getCutoff(budget, total_disease);
				System.out.println("cutoff:" + cutoff);
				int cur_response = 0;
				DBCursor cursor2 = coll.find(query);
				total_disease--;

				while (cursor2.hasNext() && i < 1000 && questions < 10000) {
					i++;
					DBObject obj = cursor2.next();
					String HITID = (String) obj.get("HITID");
					HashSet<String> locs = printRightAnswer(obj, questions);
					if (locs.contains("other")) {
						otherNum++;
						// System.out.println("other type of question");
						locs.remove("other");
						BasicDBObject answer_query = new BasicDBObject("type",
								"workerAnswer").append("HITID", HITID);
						DBCursor cursor_answer = coll.find(answer_query);

						QuestionModel model = new QuestionModel(locs, true);
						updateWorker(model.locs, model, P_loc);

						questions++;
						double x[] = new double[2];
						int j;
						for (j = 0; j < 10; j++) {
							DBObject answer = cursor_answer.next();
							String workerID = (String) answer.get("workerID");
							double worker_p;
							if(workers.containsKey(workerID)){
								worker_p = workers.get(workerID).getRate();
							} else {
								Worker tmp = new Worker(workerID);
								worker_p = tmp.getRate();
							}
							if (worker_p < 0.55) {
								continue;
							}
							budget--;
							responses++;
							cur_response++;
							response_square += cur_response * cur_response;
							boolean other = false;
							String locString = (String) answer.get("locString");
							String locAnswers[] = locString.split("!");
							HashSet<String> votes = new HashSet<String>();
							for (int k = 0; k < locAnswers.length; k++) {
								String vote = locAnswers[k];
								votes.add(vote);
								if (vote.matches("other")) {
									other = true;
								}
							}
							updateWorker(votes, model, worker_p);
							if (other) {
								x[0] += worker_p;
								// System.out.println("vote for other");
							} else {
								x[1] += worker_p;
								// System.out.println("vote for no other");
							}

							double prediction = gradient_ascent.h(x);
							// System.out.println(prediction);
							if (prediction > 0.6 && j >= 2) {
								System.out.println("other");
								gradient_ascent.update(x, 1);
								System.out.println();
								break;
							} else if (prediction < 0.15 && j >= 2) {
								gradient_ascent.update(x, 0);
								double max_prob = model.solve();
								if (max_prob > cutoff) {
									System.out.println("not other");
									System.out.println(model.best_index);
									System.out
											.println(model.best_prob + " at " + j);
									System.out.println();
									break;
								}
							}
						}
					} else {
						allNum++;
						questions++;
						BasicDBObject answer_query = new BasicDBObject("type",
								"workerAnswer").append("HITID", HITID);
						DBCursor cursor_answer = coll.find(answer_query);

						QuestionModel model = new QuestionModel(locs, true);
						updateWorker(model.locs, model, P_loc);

						int j;
						for (j = 0; j < 10; j++) {
							DBObject answer = cursor_answer.next();
							String workerID = (String) answer.get("workerID");
							double worker_p;
							if(workers.containsKey(workerID)){
								worker_p = workers.get(workerID).getRate();
							} else {
								Worker tmp = new Worker(workerID);
								worker_p = tmp.getRate();
							}
							 
							if (worker_p < 0.55) {
								continue;
							}
							budget--;
							responses++;
							cur_response++;
							String locString = (String) answer.get("locString");
							String locAnswers[] = locString.split("!");
							HashSet<String> votes = new HashSet<String>();
							for (int k = 0; k < locAnswers.length; k++) {
								String vote = locAnswers[k];
								votes.add(vote);
							}
							updateWorker(votes, model, worker_p);
							// System.out.println(votes);
							double max_prob = model.solve();
							// System.out.println("one round:"+j);
							if (max_prob > cutoff && j >= 1) {
								System.out.println(model.best_index);
								System.out
										.println(model.best_prob + " at " + j);
								break;
							}
						}
						response_square += cur_response * cur_response;
						if (j == 10) {
							System.out.println(model.best_index);
							System.out.println(model.best_prob + " at " + j);
							// System.out.println("more than 10");
						}
						System.out.println();
					}

				}
				cursor2.close();

				int index = (int) (cutoff / 0.1 - 1);
				reinforce.update(index, cur_response);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("number of other questions : " + otherNum);
		System.out.println("number of all questions : " + allNum);
		System.out.println("number of questions : " + questions);
		System.out.println("average responses: " + (double) responses
				/ questions);
		System.out.println("number of diseases : " + diseaseNum);
		System.out.println("response: " + responses);
		System.out.println("response square: " + response_square);
		System.out.println("average responses: " + (double) responses
				/ diseaseNum);
		System.out.println("buget: " + budget);
		System.out.println("total_disease: " + total_disease);
		// System.out
		// .println("sd: "
		// + (double) (response_square - responses * responses
		// / diseaseNum) / diseaseNum);
	}

	public static HashSet<String> printRightAnswer(DBObject obj, int questions) {
		System.out.println("question id:" + questions);
		String disease = (String) obj.get("disease");
		if (disease.matches("Leukoplakia"))
			System.out.print(disease + ":");
		HashSet<String> locs = new HashSet<String>();

		DBObject locationObjs = (DBObject) obj.get("locationResults");
		Iterator<String> itr = locationObjs.keySet().iterator();
		while (itr.hasNext()) {
			String loc = itr.next();
			System.out.print(loc + "!");
			locs.add(loc);
		}
		System.out.println();
		if ((int) obj.get("other") == -1) {

		} else {
			if ((int) obj.get("other") >= 3) {
				System.out.println("other is the right answer");
			} else {
				System.out.println("not other is the right answer");
			}
//			locs = new HashSet<String>();
			locs.add("other");
			return locs;
		}
		System.out.print("conclusion:");
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
		return locs;
	}

	public static void updateWorker(HashSet<String> votes, QuestionModel model,
			double worker_p) {
		Iterator<String> loc_itr = model.locs.iterator();
		while (loc_itr.hasNext()) {
			String loc = loc_itr.next();
			int loc_index = model.loc_mapping.get(loc);
			if (votes.contains(loc)) {
				updateLocDistribution(model.loc_distribution, loc_index,
						worker_p);
			} else {
				updateLocDistribution(model.loc_distribution, loc_index,
						(1 - worker_p));
			}
		}

		if (model.all_question) {
			if (votes.contains("all")) {
				model.all_factor[0] = model.all_factor[0] * (1 - worker_p);
				model.all_factor[1] = model.all_factor[1] * worker_p;
			} else {
				model.all_factor[1] = model.all_factor[1] * (1 - worker_p);
				model.all_factor[0] = model.all_factor[0] * worker_p;
			}
			if (votes.contains("none")) {
				model.none_factor[0] = model.none_factor[0] * (1 - worker_p);
				model.none_factor[1] = model.none_factor[1] * worker_p;
			} else {
				model.none_factor[1] = model.none_factor[1] * (1 - worker_p);
				model.none_factor[0] = model.none_factor[0] * worker_p;
			}
		} else {
			model.all_factor[0]=0;
			model.all_factor[1]=1;
			if (votes.contains("none")) {
				model.none_factor[0] = model.none_factor[0] * (1 - worker_p);
				model.none_factor[1] = model.none_factor[1] * worker_p;
			} else {
				model.none_factor[1] = model.none_factor[1] * (1 - worker_p);
				model.none_factor[0] = model.none_factor[0] * worker_p;
			}
		}
		model.normalize();
	}

	public static void updateLocDistribution(double ps[], int index, double p) {
		for (int i = 0; i < ps.length; i++) {
			String bits = Integer.toBinaryString(i);
			if (bits.length() - index - 1 >= 0
					&& bits.charAt(bits.length() - index - 1) == '1') {
				ps[i] = ps[i] * p;
			} else {
				ps[i] = ps[i] * (1 - p);
			}
		}
	}

	public static void learn() {
		checkMultipleQuestions();
		learnPiors();
		learnOther();
		// printAccuracy();
	}

	public static void learnOther() {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + trainning_exp);// test_exp

			BasicDBObject query = new BasicDBObject("type", "doneQuestion");
			DBCursor cursor = coll.find(query);
			int i = 0;
			while (cursor.hasNext() && i < 2000) {
				i++;
				DBObject obj = cursor.next();
				// String disease = (String) obj.get("disease");
				String HITID = (String) obj.get("HITID");
				int other_count = (int) obj.get("other");
				if (other_count == -1) {
					continue;
				}

				double positive = 0;
				double negative = 0;
				BasicDBObject query_answer = new BasicDBObject("type",
						"workerAnswer").append("HITID", HITID);
				DBCursor cursor2 = coll.find(query_answer);
				while (cursor2.hasNext()) {
					DBObject workerAnswer = cursor2.next();
					String workerId = (String) workerAnswer.get("workerID");
					double worker_rate = workers.get(workerId).getAccuracy();
					boolean pos = false;

					String locString = (String) workerAnswer.get("locString");
					String locs[] = locString.split("!");
					for (int j = 0; j < locs.length; j++) {
						if (locs[j].matches("other")) {
							pos = true;
						}
					}
					if (pos) {
						positive += worker_rate;
					} else {
						negative += worker_rate;
					}
				}
				double x[] = { positive, negative };
				int y = 0;
				if (other_count >= 3) {
					y = 1;
				}
				gradient_ascent.update(x, y);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			DBCollection coll = db.getCollection("exp" + trainning_exp);// test_exp

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
