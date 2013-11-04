package Exp2;

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

public class Bonus {
	public String workerID;
	public String assignmentID;
	public double amount;

	public Bonus(String workerID, String assignmentID, double amount) {
		this.workerID = workerID;
		this.assignmentID = assignmentID;
		this.amount = amount;
	}

	public void grantBonus() {
		Interface.grandBonus(workerID, assignmentID, amount);
	}

	public void writeBonus(int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject ques = new BasicDBObject("type", "bonus")
					.append("workerID", workerID)
					.append("assignmentID", assignmentID)
					.append("amount", amount);
			coll.insert(ques);
			mongo.close();
			System.out.println("write bonus:" + workerID + " " + assignmentID);
			System.out.println();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashSet<Bonus> readBonus(int exp) {
		HashSet<Bonus> bonuss = new HashSet<Bonus>();
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject ques = new BasicDBObject("type", "bonus");
			DBCursor cursor = coll.find(ques);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String workerID = (String) obj.get("workerID");
				String assignmentID = (String) obj.get("assignmentID");
				double amount = (Double) obj.get("amount");
				Bonus bonus = new Bonus(workerID, assignmentID, amount);
				bonuss.add(bonus);
			}
			cursor.close();
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bonuss;
	}

	public static HashSet<Bonus> writeAgain(HashSet<Bonus> bonuss, int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			coll.remove(new BasicDBObject("type", "bonus"));
			Iterator<Bonus> itr = bonuss.iterator();
			while (itr.hasNext()) {
				Bonus bonus = itr.next();
				BasicDBObject ques = new BasicDBObject("type", "doneBonus")
						.append("workerID", bonus.workerID)
						.append("assignmentID", bonus.assignmentID)
						.append("amount", bonus.amount);
				coll.insert(ques);
			}
			mongo.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bonuss;
	}

	public static void main(String[] args) {
		HashSet<Bonus> bonuss = readBonus(1112);
		Iterator<Bonus> itr = bonuss.iterator();
		while (itr.hasNext()) {
			Bonus bonus = itr.next();
			Interface.grandBonus(bonus.workerID, bonus.assignmentID,
					bonus.amount);
		}
		writeAgain(bonuss, 1112);
	}
}
