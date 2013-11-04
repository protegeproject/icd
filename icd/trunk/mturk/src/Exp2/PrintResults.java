package Exp2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class PrintResults {
	public static HashMap<String, HashSet<String>> ICDParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String> diseaseFinalLocatoins = new HashMap<String, String>();
	public static int exp = 1212;
	public static int totalDiseases = 0;
	public static int foundDiseases = 0;
	public static void main(String[] args){
		Hierarchy.readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/ICDStrcuture.txt");
		ICDParentChild = Hierarchy.ICDParentChild;
		readFinalLocations();
		printFinalLocations("Rosacea and related disorders","");
		System.out.println("foundDiseases:"+foundDiseases);
		System.out.println("totalDiseases:"+totalDiseases);
		System.out.println("done");
	}
	
	public static void printFinalLocations(String disease, String spaces){
		totalDiseases++;
		if(diseaseFinalLocatoins.get(disease)!=null&&diseaseFinalLocatoins.get(disease).length()!=0){
			foundDiseases++;
		} 
		System.out.println(spaces+disease+":"+diseaseFinalLocatoins.get(disease));
		HashSet<String> children = ICDParentChild.get(disease);
		Iterator<String> itr = children.iterator();
		spaces = spaces+"    ";
		while(itr.hasNext()){
			String child = itr.next();
			printFinalLocations(child, spaces);
		}
	}
	public static void readFinalLocations(){
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);

			BasicDBObject query = new BasicDBObject("type", "diseaseFinalLocation");
			DBCursor cursor = coll.find(query);
			while(cursor.hasNext()){
				DBObject cur = cursor.next();
				String disease = (String)cur.get("disease");
				String location = (String)cur.get("location");
				diseaseFinalLocatoins.put(disease,location);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
