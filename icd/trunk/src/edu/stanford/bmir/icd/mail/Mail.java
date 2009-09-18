package edu.stanford.bmir.icd.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
    
    
    /**
     * @param args
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws MessagingException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, MessagingException {
        Session session = Session.getInstance(getMailProperties());
        MimeMessage msg = new MimeMessage(session);
        msg.setRecipient(RecipientType.TO, new InternetAddress("tredmond@stanford.edu", "Timothy Redmond"));
        msg.setSubject("Hello there");
        msg.setText("This is a message from java");
        Transport.send(msg);
    }

}
