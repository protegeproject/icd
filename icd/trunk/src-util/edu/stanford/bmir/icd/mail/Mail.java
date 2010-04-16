package edu.stanford.bmir.icd.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

public class Mail {
    private static Properties parameters;
    
    
    private static Properties getMailProperties() throws FileNotFoundException, IOException {
        if (parameters == null) {
            parameters= new Properties();
            InputStream is = new FileInputStream(new File("mail.properties"));
            parameters.load(is);
            is.close();
        }
        return parameters;
    }
    
    private static String getPassword() throws IOException {
        System.out.print("Password:  ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        return in.readLine();
    }
    
    /**
     * @param args
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws MessagingException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, MessagingException {
        
        Session session = Session.getInstance(getMailProperties());
        // session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);
        // InternetAddress[]  to = InternetAddress.parse("tredmond@stanford.edu", false);
        InternetAddress[] to = new InternetAddress[] { 
            new InternetAddress("tredmond@stanford.edu", "Timothy Redmond")
        };
        msg.setRecipients(RecipientType.TO, to);
        msg.setSubject("Alt internet address 5");
        msg.setText("This is a message from java");

        SMTPTransport transport = (SMTPTransport) session.getTransport();
        transport.connect(parameters.getProperty("mail.host"), 
                          parameters.getProperty("mail.user"), 
                          getPassword());
        msg.saveChanges();
        transport.sendMessage(msg, to);

    }

}
