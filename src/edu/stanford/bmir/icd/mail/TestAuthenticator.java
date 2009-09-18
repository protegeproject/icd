package edu.stanford.bmir.icd.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class TestAuthenticator extends Authenticator {
    public static String MAIL_USER_NAME = "mail.user.name";
    public static String MAIL_PASSWORD  = "mail.password";
    
    private Properties parameters = new Properties();
    
    public TestAuthenticator(Properties parameters) {
        this.parameters = parameters;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(parameters.getProperty(MAIL_USER_NAME), parameters.getProperty(MAIL_PASSWORD));
    }
}
