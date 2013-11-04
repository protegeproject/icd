package Experiment;

import java.util.HashSet;

public class DiseaseExperiment {
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
		String diseaseP = "Rosacea and related disorders";
		String diseaseC = "Granulomatous rosacea";
		HashSet<String> locationP = new HashSet<String>();
		locationP.add("Nape of neck");
		locationP.add("Skin");
		locationP.add("Integumentary system");
		locationP.add("Hematopoietic system");
		locationP.add("Neck");
		locationP.add("Face");
		locationP.add("Digestive system");
		
		
//		String definitionC = "none";
//		String wikiC = "http://en.wikipedia.org/wiki/Ocular_rosacea";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2OKJENLVWJ5RBWHF37Q87MWB8VKS0I
//
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Granulomatous rosacea";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//26OAUC26DG6AZMEK3R6K9COK30A5CN
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Perioral dermatitis";
//		wikiC = "http://en.wikipedia.org/wiki/Perioral_dermatitis";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2JS1QP6AUC29Z1NOLSO340FK1TK812
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Phymatous rosacea";
//		wikiC = "http://en.wikipedia.org/wiki/Phymatous_rosacea";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2162L92HECWDSIKFW33F9CP5LELIHJ
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Lymphoedematous rosacea";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2CEJ5OPB0YVJLTF342YEY9U32J26YB
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Erythematotelangiectatic rosacea";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2W36VCPWZ9RQZHZ53SJ71N3DUXQAPC
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Acne agminata";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//256GQSCM6IADODVZ2LL09OQKLR0IIC
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Rosacea...";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2GXYQS1CSTMR1I8U0V9CAEYDOEEMP7
//		
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Ocular rosacea";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//21MF0OHOVR174I15AC02QECWBNS458
//
//		diseaseP = "Rosacea and related disorders";
//		diseaseC = "Rosacea fulminans";
//		wikiC = "none";
//		Interface.createTestSameQuestion(diseaseC, locationP, definitionC,
//				wikiC, diseaseP);//2N633FDEY5CRIL2XZQU8LRFLDNQS1L
		
//		Interface.getAssignmentsForTestHIT("2OKJENLVWJ5RBWHF37Q87MWB8VKS0I", "part");//part
//		Interface.getAssignmentsForTestHIT("26OAUC26DG6AZMEK3R6K9COK30A5CN", "part");//part
//		Interface.getAssignmentsForTestHIT("2JS1QP6AUC29Z1NOLSO340FK1TK812", "part");//part
//		Interface.getAssignmentsForTestHIT("2162L92HECWDSIKFW33F9CP5LELIHJ", "part");//part
//		Interface.getAssignmentsForTestHIT("2CEJ5OPB0YVJLTF342YEY9U32J26YB", "other");//other
//		Interface.getAssignmentsForTestHIT("2W36VCPWZ9RQZHZ53SJ71N3DUXQAPC", "other");//other
//		Interface.getAssignmentsForTestHIT("256GQSCM6IADODVZ2LL09OQKLR0IIC", "part");//part
//		Interface.getAssignmentsForTestHIT("2GXYQS1CSTMR1I8U0V9CAEYDOEEMP7", "part");//part or same
//		Interface.getAssignmentsForTestHIT("21MF0OHOVR174I15AC02QECWBNS458", "other");//other
//		Interface.getAssignmentsForTestHIT("2N633FDEY5CRIL2XZQU8LRFLDNQS1L", "part");//part
		
		
		
		
		
		// String diseaseP = "Rosacea and related disorders"; part
		// String diseaseC = "Acne agminata";2LKJ1LY58B75T7HZO6BHM16YG8O7ST
		// part:5same:4other:1

		// String diseaseP = "Rosacea and related disorders"; part
		// String diseaseC = "Phymatous rosacea";27IONNVRH1KKAUVXZ2Y6WV9LLCZOX7
		// part:6same:1other:3

		// String diseaseP = "Rosacea and related disorders";same
		// String diseaseC =
		// "Papulopustular rosacea";2F6CCF0CP5K0BEZ4Q9OYP4PM83QWVO
		// part:3same:5other:2

		// String diseaseP = "Rosacea and related disorders";other
		// String diseaseC =
		// "Lymphoedematous rosacea";230KMN9U8P91PTAD4O931UYINGBUDO
		// part:2same:3other:5

		// String diseaseP = "Rosacea and related disorders";part
		// String diseaseC =
		// "Granulomatous rosacea";2NVG67D1X3V315HTWBTJN1M7Q0DIBY
		// part:5same:4other:1

		// String diseaseP = "Rosacea and related disorders";other
		// String diseaseC = "Ocular rosacea";2F84J3NAB6BI80W56P9TATJFEW7SFI
		// part:3same:3other:4

		// String diseaseP = "Disorders of hair and the hair follicle";
		// String diseaseC = "Rosacea and related disorders";
		// without bonus:2YD8ER9Y8TNN47G9UWBFQS2ONGQT6D
		// with bonus: 2C8VJ8EBDPGI7RCTXNQ90NDWJ4XF0R,
		// 2UZJHP4BDDKJQJP80O0XCG358CD6Z7
		// with less than master: 217FE4EGDHDPOQ2PQ0EZ56HE0MCAK1
		// part:4same:6other:0

		// String diseaseP = "Disorders of hair and the hair follicle";
		// String diseaseC =
		// "Disorders of the sebaceous gland";part:6same:4other:0
		// with less than master: 2SVPGFL6VCPZLU84LN9O41S7T3RL6P

		// String diseaseP = "Disorders of hair and the hair follicle";
		// String diseaseC = "DDisorders of hair";part:2same:7other:1
		// with less than master: 23WGOOGQSCM94VRJ059U3U00P68FFH

		// String diseaseP =
		// "Disorders of hair and the hair follicle";part:3same:5other:2
		// String diseaseC =
		// "Pseudofolliculitis barbae";20QEBDPGFL6YYADGHIED5IOV28VI32

		// String diseaseP =
		// "Disorders of hair and the hair follicle";part:4same:5other:1
		// String diseaseC =
		// "Disorders involving the apocrine follicular unit";2ZFKO2L92HEFIVNEBPFCLF0CQL8FGG

		// String diseaseP =
		// "Disorders of hair and the hair follicle";part:3same:6other:1
		// String diseaseC =
		// "Acne and related disorders";2C4EC1PC6SKCD4PMWQ9ZNHMAB5AO08
	}

}
