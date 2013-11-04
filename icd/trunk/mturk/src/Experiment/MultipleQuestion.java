package Experiment;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

class MultipleQuestion {
	public String disease;
	public HashSet<String> locations;
	public String definition;
	public String wiki;
	public String HITID;
	public int total = -1;
	public int none = -1;
	public boolean fromParent;
	public HashMap<String, Integer> locResults = null;

	public MultipleQuestion(String disease, HashSet<String> locations, 
			String definition, String wiki) {
		this.disease = disease;
		this.locations = locations;
		this.definition = definition;
		this.wiki = wiki;
		fromParent = false;
	}

	public String postQuestion() {
		String tmpDisease = disease.replace("&", "and");
		String tmpDefinition = definition.replace("&", "and");
		
		String ls[] = (String[]) locations.toArray(new String[0]);
		String HITID = Interface.createMultipleQuestion(tmpDisease, ls,
				tmpDefinition, wiki);
		this.HITID = HITID;
		TestHierarchy.totalCost += 0.5+0.05;
		return HITID;
	}

	public void removeFromCurList(int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type", "curQuestion").append("HITID", HITID);

			DBCursor cursor = coll.find(query);
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				coll.remove(obj);
				System.out.println("remove question from cur:" + disease);
				System.out.println(locations);
				System.out.println();
			}

			cursor.close();
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeCurQues(int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject ques = new BasicDBObject("type", "curQuestion").append("disease", disease).append(
					"HITID", HITID);
			String locString = "";
			Iterator<String> itr = locations.iterator();
			locString = itr.next();
			while (itr.hasNext()) {
				String loc = itr.next();
				locString = locString+ "!" + loc;
			}
			ques.append("locations", locString);
			if(fromParent){
				ques.append("fromParent", "true");
			}
			coll.insert(ques);
			mongo.close();
			System.out.println("write question:" + disease);
			System.out.println(locString);
			System.out.println();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeFinishedQues(int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject ques = new BasicDBObject("type", "doneQuestion").append("disease", disease).append(
					"HITID", HITID).append("total", total).append("none", none);
			BasicDBObject locObj = new BasicDBObject();
			Iterator<String> itr = locResults.keySet().iterator();
			while (itr.hasNext()) {
				String loc = itr.next();
				int votes = locResults.get(loc);
				locObj.append(loc, votes);
			}
			ques.append("locationResults", locObj);
			coll.insert(ques);
			mongo.close();
			System.out.println("write question:" + disease);
			System.out.println(locResults);
			System.out.println();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}