package edu.stanford.bmir.icd.utils.publicId;

import com.google.gson.Gson;

import edu.stanford.smi.protege.util.ApplicationProperties;


public class ICDIDUtil {

    private static String WHO_SERVICE_NEW_ID_URL_PROP = "who.service.new.id.url";
    private static String WHO_SERVICE_NEW_ID_URL_DEFAULT = "https://icd.who.int/idgenerator/icd11/GetNewId";

    private static String WHO_SERVICE_APIKEY = "who.service.apikey";

    private static String WHO_SERVICE_SYSTEM_NAME="who.service.system.name";
    private static String WHO_SERVICE_SYSTEM_NAME_DEFAULT="iCat";


    public static String getPublicId(String iCATId) {
        String url = getWHOServiceNewIdURL() + "?" +
                            "type=entity&apikey=" + getWHOServiceAPIKey() +
                            "&seed=" + URLUtil.encode(iCATId) +
                            "&callerSystemName=" + getWHOServiceSystemName();
        String jSonText = URLUtil.getURLContent(url);

        if (jSonText == null) { return null;}

        Gson gson = new Gson();
        ICDPublicId publicId = gson.fromJson(jSonText, ICDPublicId.class);
        return publicId == null ? null : publicId.getUri();
    }


    private static String getWHOServiceNewIdURL() {
        return ApplicationProperties.getString(WHO_SERVICE_NEW_ID_URL_PROP, WHO_SERVICE_NEW_ID_URL_DEFAULT);
    }

    private static String getWHOServiceAPIKey() {
        return ApplicationProperties.getString(WHO_SERVICE_APIKEY);
    }

    private static String getWHOServiceSystemName() {
        return ApplicationProperties.getString(WHO_SERVICE_SYSTEM_NAME, WHO_SERVICE_SYSTEM_NAME_DEFAULT);
    }

    public static void main (String[] args) throws Exception{
        //System.out.println(getPublicId("http://who.int/icd#V"));
    	
    	printPublicId("http://who.int/icd#HealthIntervention");
    	
    	/*printPublicId("http://who.int/icd#ICHICategory");
    	printPublicId("http://who.int/icd#Target");
    	printPublicId("http://who.int/icd#Means");
    	printPublicId("http://who.int/icd#Action");
    	printPublicId("http://who.int/icd#ICHIExtensionCode");
    	
    	printPublicId("http://who.int/icf#ICFCategory");
    	printPublicId("http://who.int/icf#S");
    	printPublicId("http://who.int/icf#B");
    	printPublicId("http://who.int/icf#E");
    	printPublicId("http://who.int/icf#D");
    	
    	printPublicId("http://who.int/icf#ICFQualifier");
    	printPublicId("http://who.int/icf#Q-al");
    	printPublicId("http://who.int/icf#Q-bf");
    	printPublicId("http://who.int/icf#Q-ncbs");
    	printPublicId("http://who.int/icf#Q-p");
    	printPublicId("http://who.int/icf#Q-emi");
    	printPublicId("http://who.int/icf#Q-c");
    	*/
    }

    private static void printPublicId(String icatId) {
    	System.out.println(icatId + "\t" + getPublicId(icatId));
    }

    /*
    URL: http://apps.who.int/classifications/icd11/idgenerator/GetNewId
    Parameters required when calling the service are as follows:
    type : type of the id to be generated. Only “entity” type is supported at the moment.
    seed: A seed value to be used to generate the Id.  I guess using  iCat internal ID makes sense here.
    apiKey : key to be used
    The result is returned in Json or in Xml depending on your http Accept header
     */
    class ICDPublicId {

        private String uri;
        private String type;
        private Boolean newlyCreated;

        public ICDPublicId() {
            //empty constructor
        }

        public String getUri() {
            return uri;
        }

        public String getType() {
            return type;
        }

        public Boolean getNewlyCreated() {
            return newlyCreated;
        }
    }


}
