package edu.stanford.bmir.icd.utils.publicId;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Uses a direct SQL query to get the entities that don't have a public id set,
 * then uses connects to the remote ICD project and adds the missing public ids
 * remotely.
 * 
 * Uses the passed ICD pprj file to get the database connection info.
 * 
 * @author ttania
 *
 */
public class FillMissingPublicIds {

	private static Logger log = Log.getLogger(FillMissingPublicIds.class);

	private static String QUERY = "select frame from icd_umbrella a where "
			+ "(slot = \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" and "
			+ "short_value = \"http://who.int/icd#PostcoordinationSection\" and "
			+ "not exists (select null from icd_umbrella b where "
			+ "(a.frame=b.frame and slot=\"http://who.int/icd#publicId\")));";

	private static ICDContentModel cm;
	private static OWLModel owlModel;
	private static RDFProperty publicIDProp;

	public static void main(String[] args) {
		if (args.length != 5) {
			log.severe("Missing args. Expected: (1) ICD pprj file; (2) Protege Server url; "
					+ "(3) Protege server user; (4) Protege server password; (5) Protege server project name.");
			return;
		}

		Collection errors = new ArrayList();
		Project localPrj = Project.loadProjectFromFile(args[0], errors);

		if (errors != null) {
			ProjectManager.getProjectManager().displayErrors("Errors", errors);
		}

		if (localPrj == null) {
			log.severe("Failed to load OWL pprj file");
			return;
		}

		Statement stmt = getStatement(localPrj);

		if (stmt == null) {
			log.log(Level.SEVERE, "Cannot get a DB statement. Abort.");
			System.exit(1);
		}

		log.info("Retrieving classes with missing public id..");
		Collection<String> clsesWithMissingPublicIds = getClsesWithMissingPublicId(stmt);
		log.info("Retrieved " + clsesWithMissingPublicIds.size() + " classes with missing public ids.");

		Project remotePrj = connectToRemoteProject(args);
		if (remotePrj == null) {
			log.info("Cannot connect to remote project " + args[4] + ". Abort");
			System.exit(1);
		}
		
		owlModel = (OWLModel) remotePrj.getKnowledgeBase();

		init();

		addPublicId(clsesWithMissingPublicIds);
	}

	private static void init() {
		cm = new ICDContentModel(owlModel);
		publicIDProp = cm.getPublicIdProperty();
	}

	private static Collection<String> getClsesWithMissingPublicId(Statement stmt) {
		List<String> frameIds = new ArrayList<String>();

		try {
			ResultSet rset = stmt.executeQuery(QUERY);
			while (rset.next()) {
				String frameId = rset.getString("frame");
				frameIds.add(frameId);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Error at executing SQL query: " + e.getMessage(), e);
		}

		return frameIds;
	}

	private static Statement getStatement(Project prj) {
		PropertyList sources = prj.getSources();

		String url = DatabaseKnowledgeBaseFactory.getURL(sources);
		String user = DatabaseKnowledgeBaseFactory.getUsername(sources);
		String pass = DatabaseKnowledgeBaseFactory.getPassword(sources);

		Statement stmt = null;

		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Can't connect to database " + url + ", " + user, e);
			return null;
		}

		return stmt;
	}

	private static Project connectToRemoteProject(String[] args) {
		Project prj = null;
		try {
			prj = RemoteProjectManager.getInstance().getProject(args[1], args[2], args[3], args[4], false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot connect to remote project: " + args, e.getMessage());
			return null;
		}
		return prj;
	}

	private static void addPublicId(Collection<String> frameIds) {
		log.info("Start adding public ids to " + frameIds.size());

		int i = 0;
		int addedPublicIdCount = 0;

		for (String frameId : frameIds) {
			RDFSNamedClass cls = getCls(frameId);

			if (cls != null && addPublicIdToClass(cls) == true) {
				addedPublicIdCount++;
			}

			i++;

			if (i % 100 == 0) {
				log.info("Processed " + i + " classes");
			}
		}

		log.info("All done with " + i + " classes. Added the publicId to " + addedPublicIdCount + " classes. Done at "
				+ new Date());
	}
	

	private static RDFSNamedClass getCls(String frameId) {
		RDFSNamedClass cls = owlModel.getRDFSNamedClass(frameId);
		if (cls == null) {
			log.warning("Could not find class " + frameId);
		}
		return cls;
	}

	private static boolean addPublicIdToClass(RDFSNamedClass cat) {
		if (cm.getPublicId(cat) != null) {
			return false;
		}
		try {
			String publicId = ICDIDUtil.getPublicId(cat.getName());
			if (publicId == null) {
				log.warning("Could not get public ID from ID server for class: " + cat.getName());
			} else {
				cat.setPropertyValue(publicIDProp, publicId);
				log.info("Adding public id to class: " + cat.getName() + " publicId: " + publicId);
				return true;
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Exception at adding public ID for class: " + cat, e);
		}
		return false;
	}

}
