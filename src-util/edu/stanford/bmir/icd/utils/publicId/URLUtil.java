package edu.stanford.bmir.icd.utils.publicId;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;

import edu.stanford.smi.protege.util.Log;

public class URLUtil {

    public static BufferedReader read(String url) throws Exception {
        return new BufferedReader(
                new InputStreamReader(
                        new URL(url).openStream()));
    }

    public static String getURLContent(String url) {
        StringBuffer urlString = new StringBuffer();

        try {
            BufferedReader reader = read(url);
            String line = reader.readLine();

            while (line != null) {
                urlString.append(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            if (Log.getLogger().isLoggable(Level.FINE)) {
                Log.getLogger().log(Level.FINE, "Could not access: " + url, e);
            } else {
                Log.getLogger().warning("Could not access: " + url + " Error: " + e.getMessage() + ". Enable fine logging for more.");
            }
        }
        return urlString.toString();
    }

    public static String httpPost(String urlStr, String encodedData) {
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(urlStr);
            // Send data
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(encodedData);
            wr.flush();

            System.out.println("POST: " + url + " data:" + encodedData);
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append("\n");
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            if (Log.getLogger().isLoggable(Level.FINE)) {
                Log.getLogger().log(Level.FINE, "Could not make the HTTP POST: " + urlStr + " data: " + encodedData, e);
            } else {
                Log.getLogger().warning("Could not make the HTTP POST: " + urlStr + " data: " + encodedData);
            }
            throw new RuntimeException("HTTP POST to: " + urlStr + " failed. Message: " + e.getMessage());
        }
        return response.toString();
    }

    public static int httpPut(String urlStr) {
        return httpRequest(urlStr, "PUT");
    }

    public static int httpDelete(String urlStr) {
        return httpRequest(urlStr, "DELETE");
    }

    public static int httpRequest(String urlStr, String requestType) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestType);
            return con.getResponseCode();
        } catch (Exception e) {
            if (Log.getLogger().isLoggable(Level.FINE)) {
                Log.getLogger().log(Level.FINE, "Could not make the HTTP PUT: " + urlStr, e);
            } else {
                Log.getLogger().warning("Could not make the HTTP PUT: " + urlStr);
            }
            throw new RuntimeException("HTTP " + requestType +" to: " + urlStr + " failed. Message: " + e.getMessage());
        }
    }

    public static String encode(String urlToEncode) {
        if (urlToEncode == null) { return null; }

        String encodedString = null;
        try {
            encodedString = URLEncoder.encode(urlToEncode, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.getLogger().log(Level.WARNING, "Error at encoding url: " + urlToEncode, e1);
            return "";
        }

        return encodedString;
    }

    /**
     * Encodes the argument. If the argument is null, it will return the empty string.
     *
     * @param urlToEncode
     * @return
     */
    public static String encodeNN(String urlToEncode) {
        return encode(urlToEncode == null ? "" : urlToEncode);
    }


    public static void main (String[] args) throws Exception{
        //BufferedReader reader = read(args[0]);
        //String url = "http://rest.bioontology.org/bioportal/search/Thyroiditis";
        //System.out.println(getURLContent(url));

        String url = "http://apps.who.int/classifications/icd11/idgenerator/GetNewId?type=entity&apiKey=6507f996-71e9-47af-aa8c-ef4cb6ece3fc&seed=" + encode("http://who.int/icd#II");
        System.out.println(getURLContent(url));

    }


    /*
     * URL: http://apps.who.int/classifications/icd11/idgenerator/GetNewId



Parameters required when calling the service are as follows:

type : type of the id to be generated. Only “entity” type is supported at the moment.

seed: A seed value to be used to generate the Id.  I guess using  iCat internal ID makes sense here.

apiKey : key to be used (for iCat the key is 6507f996-71e9-47af-aa8c-ef4cb6ece3fc) Even though this is not a very high security approach I guess  it’s OK for the time being



The result is returned in Json or in Xml depending on your http Accept header
     */

}
