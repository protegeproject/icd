import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class GetIgnoreList {
	public static void readICDHierarchy(String path) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));

			String strLine;
			while ((strLine = in.readLine()) != null) {
				String disease = strLine.split("!")[0];
				if (disease.contains("other")
						&& disease.charAt(disease.indexOf("other") - 1) == ' ') {
					System.out.println(disease);
					// System.out.println(disease.indexOf("other"));
					// System.out.println();
				}

				if (disease.contains("Other")) {
					System.out.println(disease);
				}

				if (disease.contains("unspecified")) {
					System.out.println(disease);
					// System.out.println(disease.indexOf("other"));
					// System.out.println();
				}

				if (disease.contains("organized")) {
					System.out.println(disease);
					// System.out.println(disease.indexOf("other"));
					// System.out.println();
				}

			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		readICDHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/wikiDiseases.txt");
	}
}
