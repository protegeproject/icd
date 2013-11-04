package Experiment;

import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

public class MongoDB {

	public static void main(String[] args) {
		try {
			// To directly connect to a single MongoDB server (note that this
			// will not auto-discover the primary even
			// if it's a member of a replica set:
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");

			BasicDBObject doc = new BasicDBObject("name", "MongoDB")
					.append("type", "database")
					.append("count", 1)
					.append("info",
							new BasicDBObject("x", 203).append("y", 102));
			DBCollection coll = db.getCollection("testCollection");
			coll.insert(doc);
			DBObject myDoc = coll.findOne();
			System.out.println(myDoc);
			
//			for (int i=0; i < 100; i++) {
//			    coll.insert(new BasicDBObject("i", i));
//			}
			BasicDBObject query = new BasicDBObject("i", 71);

			DBCursor cursor = coll.find(query);
			try {
			   while(cursor.hasNext()) {
				   DBObject obj = cursor.next();
			       System.out.println(obj);
			       coll.remove(obj);
			   }
			} finally {
			   cursor.close();
			}
			
			 cursor = coll.find(query);
			try {
				   while(cursor.hasNext()) {
					   DBObject obj = cursor.next();
				       System.out.println(obj);
				   }
				} finally {
				   cursor.close();
				}
				
			
			System.out.println(coll.getCount());

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
