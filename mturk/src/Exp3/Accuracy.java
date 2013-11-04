package Exp3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import Experiment.Hierarchy;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class Accuracy {

	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String[]> goldStandard = new HashMap<String, String[]>();
	public static HashMap<String, String[]> answer = new HashMap<String, String[]>();
	public static HashMap<String, HashSet<String>> ICDParentChild = new HashMap<String, HashSet<String>>();
	public static double totalSen = 0;
	public static double totalSpe = 0;
	public static int count = 0;

	public static void main(String args[]) {
		Hierarchy.readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/SampleICDStrcuture3.txt");
		ICDParentChild = Hierarchy.ICDParentChild;
		Hierarchy.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct.txt");
		LocationParentChild = Hierarchy.LocationParentChild;
		// goldStandard();
		readAnswer(2002);// 1511//2002//2011//2020
		readGoldStandard();
		fixGoldStandard();
		printMeasure("Dermatoses of the scalp");
//		printMeasure("Infective disorders of the external ear");
//		printMeasure("Inflammatory disorders of the external ear");
//		printMeasure("Certain specified disorders of external ear");
//		printMeasure("Infectious disorders of eyelid");
//		printMeasure("Inflammatory disorders of eyelid");
//		printMeasure("Acquired disorders of eyelashes");
//		printMeasure("Disorders of lips");
//		printMeasure("Disturbances of oral epithelium");
//		printMeasure("Lichen planus and lichenoid reactions of oral mucosa");
//		printMeasure("Non-infective erosive and ulcerative disorders of oral mucosa");
//		Iterator<String> itr = answer.keySet().iterator();
//		while (itr.hasNext()) {
//			String disease = itr.next();
//			System.out.print(disease + ": ");
//			String[] ans = answer.get(disease);
//			String[] gold = goldStandard.get(disease);
//			if (ans == null || gold == null) {
//				System.out.println("wrong");
//				continue;
//			}
//			// if(disease.contains("Curly hair")){
//			// System.out.print(disease+": ");
//			// }
//			measure(gold, ans);
//		}
		System.out.println("sensitivity:" + totalSen / count);
		System.out.println("specificity:" + totalSpe / count);
		System.out.println("count:" + count);
	}

	public static void printMeasure(String disease){
		System.out.print(disease + "\t");

		String[] ans = answer.get(disease);
		String[] gold = goldStandard.get(disease);
		if (ans == null || gold == null) {
			System.out.println("wrong");
			return;
		}
		String ansString = ans[0];
		for(int i = 1; i < ans.length; i++) {
			ansString = ansString+","+ans[i];
		}
		String goldString = gold[0];
		for(int i = 1; i < gold.length; i++) {
			goldString = goldString+","+gold[i];
		}
		System.out.print(goldString + "\t");
		System.out.print(ansString + "\t");
		if (ans == null || gold == null) {
			System.out.println("wrong");
		} else{
			measure(gold, ans);	
		}
		HashSet<String> children = ICDParentChild.get(disease);
		Iterator<String> itr = children.iterator();
		while(itr.hasNext()) {
			String next = itr.next();
			printMeasure(next);
		}
	}
	
	public static void fixGoldStandard() {
		String disease = "Woolly hair ��� palmoplantar keratoderma ��� dilated cardiomyopathy";
		String strLine = "hair!hand!foot";
		String[] children = strLine.split("!");
		goldStandard.put(disease, children);

		disease = "Woolly hair ��� hypotrichosis ��� everted lower lip ��� outstanding ears [Salamon syndrome]";
		strLine = "hair!scalp!lip";
		children = strLine.split("!");
		goldStandard.put(disease, children);

		disease = "Curly hair ��� ankyloblepharon ��� nail dysplasia (CHAND) syndrome";
		strLine = "hair!eyelid!nail!scalp";
		children = strLine.split("!");
		goldStandard.put(disease, children);

		disease = "Cone-rod type amaurosis congenita ��� congenital hypertrichosis";
		strLine = "hair";
		children = strLine.split("!");
		goldStandard.put(disease, children);

		disease = "Cataract ��� hypertrichosis ��� intellectual deficit";
		strLine = "hair";
		children = strLine.split("!");
		goldStandard.put(disease, children);

		disease = "Woolly hair ��� hypotrichosis ��� everted lower lip ��� outstanding ears [Salamon syndrome]";
		strLine = "hair";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Autosomal dominant palmoplantar keratoderma and congenital alopecia [Stevanovi?]";
		strLine = "hair";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Neonatal sclerosing cholangitis ��� ichthyosis ��� hypotrichosis syndrome";
		strLine = "hair!body regions";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Alopecia ��� psychomotor epilepsy ��� periodontal pyorrhoea ��� mental subnormality (Shokeir) syndrome";
		strLine = "hair";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Macrocephaly ��� alopecia  ��� cutis laxa  ��� scoliosis (MACS) syndrome";
		strLine = "hair!body regions";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Sch���pf-Schulz-Passarge syndrome";
		strLine = "hair!nail";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Ichthyosis ��� hypotrichosis syndrome";
		strLine = "hair!body regions";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Cervical hypertrichosis ��� peripheral sensory and motor neuropathy";
		strLine = "hair!neck";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Onycho-tricho-dysplasia ��� neutropenia (ONMR) syndrome";
		strLine = "hair!scalp!nail";
		children = strLine.split("!");
		goldStandard.put(disease, children);
		
		disease = "Bj���rnstad syndrome";
		strLine = "hair";
		children = strLine.split("!");
		goldStandard.put(disease, children);
	}

	public static void readAnswer(int exp) {
		try {
			Mongo mongo = new Mongo("localhost", 27017);
			DB db = mongo.getDB("mydb");
			DBCollection coll = db.getCollection("exp" + exp);
			BasicDBObject query = new BasicDBObject("type",
					"diseaseFinalLocation");
			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String disease = (String) obj.get("disease");
				String location = (String) obj.get("location");
				String[] children = location.split("!");
				answer.put(disease, children);
			}
			cursor.close();
			mongo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readGoldStandard() {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader("SampleGoldStandard.txt"));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String parent = strLine;
				strLine = in.readLine();
				String[] children = strLine.split("!");
				if (parent.contains("ankyloblepharon")) {
					System.out.println();
				}
				if (parent.contains("���")) {
					System.out.println();
				}
				parent = parent.replace("���", "-");
				goldStandard.put(parent, children);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void goldStandard() {
		Hierarchy
				.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct.txt");
		LocationParentChild = Hierarchy.LocationParentChild;

		String[] right = { "hair" };
		String[] answer = { "hair" };
		measure(right, answer);

		right = new String[] { "hand", "hair", "foot" };
		answer = new String[] { "hand", "hair" };
		measure(right, answer);

		right = new String[] { "hand", "hair", "foot" };
		answer = new String[] { "hand", "hair" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "body regions", "hair" };
		measure(right, answer);

		right = new String[] { "body regions", "hair" };
		answer = new String[] { "body regions", "hair" };
		measure(right, answer);

		right = new String[] { "hand", "foot", "mouth" };
		answer = new String[] { "hair", "nail" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "scalp", "lip" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hand", "foot", "scalp" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hand", "foot" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair" };
		measure(right, answer);

		right = new String[] { "hair", "lower back" };
		answer = new String[] { "hair", "lower back" };
		measure(right, answer);

		right = new String[] { "hair", "body regions" };
		answer = new String[] { "hair", "scalp" };
		measure(right, answer);

		right = new String[] { "hair", "eyebrows" };
		answer = new String[] { "hair", "eyebrows" };
		measure(right, answer);

		right = new String[] { "hair", "eyelid" };
		answer = new String[] { "hair", "eyelid" };
		measure(right, answer);

		right = new String[] { "hair", "body regions" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		right = new String[] { "hair", "body regions" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		right = new String[] { "hair", "scalp" };
		answer = new String[] { "hair", "scalp" };
		measure(right, answer);

		right = new String[] { "hair", "scalp" };
		answer = new String[] { "hair", "scalp" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "scalp" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		System.out.println(totalSen / count);
		System.out.println(totalSpe / count);
	}

	public static void firstSampe() {
		Hierarchy
				.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct.txt");
		LocationParentChild = Hierarchy.LocationParentChild;

		String[] right = { "body regions", "hair" };
		String[] answer = { "eyebrow hairs", "scalp hair", "pubic hair",
				"body regions" };
		measure(right, answer);

		right = new String[] { "body regions", "hair" };
		answer = new String[] { "body regions", "hair" };
		measure(right, answer);

		right = new String[] { "body regions", "hair" };
		answer = new String[] { "hair" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "head" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		right = new String[] { "hair" };
		answer = new String[] { "hair", "scalp", "perineum" };
		measure(right, answer);

		right = new String[] { "scalp", "hair" };
		answer = new String[] { "hair" };
		measure(right, answer);

		right = new String[] { "scalp", "hair" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		right = new String[] { "scalp", "hair" };
		answer = new String[] { "hair", "upper extremity", "head and neck" };
		measure(right, answer);

		right = new String[] { "scalp", "body regions" };
		answer = new String[] { "hair", "head and neck", "Trunk" };
		measure(right, answer);

		right = new String[] { "hair", "body regions" };
		answer = new String[] { "hair", "body regions" };
		measure(right, answer);

		System.out.println(totalSen / count);
		System.out.println(totalSpe / count);
	}

	public static void measure(String rightArr[], String answerArr[]) {
		HashSet<String> right = new HashSet<String>();
		for (int i = 0; i < rightArr.length; i++) {
			right.add(rightArr[i]);
		}

		HashSet<String> answer = new HashSet<String>();
		for (int i = 0; i < answerArr.length; i++) {
			answer.add(answerArr[i]);
		}

		AccuracyMeasure measure = getAccuracyMeasure(right, answer,
				LocationParentChild);
		if(measure.TP+measure.FN==0){
			return;
		}
		System.out.println(measure.toPrint());
		totalSen += measure.getSensitivity();
		totalSpe += measure.getSpecificity();
		count++;
	}

	public static HashSet<String> getLocs(String parent) {
		HashSet<String> output = new HashSet<String>();
		output.add(parent);
		if (!LocationParentChild.containsKey(parent)) {
			return output;
		}
		Iterator<String> itr = LocationParentChild.get(parent).iterator();
		while (itr.hasNext()) {
			String child = itr.next();
			output.addAll(getLocs(child));
		}
		return output;
	}

	public static AccuracyMeasure getAccuracyMeasure(HashSet<String> right,
			HashSet<String> answer,
			HashMap<String, HashSet<String>> LocationParentChild) {
		LocationParentChild = Hierarchy.LocationParentChild;
		HashSet<String> allLocs = getLocs("body regions");
		allLocs.addAll(getLocs("body organs"));

		HashSet<String> rightP = new HashSet<String>();
		Iterator<String> rightItr = right.iterator();
		while (rightItr.hasNext()) {
			rightP.addAll(getLocs(rightItr.next()));
		}
		HashSet<String> rightN = new HashSet<String>(allLocs);
		rightN.removeAll(rightP);

		HashSet<String> answerP = new HashSet<String>();
		Iterator<String> answerItr = answer.iterator();
		while (answerItr.hasNext()) {
			answerP.addAll(getLocs(answerItr.next()));
		}
		HashSet<String> answerN = new HashSet<String>(allLocs);
		answerN.removeAll(answerP);

		HashSet<String> tmp = new HashSet<String>(rightP);
		tmp.retainAll(answerP);
		int TP = tmp.size();
		tmp = new HashSet<String>(rightN);
		tmp.retainAll(answerN);
		int TN = tmp.size();

		tmp = new HashSet<String>(rightN);
		tmp.retainAll(answerP);
		int FP = tmp.size();

		tmp = new HashSet<String>(rightP);
		tmp.retainAll(answerN);
		int FN = tmp.size();

		// System.out.println(allLocs.size());
		// System.out.println(rightP.size());
		// System.out.println(rightN.size());
		// System.out.println(answerP.size());
		// System.out.println(answerN.size());

		AccuracyMeasure measure = new AccuracyMeasure(TP, TN, FP, FN);
		return measure;
	}
}

class AccuracyMeasure {
	public int TP;
	public int TN;
	public int FP;
	public int FN;

	public AccuracyMeasure(int TP, int TN, int FP, int FN) {
		this.TP = TP;
		this.TN = TN;
		this.FP = FP;
		this.FN = FN;
	}

	public double getSensitivity() {
		return (double) TP / (TP + FN);
	}

	public double getSpecificity() {
		return (double) TN / (TN + FP);
	}

	public String toPrint(){
		return 	TP + "\t" + TN + "\t" + FP + "\t" + FN
				+ "\t" + getSensitivity() + "\t"
				+ getSpecificity();
	}
	public String toString() {
		return "TP:" + TP + " TN:" + TN + " FP:" + FP + " FN:" + FN
				+ " sensitivity:" + getSensitivity() + " specificity:"
				+ getSpecificity();
	}
}