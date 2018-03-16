package org.apache.tools.ant.taskdefs.optional.vss;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

import java.io.File;

/**
 * Perform Get commands to Microsoft Visual SourceSafe.
 * <p>
 * The following attributes are interpreted:
 * <table border="1">
 *   <tr>
 *     <th>Attribute</th>
 *     <th>Values</th>
 *     <th>Required</th>
 *   </tr>
 *   <tr>
 *      <td>login</td>
 *      <td>username,password</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>vsspath</td>
 *      <td>SourceSafe path</td>
 *      <td>Yes</td>
 *   </tr>
 *   <tr>
 *      <td>localpath</td>
 *      <td>Override the working directory and get to the specified path</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>writable</td>
 *      <td>true or false</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>recursive</td>
 *      <td>true or false</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>version</td>
 *      <td>a version number to get</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>date</td>
 *      <td>a date stamp to get at</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>label</td>
 *      <td>a label to get for</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>quiet</td>
 *      <td>suppress output (off by default)</td>
 *      <td>No</td>
 *   </tr>
 *   <tr>
 *      <td>autoresponse</td>
 *      <td>What to respond with (sets the -I option). By default, -I- is
 *      used; values of Y or N will be appended to this.</td>
 *      <td>No</td>
 *   </tr>
 * </table>
 * <p>Note that only one of version, date or label should be specified</p>
 *
 * @author Craig Cottingham
 * @author Andrew Everitt
 *
 * @ant.task name="vssget" category="scm"
 */
public class MSVSSGET extends MSVSS {

    private String m_LocalPath = null;
    private boolean m_Recursive = false;
    private boolean m_Writable = false;
    private String m_Version = null;
    private String m_Date = null;
    private String m_Label = null;
    private String m_AutoResponse = null;
    private boolean m_Quiet = false;

    /**
     * Executes the task.
     * <p>
     * Builds a command line to execute ss and then calls Exec's run method
     * to execute the command line.
     */
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        int result = 0;

        if (getVsspath() == null) {
            String msg = "vsspath attribute must be set!";
            throw new BuildException(msg, location);
        }


        commandLine.setExecutable(getSSCommand());
        commandLine.createArgument().setValue(COMMAND_GET);

        commandLine.createArgument().setValue(getVsspath());
        getLocalpathCommand(commandLine);
        getAutoresponse(commandLine);
        getQuietCommand(commandLine);
        getRecursiveCommand(commandLine);
        getVersionCommand(commandLine);
        getWritableCommand(commandLine);
        getLoginCommand(commandLine);

        result = run(commandLine);
        if (result != 0) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, location);
        }
    }

    /**
     * Override the working directory and get to the specified path; optional.
     */
    public void setLocalpath(Path localPath) {
        m_LocalPath = localPath.toString();
    }

    /**
     * Builds and returns the -GL flag command if required.
     * <p>
     * The localpath is created if it didn't exist
     */
    public void getLocalpathCommand(Commandline cmd) {
        if (m_LocalPath == null) {
            return;
        } else {
            File dir = project.resolveFile(m_LocalPath);
            if (!dir.exists()) {
                boolean done = dir.mkdirs();
                if (!done) {
                    String msg = "Directory " + m_LocalPath + " creation was not " +
                        "successful for an unknown reason";
                    throw new BuildException(msg, location);
                }
                project.log("Created dir: " + dir.getAbsolutePath());
            }

            cmd.createArgument().setValue(FLAG_OVERRIDE_WORKING_DIR + m_LocalPath);
        }
    }

    /**
     * Flag to tell the task to recurse down the tree;
     * optional, default false.
     */
    public void setRecursive(boolean recursive) {
        m_Recursive = recursive;
    }

    /**
     * @return the 'recursive' command if the attribute was 'true', otherwise an empty string
     */
    public void getRecursiveCommand(Commandline cmd) {
        if (!m_Recursive) {
            return;
        } else {
            cmd.createArgument().setValue(FLAG_RECURSION);
        }
    }

    /**
     * Flag to suppress output when true ; false by default.
     */
    public final void setQuiet (boolean quiet) {
        this.m_Quiet = quiet;
    }
    
    public void getQuietCommand (Commandline cmd) {
        if (m_Quiet) {
            cmd.createArgument().setValue (FLAG_QUIET);
        }
    }
    
    /**
     * make fetched files  writable; optional, default false.
     */
    public final void setWritable(boolean argWritable) {
        m_Writable = argWritable;
    }

    /**
     * @return the 'make writable' command if the attribute was 'true', otherwise an empty string
     */
    public void getWritableCommand(Commandline cmd) {
        if (!m_Writable) {
            return;
        } else {
            cmd.createArgument().setValue(FLAG_WRITABLE);
        }
    }

    /**
     * Set a version number to get; 
     * optional, only one of <tt>version</tt>, <tt>label</tt>, or <tt>date</tt>
     * allowed.
     * <p>
     * ORIGINAL COMMENT THAT DOES NOT SEEM AT ALL VALID:
     * Note we assume that if the supplied string has the value "null" that something
     * went wrong and that the string value got populated from a null object. This
     * happens if a ant variable is used e.g. version="${ver_server}" when ver_server
     * has not been defined to ant!
     * NO, in this case the version string is "${ver_server}".
     * @todo fix this
     */
    public void setVersion(String version) {
        if (version.equals("") || version.equals("null")) {
            m_Version = null;
        } else {
            m_Version = version;
        }
    }

    /**
     * Set the date to get;
     * optional, only one of <tt>version</tt>, <tt>label</tt>, or <tt>date</tt>
     * allowed.
     * <p>
     * ORIGINAL COMMENT THAT DOES NOT SEEM AT ALL VALID:
     * Note we assume that if the supplied string has the value "null" that something
     * went wrong and that the string value got populated from a null object. This
     * happens if a ant variable is used e.g. date="${date}" when date
     * has not been defined to ant!
     * @todo fix this
     */
    public void setDate(String date) {
        if (date.equals("") || date.equals("null")) {
            m_Date = null;
        } else {
            m_Date = date;
        }
    }

    /**
     * Set the label to get;
     * optional, only one of <tt>version</tt>, <tt>label</tt>, or <tt>date</tt>
     * allowed.
     * <p>
     * Note we assume that if the supplied string has the value "null" that something
     * went wrong and that the string value got populated from a null object. This
     * happens if a ant variable is used e.g. label="${label_server}" when label_server
     * has not been defined to ant!
     */
    public void setLabel(String label) {
        if (label.equals("") || label.equals("null")) {
            m_Label = null;
        } else {
            m_Label = label;
        }
    }

    /**
     * Simple order of priority. Returns the first specified of version, date, label.
     * If none of these was specified returns ""
     */
    public void getVersionCommand(Commandline cmd) {

        if (m_Version != null) {
            cmd.createArgument().setValue(FLAG_VERSION + m_Version);
        } else if (m_Date != null) {
            cmd.createArgument().setValue(FLAG_VERSION_DATE + m_Date);
        } else if (m_Label != null) {
            cmd.createArgument().setValue(FLAG_VERSION_LABEL + m_Label);
        }
    }

    /**
     * What to respond with (sets the -I option). By default, -I- is
     * used; values of Y or N will be appended to this.
     */
    public void setAutoresponse(String response){
        if (response.equals("") || response.equals("null")) {
            m_AutoResponse = null;
        } else {
            m_AutoResponse = response;
        }
    }
    
    /**
     * Checks the value set for the autoResponse.
     * if it equals "Y" then we return -I-Y
     * if it equals "N" then we return -I-N
     * otherwise we return -I
     */
    public void getAutoresponse(Commandline cmd) {
        
        if (m_AutoResponse == null) {
            cmd.createArgument().setValue(FLAG_AUTORESPONSE_DEF);
        } else if (m_AutoResponse.equalsIgnoreCase("Y")) {
            cmd.createArgument().setValue(FLAG_AUTORESPONSE_YES);
            
        } else if (m_AutoResponse.equalsIgnoreCase("N")) {
            cmd.createArgument().setValue(FLAG_AUTORESPONSE_NO);
        } else {
            cmd.createArgument().setValue(FLAG_AUTORESPONSE_DEF);

    }
}
