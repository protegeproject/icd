package Exp3;

import java.util.HashSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class ReadAll {
	public static int exp = 1601;
	
	public static void main(String[] args) {
		readDoneQuestions();
	}
	
	public static void readDoneQuestions(){
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type", "doneQuestion");
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String disease = (String) obj.get("disease");
				String HITID = (String) obj.get("HITID");
				String fromP = (String) obj.get("fromParent");
				Interface.printResultsForMutiple(HITID);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
