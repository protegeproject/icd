package Experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class prepareData {
	public static HashMap<String, String> diseasesDefinition = new HashMap<String, String>();
	public static HashMap<String, HashSet<String>> parentChildDiseases = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String> diseasesWiki = new HashMap<String, String>();
	public static HashMap<String, String> synonyms = new HashMap<String, String>();
	public static LinkedList<String> diseases = new LinkedList<String>();
	
	public static void main(String[] args) {
		 String root = "Dermatoses of the head, neck and oral cavity";
		 readSampleDiseases();
		 readDiseases();
//		// rebuildStructure(root);
//		 writeDiseases(root);
//		 writeGoldStanford();
		 
//		//
		 readWiki();
		 writeWikiDef(root);
//		 System.out.println("done");

//		readICDAnatomy();
//		writeSynonyms();
//		readSampleDiseases();
//		writeDiseases(root);
	}
	
	public static void writeGoldStanford() {
		FileWriter fstream;
		try {
			fstream = new FileWriter("SampleGoldStandard.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			
			Iterator<String> itr = diseases.iterator();
			while(itr.hasNext()){
				String dis = itr.next();
				out.write(dis);
				out.newLine();
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeSynonyms() {
		FileWriter fstream;
		try {
			fstream = new FileWriter("Synonyms.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			
			Iterator<String> itr = synonyms.keySet().iterator();
			while(itr.hasNext()){
				String loc = itr.next();
				String syn = synonyms.get(loc);
				out.write(loc+"!"+syn);
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void writeWikiDef(String root) {
		FileWriter fstream;
		try {
			int count = 0;
			fstream = new FileWriter("DiseaseDefiAndWiki.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			LinkedList<String> todo = new LinkedList<String>();
			todo.add(root);
			while (!todo.isEmpty()) {
				count++;
				String parent = todo.getFirst();
				todo.removeFirst();
				Iterator<String> itr = parentChildDiseases.get(parent)
						.iterator();
				out.write(parent);
				out.newLine();
				if(diseasesDefinition.containsKey(parent)){
					out.write(diseasesDefinition.get(parent));
					out.newLine();
					if(diseasesWiki.containsKey(parent)){
						out.write(diseasesWiki.get(parent));
					} else {
						out.newLine();
					}
					out.newLine();
				} else {
					System.out.println("!!!"+parent);
					out.write("none");
					out.newLine();
					out.write("none");
					out.newLine();
				}

				out.newLine();
				while (itr.hasNext()) {
					String next = itr.next();
					todo.add(next);
				}
			}
			out.close();
			System.out.println("total number of diseases:" + count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readWiki() {
		try {
			String path = "wikiDiseases.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String terms[] = strLine.split("!");
				if (terms[1].matches("1")) {
					String wiki = terms[0].replace(" ", "_");
					String link = "http://en.wikipedia.org/wiki/" + wiki;
					diseasesWiki.put(terms[0], link);
				} else {
					diseasesWiki.put(terms[0], "none");
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void rebuildStructure(String root) {
		HashMap<String, HashSet<String>> newParentChildDiseases = new HashMap<String, HashSet<String>>();
		LinkedList<String> todo = new LinkedList<String>();
		todo.add(root);
		while (!todo.isEmpty()) {
			String parent = todo.getFirst();
			todo.removeFirst();
			HashSet<String> children = new HashSet<String>();
			Iterator<String> itr = parentChildDiseases.get(parent).iterator();
			if (parentChildDiseases.get(parent).isEmpty()) {
				newParentChildDiseases.put(parent, children);
				continue;
			}
			while (itr.hasNext()) {
				String disease = itr.next();
				boolean toIgnore = false;
				if (disease.contains("other")
						&& disease.charAt(disease.indexOf("other") - 1) == ' ') {
					System.out.println(disease);
					toIgnore = true;
				}

				if (disease.contains("Other")) {
					System.out.println(disease);
					toIgnore = true;
				}

				if (disease.contains("unspecified")) {
					System.out.println(disease);
					toIgnore = true;
				}

				if (disease.contains("organized")) {
					System.out.println(disease);
					toIgnore = true;
				}

				if (!toIgnore) {
					todo.add(disease);
					children.add(disease);
				} else {
					HashSet<String> childs = getValidChildren(disease);
					todo.addAll(childs);
					children.addAll(childs);
				}

			}
			newParentChildDiseases.put(parent, children);
		}
		parentChildDiseases = newParentChildDiseases;
	}

	public static HashSet<String> getValidChildren(String root) {
		HashSet<String> children = new HashSet<String>();
		Iterator<String> itr = parentChildDiseases.get(root).iterator();
		if (parentChildDiseases.get(root).isEmpty()) {
			return children;
		}
		while (itr.hasNext()) {
			String disease = itr.next();
			boolean toIgnore = false;
			if (disease.contains("other")
					&& disease.charAt(disease.indexOf("other") - 1) == ' ') {
				System.out.println(disease);
				toIgnore = true;
			}

			if (disease.contains("Other")) {
				System.out.println(disease);
				toIgnore = true;
			}

			if (disease.contains("unspecified")) {
				System.out.println(disease);
				toIgnore = true;
			}

			if (disease.contains("organized")) {
				System.out.println(disease);
				toIgnore = true;
			}

			if (!toIgnore) {
				children.add(disease);
			} else {
				children.addAll(getValidChildren(disease));
			}

		}
		return children;
	}

	public static void writeDiseases(String root) {
		FileWriter fstream;
		try {
			int count = 0;
			fstream = new FileWriter("SampleICDStrcuture3.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			LinkedList<String> todo = new LinkedList<String>();
			todo.add(root);
			while (!todo.isEmpty()) {
				count++;
				String parent = todo.getFirst();
//				System.out.println(parent);
				todo.removeFirst();
				Iterator<String> itr = parentChildDiseases.get(parent)
						.iterator();
				out.write(parent);
				out.newLine();
				if (parentChildDiseases.get(parent).isEmpty()) {
					out.newLine();
					out.newLine();
					continue;
				}
				String first = itr.next();
				todo.add(first);
				out.write(first);
				while (itr.hasNext()) {
					String next = itr.next();
					todo.add(next);
					out.write("!" + next);
				}
				out.newLine();
				out.newLine();
			}
			out.close();
			System.out.println("total number of diseases:" + count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readICDAnatomy() {
		Workbook wb;
		try {
			wb = WorkbookFactory
					.create(new File(
							"/Users/Yun/Documents/medical/X_Chapter_Parameters_Value_Sets.V15.xlsx"));
			Sheet sheet = wb.getSheet("Specific Anatomic Location");
			Row row = sheet.getRow(1);
			Cell cell = row.getCell(1);
			for (int i = 1; i < 2500; i++) {
				row = sheet.getRow(i);
				if (row == null)
					break;
				String loc = row.getCell(14).getStringCellValue();
				cell = row.getCell(18);
				if (cell != null) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().matches(""))
						continue;
					String synonym = cell.getStringCellValue();
					System.out.println(synonym);
					synonyms.put(loc, synonym);
					// int tmp = Integer.parseInt(cell.getStringCellValue());
					// Site tmpSite = sites.get(tmp);
					// if(tmpSite==null)
					// {
					// System.out.println("problem:"+tmp);
					// continue;
					// }
					// tmpSite.fromICD=true;
				}
			}
			System.out.println("done.");
		} catch (InvalidFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void readSampleDiseases() {
		try {
			Workbook wb = WorkbookFactory.create(new File(
					"/Users/Yun/Documents/medical/Sample sanctioning site rules for topography.xls"));
			Sheet sheet = wb.getSheet("Sanctioning rules");
			String parents[] = new String[13];
			Row row = sheet.getRow(1);
			Cell cell;
			for (int i = 2; i < 6133; i++) {// 4 6133
				row = sheet.getRow(i);
				if (row == null)
					break;
				for (int j = 2; j < 12; j++) {
					cell = row.getCell(j);
					if (cell == null)
						continue;
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().matches(""))
						continue;
					String disease = cell.getStringCellValue();
					diseases.add(disease);
//					System.out.println(disease);

					// cell = row.getCell(23);
					// if (cell != null && cell.getStringCellValue().length() >
					// 3) {
					// // System.out.println(cell.getStringCellValue());
					// diseasesKnownLocation.put(disease, 1);
					// } else {
					// diseasesKnownLocation.put(disease, 0);
					// }
					if (parents[j - 1] != null) {
						parentChildDiseases.get((parents[j - 1])).add(disease);
					}
					parents[j] = disease;
					if(parentChildDiseases.containsKey(disease)) {
						System.out.println("duplicate: "+disease);
					} else {
						parentChildDiseases.put(disease, new HashSet<String>());
					}

					break;
				}

			}
			System.out.println("finish reading diseases");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static void readDiseases() {
		try {
			Workbook wb = WorkbookFactory.create(new File(
					"/Users/Yun/Documents/medical/skin.xls"));
			
			Sheet sheet = wb.getSheet("Authoring template");
			String parents[] = new String[13];
			Row row = sheet.getRow(4);
			Cell cell = row.getCell(3);
			for (int i = 2; i < 6133; i++) {// 4 6133
				row = sheet.getRow(i);
				if (row == null)
					break;
				for (int j = 2; j < 13; j++) {
					cell = row.getCell(j);
					if (cell == null)
						continue;
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().matches(""))
						continue;
					String disease = cell.getStringCellValue();

					HSSFCell tmpcell = (HSSFCell) cell;
					short color = tmpcell.getCellStyle()
							.getFillForegroundColor();
					if (color == 29) {
						// System.out.println(color);
						// System.out.println(disease);
						continue;
					}

					// diseases.put(disease, 0);
					cell = row.getCell(16);
					if (cell != null && cell.getStringCellValue().length() > 3) {
						// System.out.println(cell.getStringCellValue());
						diseasesDefinition.put(disease,
								cell.getStringCellValue());
					} else if(!diseasesDefinition.containsKey(disease)){
						diseasesDefinition.put(disease, "none");
					}
					// cell = row.getCell(23);
					// if (cell != null && cell.getStringCellValue().length() >
					// 3) {
					// // System.out.println(cell.getStringCellValue());
					// diseasesKnownLocation.put(disease, 1);
					// } else {
					// diseasesKnownLocation.put(disease, 0);
					// }
					if (parents[j - 1] != null) {
						parentChildDiseases.get((parents[j - 1])).add(disease);
					}
					parents[j] = disease;
					parentChildDiseases.put(disease, new HashSet<String>());
					break;
				}

			}
			System.out.println("finish reading diseases");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
