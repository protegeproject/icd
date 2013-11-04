package printScript;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class printScript {
	public static HashMap<String, Boolean> hasLaterality = new HashMap<String, Boolean>();
	public static HashMap<String, String> sources = new HashMap<String, String>();
	public static LinkedList<String> ordering = new LinkedList<String>();
	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();
	
	public static void main(String[] args){

		readOrdering();
//		readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
//		label("Bones of the Upper Extremity", "Bones of the Upper Extremity's child");
//		label("Bones of the Lower Extremity", "Bones of the Lower Extremity's child");
//		label("Muscles of the Upper Extremity", "Muscles of the Upper Extremity's child");
//		label("Muscles of the Lower Extremity", "Muscles of the Lower Extremity's child");
//		label("Lymphatics of the Upper Extremity", "Lymphatics of the Upper Extremity's child");
//		label("Lymphatics of the Lower Extremity", "Lymphatics of the Lower Extremity's child");
//		label("Ligaments and Joints of the Upper Extremity", "Ligaments and Joints of the Upper Extremity's child");
//		label("Ligaments and Joints of the Lower Extremity", "Ligaments and Joints of the Lower Extremity's child");
//		label("Tendons of the Lower Extremity", "Tendons of the Lower Extremity's child");
//		label("Tendons of the Upper Extremity", "Tendons of the Upper Extremity's child");
//		label("Ear", "Ear's child");
//		label("Eye", "Eye's child");
//		readRobertLocs();
//		printResult();	
		
		Laterality.run();
		printFMA();

	}
	
	public static void printFMA() {
		Iterator<String> itr = ordering.iterator();
		while(itr.hasNext()){
			String loc = itr.next();
			if(loc.matches("Cardiovascular system")){
				System.out.print(loc+": ");
			}
//			
			if(!Laterality.containedLocations.contains(loc)){
				System.out.println();
			} else if(Laterality.bothLocations.contains(loc)){
				System.out.println("Y");
//				System.out.println(sources.get(loc));
			} else {
				System.out.println("N");
//				System.out.println(sources.get(loc));
			}

		}
	}
	
	public static void printResult() {
		Iterator<String> itr = ordering.iterator();
		while(itr.hasNext()){
			String loc = itr.next();
//			System.out.print(loc+": ");
			if(!hasLaterality.containsKey(loc)){
				System.out.println();
			} else if(hasLaterality.get(loc)){
//				System.out.println("Y");
				System.out.println(sources.get(loc));
			} else {
//				System.out.println("N");
				System.out.println(sources.get(loc));
			}

		}
	}
	
	public static void label(String loc, String source){
		hasLaterality.put(loc, true);
		sources.put(loc, source);
		HashSet<String> children = LocationParentChild.get(loc);
		if(!LocationParentChild.containsKey(loc)) {
			return;
		}
		Iterator<String> itr = children.iterator();
		while(itr.hasNext()){
			label(itr.next(),source);
		}
	}
	
	public static void readLocationHierarchy(String path) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String parent = strLine;
				strLine = in.readLine();
				String[] children = strLine.split("!");
				// if(children.length>7){
				// System.out.println(parent+children.length);
				// }
				for (int i = 0; i < children.length; i++) {
					if (LocationParentChild.containsKey(parent)) {
						Set<String> tmp = LocationParentChild.get(parent);
						tmp.add(children[i]);
					} else {
						HashSet<String> tmp = new HashSet<String>();
						tmp.add(children[i]);
						LocationParentChild.put(parent, tmp);
					}
				}
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readOrdering(){
		try {
			Workbook wb = WorkbookFactory.create(new File(
					"/Users/Yun/Documents/medical/ICD Specific Anatomy subset  with Topographical Anatomy 160613 mod 250713.xls"));
			Sheet sheet = wb.getSheet("Specific Anatomic Location");
			Row row = sheet.getRow(1);
			Cell cell;
			for (int i = 2; i < 6133; i++) {// 4 6133
				row = sheet.getRow(i);
				if (row == null)
					break;
				cell = row.getCell(14);
				if (cell == null)
					break;
				String loc = cell.getStringCellValue();
				ordering.add(loc);
//				System.out.println(loc);
			}
			System.out.println("finish reading locs");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	
	}
	
	public static void readRobertLocs(){
		try {
			Workbook wb = WorkbookFactory.create(new File(
					"/Users/Yun/Documents/medical/ICD Specific Anatomy subset  with Topographical Anatomy 160613 mod 250713.xls"));
			Sheet sheet = wb.getSheet("Topographical anatomy");
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
					String loc = cell.getStringCellValue();

//					System.out.println(loc);

					 cell = row.getCell(8);
					 String laterality = cell.getStringCellValue();
					 if(laterality.contains("B")){
						 hasLaterality.put(loc, true);
						 sources.put(loc, "Robert");
					 } else{
						 hasLaterality.put(loc, false);
						 sources.put(loc, "Robert");
					 }

					break;
				}

			}
			System.out.println("finish reading locs");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	
	}
}
