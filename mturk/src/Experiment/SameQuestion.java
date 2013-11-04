package Experiment;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class SameQuestion {
	public String diseaseP;
	public String diseaseC;
	public HashSet<String> locationsP;
	public String definitionC;
	public String wikiC;
	public String HITID;
	public int total = -1;
	public int yes = -1;
	
	public SameQuestion(String diseaseP, HashSet<String> locationsP, 
			String diseaseC, String definitionC, String wikiC) {
		this.diseaseP = diseaseP;
		this.diseaseC = diseaseC;
		this.locationsP = locationsP;
		this.definitionC = definitionC;
		this.wikiC = wikiC;
	}
	

	public String postQuestion() {
		String tmpDiseaseC = diseaseC.replace("&", "and");
		String tmpDiseaseP = diseaseP.replace("&", "and");
		String tmpDefinitionC = definitionC.replace("&", "and");
		String HITID = Interface.createSameQuestion(tmpDiseaseC, locationsP,
				tmpDefinitionC, wikiC, tmpDiseaseP);
		this.HITID = HITID;
		Hierarchy.totalCost += 0.2+0.05;
		return HITID;
	}

	public void removeFromCurList(int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type", "curSameQuestion").append("HITID", HITID);

			coll.remove(query);
			mongo.close();
			System.out.println("remove same question from cur:" + diseaseC);
			System.out.println();
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

			if(diseaseC.matches("Alopecia areata")||diseaseC.matches("Heterotopic sebaceous glands")){
				System.out.println();
			}
			BasicDBObject ques = new BasicDBObject("type", "curSameQuestion").append("disease", diseaseC).append(
					"HITID", HITID).append("diseaseParent", diseaseP);
			String locString = "";
			Iterator<String> itr = locationsP.iterator();
			if(locationsP==null||locationsP.size()==0){
				System.out.println();
			}
			locString = itr.next();
			while (itr.hasNext()) {
				String loc = itr.next();
				locString = locString+ "!" + loc;
			}
			ques.append("locations", locString);
			coll.insert(ques);
			mongo.close();
			System.out.println("write same question:" + diseaseC);
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

			BasicDBObject ques = new BasicDBObject("type", "doneSameQuestion").append("disease", diseaseC).append("diseaseParent", diseaseP).append(
					"HITID", HITID).append("total", total).append("yes", yes);
			
			String locString = "";
			Iterator<String> itr = locationsP.iterator();
			locString = itr.next();
			while (itr.hasNext()) {
				String loc = itr.next();
				locString = locString+ "!" + loc;
			}
			ques.append("locations", locString);

			coll.insert(ques);
			mongo.close();
			System.out.println("write same finished question:" + diseaseC);
			System.out.println(locString+":"+yes);
			System.out.println();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
