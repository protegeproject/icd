package Exp2;

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

	public static void grandBonus(String id, String assignmentID, double amount) {
		try {
			service.grantBonus(id, amount, assignmentID,
					"You got the right answer. Thanks!");
			System.out.println("Successfully:" + id);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("not:" + id);
		}
	}
	
//	public static HashMap<String, String> getBonusForMutiple(
//			String HITID, HashSet<String> locations, boolean other) {
//		String xml = "<HITId>" + HITID + "</HITId>";
//		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
//		String turkOutput = "";
//		HashMap<String, String> output = new HashMap<String, String>();
//		try {
//			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
//			
//			String all_answers[] = turkOutput
//					.split("</Answer></Assignment><Assignment>");
//			for (int j = 0; j < all_answers.length; j++) {
//				String[] answers = all_answers[j]
//						.split("&lt;SelectionIdentifier&gt;");
//				boolean otherReply = false;
//				for (int i = 1; i < answers.length; i++) {
//					String answer = answers[i]
//							.split("&lt;/SelectionIdentifier&gt;")[0];
//					if(answer.matches("other")){
//						otherReply = true;
//					}
//				}
//				if(otherReply==other){
//					String[] workerString = answers[0].split("<WorkerId>");
//					String[] workerString2 = workerString[1].split("</WorkerId>");
//					String workerID = workerString2[0];
//					// System.out.println(workerID);
//
//					String[] assignmentID1 = workerString[0]
//							.split("<AssignmentId>");
//					String[] assignmentID2 = assignmentID1[1]
//							.split("</AssignmentId>");
//					String assignmentID = assignmentID2[0];
//					// System.out.println(assignmentID);
//					output.put(workerID, assignmentID);
//				}
//			}
//			System.out.println("finish reading"+HITID);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return output;
//	}
	
	public static MultipleAnswer getAssignmentsForMutiple(
			String HITID, HashSet<String> locations) {
		String xml = "<HITId>" + HITID + "</HITId>";
		// secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		HashMap<String, Integer> locVotes = new HashMap<String, Integer>();
		HashMap<String, HashMap<String, HashSet<String>>> userVotes = new HashMap<String, HashMap<String, HashSet<String>>>();
		int noneNum = 0;

		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			// System.out.println(turkOutput);
			// HashMap<String, Integer> votes = new HashMap<String, Integer>();
			Iterator<String> itr = locations.iterator();
			while (itr.hasNext()) {
				locVotes.put(itr.next(), 0);
			}

			String all_answers[] = turkOutput
					.split("<Assignment>");
			for (int j = 1; j < all_answers.length; j++) {
				String[] answers = all_answers[j]
						.split("&lt;SelectionIdentifier&gt;");
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
				HashSet<String> votes = new HashSet<String>();
				
				for (int i = 1; i < answers.length; i++) {
					String answer = answers[i]
							.split("&lt;/SelectionIdentifier&gt;")[0];
					// System.out.println("answer is: " + answer);
					if (locVotes.containsKey(answer)) {
						int vote = locVotes.get(answer) + 1;
						locVotes.put(answer, vote);
						votes.add(answer);
					}
					if (answer.matches("none")) {
						noneNum++;
					}
				}
				HashMap<String, HashSet<String>> tmp = new HashMap<String, HashSet<String>>();
				tmp.put(assignmentID, votes);
				userVotes.put(workerID, tmp);
			}
			locVotes.put("total", all_answers.length-1);
			locVotes.put("none", noneNum);
			System.out.println(HITID);
			System.out.print(locVotes);
			System.out.println("none:" + noneNum);
			System.out.println("total number of responses:"
					+ (all_answers.length-1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new MultipleAnswer(locVotes, userVotes);
	}

	public static void main(String[] args) throws Exception {
		Hierarchy
				.readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		Interface.LocationParentChild = Hierarchy.LocationParentChild;
		readWiki();

		
	}
}

class MultipleAnswer{
	public HashMap<String, Integer> locVotes;
	public HashMap<String, HashMap<String, HashSet<String>>> userVotes;
	public MultipleAnswer(HashMap<String, Integer> locVotes, HashMap<String, HashMap<String, HashSet<String>>> userVotes) {
		this.locVotes = locVotes;
		this.userVotes = userVotes;
	}
}
