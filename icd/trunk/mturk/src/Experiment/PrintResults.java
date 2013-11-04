package Experiment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
	public static HashSet<String> regions = new HashSet<String>();
	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String> diseaseFinalLocatoins = new HashMap<String, String>();
	public static int exp = 2011;
	public static int totalDiseases = 0;
	public static int foundDiseases = 0;
	public static int locCount = 0;
	public static void main(String[] args) throws Exception{
		Hierarchy.readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/SampleICDStrcuture3.txt");
		ICDParentChild = Hierarchy.ICDParentChild;
//		Hierarchy.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct2.txt");
		Hierarchy.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure2.txt");
		
		LocationParentChild = Hierarchy.LocationParentChild;
		readFinalLocations();
		printFinalLocations("Infectious disorders of eyelid","");
		
//		printFinalLocations("Drug-induced hair abnormalities","");
//		printFinalLocations("Acquired disorders of the hair shaft","");
//		printFinalLocations("Hirsutism and syndromes with hirsutism","");
		
//		System.out.println("foundDiseases:"+foundDiseases);
//		System.out.println("totalDiseases:"+totalDiseases);
//		System.out.println("done");
//		System.out.println(count("Scalp"));
		
//		printLocations("body systems","");
//		printLocations("Nerve","");
//		printLocations("Blood Vessels","");
//		printLocations("Lymphatics","");
//		printLocations("Body Organ","");
//		printLocations("Body Cavities","");
//		printLocations("Ligaments and Joints","");
//		printLocations("Body Tissues","");
//		printLocations("Walls in the Body","");
//		printLocations("Tendons of the body","");
		
//		printLocations("head","");
//		printLocations("neck","");
//		printLocations("upper extremity","");
//		printLocations("lower extremity","");
//		printLocations("extremity","");
//		printLocations("trunk","");
//		printLocations("pelvis and perineum","");
//		printLocations("body organs","");
//		writeLocations("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct2.txt");
//		System.out.println("Count:"+locCount);
	}
	
	public static void writeLocations(String path) {
		FileWriter fstream;
		try {
			fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			Iterator<String> itr = LocationParentChild.keySet().iterator();
			while(itr.hasNext()){
				String parent = itr.next();
				if(!regions.contains(parent)) {
					continue;
				}
				HashSet<String> children = LocationParentChild.get(parent);
				String childString = "";
				Iterator<String> itrChild = children.iterator();
				while(itrChild.hasNext()){
					String loc = itrChild.next();
					if(childString.matches("")){
						childString = loc;
					} else {
						childString += "!"+loc;
					}
				}
				parent = parent.toLowerCase();
				childString = childString.toLowerCase();
				out.write(parent);
				out.newLine();
				out.write(childString);
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void printLocations(String loc, String spaces){
		regions.add(loc);
		System.out.println(spaces+loc);	
		HashSet<String> children = LocationParentChild.get(loc);
		locCount++;
		if(children==null)
			return;
		Iterator<String> itr = children.iterator();
		spaces = spaces+"    ";
		while(itr.hasNext()){
			String child = itr.next();
			printLocations(child, spaces);
		}
	}
	
	public static int count(String node){
		int count = 1;
		HashSet<String> tmp = LocationParentChild.get(node);
		if(tmp==null)return count;
		Iterator<String> itr = LocationParentChild.get(node).iterator();
		while(itr.hasNext()){
			String next = itr.next();
			count += count(next);
		}
		return count;
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
