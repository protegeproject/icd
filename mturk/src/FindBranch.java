import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FindBranch {

	public static HashMap<String, Integer> diseases = new HashMap<String, Integer>();
	public static HashMap<String, Integer> diseasesDefinition = new HashMap<String, Integer>();
	public static HashMap<String, Integer> diseasesKnownLocation = new HashMap<String, Integer>();
	public static HashMap<String, HashSet<String>> parentChildDiseases = new HashMap<String, HashSet<String>>();
	public static HashMap<String, Integer> diseasesChildren = new HashMap<String, Integer>();
	public static HashMap<String, Integer> allLocations = new HashMap<String, Integer>();

	public static void main(String[] args) throws Exception {
		
		
		System.out.println();
		readLocations("/Users/Yun/Dropbox/medical/workspace/turk/newLocStruct.txt");

		// readDiseases();
		// System.out.println(diseases.size());
		  readWiki();
		// // writeWiki();
		// readWikiBack();
		// buildDiseasesChildrenNumber();
		// findBranch();
		// System.out.println(diseasesChildren.get("Infections and infestations affecting the skin"));
		  writeLocationWiki();
		  System.out.println();
	}

	public static void readLocations(String path) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(path));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			String parent = strLine;
			allLocations.put(parent, 0);
			strLine = in.readLine();
			String[] children = strLine.split("!");
			for (int i = 0; i < children.length; i++) {
				allLocations.put(children[i], 0);
			}
			in.readLine();
		}
		in.close();
	}

	public static void findBranch() {
		Iterator<String> itr = diseases.keySet().iterator();
		while (itr.hasNext()) {
			String disease = itr.next();
			int childrenNumber = diseasesChildren.get(disease);
			if (childrenNumber >= 50 && childrenNumber <= 200) {
				int definition = getDefNum(disease);
				int wiki = getWikiNum(disease);
				int KnownLocation = getKnownLocationNum(disease);
				System.out.println("Root of sub-branch: " + disease);
				System.out.println("Size of sub-branch: " + childrenNumber);
				System.out
						.println("Number of diseases with definition on Skin Chapter: "
								+ definition);
				System.out.println("Number of diseases with a Wikipedia page: "
						+ wiki);
				System.out
						.println("Number of diseases with known Anatomy location in Skin chapter: "
								+ KnownLocation);
				System.out.println();
			}
		}
	}

	public static int getKnownLocationNum(String disease) {
		int count = diseasesKnownLocation.get(disease);
		Iterator<String> itr = parentChildDiseases.get(disease).iterator();
		while (itr.hasNext()) {
			String child = itr.next();
			count += getKnownLocationNum(child);
			;
		}
		return count;
	}

	public static int getWikiNum(String disease) {
		int count = diseases.get(disease);
		Iterator<String> itr = parentChildDiseases.get(disease).iterator();
		while (itr.hasNext()) {
			String child = itr.next();
			count += getWikiNum(child);
			;
		}
		return count;
	}

	public static int getDefNum(String disease) {
		int count = diseasesDefinition.get(disease);
		Iterator<String> itr = parentChildDiseases.get(disease).iterator();
		while (itr.hasNext()) {
			String child = itr.next();
			count += getDefNum(child);
			;
		}
		return count;
	}

	public static void getChildrenNumber(String disease) {
		if (diseasesChildren.containsKey(disease)) {
			return;
		}
		// if(disease.matches("Infections and infestations affecting the skin"))
		// System.out.println(diseasesChildren.get("Infections and infestations affecting the skin"));
		int count = parentChildDiseases.get(disease).size();
		Iterator<String> itr = parentChildDiseases.get(disease).iterator();
		while (itr.hasNext()) {
			String child = itr.next();
			getChildrenNumber(child);
			count += diseasesChildren.get(child);
		}
		diseasesChildren.put(disease, count);
		// if (count > 50) {
		// System.out.println(disease + " " + count);
		// }
	}

	public static void buildDiseasesChildrenNumber() {
		Iterator<String> itr = diseases.keySet().iterator();
		while (itr.hasNext()) {
			String disease = itr.next();
			getChildrenNumber(disease);
		}
	}

	public static void readWikiBack() throws NumberFormatException, IOException {
		String path = "/Users/Yun/Dropbox/medical/workspace/test/wikiDiseases.txt";
		BufferedReader in = new BufferedReader(new FileReader(path));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			String[] sublines = strLine.split("!");
			int tmp = Integer.parseInt(sublines[1]);
			diseases.put(sublines[0], tmp);
			in.readLine();
		}
		in.close();
	}

	public static void writeLocationWiki() throws IOException {
		FileWriter fstream = new FileWriter("wikiLocations.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<String> itr = allLocations.keySet().iterator();
		while (itr.hasNext()) {
			String id = itr.next();
			int tmp = allLocations.get(id);
			out.write(id + "!" + tmp);
			out.newLine();
		}
		out.close();
	}
	
	public static void writeWiki() throws IOException {
		FileWriter fstream = new FileWriter("wikiDiseases.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<String> itr = diseases.keySet().iterator();
		while (itr.hasNext()) {
			String id = itr.next();
			int tmp = diseases.get(id);
			out.write(id + "!" + tmp);
			out.newLine();
		}
		out.close();
	}

	public static void readWiki() {
		try {
			int i = 0;
			BufferedReader in = new BufferedReader(new FileReader(
					"/Users/Yun/Downloads/enwiki-20120403.xml"));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				// if(i>1000){
				// break;
				// }
				if (i % 10000000 == 0) {
					System.out.println("reading line:" + i);
				}
				String[] titles = strLine.split("<title>");
				if (titles.length == 2 && titles[1].length() < 50) {
					
					if (titles[1].length() < 5){
						System.out.println(titles[1]);
						continue;
					}
						
					String title = titles[1].substring(0,
							titles[1].length() - 8);
					title = title.toLowerCase();
					if (diseases.containsKey(title)) {
						diseases.put(title, 1);
//						System.out.println(title);
					}
					if(allLocations.containsKey(title)) {
						System.out.println(title);
						allLocations.put(title, 1);
					}
//					 System.out.println(title);
				}
//				 System.out.println(strLine);
				i++;
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

}
