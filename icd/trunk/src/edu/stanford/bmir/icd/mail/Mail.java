package edu.stanford.bmir.icd.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPSSLTransport;

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
        // session.setDebug(true);
        MimeMessage msg = new MimeMessage(session);
        // InternetAddress[]  to = InternetAddress.parse("tredmond@stanford.edu", false);
        InternetAddress[] to = new InternetAddress[] { 
            new InternetAddress("tredmond@stanford.edu", "Timothy Redmond")
        };
        msg.setRecipients(RecipientType.TO, to);
        msg.setSubject("Alt internet address 3");
        msg.setText("This is a message from java");

        SMTPSSLTransport transport = (SMTPSSLTransport) session.getTransport();
        transport.connect(parameters.getProperty("mail.host"), 
                          parameters.getProperty("mail.user"), 
                          parameters.getProperty("mail.password"));
        msg.saveChanges();
        transport.sendMessage(msg, to);

    }

}
