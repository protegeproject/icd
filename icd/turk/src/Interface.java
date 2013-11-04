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

	public static String id = "AKIAIZCFLKY2J2WPBKZA";
	public static String secretKey = "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";
	public static boolean sandbox;
	public static LinkedList<Question> questionLogs = new LinkedList<Question>();
	public static HashMap<String, HashSet<String>> LocationParentChild = new HashMap<String, HashSet<String>>();

	public static HashMap<String, String> diseasesWiki = new HashMap<String, String>();
	public static HashMap<String, String> locationsWiki = new HashMap<String, String>();

	public static void readWiki() {
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

	public static void readLog() {
		try {
			String path = "/Users/Yun/Dropbox/medical/workspace/turk/questionLog.txt";
			BufferedReader in = new BufferedReader(new FileReader(path));
			String strLine;
			while ((strLine = in.readLine()) != null) {
				String items[] = strLine.split("!");
				String disease = items[0];
				String location = items[1];
				String HITID = items[2];
				String type = items[3];
				HashSet<String> locations = new HashSet<String>();
				locations.add(location);
				Question question = new Question(type, disease, locations,
						HITID);
				questionLogs.add(question);
				in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeLog() {
		try {
			FileWriter fstream = new FileWriter("questionLog.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			for (int i = 0; i < questionLogs.size(); i++) {
				Question question = questionLogs.get(i);
				String location = question.locations.iterator().next();
				out.write(question.disease + "!");
				out.write(location + "!");
				out.write(question.HITID + "!");
				out.write(question.type);
				out.newLine();
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
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

	public static String createBinaryQuestion(String disease, String location,
			String definition, String wiki) {
		HashSet<String> locations = new HashSet<String>();
		locations.add(location);
		questionLogs.add(new Question("binary", disease, locations, "none"));
		String question = "It is possible that " + disease + " can occur in "
				+ location + " or part of " + location + ".";
		if (wiki.matches("none")) {
			wiki = "https://www.google.com/search?q="
					+ disease.replace(" ", "+");
		}
		return createQuestion(disease, location, definition, wiki, question);
	}

	public static String createOtherThanQuestion(String disease,
			HashSet<String> location, String definition, String wiki) {
		// questionLogs.add(new Question("other", disease, location, "none"));
		String locationString = "";
		Iterator<String> itr = location.iterator();
		locationString = itr.next();
		while (itr.hasNext()) {
			locationString = locationString + " ," + itr.next();
		}
		String question = "It is possible that " + disease
				+ " can occur in locations other than " + locationString + ".";
		System.out.println("other than question. disease: " + disease
				+ " Locations:" + location);
		System.out.println("definition: " + definition);
		System.out.println("wiki: " + wiki);
		return createQuestion(disease, locationString, definition, wiki,
				question);
	}

	public static String createSameQuestion(String diseaseA,
			HashSet<String> locationA, String definitionA, String wikiA,
			String diseaseB) {

		String question = "Given that the possible locations of " + diseaseB
				+ " is " + locationA.iterator().next()
				+ ", the possible locations of  " + diseaseA
				+ " is exactly same of the possible locations of " + diseaseB
				+ ".";
		return createQuestion(diseaseA, "", definitionA, wikiA, question);
	}

	public static String createQuestion(String disease, String location,
			String definition, String wiki, String question) {
		MyRequesterService service = new MyRequesterService(
				new PropertiesClientConfig(
						"/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));
		String title = "One true or false Quesion about Medical";
		String description = "You will help us find the locations of a disease.";
		int numAssignments = 10;
		double reward = 0.02;

		String locationDef = "";

		if (LocationParentChild.containsKey(location)) {
			HashSet<String> childrenNames = LocationParentChild.get(location);
			String childrenName = "";
			Iterator<String> itr = childrenNames.iterator();
			int k = 1;
			childrenName = itr.next();
			while (itr.hasNext() && k <= 5) {
				childrenName = childrenName + ", " + itr.next();
				k++;
			}
			locationDef = locationDef + location + " contains: " + childrenName
					+ " and other similar anatomic locations. ";
		} else {
			locationDef = locationDef + location + ": ";
		}

		if (locationsWiki.get(location).matches("none")) {
			locationDef = locationDef + "  <a href='"
					+ "https://www.google.com/search?q="
					+ location.replace(" ", "+")
					+ "' target='_blank'>Google search results of " + location
					+ "</a><br/>";
		} else {
			locationDef = locationDef + "  <a href='"
					+ locationsWiki.get(location)
					+ "' target='_blank'>Wikipedia page of " + location
					+ "</a><br/>";
		}

		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String wikiOrGoogle = "wikipedia";
			if (wiki.contains("google")) {
				wikiOrGoogle = "Google search result";
			}
			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>One True or False Medical Question</Title>"
					+ "<Text>" + "Please help us to identify the location of "
					+ disease
					+ ". You will learn medical knowledge as well! "
					+ "</Text>"
					+ "<Text>"
					+ "Information about  "
					+ disease
					+ " and "
					+ location
					+ " is appended at the end. Thank you!"
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<Text>"
					+ question
					+ "</Text>"
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
					+ "<Text>"
					+ "The definition of "
					+ disease
					+ " is: "
					+ definition
					+ "."
					+ "</Text>"
					+ "<FormattedContent><![CDATA["
					+ "  <p>This is the "
					+ wikiOrGoogle
					+ " Page for"
					+ "  <a href='"
					+ wiki
					+ "' target='_blank'>"
					+ disease
					+ "</a><br/><br/>"
					+ locationDef
					+ "</p>"
					+ "]]></FormattedContent>"
					+ "</Overview>"
					+ "</QuestionForm>";

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

	public static String createMultipleQuestion(String disease,
			String[] locations, String definition, String wiki) {
		MyRequesterService service = new MyRequesterService(
				new PropertiesClientConfig(
						"/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));
		String title = "One Multiple Choice Medical Question";
		String description = "You will learn medical knowledge";
		int numAssignments = 10;
		double reward = 0.05;
		String selections = "";
		String locationDefs = "";
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
				locationString = locationString + " and " + locations[i] + " ";
			} else {
				locationString = locationString + ", " + locations[i];
			}

			selections = selections + "<Selection>" + "<SelectionIdentifier>"
					+ locations[i] + "</SelectionIdentifier>" + "<Text>"
					+ locations[i] + "</Text></Selection>";
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
				locationDefs = locationDefs + locations[i] + " contains: "
						+ childrenName
						+ " and other similar anatomic locations. ";
			} else {
				locationDefs = locationDefs + locations[i] + ": ";
			}
			System.out.println(locations[i]);
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
		selections = selections + "<Selection>"
				+ "<SelectionIdentifier>none</SelectionIdentifier>"
				+ "<Text>None of the above applies</Text>" + "</Selection>";
		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String xml = 
					"<QualificationRequirement>"
			 + "<QualificationTypeId>2F1QJWKUDD8XADTFD2Q0G6UTO95ALH</QualificationTypeId>"
			 + "<Comparator>Exists</Comparator>"
			+"</QualificationRequirement>"
					
					+"<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>One Multiple Choice Medical Quesion</Title>"
					+ "<Text>" + "Please help us to identify the location of "
					+ disease
					+ ". You will learn medical knowledge as well! "
					+ "</Text>"
					+ "<Text>"
					+ "Information about  "
					+ disease
					+ locationString
					+ " is appended at the end. Thank you!"
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<Text>"
					+ disease
					+ " can occur in the following locations(choose all that apply):"
					+ "</Text>"
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
					+ "<Text>"
					+ "The definition of "
					+ disease
					+ " is: "
					+ definition
					+ "</Text>"
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
		secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		Answer output = null;
		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
//			System.out.println(turkOutput);
			String[] answers = turkOutput.split("&lt;SelectionIdentifier&gt;");
			int yes = 0;
			int total = 0;
			for (int i = 1; i < answers.length; i++) {
				String[] answer = answers[i]
						.split("&lt;/SelectionIdentifier&gt;");
//				System.out.println("answer is: " + answer[0]);
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

	public static Answer getAssignmentsForMutiple(String HITID,
			HashSet<String> locations) {
		String xml = "<HITId>" + HITID + "</HITId>";
		secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		Answer output = null;
		int noneNum = 0;

		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			 System.out.println(turkOutput);
			HashMap<String, Integer> votes = new HashMap<String, Integer>();
			Iterator<String> itr = locations.iterator();
			while (itr.hasNext()) {
				votes.put(itr.next(), 0);
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
					// System.out.println("answer is: " + answer);
					if (votes.containsKey(answer)) {
						int vote = votes.get(answer) + 1;
						votes.put(answer, vote);
					}
					if (answer.matches("none")) {
						noneNum++;
					}

				}
			}
			System.out.println(HITID);
			System.out.print(votes);
			System.out.println("none:" + noneNum);
			System.out.println("total number of responses:"
					+ all_answers.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	public static LinkedList<String> getAssignIDsForHIT(String HITID) {
		LinkedList<String> assignIDs = new LinkedList<String>();
		String xml = "<HITId>" + HITID + "</HITId>";
		secretKey = "oVjZAE43v7FHCYrnydGhcgu5U2pDG/XS0Q/shjcy";
		String turkOutput = "";
		try {
			turkOutput = soapRequest("GetAssignmentsForHIT", xml);
			System.out.println(turkOutput);
			String[] answers = turkOutput.split("<AssignmentId>");
			for (int i = 1; i < answers.length; i++) {
				String[] answer = answers[i].split("</AssignmentId>");
				assignIDs.add(answer[0]);
				System.out.println("AssignmentId:" + answer[0]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return assignIDs;
	}

	public static void approveAssignment(String id) {
		try {
			String turkOutput = soapRequest("ApproveAssignment",
					"<AssignmentId>" + id + "</AssignmentId>");
			System.out.println(turkOutput);
			String subs[] = turkOutput.split("<IsValid>");
			subs = subs[1].split("</IsValid>");
			System.out.println(subs[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void searchHITs(){
		
	}

	public static void approveAssignments() {
		LinkedList<String> HITIDs = new LinkedList<String>();
		try {
			// String path =
			// "/Users/Yun/Dropbox/medical/workspace/turk/questionLog.txt";
			// BufferedReader in = new BufferedReader(new FileReader(path));
			// String strLine;
			// while ((strLine = in.readLine()) != null) {
			// String items[] = strLine.split("!");
			// String HITID = items[2];
			// HITIDs.add(HITID);
			// in.readLine();
			// }
			// in.close();
			
			HITIDs.add("24VWIOV1S7SN03T31SUYNZ8N9K2ZKA‏");

			for (int i = 0; i < HITIDs.size(); i++) {
				LinkedList<String> assignIDs = getAssignIDsForHIT(HITIDs.get(i));
				for (int j = 0; j < assignIDs.size(); j++) {
					approveAssignment(assignIDs.get(j));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String createLocationQuestion(String location) {
		RequesterService service = new RequesterService(
				new PropertiesClientConfig(
						"/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));
		String title = "One true or false Quesion about Medical";
		String description = "You will help us find the locations of a disease.";
		int numAssignments = 20;
		double reward = 0.02;

		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>One True or False Medical Question</Title>"
					+ "<Text>"
					+ "Please help us to identify the locations of diseases"
					+ ". Thank you!"
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>1</QuestionIdentifier>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<Text>"
					+ "Does it make sense to talk about the "
					+ location
					+ " to be the anatomic location of some diseases?"
					+ "</Text>"
					+ "</QuestionContent>"
					+ "<AnswerSpecification>"
					+ "<SelectionAnswer>"
					+ "<StyleSuggestion>radiobutton</StyleSuggestion>"
					+ "<Selections>"
					+ "<Selection>"
					+ "<SelectionIdentifier>true</SelectionIdentifier>"
					+ "<Text>true</Text>"
					+ "</Selection>"
					+ "<Selection>"
					+ "<SelectionIdentifier>false</SelectionIdentifier>"
					+ "<Text>false</Text>"
					+ "</Selection>"
					+ "</Selections>"
					+ "</SelectionAnswer>"
					+ "</AnswerSpecification>"
					+ "</Question>" + "</QuestionForm>";

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

	public static void postQuestions() {
		// createQuestion(
		// "Dermatophytosis",
		// "",
		// "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
		// "http://en.wikipedia.org/wiki/Dermatophyte",
		// "Can Dermatophytosis occur in a specific anatomic location?");
		//
		// createQuestion(
		// "Tanapox",
		// "",
		// "Tanapox is caused by a yatapoxvirus. It is endemic amongst monkeys in East Africa.  In humans it presents as a single or a small number of umbilicated papules or nodules, usually on the lower limbs or trunk.  It is accompanied by systemic malaise and lymphadenopathy.  The lesions heal with scarring.",
		// "https://en.wikipedia.org/wiki/Tanapox",
		// "Can Tanapox occur in a specific anatomic location?");
		//
		// createQuestion(
		// "Ecthyma",
		// "",
		// "Ecthyma is an ulcerative form of impetigo. It is characterized by small, purulent, shallow, punched-out ulcers with thick, brown-black crusts and surrounding erythema.",
		// "http://en.wikipedia.org/wiki/Ecthyma",
		// "Can Ecthyma occur in a specific anatomic location?");

		// createLocationQuestion("Immune system");//2IANQQ7Q6705YGKZUIS23QC1YHEVBH
		// 4/10
		// createLocationQuestion("Mesentery");//2H6HOVR14IXKLSLW985C6A6X4TN874
		// 4/10
		// createLocationQuestion("Blood Vessels");//2CQER9Y8TNKIJPS9CB6H22OM1KE7UI
		// 8/10
		// createLocationQuestion("Lymphocytic tissue");//2N2W4ZMAZHHROBFD7FEN5RH1LCMAJG
		// 6/10
		// createLocationQuestion("Bone Marrow");//2EXB0HFL42J1IO5VIYT7W0IGGFG0LI
		// 8/10
		// createLocationQuestion("Outer Ear");//2VYDHDM25L8I6DZJD85ZGOE4RR5FPY
		// 9/10

		// createQuestion(
		// // 29E6NG1FS3NYK9BLMS6Z2ZL2YMYLZU 6/10
		// "Ecthyma",
		// "",
		// "Ecthyma is an ulcerative form of impetigo. It is characterized by small, purulent, shallow, punched-out ulcers with thick, brown-black crusts and surrounding erythema.",
		// "http://en.wikipedia.org/wiki/Ecthyma",
		// "Does it make sense to talk about whether Mesentery is the anatomic location of Ecthyma or not?");
		//
		// createQuestion(
		// // 2JV21O3W5XH0GFPFPTBYOPLSQ23BH7 6/10
		// "Ecthyma",
		// "",
		// "Ecthyma is an ulcerative form of impetigo. It is characterized by small, purulent, shallow, punched-out ulcers with thick, brown-black crusts and surrounding erythema.",
		// "http://en.wikipedia.org/wiki/Ecthyma",
		// "Does it make sense to talk about whether Skin Structure is the anatomic location of Ecthyma or not?");
		//
		// createQuestion(
		// // 2WTNFSVGULF36VSJDE71PS3NZIH8ME 10/10
		// "Dermatophytosis",
		// "",
		// "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
		// "http://en.wikipedia.org/wiki/Dermatophyte",
		// "Does it make sense to talk about whether Skin Structure is the anatomic location of Dermatophytosis or not?");
		//
		// createQuestion(
		// // 2FT49F9SSSHXYRJBUJK5R5F8KZLXTB 6/10
		// "Dermatophytosis",
		// "",
		// "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
		// "http://en.wikipedia.org/wiki/Dermatophyte",
		// "Does it make sense to talk about whether Lymphocytic tissue is the anatomic location of Dermatophytosis or not?");

	}

	public static void main(String[] args) throws Exception {
//		approveAssignments();
		 Hierarchy
		 .readLocationHierarchy("/Users/Yun/Dropbox/medical/workspace/turk/LocationStructure.txt");
		 Interface.LocationParentChild = Hierarchy.LocationParentChild;
		 readWiki();
		// // Interface.getAssignmentsForHIT("2VYDHDM25L8I6DZJD85ZGOE4RR5FPY");
//		 createBinaryQuestion(
//		 "Dermatophytosis",
//		 "Body Organ",
//		 "Dermatophytosis1 (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
//		 "http://en.wikipedia.org/wiki/Eosinophilic_cellulitis");

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

//		 String[] locations = { "Cranial Bones", "facial bones",
//		 "Middle Ear Bone"};
//		 Interface.createMultipleQuestion(
//		 "Malignant otitis externa",
//		 locations,
//		 "Dermatophytosis (tinea, ringworm) is a superficial infection of the skin, hair or nails with fungi of the genera Microsporum, Trichophyton or Epidermophyton.   These fungi normally invade only the outer keratinous layer of the epidermis (stratum corneum), the hair shaft and the nail.  They count amongst the commonest infections in man.   Some species (e.g. Trichophyton rubrum) are essentially anthropophilic and infect only man whereas others are zoophilic (e.g. Trichophyton verrucosum) but may cause human infection from contact with infected animals.",
//		 "none");

		// HashSet<String> locations = new HashSet<String>();
		// locations.add("skin");
		// createSameQuestion("yellow fever", locations, "definition",
		// "http://en.wikipedia.org/wiki/Allergic_contact_cheilitis",
		// "fever");

		// approveAssignments();

		// HashSet<String> locations = new HashSet<String>();
		// locations.add("skin1");
		// locations.add("skin2");

		// String locations[] = {"Head and Neck", "Trunk", "Upper Extremity",
		// "Lower Extremity", "Pelvis and Perineum"};
		// createMultipleQuestion("Urticaria", locations,
		// "Spontaneous urticaria is a disease characterized by the daily or almost daily eruption of spontaneous weals, angioedema or both.",
		// "https://en.wikipedia.org/wiki/Urticaria");//2CIFK0COK2JEYC7CRB0L1W6O08BKR4

		 Interface.getAssignmentsForHIT("2WT0HFL42J1LVV8YETYMAIGFLGJ1M3");
		 HashSet<String> locations = new HashSet<String>();
		 locations.add("Head and Neck");
		 locations.add("Trunk");
		 locations.add("Upper Extremity");
		 locations.add("Lower Extremity");
		 locations.add("Pelvis and Perineum");
		
		 Interface.getAssignmentsForMutiple("2CIFK0COK2JEYC7CRB0L1W6O08BKR4",locations);
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
