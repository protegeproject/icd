package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestJavaProperties {

	private static final String PROP_FILE_NAME = "/tmp/notes.counts";

	public static void main(String[] args) {
		writePropFile();
		//readPropertyFile();
	}

	public static void writePropFile() {
		printMemory();

		Properties props = new Properties();
		for (int i = 0; i < 100000; i++) {
			props.setProperty("prop" + i, Integer.toString(i));
		}
		try {
			File f = new File(PROP_FILE_NAME);
			OutputStream out = new FileOutputStream(f);
			props.store(out, "This is an optional header comment string");
		} catch (Exception e) {
			e.printStackTrace();
		}
		printMemory();
		System.out.println("Write to hashmap");
		writeToHashMap(props);
		printMemory();
	}

	private static void writeToHashMap(Properties props) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Object prop : props.keySet()) {
			String value = (String) props.get(prop);
			map.put((String) prop, Integer.valueOf(value));
		}
	}

	private static void readPropertyFile() {
		Properties props = new Properties();
		InputStream is = null;

		System.out.println("Before read prop file");
		printMemory();
		
		try {
			File f = new File(PROP_FILE_NAME);
			is = new FileInputStream(f);
		} catch (Exception e) {
			is = null;
		}

		try {
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("After read prop file");
		printMemory();
		
		writeToHashMap(props);
		
		System.out.println("After write to hash map");
		printMemory();
	}

	private static void printMemory() {
		{
			System.out.println("---------");
			int mb = 1024 * 1024;

			// Getting the runtime reference from system
			Runtime runtime = Runtime.getRuntime();

			System.out.println("##### Heap utilization statistics [MB] #####");

			// Print used memory
			System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

			// Print free memory
			System.out.println("Free Memory:" + runtime.freeMemory() / mb);

			// Print total available memory
			System.out.println("Total Memory:" + runtime.totalMemory() / mb);

			// Print Maximum available memory
			System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		}
	}

}
