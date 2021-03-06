package org.apache.tools.ant.taskdefs.optional.ssh;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSch;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Base class for Ant tasks using jsch.
 *
 * @author charliehubbard76@yahoo.com
 * @author riznob@hotmail.com
 * @since Ant 1.6
 */
public abstract class SSHBase extends Task implements LogListener {

    /** Default listen port for SSH daemon */
    private static final int SSH_PORT = 22;

    private String host;
    private String keyfile;
    private String knownHosts;
    private int port = SSH_PORT;
    private boolean failOnError = true;
    private SSHUserInfo userInfo;

    /**
     * Constructor for SSHBase.
     */
    public SSHBase() {
        super();
        userInfo = new SSHUserInfo();
    }

    /**
     * Remote host, either DNS name or IP.
     *
     * @param host  The new host value
     */
    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setFailonerror(boolean failure) {
        failOnError = failure;
    }

    public boolean getFailonerror() {
        return failOnError;
    }

    /**
     * Username known to remote host.
     *
     * @param username  The new username value
     */
    public void setUsername(String username) {
        userInfo.setName(username);
    }


    /**
     * Sets the password for the user.
     *
     * @param password  The new password value
     */
    public void setPassword(String password) {
        userInfo.setPassword(password);
    }

    /**
     * Sets the keyfile for the user.
     *
     * @param keyfile  The new keyfile value
     */
    public void setKeyfile(String keyfile) {
        userInfo.setKeyfile(keyfile);
    }

    /**
     * Sets the passphrase for the users key.
     *
     * @param passphrase  The new passphrase value
     */
    public void setPassphrase(String passphrase) {
        userInfo.setPassphrase(passphrase);
    }

    /**
     * Sets the path to the file that has the identities of
     * all known hosts.  This is used by SSH protocol to validate
     * the identity of the host.  The default is
     * <i>${user.home}/.ssh/known_hosts</i>.
     *
     * @param knownHosts a path to the known hosts file.
     */
    public void setKnownhosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    /**
     * Setting this to true trusts hosts whose identity is unknown.
     *
     * @param yesOrNo if true trust the identity of unknown hosts.
     */
    public void setTrust(boolean yesOrNo) {
        userInfo.setTrust(yesOrNo);
    }

    /**
     * Changes the port used to connect to the remote host.
     *
     * @param port port number of remote host.
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void init() throws BuildException {
        super.init();
        this.knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
        this.port = SSH_PORT;
    }

    protected Session openSession() throws JSchException {
        JSch jsch = new JSch();
        if (null != userInfo.getKeyfile()) {
            jsch.addIdentity(userInfo.getKeyfile());
        }

        if (!userInfo.getTrust() && knownHosts != null) {
            log("Using known hosts: " + knownHosts, Project.MSG_DEBUG);
            jsch.setKnownHosts(knownHosts);
        }

        Session session = jsch.getSession(userInfo.getName(), host, port);
        session.setUserInfo(userInfo);
        log("Connecting to " + host + ":" + port);
        session.connect();
        return session;
    }

    protected SSHUserInfo getUserInfo() {
        return userInfo;
    }
}
