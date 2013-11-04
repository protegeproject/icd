package Experiment;

import java.util.HashSet;

public class TestExtensionQuestion {
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
		// String diseaseP = "Rosacea and related disorders";

		HashSet<String> location = new HashSet<String>();
		location.add("Nape of neck");
		location.add("Skin");
		location.add("Integumentary system");
		location.add("Hematopoietic system");
		location.add("Neck");
		location.add("Face");
		location.add("Digestive system");
		location.add("Other");

		HashSet<String> location2 = new HashSet<String>();
		location2.add("Skin");
		location2.add("Integumentary system");
		location2.add("Face");
		location2.add("Other");
		
		String diseaseC = "Papulopustular rosacea";
		String locationP[] = { "Nape of neck", "Skin", "Integumentary system",
				"Hematopoietic system", "Neck", "Face", "Digestive system",
				"Other" };
		String definitionC = "none";
		String wikiC = "none";
//		Interface.createTestMultipleQuestion(diseaseC, locationP, definitionC,
//				wikiC);
//
//		diseaseC = "Rosacea fulminans";
//		wikiC = "none";
//		Interface.createTestMultipleQuestion(diseaseC, locationP, definitionC,
//				wikiC);
//
//		diseaseC = "Rhinophyma";
//		String locationP2[] = { "Skin", "Integumentary system", "Face","Other" };
//		wikiC = "http://en.wikipedia.org/wiki/Rhinophyma";
//		Interface.createTestMultipleQuestion(diseaseC, locationP2, definitionC,
//				wikiC);

//		Interface.getAssignmentsForTestMutiple("28AQV66RLPHLFOYC41MKR62NK8DE6M",
//		location,false);// part
//Interface.getAssignmentsForTestMutiple("2W151Y7QEOZIKB9IKJKMXFXREAFGDX",
//		location,false);// part
//Interface.getAssignmentsForTestMutiple("2JV21O3W5XH35A69QTBYNPLSQPGBHK",
//		location2,false);// part
		
		
		// String locationP[] = {"Nape of neck", "Skin", "Integumentary system",
		// "Hematopoietic system", "Neck"
		// ,"Face","Digestive system", "Other"};
		// String definitionC = "none";
		// String wikiC = "http://en.wikipedia.org/wiki/Ocular_rosacea";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Granulomatous rosacea";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Perioral dermatitis";
		// wikiC = "http://en.wikipedia.org/wiki/Perioral_dermatitis";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Phymatous rosacea";
		// wikiC = "http://en.wikipedia.org/wiki/Phymatous_rosacea";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Lymphoedematous rosacea";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Erythematotelangiectatic rosacea";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Acne agminata";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Rosacea...";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Ocular rosacea";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		// diseaseC = "Rosacea fulminans";
		// wikiC = "none";
		// Interface.createTestMultipleQuestion(diseaseC, locationP,
		// definitionC, wikiC);
		//
		Interface.getAssignmentsForTestMutiple("24SB6BFMFFO1K3AI1A6DPESC191XKC",
				location, false);// part
		Interface.getAssignmentsForTestMutiple("22X395SW6NG41DK46EAB7F1F0A4GU1",
				location, false);// part
		Interface.getAssignmentsForTestMutiple("29BLB3L0FBJC49S0MAIE4E5RQ96XLF",
				location, false);// part
		Interface.getAssignmentsForTestMutiple("2NFBAT2D5NQ19QWKYRAGETYMOU137D",
				location, false);// part
		Interface.getAssignmentsForTestMutiple("2CPWGKCCVLL6YVKKU81PZ4C9O8YV5M",
				location, true);// other
		Interface.getAssignmentsForTestMutiple("2KXDEY5COW0O2CGKG3IFUC6VOC34VQ",
				location, false);// part
		Interface.getAssignmentsForTestMutiple("2SLQ39Z0B6UWARALMMJT4RKGQEWXMH",
				location, false);// part
		Interface.getAssignmentsForTestMutiple("2FPQEOZFYQS4YDA3W6ORMS4IDJIHK3",
				location, false);// part or same
		Interface.getAssignmentsForTestMutiple("2ZIOUQIHPCGMOYJIWUN56H0JQ7X60M",
				location, true);// other
		Interface.getAssignmentsForTestMutiple("2F5J92NJKM08X70A28PWBGTPA9FE9R",
				location, false);// part

	}
}
