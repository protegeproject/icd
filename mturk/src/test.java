import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.HIT;

public class test {
	private RequesterService service;

	// Define the properties of the HIT to be created.
	private String title = "Movie Survey";
	private String description = "This is a survey to find out how many movies you have watched recently.";
	private int numAssignments = 2;
	private double reward = 0.01;

	/**
	 * Constructor
	 */
	public test() {
		service = new RequesterService(new PropertiesClientConfig(
				"/Applications/java-aws-mturk-1.6.2/samples/mturk.properties"));
	}

	public void getAllAssignmentsForHIT() {
		Assignment[] a = service.getAssignmentsForHIT(
				"2787Q67051QK9DRO9KHCBXMGJ6JYE5", 10);
		System.out.println(service.getAccountBalance());
	}

	/**
	 * Create a simple survey.
	 * 
	 */

	public void createMovieSurvey() {
		try {
			// The createHIT method is called using a convenience static method
			// RequesterService.getBasicFreeTextQuestion() that generates the
			// question format
			// for the HIT.
			String xml = "<QuestionForm xmlns='http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd'>"
					+ "<Overview>"
					+ "<Title>some medical question</Title>"
					+ "<Text>"
					+ "You are helping us to identify the location of diseases"
					+ "</Text>"
					+ "<Text>"
					+ "Player 'X' has the next move.>"
					+ "</Text>"
					+ "</Overview>"
					+ "<Question>"
					+ "<QuestionIdentifier>likelytowin</QuestionIdentifier>"
					+ "<DisplayName>The Next Move</DisplayName>"
					+ "<IsRequired>true</IsRequired>"
					+ "<QuestionContent>"
					+ "<Text>"
					+ "How likely is it that player X will win this game?"
					+ "</Text>"
					+ "</QuestionContent>"
					+ "<AnswerSpecification>"
					+ "<SelectionAnswer>"
					+ "<StyleSuggestion>radiobutton</StyleSuggestion>"
					+ "<Selections>"
					+ "<Selection>"
					+ "<SelectionIdentifier>notlikely</SelectionIdentifier>"
					+ "<Text>Not likely</Text>"
					+ "</Selection>"
					+ "<Selection>"
					+ "<SelectionIdentifier>unsure</SelectionIdentifier>"
					+ "<Text>It could go either way</Text>"
					+ "</Selection>"
					+ "</Selections>"
					+ "</SelectionAnswer>"
					+ "</AnswerSpecification>"
					+ "</Question>" + "</QuestionForm>";
			HIT hit = service.createHIT(title, description, reward, xml,
					numAssignments);

			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL()
					+ "/mturk/preview?groupId=" + hit.getHITTypeId());

		} catch (ServiceException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Create an instance of this class.
		test app = new test();

		// Create the new HIT.
		app.createMovieSurvey();
		// app.getAllAssignmentsForHIT();
	}
}
