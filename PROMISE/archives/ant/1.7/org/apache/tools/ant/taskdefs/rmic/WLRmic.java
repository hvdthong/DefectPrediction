package org.apache.tools.ant.taskdefs.rmic;

import java.lang.reflect.Method;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;

/**
 * The implementation of the rmic for WebLogic
 *
 * @since Ant 1.4
 */
public class WLRmic extends DefaultRmicAdapter {
    /** The classname of the weblogic rmic */
    public static final String WLRMIC_CLASSNAME = "weblogic.rmic";
    /**
     * the name of this adapter for users to select
     */
    public static final String COMPILER_NAME = "weblogic";

    /** The error string to use if not able to find the weblogic rmic */
    public static final String ERROR_NO_WLRMIC_ON_CLASSPATH =
        "Cannot use WebLogic rmic, as it is not "
    	+ "available. Add it to Ant's classpath with the -lib option";

    /** The error string to use if not able to start the weblogic rmic */
    public static final String ERROR_WLRMIC_FAILED = "Error starting WebLogic rmic: ";
    /** The stub suffix */
    public static final String WL_RMI_STUB_SUFFIX = "_WLStub";
    /** The skeleton suffix */
    public static final String WL_RMI_SKEL_SUFFIX = "_WLSkel";
    /** unsupported error message */
    public static final String UNSUPPORTED_STUB_OPTION = "Unsupported stub option: ";

    /**
     * Carry out the rmic compilation.
     * @return true if the compilation succeeded
     * @throws  BuildException on error
     */
    public boolean execute() throws BuildException {
        getRmic().log("Using WebLogic rmic", Project.MSG_VERBOSE);
        Commandline cmd = setupRmicCommand(new String[] {"-noexit"});

        AntClassLoader loader = null;
        try {
            Class c = null;
            if (getRmic().getClasspath() == null) {
                c = Class.forName(WLRMIC_CLASSNAME);
            } else {
                loader
                    = getRmic().getProject().createClassLoader(getRmic().getClasspath());
                c = Class.forName(WLRMIC_CLASSNAME, true, loader);
            }
            Method doRmic = c.getMethod("main",
                                        new Class [] {String[].class});
            doRmic.invoke(null, new Object[] {cmd.getArguments()});
            return true;
        } catch (ClassNotFoundException ex) {
            throw new BuildException(ERROR_NO_WLRMIC_ON_CLASSPATH, getRmic().getLocation());
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
            } else {
                throw new BuildException(ERROR_WLRMIC_FAILED, ex,
                                         getRmic().getLocation());
            }
        } finally {
            if (loader != null) {
                loader.cleanup();
            }
        }
    }

    /**
     * Get the suffix for the rmic stub classes
     * @return the stub suffix
     */
    public String getStubClassSuffix() {
        return WL_RMI_STUB_SUFFIX;
    }

    /**
     * Get the suffix for the rmic skeleton classes
     * @return the skeleton suffix
     */
    public String getSkelClassSuffix() {
        return WL_RMI_SKEL_SUFFIX;
    }

    /**
     * Strip out all -J args from the command list.
     *
     * @param compilerArgs the original compiler arguments
     * @return the filtered set.
     */
    protected String[] preprocessCompilerArgs(String[] compilerArgs) {
        return filterJvmCompilerArgs(compilerArgs);
    }
    
    /**
     * This is an override point; no stub version is returned. If any
     * stub option is set, a warning is printed.
     * @return null, for no stub version
     */
    protected String addStubVersionOptions() {
        String stubVersion = getRmic().getStubVersion();
        if (null != stubVersion) {
            getRmic().log(UNSUPPORTED_STUB_OPTION + stubVersion,
                          Project.MSG_WARN);
        }
        return null;
    }
}
