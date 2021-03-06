package org.apache.tools.ant.taskdefs.optional.ccm;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;


/**
 * A base class for creating tasks for executing commands on Continuus 5.1.
 * <p>
 * The class extends the  task as it operates by executing the ccm.exe program
 * supplied with Continuus/Synergy. By default the task expects the ccm executable to be
 * in the path,
 * you can override this be specifying the ccmdir attribute.
 * </p>
 *
 * @author Benoit Moussaud benoit.moussaud@criltelecom.com
 */
public abstract class Continuus extends Task {

    private String ccmDir = "";
    private String ccmAction = "";

    /**
     * Get the value of ccmAction.
     * @return value of ccmAction.
     */
    public String getCcmAction() {
        return ccmAction;
    }

    /**
     * Set the value of ccmAction.
     * @param v  Value to assign to ccmAction.
     * @ant.attribute ignore="true"
     */
    public void setCcmAction(String v) {
        this.ccmAction = v;
    }


    /**
     * Set the directory where the ccm executable is located.
     *
     * @param dir the directory containing the ccm executable
     */
    public final void setCcmDir(String dir) {
        ccmDir = project.translatePath(dir);
    }

    /**
     * Builds and returns the command string to execute ccm
     * @return String containing path to the executable
     */
    protected final String getCcmCommand() {
        String toReturn = ccmDir;
        if (!toReturn.equals("") && !toReturn.endsWith("/")) {
            toReturn += "/";
        }

        toReturn += CCM_EXE;

        return toReturn;
    }


    protected int run(Commandline cmd, ExecuteStreamHandler handler) {
        try {
            Execute exe = new Execute(handler);
            exe.setAntRun(getProject());
            exe.setWorkingDirectory(getProject().getBaseDir());
            exe.setCommandline(cmd.getCommandline());
            return exe.execute();
        } catch (java.io.IOException e) {
            throw new BuildException(e, location);
        }
    }

    protected int run(Commandline cmd) {
        return run(cmd, new LogStreamHandler(this, Project.MSG_VERBOSE, Project.MSG_WARN));
    }

    /**
     * Constant for the thing to execute
     */
    private static final String CCM_EXE = "ccm";

    /**
     * The 'CreateTask' command
     */
    public static final String COMMAND_CREATE_TASK = "create_task";
    /**
     * The 'Checkout' command
     */
    public static final String COMMAND_CHECKOUT = "co";
    /**
     * The 'Checkin' command
     */
    public static final String COMMAND_CHECKIN = "ci";
    /**
     * The 'Reconfigure' command
     */
    public static final String COMMAND_RECONFIGURE = "reconfigure";

    /**
     * The 'Reconfigure' command
     */
    public static final String COMMAND_DEFAULT_TASK = "default_task";


}
