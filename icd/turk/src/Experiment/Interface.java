package Experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;

import edu.mit.csail.uid.turkit.util.Base64;
import edu.mit.csail.uid.turkit.util.U;

public class Interface {

	// public static String id = "AKIAIZCFLKY2J2WPBKZA";
	// public static String secretKey =
	// "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";

	public static String id = "AKIAIBQSY2CDIWNDAAUQ";
	public static String secretKey = "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";
	public static boolean sandbox;
	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();

	public static HashMap<String, String> diseasesWiki = new HashMap<String, String>();
	public static HashMap<String, String> locationsWiki = new HashMap<String, String>();
	public static HashMap<String, String> synonyms = new HashMap<String, String>();

	// MyRequesterService service = new MyRequesterService(
	// new PropertiesClientConfig(
	// "/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));
	public static MyRequesterService service = new MyRequesterService(
			new PropertiesClientConfig(
					"/Applications/java-aws-mturk-1.6.2/samples/mturk2.properties"));

	public static void readSynonyms() {
		try {
			String path = "Synonyms.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String terms[] = strLine.split("!");
				String loc = terms[0];
				String synonym = terms[1];
				synonyms.put(loc, synonym);
			}

			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void readWiki() {
		readSynonyms();
		try {
			String path = "wikiDiseases.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String terms[] = strLine.split("!");
				String disease = terms[0];
				if (terms[1].matches("1")) {
					String wiki = terms[0].replace(" ", "_");
					String link = "http://en.wikipedia.org/wiki/" + wiki;
					diseasesWiki.put(disease, link);
					// System.out.println(link);
				} else {
					diseasesWiki.put(disease, "none");
				}
			}
			in.close();

			path = "wikiLocations.txt";
			in = new BufferedReader(new FileReader(path));
			while ((strLine = in.readLine()) != null) {
				String terms[] = strLine.split("!");
				String location = terms[0];
				if (terms[1].matches("1")) {
					String wiki = terms[0].replace(" ", "_");
					String link = "http://en.wikipedia.org/wiki/" + wiki;
					locationsWiki.put(location, link);
					// System.out.println(link);
				} else {
					locationsWiki.put(location, "none");
				}
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	/*
	 * A date formatter for XML DateTimes.
	 */
	public static final SimpleDateFormat xmlTimeFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

	static {
		xmlTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Gets the current time as an XML DateTime string.
	 */
	public static String getTimestamp() {
		return xmlTimeFormat.format(new Date());
	}

	/**
	 * Computes a Signature for use in MTurk REST requests.
	 */
	public static String getSignature(String service, String operation,
			String timestamp, String secretKey) throws Exception {

		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM));
		return Base64.encodeBytes(mac.doFinal((service + operation + timestamp)
				.getBytes()));
	}

	/**
	 * Performs a SOAP request on MTurk. The <code>paramsList</code> must be a
	 * sequence of strings of the form a1, b1, a2, b2, a3, b3 ... Where aN is a
	 * parameter name, and bN is the value for that parameter. Most common
	 * parameters have suitable default values, namely: Version, Timestamp,
	 * Query, and Signature.
	 */
	public static String soapRequest(String operation, String XMLstring)
			throws Exception {

		URL url = new URL(
				sandbox ? "https://mechanicalturk.sandbox.amazonaws.com/"
						: "https://mechanicalturk.amazonaws.com/");

		String timestamp = getTimestamp();
		String sig = getSignature("AWSMechanicalTurkRequester", operation,
				timestamp, secretKey);

		StringBuffer x = new StringBuffer();
		x.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\t\t<soapenv:Envelope\n\t\t     xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n\t\t     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n\t\t     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\t\t  <soapenv:Body>\n\t\t    <");
		x.append(operation);
		x.append(" xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkRequester/2008-08-02\">");
		x.append("<AWSAccessKeyId>");
		x.append(id);
		x.append("</AWSAccessKeyId>");
		x.append("<Timestamp>");
		x.append(timestamp);
		x.append("</Timestamp>");
		x.append("<Signature>");
		x.append(sig);
		x.append("</Signature>");
		x.append("<Request>");
		x.append(XMLstring);
		x.append("</Request>");
		x.append("</");
		x.append(operation);
		x.append(">");
		x.append("</soapenv:Body></soapenv:Envelope>");

		String soap = x.toString();

		for (int t = 0; t < 100; t++) {
			try {
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.addRequestProperty("Content-Type",
						"application/soap+xml; charset=utf-8");
				String s = U.webPost(c, soap);
				Matcher m = Pattern.compile(
						"(?msi)<soapenv:Body>(.*)</soapenv:Body>").matcher(s);
				if (m.find()) {
					s = m.group(1);
					s = s.replaceFirst(" xmlns=\"[^\"]*\"", "");
					return s;
				} else {
					throw new IllegalArgumentException(
							"unexpected response from MTurk: " + s);
				}
			} catch (IOException e) {
				if (e.getMessage().startsWith(
						"Server returned HTTP response code: 503")) {
					Thread.sleep(100 + (int) Math.min(3000, Math.pow(t, 3)));
				} else {
					throw e;
				}
			}
		}
		throw new Exception("MTurk seems to be down.");

	}

	public static String createOtherThanQuestion(String diseaseC,
			HashSet<String> locationP, String definitionC, String wikiC,
			String diseaseP) {
		String question;
		String parentInf;
		if (locationP.size() > 0) {
			String locations[] = (String[]) locationP.toArray(new String[0]);
			Iterator<String> itr = locationP.iterator();
			int size = locationP.size();
			int i = 1;
			String locationString = "\"<b>" + itr.next() + "</b>\"";
			while (itr.hasNext()) {
				if (i == size - 1) {
					locationString = locationString + ", and \"<b>"
							+ itr.next() + "</b>\"";
				} else {
					locationString = locationString + ", \"<b>" + itr.next()
							+ "</b>\"";
				}
				i++;
			}

			parentInf = "<FormattedContent><![CDATA[We know that \"<b>"
					+ diseaseC + "</b>\" is a kind of \"<b>" + diseaseP
					+ "</b>\"; and we think \"<b>" + diseaseP
					+ "</b>\" can occur in " + locationString
					+ ". ]]></FormattedContent>";

			locationString.replaceAll("and", "and/or");
			question = "<FormattedContent><![CDATA[\"<b>" + diseaseC
					+ "</b>\" can <u>only</u> occur in part of "
					+ locationString + ".]]></FormattedContent>";

			return createQuestion(diseaseC, locations, definitionC, wikiC,
					question, parentInf);

		} else {
			return "Error!";
		}
	}

	public static String createSameQuestion(String diseaseC,
			HashSet<String> locationP, String definitionC, String wikiC,
			String diseaseP) {
		String question;
		String parentInf;
		if (locationP.size() > 0) {
			String locations[] = (String[]) locationP.toArray(new String[0]);
			Iterator<String> itr = locationP.iterator();
			int size = locationP.size();
			int i = 1;
			String locationString = "\"<b>" + itr.next() + "</b>\"";
			while (itr.hasNext()) {
				if (i == size - 1) {
					locationString = locationString + ", and \"<b>"
							+ itr.next() + "</b>\"";
				} else {
					locationString = locationString + ", \"<b>" + itr.next()
							+ "</b>\"";
				}
				i++;
			}

			parentInf = "<FormattedContent><![CDATA[We know that \"<b>"
					+ diseaseC + "</b>\" is a kind of \"<b>" + diseaseP
					+ "</b>\"; and we think \"<b>" + diseaseP
					+ "</b>\" can occur in " + locationString
					+ ". ]]></FormattedContent>";
			question = "<FormattedContent><![CDATA[The possible finding sites (anatomic locations) of \"<b>"
					+ diseaseC
					+ "</b>\" are <u>exactly</u> "
					+ locationString
					+ ".]]></FormattedContent>";

			return createQuestion(diseaseC, locations, definitionC, wikiC,
					question, parentInf);

		} else {
			return "Error!";
		}
	}

	public static String createTestSameQuestion(String diseaseC,
			HashSet<String> locationP, String definitionC, String wikiC,
			String diseaseP) {
		String selection1, selection2, selection3;
		String parentInf;
		if (locationP.size() > 0) {
			String locations[] = (String[]) locationP.toArray(new String[0]);
			Iterator<String> itr = locationP.iterator();
			int size = locationP.size();
			int i = 1;
			String locationString = "\"<b>" + itr.next() + "</b>\"";
			while (itr.hasNext()) {
				String loc = itr.next();
				if (loc.contains("Body Regions")) {
					loc = "Body Regions(Whole surface area of human)";
				}
				if (i == size - 1) {
					locationString = locationString + ", and \"<b>" + loc
							+ "</b>\"";
				} else {
					locationString = locationString + ", \"<b>" + loc
							+ "</b>\"";
				}
				i++;
			}

			parentInf = "<FormattedContent><![CDATA[We know that \"<b>"
					+ diseaseC + "</b>\" is a kind of \"<b>" + diseaseP
					+ "</b>\"; and we think \"<b>" + diseaseP
					+ "</b>\" can occur in " + locationString
					+ ". ]]></FormattedContent>";

			selection1 = "<FormattedContent><![CDATA[\"<b>"
					+ diseaseC
					+ "</b>\" can occur in anatomic locations <u>other than</u> "
					+ locationString + ".]]></FormattedContent>";

			selection2 = "<FormattedContent><![CDATA[\"<b>" + diseaseC
					+ "</b>\" can occur in <u>exactly</u> " + locationString
					+ ".]]></FormattedContent>";
			
			locationString.replace("and", "and/or");

			selection3 = "<FormattedContent><![CDATA[\"<b>" + diseaseC
					+ "</b>\" can occur in <u>some part of</u> " + locationString
					+ ".]]></FormattedContent>";

			// selection4 = "<FormattedContent><![CDATA[\"<b>"
			// + diseaseP
			// + "</b>\" can occur in <u>part of</u> "
			// + locationString
			// + ".]]></FormattedContent>";

			return createTestQuestion(diseaseC, locations, definitionC, wikiC,
					selection1, selection2, selection3, parentInf);

		} else {
			return "Error!";
		}
	}

	public static String createTestQuestion(String disease, String[] locations,
			String definition, String wiki, String selection1,
			String selection2, String selection3, String parentInf) {

		String title = "Pick some anatomic locations for a disease";
		String description = "You will learn medical knowledge. If you get the right answer, we will pay you 8 cents as bonus!";
		int numAssignments = 10;
		double reward = 0.02;

		String locationDefs = "";
		String disDef = "";
		String locationString = "";
		String wikiLink = "<p>This is the Wikipedia Page for " + "<a href='"
				+ wiki + "' target='_blank'>";

		if (wiki.matches("none")) {
			wikiLink = "<p>This is the Google search result for " + "<a href='"
					+ "https://www.google.com/search?q="
					+ disease.replace(" ", "+") + "' target='_blank'>";
		}
		for (int i = 0; i < locations.length; i++) {
			if (i == locations.length - 1) {
				locationString = locationString + " and \"" + locations[i]
						+ "\" ";
			} else {
				locationString = locationString + ", \"" + locations[i] + "\"";
			}

			if (LocationParentChild.containsKey(locations[i])) {
				HashSet<String> childrenNames = LocationParentChild
						.get(locations[i]);
				String childrenName = "";
				Iterator<String> itr = childrenNames.iterator();
				int k = 1;
				childrenName = "\"" + itr.next() + "\"";
				while (itr.hasNext() && k <= 5) {
					childrenName = childrenName + ", \"" + itr.next() + "\"";
					k++;
				}
				locationDefs = locationDefs + "\"" + locations[i]
						+ "\" contains: " + childrenName
						+ " and other similar anatomic locations. ";
			} else {
				locationDefs = locationDefs + "\"" + locations[i] + "\": ";
			}
			System.out.print(locations[i] + "!");
			if (locationsWiki.get(locations[i]).matches("none")) {
				locationDefs = locationDefs + "  <a href='"
						+ "https://www.google.com/search?q="
						+ locations[i].replace(" ", "+")
						+ "' target='_blank'>Google search results of "
						+ locations[i] + "</a><br/>";
			} else {
				locationDefs = locationDefs + "  <a href='"
						+ locationsWiki.get(locations[i])
						+ "' target='_blank'>Wikipedia page of " + locations[i]
						+ "</a><br/>";
			}
		}
		if (!definition.matches("none")) {
			disDef = "<Text>" + "The definition of \"" + disease + "\" is: "
					+ definition + "." + "</Text>";
		}

		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			// "<QualificationRequirement>"
			// +
			// "<QualificationTypeId>2F1QJWKUDD8XADTFD2Q0G6UTO95ALH</QualificationTypeId>"
			// + "<Comparator>Exists</Comparator>"
			// +"</QualificationRequirement>"

			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>Pick some anatomic locations for a disease</Title>"
					+ "<Text>"
					+ "Please help us to identify the location of \""
					+ disease
					+ "\". You will learn medical knowledge as well! "
					+ "</Text>"
					+ parentInf
					+ "<Text>"
					+ "For your convenience we have provided some information about \""
					+ disease
					+ "\""
					+ locationString
					+ " below the question."
					+ "</Text>"
					+ "<Text>If you get the right answer, we will grant you 8 cents as bonus.</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<Text>Choose the right answer:</Text>"
					+ "</QuestionContent>"
					+ "<AnswerSpecification>"
					+ "<SelectionAnswer>"
					+ "<StyleSuggestion>radiobutton</StyleSuggestion>"
					+ "<Selections>"
					+ "<Selection>"
					+ "<SelectionIdentifier>other</SelectionIdentifier>"
					+ selection1
					+ "</Selection>"
					+ "<Selection>"
					+ "<SelectionIdentifier>same</SelectionIdentifier>"
					+ selection2
					+ "</Selection>"
					+ "<Selection>"
					+ "<SelectionIdentifier>part</SelectionIdentifier>"
					+ selection3
					+ "</Selection>"
					+ "</Selections>"
					+ "</SelectionAnswer>"
					+ "</AnswerSpecification>"
					+ "</Question>"
					+ "<Overview>"
					+ disDef
					+ "<FormattedContent><![CDATA["
					+ wikiLink
					+ disease
					+ "</a><br/></p><p>Here is information about the anatomic locations:<br/>"
					+ locationDefs
					+ "</p>]]></FormattedContent>"
					+ "</Overview>" + "</QuestionForm>";

			HIT hit = service.createHIT(title, description, reward, xml,
					numAssignments);
			// if (!question.contains("exactly same of the possible"))
			// questionLogs.getLast().HITID = hit.getHITId();
			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL()
					+ "/mturk/preview?groupId=" + hit.getHITTypeId());
			return hit.getHITId();
		} catch (ServiceException e) {
			System.err.println(e.getLocalizedMessage());
			return null;
		}
	}

	public static String createQuestion(String disease, String[] locations,
			String definition, String wiki, String question, String parentInf) {

		String title = "Pick some anatomic locations for a disease";
		String description = "You will learn medical knowledge.";
		int numAssignments = 10;
		double reward = 0.02;

		String locationDefs = "";
		String disDef = "";
		String locationString = "";
		String wikiLink = "<p>This is the Wikipedia Page for " + "<a href='"
				+ wiki + "' target='_blank'>";

		if (wiki.matches("none")) {
			wikiLink = "<p>This is the Google search result for " + "<a href='"
					+ "https://www.google.com/search?q="
					+ disease.replace(" ", "+") + "' target='_blank'>";
		}
		for (int i = 0; i < locations.length; i++) {
			if (i == locations.length - 1) {
				locationString = locationString + " and \"" + locations[i]
						+ "\" ";
			} else {
				locationString = locationString + ", \"" + locations[i] + "\"";
			}

			if (LocationParentChild.containsKey(locations[i])) {
				HashSet<String> childrenNames = LocationParentChild
						.get(locations[i]);
				String childrenName = "";
				Iterator<String> itr = childrenNames.iterator();
				int k = 1;
				childrenName = "\"" + itr.next() + "\"";
				while (itr.hasNext() && k <= 5) {
					childrenName = childrenName + ", \"" + itr.next() + "\"";
					k++;
				}
				locationDefs = locationDefs + "\"" + locations[i]
						+ "\" contains: " + childrenName
						+ " and other similar anatomic locations. ";
			} else {
				locationDefs = locationDefs + "\"" + locations[i] + "\": ";
			}
			// System.out.print(locations[i] + "!");
			if (locationsWiki.get(locations[i]).matches("none")) {
				locationDefs = locationDefs + "  <a href='"
						+ "https://www.google.com/search?q="
						+ locations[i].replace(" ", "+")
						+ "' target='_blank'>Google search results of "
						+ locations[i] + "</a><br/>";
			} else {
				locationDefs = locationDefs + "  <a href='"
						+ locationsWiki.get(locations[i])
						+ "' target='_blank'>Wikipedia page of " + locations[i]
						+ "</a><br/>";
			}
		}
		if (!definition.matches("none")) {
			disDef = "<Text>" + "The definition of \"" + disease + "\" is: "
					+ definition + "." + "</Text>";
		}

		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>Pick some anatomic locations for a disease</Title>"
					+ "<Text>"
					+ "Please help us to identify the location of \""
					+ disease
					+ "\". You will learn medical knowledge as well! "
					+ "</Text>"
					+ parentInf
					+ "<Text>"
					+ "For your convenience we have provided some information about \""
					+ disease
					+ "\""
					+ locationString
					+ " below the question."
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ question
					+ "</QuestionContent>"
					+ "<AnswerSpecification>"
					+ "<SelectionAnswer>"
					+ "<StyleSuggestion>radiobutton</StyleSuggestion>"
					+ "<Selections>"
					+ "<Selection>"
					+ "<SelectionIdentifier>true</SelectionIdentifier>"
					+ "<Text>True</Text>"
					+ "</Selection>"
					+ "<Selection>"
					+ "<SelectionIdentifier>false</SelectionIdentifier>"
					+ "<Text>false</Text>"
					+ "</Selection>"
					+ "</Selections>"
					+ "</SelectionAnswer>"
					+ "</AnswerSpecification>"
					+ "</Question>"
					+ "<Overview>"
					+ disDef
					+ "<FormattedContent><![CDATA["
					+ wikiLink
					+ disease
					+ "</a><br/></p><p>Here is information about the anatomic locations:<br/>"
					+ locationDefs
					+ "</p>]]></FormattedContent>"
					+ "</Overview>" + "</QuestionForm>";

			HIT hit = service.createHIT(title, description, reward, xml,
					numAssignments);
			// if (!question.contains("exactly same of the possible"))
			// questionLogs.getLast().HITID = hit.getHITId();
			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL()
					+ "/mturk/preview?groupId=" + hit.getHITTypeId());
			return hit.getHITId();
		} catch (ServiceException e) {
			System.err.println(e.getLocalizedMessage());
			return null;
		}
	}

	public static String createTestMultipleQuestion(String disease,
			String[] locations, String definition, String wiki) {
		System.out.println(disease);

		String title = "Pick some anatomic locations for a disease";
		String description = "You will learn medical knowledge. If you get the right answer, you will get 2 cents as bonus.";
		int numAssignments = 10;
		double reward = 0.05;
		String selections = "";
		String locationDefs = "";
		String locationString = "";
		String disDef = "";
		String wikiLink = "<p>This is the Wikipedia Page for " + "<a href='"
				+ wiki + "' target='_blank'>";

		if (!definition.matches("none")) {
			disDef = "<Text>" + "The definition of \"" + disease + "\" is: "
					+ definition + "." + "</Text>";
		}

		if (wiki.matches("none")) {
			wikiLink = "<p>This is the Google search result for " + "<a href='"
					+ "https://www.google.com/search?q="
					+ disease.replace(" ", "+") + "' target='_blank'>";
		}
		for (int i = 0; i < locations.length; i++) {
			if (!locations[i].matches("Other")) {
				if (i == locations.length - 2) {
					locationString = locationString + " and \"" + locations[i]
							+ "\" ";
				} else {
					locationString = locationString + ", \"" + locations[i]
							+ "\"";
				}
				String loc;
				if (synonyms.containsKey(locations[i])) {
					loc = locations[i] + "(" + synonyms.get(locations[i]) + ")";
				} else {
					loc = locations[i];
				}
				selections = selections + "<Selection>"
						+ "<SelectionIdentifier>" + locations[i]
						+ "</SelectionIdentifier>" + "<Text>" + loc
						+ "</Text></Selection>";
				if (LocationParentChild.containsKey(locations[i])) {
					HashSet<String> childrenNames = LocationParentChild
							.get(locations[i]);
					String childrenName = "";
					Iterator<String> itr = childrenNames.iterator();
					int k = 1;
					childrenName = itr.next();
					while (itr.hasNext() && k <= 5) {
						childrenName = childrenName + ", " + itr.next();
						k++;
					}
					locationDefs = locationDefs + "\"" + locations[i]
							+ "\" contains: " + childrenName
							+ " and other similar anatomic locations. ";
				} else {
					locationDefs = locationDefs + locations[i] + ": ";
				}
				System.out.print(locations[i] + "!");

				if (locationsWiki.get(locations[i]).matches("none")) {
					locationDefs = locationDefs + "  <a href='"
							+ "https://www.google.com/search?q="
							+ locations[i].replace(" ", "+")
							+ "' target='_blank'>Google search results of '"
							+ locations[i] + "'</a><br/>";
				} else {
					locationDefs = locationDefs + "  <a href='"
							+ locationsWiki.get(locations[i])
							+ "' target='_blank'>Wikipedia page of '"
							+ locations[i] + "'</a><br/>";
				}
			} else {
				String otherString = "some other anatomic locations";
				selections = selections + "<Selection>"
						+ "<SelectionIdentifier>Other" 
						+ "</SelectionIdentifier>" + "<Text>"+otherString
						+ "</Text></Selection>";
			}
		}
		selections = selections + "<Selection>"
				+ "<SelectionIdentifier>none</SelectionIdentifier>"
				+ "<Text>None of the above applies</Text>" + "</Selection>";
		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>Pick some anatomic locations for a disease</Title>"
					+ "<Text>"
					+ "Please help us to identify the location of \""
					+ disease
					+ "\". You will learn medical knowledge as well! "
					+ "</Text>"
					+ "<Text>"
					+ "For your convenience we have provided some information about \""
					+ disease
					+ "\""
					+ locationString
					+ " below the question."
					+ "</Text>"
					+ "<Text>"
					+ "If you correctly figure out whether \""
					+ disease
					+ "\" can occur in anatomic locations other than the following list, we will get 2 cents as bonus."
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<FormattedContent><![CDATA[\"<b>"
					+ disease
					+ "</b>\" can occur in the following locations(choose all that apply):"
					+ "]]></FormattedContent>"
					+ "</QuestionContent>"
					+ "<AnswerSpecification>"
					+ "<SelectionAnswer>"
					+ "<StyleSuggestion>checkbox</StyleSuggestion>"
					+ "<Selections>"
					+ selections
					+ "</Selections>"
					+ "</SelectionAnswer>"
					+ "</AnswerSpecification>"
					+ "</Question>"
					+ "<Overview>"
					+ disDef
					+ "<FormattedContent><![CDATA["
					+ wikiLink
					+ disease
					+ "</a><br/></p><p>Here is information about the anatomic locations:<br/>"
					+ locationDefs
					+ "</p>]]></FormattedContent>"
					+ "</Overview>" + "</QuestionForm>";

			HIT hit = service.createHIT(title, description, reward, xml,
					numAssignments);

			System.out.println();
			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL()
					+ "/mturk/preview?groupId=" + hit.getHITTypeId());
			return hit.getHITId();
		} catch (ServiceException e) {
			System.err.println(e.getLocalizedMessage());
			return null;
		}
	}

	public static String createMultipleQuestion(String disease,
			String[] locations, String definition, String wiki) {
		System.out.println(disease);

		String title = "Pick some anatomic locations for a disease";
		String description = "You will learn medical knowledge.";
		int numAssignments = 10;
		double reward = 0.04;
		String selections = "";
		String locationDefs = "";
		String locationString = "";
		String disDef = "";
		String wikiLink = "<p>This is the Wikipedia Page for " + "<a href='"
				+ wiki + "' target='_blank'>";

		if (!definition.matches("none")) {
			disDef = "<Text>" + "The definition of \"" + disease + "\" is: "
					+ definition + "." + "</Text>";
		}

		if (wiki.matches("none")) {
			wikiLink = "<p>This is the Google search result for " + "<a href='"
					+ "https://www.google.com/search?q="
					+ disease.replace(" ", "+") + "' target='_blank'>";
		}
		for (int i = 0; i < locations.length; i++) {
			if (!locations[i].matches("other")) {
				if (i == locations.length - 2) {
					locationString = locationString + " and \"" + locations[i]
							+ "\" ";
				} else {
					locationString = locationString + ", \"" + locations[i]
							+ "\"";
				}
				String loc;
				if (synonyms.containsKey(locations[i])) {
					loc = locations[i] + "(" + synonyms.get(locations[i]) + ")";
				} else {
					loc = locations[i];
				}
				selections = selections + "<Selection>"
						+ "<SelectionIdentifier>" + locations[i]
						+ "</SelectionIdentifier>" + "<Text>" + loc
						+ "</Text></Selection>";
				if (LocationParentChild.containsKey(locations[i])) {
					HashSet<String> childrenNames = LocationParentChild
							.get(locations[i]);
					String childrenName = "";
					Iterator<String> itr = childrenNames.iterator();
					int k = 1;
					childrenName = itr.next();
					while (itr.hasNext() && k <= 5) {
						childrenName = childrenName + ", " + itr.next();
						k++;
					}
					locationDefs = locationDefs + "\"" + locations[i]
							+ "\" contains: " + childrenName
							+ " and other similar anatomic locations. ";
				} else {
					locationDefs = locationDefs + locations[i] + ": ";
				}
				System.out.print(locations[i] + "!");

				if (!locationsWiki.containsKey(locations[i])||locationsWiki.get(locations[i]).matches("none")) {
					locationDefs = locationDefs + "  <a href='"
							+ "https://www.google.com/search?q="
							+ locations[i].replace(" ", "+")
							+ "' target='_blank'>Google search results of '"
							+ locations[i] + "'</a><br/>";
				} else {
					locationDefs = locationDefs + "  <a href='"
							+ locationsWiki.get(locations[i])
							+ "' target='_blank'>Wikipedia page of '"
							+ locations[i] + "'</a><br/>";
				}
			} else {
				String otherString = "some other anatomic locations";
				selections = selections + "<Selection>"
						+ "<SelectionIdentifier>other" 
						+ "</SelectionIdentifier>" + "<Text>"+otherString
						+ "</Text></Selection>";
			}
		}
		selections = selections + "<Selection>"
				+ "<SelectionIdentifier>none</SelectionIdentifier>"
				+ "<Text>None of the above applies</Text>" + "</Selection>";
		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>Pick some anatomic locations for a disease</Title>"
					+ "<Text>"
					+ "Please help us to identify the location of \""
					+ disease
					+ "\". You will learn medical knowledge as well! "
					+ "</Text>"
					+ "<Text>"
					+ "For your convenience we have provided some information about \""
					+ disease
					+ "\""
					+ locationString
					+ " below the question."
					+ "</Text>"
					+ "<Text>"
					+ "If you get the right answer, we will get 2 cents as bonus."
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<FormattedContent><![CDATA[\"<b>"
					+ disease
					+ "</b>\" can occur in the following locations(choose all that apply):"
					+ "]]></FormattedContent>"
					+ "</QuestionContent>"
					+ "<AnswerSpecification>"
					+ "<SelectionAnswer>"
					+ "<StyleSuggestion>checkbox</StyleSuggestion>"
					+ "<Selections>"
					+ selections
					+ "</Selections>"
					+ "</SelectionAnswer>"
					+ "</AnswerSpecification>"
					+ "</Question>"
					+ "<Overview>"
					+ disDef
					+ "<FormattedContent><![CDATA["
					+ wikiLink
					+ disease
					+ "</a><br/></p><p>Here is information about the anatomic locations:<br/>"
					+ locationDefs
					+ "</p>]]></FormattedContent>"
					+ "</Overview>" + "</QuestionForm>";

			HIT hit = service.createHIT(title, description, reward, xml,
					numAssignments);

			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL()
					+ "/mturk/preview?groupId=" + hit.getHITTypeId());
			return hit.getHITId();
		} catch (ServiceException e) {
			System.err.println(e.getLocalizedMessage());
			return null;
		}
	}

	public static Answer getAssignmentsForHIT(String HITID) {
		String xml = "<HITId>" + HITID + "</HITId>";
		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		Answer output = null;
		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			System.out.println(turkOutput);
			String[] answers = turkOutput.split("&lt;SelectionIdentifier&gt;");
			int yes = 0;
			int total = 0;
			for (int i = 1; i < answers.length; i++) {
				String[] answer = answers[i]
						.split("&lt;/SelectionIdentifier&gt;");
				// System.out.println("answer is: " + answer[0]);
				if (answer[0].matches("true")) {
					yes++;
				}
				total++;
			}

			System.out.println("yes:" + yes + " total:" + total);
			output = new Answer(yes, total);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public static void grandBonus(String id, String assignmentID) {
		try {
			service.grantBonus(id, 0.03, assignmentID,
					"You got the right answer. Thanks!");
			System.out.println("Successfully:" + id);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("not:" + id);
		}

	}

	public static TestAnswer getAssignmentsForTestHIT(String HITID,
			String rightAnswer) {
		String xml = "<HITId>" + HITID + "</HITId>";
		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		TestAnswer output = null;
		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			// System.out.println(turkOutput);
			String[] answers = turkOutput.split("&lt;SelectionIdentifier&gt;");
			int part = 0;
			int same = 0;
			int other = 0;

			int total = 0;
			for (int i = 1; i < answers.length; i++) {
				String[] workerString = answers[i - 1].split("<WorkerId>");
				String[] workerString2 = workerString[1].split("</WorkerId>");
				String workerID = workerString2[0];
				// System.out.println(workerID);

				String[] assignmentID1 = workerString[0]
						.split("<AssignmentId>");
				String[] assignmentID2 = assignmentID1[1]
						.split("</AssignmentId>");
				String assignmentID = assignmentID2[0];
				// System.out.println(assignmentID);

				String[] answer = answers[i]
						.split("&lt;/SelectionIdentifier&gt;");
				// System.out.println("answer is: " + answer[0]);
				if (answer[0].matches("part")) {
					part++;
				}
				if (answer[0].matches("same")) {
					same++;
				}
				if (answer[0].matches("other")) {
					other++;
				}
				total++;

				if (answer[0].matches(rightAnswer)) {
					grandBonus(workerID, assignmentID);
				}
			}
			output = new TestAnswer(same, part, other, total);
			System.out.println("part:" + part + "same:" + same + "other:"
					+ other + " total:" + total);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public static String reformatDate(String input) {
		String regx = "T";
		String[] ca = input.split(regx);
		input = ca[0] + ca[1];
		input = input.substring(0, input.length() - 1);
		return input;
	}

	public static HashMap<String, HashSet<String>> evaluateAssignmentsForMutiple(
			String HITID, HashSet<String> locations) {
		String xml = "<HITId>" + HITID + "</HITId>";
		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		HashMap<String, HashSet<String>> output = new HashMap<String, HashSet<String>>();
		int noneNum = 0;

		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			System.out.println(turkOutput);
			// HashMap<String, Integer> votes = new HashMap<String, Integer>();

			String all_answers[] = turkOutput
					.split("<Assignment>");
			for (int j = 1; j < all_answers.length; j++) {
				String[] workerId = all_answers[j].split("<WorkerId>");
				workerId = workerId[1].split("</WorkerId>");

				String[] time = workerId[1].split("</AcceptTime>");
				time = time[0].split("<AcceptTime>");
				// System.out.println("accept:"+time[1]);
				String acceptTime = time[1];
				time = workerId[1].split("</SubmitTime>");
				time = time[0].split("<SubmitTime>");
				// System.out.println("submit:"+time[1]);
				SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat(
						"yyyy-MM-ddhh:mm:ss");
				acceptTime = reformatDate(acceptTime);
				String submitTime = reformatDate(time[1]);
				long difference = datetimeFormatter1.parse(submitTime)
						.getTime()
						- datetimeFormatter1.parse(acceptTime).getTime();
				difference = difference / 1000;
				int index = (int) (difference / 10);
				if (index > 60) {
					index = 60;
				}
				WorkerBehavior.timeDistribution[index]++;

				// System.out.println(difference);
				WorkerBehavior.totalQuestion++;
				WorkerBehavior.totalTime += difference;

				HashSet<String> workerLocations = new HashSet<String>();
				String[] answers = all_answers[j]
						.split("&lt;SelectionIdentifier&gt;");
				boolean all = false;
				for (int i = 1; i < answers.length; i++) {
					String answer = answers[i]
							.split("&lt;/SelectionIdentifier&gt;")[0];
					// System.out.println("answer is: " + answer);
					if (locations.contains(answer)) {
						workerLocations.add(answer);
						// System.out.print("," + answer);
					}			
					
					if (answer.matches("all")) {
						all = true;
						workerLocations.add("all");
					}
					if (answer.matches("other")) {
						workerLocations.add("other");
					}
					if (answer.matches("none")) {
						workerLocations.add("none");
					}
				}
				if (all) {
					workerLocations = new HashSet<String>(locations);
					workerLocations.add("all");
					// System.out.println("all");
				}
				// System.out.println();
				output.put(workerId[0], workerLocations);
			}

			System.out.println(HITID);
			System.out.println("none:" + noneNum);
			System.out.println("total number of responses:"
					+ all_answers.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	
	public static HashMap<String, Integer> getAssignmentsForTestMutiple(
			String HITID, HashSet<String> locations, boolean other) {
		String xml = "<HITId>" + HITID + "</HITId>";
		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		HashMap<String, Integer> output = new HashMap<String, Integer>();
		int noneNum = 0;

		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			// System.out.println(turkOutput);
			// HashMap<String, Integer> votes = new HashMap<String, Integer>();
			Iterator<String> itr = locations.iterator();
			while (itr.hasNext()) {
				output.put(itr.next(), 0);
			}

			String all_answers[] = turkOutput
					.split("</Answer></Assignment><Assignment>");
			for (int j = 0; j < all_answers.length; j++) {
				String[] answers = all_answers[j]
						.split("&lt;SelectionIdentifier&gt;");
				// System.out.println(j);
				boolean otherReply = false;
				for (int i = 1; i < answers.length; i++) {
					String answer = answers[i]
							.split("&lt;/SelectionIdentifier&gt;")[0];
					// System.out.println("answer is: " + answer);
					if (output.containsKey(answer)) {
						int vote = output.get(answer) + 1;
						output.put(answer, vote);
					}
					if (answer.matches("none")) {
						noneNum++;
					}
					if(answer.matches("Other")){
						otherReply = true;
					}
				}
				if(otherReply==other){
					String[] workerString = answers[0].split("<WorkerId>");
					String[] workerString2 = workerString[1].split("</WorkerId>");
					String workerID = workerString2[0];
					// System.out.println(workerID);

					String[] assignmentID1 = workerString[0]
							.split("<AssignmentId>");
					String[] assignmentID2 = assignmentID1[1]
							.split("</AssignmentId>");
					String assignmentID = assignmentID2[0];
					// System.out.println(assignmentID);
					grandBonus(workerID, assignmentID);
				}
			}
			output.put("total", all_answers.length);
			output.put("none", noneNum);
			System.out.println(HITID);
			System.out.print(output);
			System.out.println("none:" + noneNum);
			System.out.println("total number of responses:"
					+ all_answers.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	
	public static HashMap<String, Integer> getAssignmentsForMutiple(
			String HITID, HashSet<String> locations) {
		String xml = "<HITId>" + HITID + "</HITId>";
		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		HashMap<String, Integer> output = new HashMap<String, Integer>();
		int noneNum = 0;

		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			// System.out.println(turkOutput);
			// HashMap<String, Integer> votes = new HashMap<String, Integer>();
			Iterator<String> itr = locations.iterator();
			while (itr.hasNext()) {
				output.put(itr.next(), 0);
			}

			String all_answers[] = turkOutput
					.split("</Answer></Assignment><Assignment>");
			for (int j = 0; j < all_answers.length; j++) {
				String[] answers = all_answers[j]
						.split("&lt;SelectionIdentifier&gt;");
				// System.out.println(j);
				for (int i = 1; i < answers.length; i++) {
					String answer = answers[i]
							.split("&lt;/SelectionIdentifier&gt;")[0];
					System.out.print(answer+"!");
					// System.out.println("answer is: " + answer);
					if (output.containsKey(answer)) {
						int vote = output.get(answer) + 1;
						output.put(answer, vote);
					}
					if (answer.matches("none")) {
						noneNum++;
					}
				}
				System.out.println();
			}
			output.put("total", all_answers.length);
			output.put("none", noneNum);
			System.out.println(HITID);
			System.out.print(output);
			System.out.println("none:" + noneNum);
			System.out.println("total number of responses:"
					+ all_answers.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public static void blockWorker() {

		// service.blockWorker("A1NP3XGUAKE96X",
		// "Your accuracy rate is only 30%.");
		// service.blockWorker("A2A9RTENQYW80P", "test.");
		service.unblockWorker("A2A9RTENQYW80P", "test.");
		// A1NP3XGUAKE96X
		// A2J237J8KM3OCS
		// AHJQDF8PAHVDP
		// A259WL09N16KPL
		// AZMJHIX05LZ7D
	}

	// public static String createLocationQuestion(String location) {
	// RequesterService service = new RequesterService(
	// new PropertiesClientConfig(
	// "/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));
	// String title = "One true or false Quesion about Medical";
	// String description = "You will help us find the locations of a disease.";
	// int numAssignments = 20;
	// double reward = 0.02;
	//
	// try {
	// // The createHIT method is called using a convenience static method
	// // RequesterService.getBasicFreeTextQuestion() that generates the
	// // question format
	// // for the HIT.
	// String xml =
	// "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
	// + "<Overview>"
	// + "<Title>One True or False Medical Question</Title>"
	// + "<Text>"
	// + "Please help us to identify the locations of diseases"
	// + ". Thank you!"
	// + "</Text>"
	// + "</Overview>"
	// + "<Question>"
	// + "<QuestionIdentifier>1</QuestionIdentifier>"
	// + "<IsRequired>true</IsRequired>"
	// + "<QuestionContent>"
	// + "<Text>"
	// + "Does it make sense to talk about the "
	// + location
	// + " to be the anatomic location of some diseases?"
	// + "</Text>"
	// + "</QuestionContent>"
	// + "<AnswerSpecification>"
	// + "<SelectionAnswer>"
	// + "<StyleSuggestion>radiobutton</StyleSuggestion>"
	// + "<Selections>"
	// + "<Selection>"
	// + "<SelectionIdentifier>true</SelectionIdentifier>"
	// + "<Text>true</Text>"
	// + "</Selection>"
	// + "<Selection>"
	// + "<SelectionIdentifier>false</SelectionIdentifier>"
	// + "<Text>false</Text>"
	// + "</Selection>"
	// + "</Selections>"
	// + "</SelectionAnswer>"
	// + "</AnswerSpecification>"
	// + "</Question>" + "</QuestionForm>";
	//
	// HIT hit = service.createHIT(title, description, reward, xml,
	// numAssignments);
	// // if (!question.contains("exactly same of the possible"))
	// // questionLogs.getLast().HITID = hit.getHITId();
	// // Print out the HITId and the URL to view the HIT.
	// System.out.println("Created HIT: " + hit.getHITId());
	// System.out.println("HIT location: ");
	// System.out.println(service.getWebsiteURL()
	// + "/mturk/preview?groupId=" + hit.getHITTypeId());
	// return hit.getHITId();
	// } catch (ServiceException e) {
	// System.err.println(e.getLocalizedMessage());
	// return null;
	// }
	// }

	public static void main(String[] args) throws Exception {
		Hierarchy
				.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		Interface.LocationParentChild = Hierarchy.LocationParentChild;
		readWiki();

//		service.disableHIT("2X8SCM6IAA2V03BB2RROZKKA5Y6KKQ");
		// blockWorker();
		// // Interface.getAssignmentsForHIT("2VYDHDM25L8I6DZJD85ZGOE4RR5FPY");
		// createBinaryQuestion(
		// "Dermatophytosis",
		// "Skin Structure",
		// "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
		// "http://en.wikipedia.org/wiki/Eosinophilic_cellulitis");

		// createBinaryQuestion(
		// "Eosinophilic cellulitis",
		// "Antitragus",
		// "Wells syndrome is an acquired skin disease characterised by the presence of recurrent cellulitis-like eruptions with eosinophilia. The lesions are usually filled with fluid, tender and progress to form hardened plaques of edema and erythema, and usually resolve without scarring.",
		// "none");

		// HashSet<String> locations = new HashSet<String>();
		// locations.add("skin");
		// Interface.createOtherThanQuestion("Dermatophytosis", locations,
		// "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
		// "http://en.wikipedia.org/wiki/Dermatophyte");

		// String[] locations = { "Cranial Bones",
		// "Middle Ear Bone" };
		// Interface
		// .createMultipleQuestion(
		// "Malignant otitis externa",
		// locations,
		// "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
		// "none");

		// HashSet<String> locations = new HashSet<String>();
		// locations.add("Trunk");
		// locations.add("Upper Extremity");
		// locations.add("Lower Extremity");
		// locations.add("Pelvis and Perineum");
		// createTestSameQuestion("Urticaria", locations, "definition",
		// "http://en.wikipedia.org/wiki/Allergic_contact_cheilitis",
		// "skin disorder");

		// service.grantBonus("A31VJJG79G4TBQ", 0.02,
		// "20K1CSTMOFXUZDLZKS5YMNXJKNNSPK", "You got the right answer");

		// HIT[] results = service.searchAllHITs();
		// for(int i=0;i<results.length;i++) {
		// if(!results[i].getQuestion().contains("Rosacea and related disorders"))
		// continue;
		// //
		// if(!results[i].getHITId().matches("2JVIPDOBDDPBB4D124486MB3RJB1C5"))
		// // continue;
		// System.out.println(results[i].getQuestion());
		// System.out.println(results[i].getHITId());
		// System.out.println(results[i].getHITReviewStatus());
		// // getAssignmentsForTestHIT(results[i].getHITId());
		//
		// System.out.println();
		// }
		//
		// System.out.println(results.length);
		// getAssignmentsForHIT("2JVIPDOBDDPBB4D124486MB3RJB1C5");
		// System.out.println(results);
		 HashSet<String> locations = new HashSet<String>();
		 locations.add("Trunk");
		 locations.add("Upper Extremity");
		 locations.add("Lower Extremity");
		 locations.add("Pelvis and Perineum");
		 createSameQuestion("Urticaria", locations, "none",
		 "http://en.wikipedia.org/wiki/Allergic_contact_cheilitis",
		 "skin disorder");
		// approveAssignments();

		// HashSet<String> locations = new HashSet<String>();
		// locations.add("skin1");
		// locations.add("skin2");

		// String locations[] = {"Head and Neck", "Trunk", "Upper Extremity",
		// "Lower Extremity", "Pelvis and Perineum"};
		// createMultipleQuestion("Urticaria", locations,
		// "Spontaneous urticaria is a disease characterized by the daily or almost daily eruption of spontaneous weals, angioedema or both.",
		// "https://en.wikipedia.org/wiki/Urticaria");//2CIFK0COK2JEYC7CRB0L1W6O08BKR4

		// Interface.getAssignmentsForHIT("2WT0HFL42J1LVV8YETYMAIGFLGJ1M3");
		// HashSet<String> locations = new HashSet<String>();
		// locations.add("Head and Neck");
		// locations.add("Trunk");
		// locations.add("Upper Extremity");
		// locations.add("Lower Extremity");
		// locations.add("Pelvis and Perineum");
		//
		// Interface.getAssignmentsForMutiple("2CIFK0COK2JEYC7CRB0L1W6O08BKR4",locations);
		//

		// String locations[] = {Skin, Organs of the Thorax, Organs of the
		// Pelvis, Hair, Organs of the Head and Neck};
		// HashSet<String> locations = new HashSet<String>();
		// locations.add("Cardiovascular system");
		// locations.add("Immune system");
		// locations.add("Hematopoietic system");
		// locations.add("Endocrine system");
		// locations.add("Digestive system");
		//
		// Interface.getAssignmentsForMutiple("2BLK4F0OHOVRYUIKRFTLJ2HEDVF325",locations);
		//

		// HashSet<String> locations = new HashSet<String>();
		// locations.add("Nervous system");
		// locations.add("Musculoskeletal system");
		// locations.add("Integumentary system");
		// locations.add("Genitourinary System");
		// locations.add("Respiratory system");
		//
		// Interface.getAssignmentsForMutiple("2M998D8J3VE7TISGUP3GDMCK99XXHG",locations);
		//

		// postQuestions();
	}
}

class Answer {
	int yes;
	int total;

	public Answer(int yes, int total) {
		this.yes = yes;
		this.total = total;
	}
}

class TestAnswer {
	int same;
	int part;
	int other;
	int total;

	public TestAnswer(int same, int part, int other, int total) {
		this.same = same;
		this.part = part;
		this.other = other;
		this.total = total;
	}
}
