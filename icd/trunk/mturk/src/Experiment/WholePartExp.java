package Experiment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.HashSet;


public class WholePartExp {
	public static void main(String[] args) {
		try {
			Hierarchy
					.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/RLocationStructure.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Interface.LocationParentChild = Hierarchy.LocationParentChild;
		Interface.readWiki();

		String disease = "Drug-induced hair abnormalities";
		HashSet<String> locations = new HashSet<String>();
		String definition = "none";
		String wiki = "none";
		
		
		
		disease = "Drug-induced hair abnormalities";
		locations = new HashSet<String>();
		locations.addAll(Interface.LocationParentChild.get("Body Regions"));
		locations.add("any part of body surface area");
//		MultipleQuestion ques = new MultipleQuestion( disease, locations, 
//				 definition,  wiki);
//		ques.postQuestion();//2ZFKO2L92HEFIVNEBPFCLF0CQRYGFJ
		Interface.getAssignmentsForMutiple("2ZFKO2L92HEFIVNEBPFCLF0CQRYGFJ",locations);
		
		
		locations = new HashSet<String>();
		locations.addAll(Interface.LocationParentChild.get("Hair"));
		locations.add("any part of Hair");
//		ques = new MultipleQuestion( disease, locations, 
//				 definition,  wiki);
//		ques.postQuestion();//2776AUC26DG9TYIEBMRFT0COLOY4B3
		Interface.getAssignmentsForMutiple("2776AUC26DG9TYIEBMRFT0COLOY4B3",locations);
		
		disease = "Acquired disorders of the hair shaft";
		locations = new HashSet<String>();
		locations.addAll(Interface.LocationParentChild.get("Body Regions"));
		locations.add("any part of body surface area");
//		ques = new MultipleQuestion( disease, locations, 
//				 definition,  wiki);
//		ques.postQuestion();//2G4AZHHRRLFTM9443I81THO9F26EN8
		Interface.getAssignmentsForMutiple("2G4AZHHRRLFTM9443I81THO9F26EN8",locations);
		
		locations = new HashSet<String>();
		locations.addAll(Interface.LocationParentChild.get("Hair"));
		locations.add("any part of Hair");
//		ques = new MultipleQuestion( disease, locations, 
//				 definition,  wiki);
//		ques.postQuestion();//2BXJ51Y7QEO21J7993JTVOFXSZ7CF6
		Interface.getAssignmentsForMutiple("2BXJ51Y7QEO21J7993JTVOFXSZ7CF6",locations);
		
		disease = "Hirsutism and syndromes with hirsutism";
		locations = new HashSet<String>();
		locations.addAll(Interface.LocationParentChild.get("Body Regions"));
		locations.add("any part of body surface area");
//		ques = new MultipleQuestion( disease, locations, 
//				 definition,  wiki);
//		ques.postQuestion();//2ORKQW618N49GIWTLVWXLETNEE2MOK
		Interface.getAssignmentsForMutiple("2ORKQW618N49GIWTLVWXLETNEE2MOK",locations);
		
		locations = new HashSet<String>();
		locations.addAll(Interface.LocationParentChild.get("Hair"));
		locations.add("any part of Hair");
//		ques = new MultipleQuestion( disease, locations, 
//				 definition,  wiki);
//		ques.postQuestion();//2522JE1M7PKNV68DEFQAL04L0J3QXF
		Interface.getAssignmentsForMutiple("2522JE1M7PKNV68DEFQAL04L0J3QXF",locations);
	}
}
