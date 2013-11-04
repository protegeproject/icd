package Exp3;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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
	public String parent;
	public int total = -1;
	public int none = -1;
	public int other = -1;
	public int all = -1;
	public boolean fromParent;
	public HashMap<String, Integer> locResults = null;

	public MultipleQuestion(String disease, HashSet<String> locations, 
			String definition, String wiki) {
		this.disease = disease;
		this.locations = locations;
		this.definition = definition;
		this.wiki = wiki;
		fromParent = false;
		parent = "none";
	}

	public String postQuestion() {
		String tmpDisease = disease.replace("&", "and");
		String tmpDefinition = definition.replace("&", "and");
		String ls[];
		if(locations.contains("other")){
			locations.remove("other");
			ls = (String[]) locations.toArray(new String[0]);
			String tmp[] = new String[ls.length+1];
			for(int i = 0; i<ls.length; i++){
				tmp[i] = ls[i];
			}
			tmp[ls.length] = "other";
			locations.add("other");
			ls = tmp;
		} else {
			ls = (String[]) locations.toArray(new String[0]);
		}
		
		if(other==-1){
			parent = Hierarchy2.LocationChildParent.get(ls[0]);
		}
		String HITID = Interface.createMultipleQuestion(tmpDisease, ls,
				tmpDefinition, wiki, parent);
		if(!tmpDefinition.matches("none")){
			System.out.print("definition: yes");
		} else {
			System.out.print("definition: no");
		}
		if(!wiki.matches("none")){
			System.out.println("  wiki: yes");
		} else {
			System.out.println("  wiki: no");
		}
		this.HITID = HITID;
		Hierarchy2.totalCost += 0.45;
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
					"HITID", HITID).append("other", other);
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
					"HITID", HITID).append("total", total).append("none", none).append("other", other).append("all", all);
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