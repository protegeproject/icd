package printScript;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Laterality {
	public static HashSet<String> origninLocations = new HashSet<String>();
	public static HashSet<String> locations = new HashSet<String>();
	
	public static HashSet<String> leftLocations = new HashSet<String>();
	public static HashSet<String> rightLocations = new HashSet<String>();
	public static HashSet<String> bothLocations = new HashSet<String>();
	public static HashSet<String> containedLocations = new HashSet<String>();
	
	public static HashMap<String, HashSet<String>> partialMapping1 = new HashMap<String, HashSet<String>>();
	public static HashMap<String, HashSet<String>> partialMapping2 = new HashMap<String, HashSet<String>>();
	
	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();

	public static void main(String[] args) {
		run();
	}
	
	public static void run(){
		System.out.println("run");
		readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		readLocations();
		readFMA();
		calculateBoth();
		System.out.println("total size:"+origninLocations.size());
		System.out.println("contained size:"+containedLocations.size());
		System.out.println("Laterality size:"+bothLocations.size());
//		if(leftLocations.contains("heart")){
//			System.out.println("left heart");
//		}
//		if(rightLocations.contains("heart")){
//			System.out.println("right heart");
//		}
//		leftLocations.addAll(rightLocations);
//		leftLocations.removeAll(bothLocations);

//		origninLocations.removeAll(containedLocations);
//		origninLocations.remove("root");
//		readFMA2();

		System.out.println("locations cannot map:"+origninLocations.size());
		
//		printLocationTree("root", "");
//		printSet(origninLocations);
//		print(partialMapping2);
//		System.out.println(origninLocations);
	}
	
	public static void printSet(HashSet<String> toprint){
		Iterator<String> itr = toprint.iterator();
		int i = 0;
		while(itr.hasNext()){
			String loc = itr.next();
//			if(loc.matches("mesoderm"))
			System.out.println(loc);
//			System.out.println(toprint.get(loc));
//			System.out.println();
			i++;
		}
		System.out.println(i);
	}
	
	public static void printLocationTree(String loc, String spaces){
		if(containedLocations.contains(loc)){
			if(bothLocations.contains(loc)){
				System.out.println(spaces+loc+":1");
			} else {
				System.out.println(spaces+loc+":0");
			}
		}else{
			System.out.println(spaces+loc+":none");
		}

		HashSet<String> children = LocationParentChild.get(loc);
		if(children==null){
			return;
		}
		Iterator<String> itr = children.iterator();
		spaces = spaces+"    ";
		while(itr.hasNext()){
			String child = itr.next();
			printLocationTree(child, spaces);
		}
	}
	
	public static void print(HashMap<String, HashSet<String>> toprint){
		Iterator<String> itr = toprint.keySet().iterator();
		int i = 0;
		while(itr.hasNext()){
			String loc = itr.next();
//			if(loc.matches("mesoderm"))
			System.out.println(loc);
			System.out.println(toprint.get(loc));
			System.out.println();
			i++;
		}
		System.out.println(i);
	}
	
	public static void calculateBoth(){
		Iterator<String> itr = leftLocations.iterator();
		while(itr.hasNext()){
			String loc = itr.next();
			if(!rightLocations.contains(loc)){
				continue;
			}
			if(!containedLocations.contains(loc)){
				continue;
			}
			bothLocations.add(loc);
		}
	}
	
	public static void readFMA2() {
		String path = "/Users/Yun/Downloads/fma_3.1.owl";
		int i=0;
		int j=0;
//		HashSet<String> toRemove = new HashSet<String>();
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				if (strLine.contains("<fma:name")) {
					String cut[] = strLine.split(">");
					cut = cut[1].split("<");
					String loc = cut[0].toLowerCase();

					Iterator<String> itr = origninLocations.iterator();
					while(itr.hasNext()){
						String origninLocation = itr.next();
						if(loc.contains(origninLocation)){
							if(partialMapping1.containsKey(origninLocation)){
								HashSet<String> tmp = partialMapping1.get(origninLocation);
								tmp.add(loc);
							} else {
								HashSet<String> tmp = new HashSet<String>();;
								tmp.add(loc);
								partialMapping1.put(origninLocation, tmp);
							}
							i++;
						}
						if(origninLocation.contains(loc)&&loc.length()>3&&loc.length()>origninLocation.length()/2){
							if(partialMapping2.containsKey(origninLocation)){
								HashSet<String> tmp = partialMapping2.get(origninLocation);
								tmp.add(loc);
							} else {
								HashSet<String> tmp = new HashSet<String>();;
								tmp.add(loc);
								partialMapping2.put(origninLocation, tmp);
							}
							j++;
						}
					}

				}
			}
			in.close();
//			System.out.println(i);
//			System.out.println(j);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readFMA() {
		String path = "/Users/Yun/Downloads/fma_3.1.owl";
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				if (strLine.contains("<fma:name")) {
					String cut[] = strLine.split(">");
					cut = cut[1].split("<");
					String loc = cut[0].toLowerCase();
					
					
					if(origninLocations.contains(loc)){
						containedLocations.add(loc);
					}
					if(locations.contains(loc)){
						String trueLoc;
						if(loc.contains("right")){
							trueLoc = loc.substring(6);
							rightLocations.add(trueLoc);
						} else {
							trueLoc = loc.substring(5);
							leftLocations.add(trueLoc);
						}
					}
				}
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readLocations() {
		String path = "/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt";
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));

			String strLine;
			while ((strLine = in.readLine()) != null) {
				String parent = strLine;
				parent = parent.toLowerCase();
				locations.add("right " + parent);
				locations.add("left " + parent);
//				if(parent.charAt(parent.length()-1)==' '){
//					System.out.println(parent);
//				}
				origninLocations.add(parent);
				strLine = in.readLine();
				String[] children = strLine.split("!");
				for (int i = 0; i < children.length; i++) {
					children[i] = children[i].toLowerCase();
					locations.add("right " + children[i]);
					locations.add("left " + children[i]);
					origninLocations.add(children[i]);
					if(children[i].charAt(children[i].length()-1)==' '){
						System.out.println(children[i]);
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
	
	public static void readLocationHierarchy(String path) {
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(path));

		String strLine;
		while ((strLine = in.readLine()) != null) {
			String parent = strLine;
			parent = parent.toLowerCase();
			strLine = in.readLine();
			String[] children = strLine.split("!");
			for (int i = 0; i < children.length; i++) {
				children[i] = children[i].toLowerCase();
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

}
