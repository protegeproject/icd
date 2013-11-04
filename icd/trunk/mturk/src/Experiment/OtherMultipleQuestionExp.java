package Experiment;

import java.util.HashSet;

public class OtherMultipleQuestionExp {
	public static void main(String[] args) {
		try {
			Hierarchy
					.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Interface.LocationParentChild = Hierarchy.LocationParentChild;
		Interface.readWiki();
		

		HashSet<String> location = new HashSet<String>();
		location.add("Nape of neck");
		location.add("Skin");
		location.add("Integumentary system");
		location.add("Hematopoietic system");
		location.add("Neck");
		location.add("Face");
		location.add("Other");
		location.add("Digestive system");
		String definition = "none";
		String wiki = "none";
		
		String disease = "Papulopustular rosacea";
		MultipleQuestion ques = new MultipleQuestion( disease, location, 
				 definition,  wiki);
		ques.postQuestion();
		ques.writeCurQues(501);
		
		disease = "Rosacea fulminans";
		ques = new MultipleQuestion( disease, location, 
				 definition,  wiki);
		ques.postQuestion();
		ques.writeCurQues(501);
		
		
		location = new HashSet<String>();
		location.add("Skin");
		location.add("Integumentary system");
		location.add("Face");
		location.add("Other");
		disease = "Rhinophyma";
		ques = new MultipleQuestion( disease, location, 
				 definition,  wiki);
		ques.postQuestion();
		ques.writeCurQues(501);
		
		
		
		
		
//		String disease = "Perioral dermatitis";
//		MultipleQuestion ques = new MultipleQuestion( disease, location, 
//				 definition,  wiki);
//		ques.postQuestion();
//		ques.writeCurQues(500);
//		
//		disease = "Phymatous rosacea";
//		ques = new MultipleQuestion( disease, location, 
//				 definition,  wiki);
//		ques.postQuestion();
//		ques.writeCurQues(500);
//
//		disease = "Lymphoedematous rosacea";
//		ques = new MultipleQuestion( disease, location, 
//				 definition,  wiki);
//		ques.postQuestion();
//		ques.writeCurQues(500);
//		
//		disease = "Acne agminata";
//		ques = new MultipleQuestion( disease, location, 
//				 definition,  wiki);
//		ques.postQuestion();
//		ques.writeCurQues(500);
//		
//		disease = "Rosacea...";
//		ques = new MultipleQuestion( disease, location, 
//				 definition,  wiki);
//		ques.postQuestion();
//		ques.writeCurQues(500);
		
//		Interface.getAssignmentsForMutiple("2DH9RNDWIOV4ES94B4KG313YEG3GVE",location);
//		Interface.getAssignmentsForMutiple("248YN5F3Q0JJREF3V3NYK49FA9NBF4",location);
//		Interface.getAssignmentsForMutiple("2N6Q1SNQQ7Q9TLMIYB3N012TRTW8SL",location);
//		Interface.getAssignmentsForMutiple("2TEYB49F9SSV3IIIRFES25H5GPDVRO",location);
//		Interface.getAssignmentsForMutiple("289LFQ0ONNVU3M1YW05G0B76OC3KT4",location);
		
//		String disease = "Granulomatous rosacea";
//		disease = "Erythematotelangiectatic rosacea";
//		disease = "Ocular rosacea";
//		Interface.getAssignmentsForMutiple("2JP4J79KQW64U8LN2O6CM45XDVFIKD",location);
//		Interface.getAssignmentsForMutiple("2ZZPCGJ2D21RPHMEPRAPYSI2LF05B6",location);
//		Interface.getAssignmentsForMutiple("2SIHFL42J1L1RTSOAYD0RGFKIUN2NR",location);
	}
}
