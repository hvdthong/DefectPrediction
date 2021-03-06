package fr.jayasoft.ivy.url;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import fr.jayasoft.ivy.util.Credentials;
import fr.jayasoft.ivy.util.Message;

/**
 * 
 * @author Christian Riege
 * @author Xavier Hanin
 */
public final class IvyAuthenticator extends Authenticator {


    /**
     * The sole instance.
     */
    public final static IvyAuthenticator INSTANCE = new IvyAuthenticator();

    /**
     * Private c'tor to prevent instantiation. Also installs this as the default
     * Authenticator to use by the JVM.
     */
    private IvyAuthenticator() {
        Authenticator.setDefault(this);
    }



    protected PasswordAuthentication getPasswordAuthentication() {
    	Credentials c = CredentialsStore.INSTANCE.getCredentials(getRequestingPrompt(), getRequestingHost());
        Message.debug("authentication: k='"+Credentials.buildKey(getRequestingPrompt(), getRequestingHost())+"' c='" + c + "'");
        return c != null ? new PasswordAuthentication(c.getUserName(), c.getPasswd().toCharArray()) : null;
    }


}
