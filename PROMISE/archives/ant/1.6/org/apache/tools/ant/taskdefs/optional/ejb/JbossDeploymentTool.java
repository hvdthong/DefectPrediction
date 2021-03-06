package org.apache.tools.ant.taskdefs.optional.ejb;

import java.io.File;
import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * The deployment tool to add the jboss specific deployment descriptor to the ejb jar file.
 * Jboss only requires one additional file jboss.xml and does not require any additional
 * compilation.
 *
 * @version 1.0
 * @see EjbJar#createJboss
 */
public class JbossDeploymentTool extends GenericDeploymentTool {
    protected static final String JBOSS_DD = "jboss.xml";
    protected static final String JBOSS_CMP10D = "jaws.xml";
    protected static final String JBOSS_CMP20D = "jbosscmp-jdbc.xml";

    /** Instance variable that stores the suffix for the jboss jarfile. */
    private String jarSuffix = ".jar";

    /**
     * Setter used to store the suffix for the generated JBoss jar file.
     * @param inString the string to use as the suffix.
     */
    public void setSuffix(String inString) {
        jarSuffix = inString;
    }

    /**
     * Add any vendor specific files which should be included in the
     * EJB Jar.
     */
    protected void addVendorFiles(Hashtable ejbFiles, String ddPrefix) {
        File jbossDD = new File(getConfig().descriptorDir, ddPrefix + JBOSS_DD);
        if (jbossDD.exists()) {
            ejbFiles.put(META_DIR + JBOSS_DD, jbossDD);
        } else {
            log("Unable to locate jboss deployment descriptor. "
                + "It was expected to be in " + jbossDD.getPath(),
                Project.MSG_WARN);
            return;
        }
        String descriptorFileName = JBOSS_CMP10D;
        if (EjbJar.CMPVersion.CMP2_0.equals(getParent().getCmpversion())) {
            descriptorFileName = JBOSS_CMP20D;
        }
        File jbossCMPD
            = new File(getConfig().descriptorDir, ddPrefix + descriptorFileName);

        if (jbossCMPD.exists()) {
            ejbFiles.put(META_DIR + descriptorFileName, jbossCMPD);
        } else {
            log("Unable to locate jboss cmp descriptor. "
                + "It was expected to be in "
                + jbossCMPD.getPath(), Project.MSG_VERBOSE);
            return;
        }
    }

    /**
     * Get the vendor specific name of the Jar that will be output. The modification date
     * of this jar will be checked against the dependent bean classes.
     */
    File getVendorOutputJarFile(String baseName) {
        if (getDestDir() == null && getParent().getDestdir() == null) {
            throw new BuildException("DestDir not specified");
        }
        if (getDestDir() == null) {
            return new File(getParent().getDestdir(), baseName + jarSuffix);
        } else {
            return new File(getDestDir(), baseName + jarSuffix);
        }
    }

    /**
     * Called to validate that the tool parameters have been configured.
     *
     * @throws BuildException If the Deployment Tool's configuration isn't
     *                        valid
     * @since ant 1.6
     */
    public void validateConfigured() throws BuildException {
    }

    private EjbJar getParent() {
        return (EjbJar) this.getTask();
    }
}
