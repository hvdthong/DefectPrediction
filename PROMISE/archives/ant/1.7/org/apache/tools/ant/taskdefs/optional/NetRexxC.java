package org.apache.tools.ant.taskdefs.optional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import netrexx.lang.Rexx;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileUtils;

/**
 * Compiles NetRexx source files.
 * This task can take the following
 * arguments:
 * <ul>
 * <li>binary</li>
 * <li>classpath</li>
 * <li>comments</li>
 * <li>compile</li>
 * <li>console</li>
 * <li>crossref</li>
 * <li>decimal</li>
 * <li>destdir</li>
 * <li>diag</li>
 * <li>explicit</li>
 * <li>format</li>
 * <li>keep</li>
 * <li>logo</li>
 * <li>replace</li>
 * <li>savelog</li>
 * <li>srcdir</li>
 * <li>sourcedir</li>
 * <li>strictargs</li>
 * <li>strictassign</li>
 * <li>strictcase</li>
 * <li>strictimport</li>
 * <li>symbols</li>
 * <li>time</li>
 * <li>trace</li>
 * <li>utf8</li>
 * <li>verbose</li>
 * <li>suppressMethodArgumentNotUsed</li>
 * <li>suppressPrivatePropertyNotUsed</li>
 * <li>suppressVariableNotUsed</li>
 * <li>suppressExceptionNotSignalled</li>
 * <li>suppressDeprecation</li>
 * </ul>
 * Of these arguments, the <b>srcdir</b> argument is required.
 *
 * <p>When this task executes, it will recursively scan the srcdir
 * looking for NetRexx source files to compile. This task makes its
 * compile decision based on timestamp.
 * <p>Before files are compiled they and any other file in the
 * srcdir will be copied to the destdir allowing support files to be
 * located properly in the classpath. The reason for copying the source files
 * before the compile is that NetRexxC has only two destinations for classfiles:
 * <ol>
 * <li>The current directory, and,</li>
 * <li>The directory the source is in (see sourcedir option)
 * </ol>
 *
 */
public class NetRexxC extends MatchingTask {

    private boolean binary;
    private String classpath;
    private boolean comments;
    private boolean compile = true;
    private boolean console;
    private boolean crossref;
    private boolean decimal = true;
    private File destDir;
    private boolean diag;
    private boolean explicit;
    private boolean format;
    private boolean keep;
    private boolean logo = true;
    private boolean replace;
    private boolean savelog;
    private File srcDir;
    private boolean strictargs;
    private boolean strictassign;
    private boolean strictcase;
    private boolean strictimport;
    private boolean strictprops;
    private boolean strictsignal;
    private boolean symbols;
    private boolean time;
    private String trace = "trace2";
    private boolean utf8;
    private String verbose = "verbose3";
    private boolean suppressMethodArgumentNotUsed = false;
    private boolean suppressPrivatePropertyNotUsed = false;
    private boolean suppressVariableNotUsed = false;
    private boolean suppressExceptionNotSignalled = false;
    private boolean suppressDeprecation = false;

    static final String MSG_METHOD_ARGUMENT_NOT_USED
        = "Warning: Method argument is not used";
    static final String MSG_PRIVATE_PROPERTY_NOT_USED
        = "Warning: Private property is defined but not used";
    static final String MSG_VARIABLE_NOT_USED
        = "Warning: Variable is set but not used";
    static final String MSG_EXCEPTION_NOT_SIGNALLED
        = "is in SIGNALS list but is not signalled within the method";
    static final String MSG_DEPRECATION = "has been deprecated";

    private Vector compileList = new Vector();
    private Hashtable filecopyList = new Hashtable();

    /**
     * Set whether literals are treated as binary, rather than NetRexx types.
     * @param binary a <code>boolean</code> value.
     */
    public void setBinary(boolean binary) {
        this.binary = binary;
    }


    /**
     * Set the classpath used for NetRexx compilation.
     * @param classpath the classpath to use.
     */
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }


    /**
     * Set whether comments are passed through to the generated java source.
     * Valid true values are "on" or "true". Anything else sets the flag to
     * false. The default value is false
     * @param comments a <code>boolean</code> value.
     */
    public void setComments(boolean comments) {
        this.comments = comments;
    }


    /**
     * Set whether error messages come out in compact or verbose format. Valid
     * true values are "on" or "true". Anything else sets the flag to false.
     * The default value is false
     * @param compact a <code>boolean</code> value.
     */
    public void setCompact(boolean compact) {
        this.compact = compact;
    }


    /**
     * Set whether the NetRexx compiler should compile the generated java code
     * Valid true values are "on" or "true". Anything else sets the flag to
     * false. The default value is true. Setting this flag to false, will
     * automatically set the keep flag to true.
     * @param compile a <code>boolean</code> value.
     */
    public void setCompile(boolean compile) {
        this.compile = compile;
        if (!this.compile && !this.keep) {
            this.keep = true;
        }
    }


    /**
     * Set whether or not messages should be displayed on the 'console' Valid
     * true values are "on" or "true". Anything else sets the flag to false.
     * The default value is true.
     * @param console a <code>boolean</code> value.
     */
    public void setConsole(boolean console) {
        this.console = console;
    }


    /**
     * Whether variable cross references are generated.
     * @param crossref a <code>boolean</code> value.
     */
    public void setCrossref(boolean crossref) {
        this.crossref = crossref;
    }


    /**
     * Set whether decimal arithmetic should be used for the netrexx code.
     * Binary arithmetic is used when this flag is turned off. Valid true
     * values are "on" or "true". Anything else sets the flag to false. The
     * default value is true.
     * @param decimal a <code>boolean</code> value.
     */
    public void setDecimal(boolean decimal) {
        this.decimal = decimal;
    }


    /**
     * Set the destination directory into which the NetRexx source files
     * should be copied and then compiled.
     * @param destDirName the destination directory.
     */
    public void setDestDir(File destDirName) {
        destDir = destDirName;
    }


    /**
     * Whether diagnostic information about the compile is generated
     * @param diag a <code>boolean</code> value.
     */
    public void setDiag(boolean diag) {
        this.diag = diag;
    }


    /**
     * Sets whether variables must be declared explicitly before use. Valid
     * true values are "on" or "true". Anything else sets the flag to false.
     * The default value is false.
     * @param explicit a <code>boolean</code> value.
     */
    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }


    /**
     * Whether the generated java code is formatted nicely or left to match
     * NetRexx line numbers for call stack debugging.
     * @param format a <code>boolean</code> value.
     */
    public void setFormat(boolean format) {
        this.format = format;
    }


    /**
     * Whether the generated java code is produced Valid true values are "on"
     * or "true". Anything else sets the flag to false. The default value is
     * false.
     * @param java a <code>boolean</code> value.
     */
    public void setJava(boolean java) {
        log("The attribute java is currently unused.", Project.MSG_WARN);
    }


    /**
     * Sets whether the generated java source file should be kept after
     * compilation. The generated files will have an extension of .java.keep,
     * <b>not</b> .java Valid true values are "on" or "true". Anything else
     * sets the flag to false. The default value is false.
     * @param keep a <code>boolean</code> value.
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }


    /**
     * Whether the compiler text logo is displayed when compiling.
     * @param logo a <code>boolean</code> value.
     */
    public void setLogo(boolean logo) {
        this.logo = logo;
    }


    /**
     * Whether the generated .java file should be replaced when compiling
     * Valid true values are "on" or "true". Anything else sets the flag to
     * false. The default value is false.
     * @param replace a <code>boolean</code> value.
     */
    public void setReplace(boolean replace) {
        this.replace = replace;
    }


    /**
     * Sets whether the compiler messages will be written to NetRexxC.log as
     * well as to the console Valid true values are "on" or "true". Anything
     * else sets the flag to false. The default value is false.
     * @param savelog a <code>boolean</code> value.
     */
    public void setSavelog(boolean savelog) {
        this.savelog = savelog;
    }


    /**
     * Tells the NetRexx compiler to store the class files in the same
     * directory as the source files. The alternative is the working directory
     * Valid true values are "on" or "true". Anything else sets the flag to
     * false. The default value is true.
     * @param sourcedir a <code>boolean</code> value.
     */
    public void setSourcedir(boolean sourcedir) {
        this.sourcedir = sourcedir;
    }


    /**
     * Set the source dir to find the source Java files.
     * @param srcDirName the source directory.
     */
    public void setSrcDir(File srcDirName) {
        srcDir = srcDirName;
    }


    /**
     * Tells the NetRexx compiler that method calls always need parentheses,
     * even if no arguments are needed, e.g. <code>aStringVar.getBytes</code>
     * vs. <code>aStringVar.getBytes()</code> Valid true values are "on" or
     * "true". Anything else sets the flag to false. The default value is
     * false.
     * @param strictargs a <code>boolean</code> value.
     */
    public void setStrictargs(boolean strictargs) {
        this.strictargs = strictargs;
    }


    /**
     * Tells the NetRexx compile that assignments must match exactly on type.
     * @param strictassign a <code>boolean</code> value.
     */
    public void setStrictassign(boolean strictassign) {
        this.strictassign = strictassign;
    }


    /**
     * Specifies whether the NetRexx compiler should be case sensitive or not.
     * @param strictcase a <code>boolean</code> value.
     */
    public void setStrictcase(boolean strictcase) {
        this.strictcase = strictcase;
    }


    /**
     * Sets whether classes need to be imported explicitly using an <code>import</code>
     * statement. By default the NetRexx compiler will import certain packages
     * automatically Valid true values are "on" or "true". Anything else sets
     * the flag to false. The default value is false.
     * @param strictimport a <code>boolean</code> value.
     */
    public void setStrictimport(boolean strictimport) {
        this.strictimport = strictimport;
    }


    /**
     * Sets whether local properties need to be qualified explicitly using
     * <code>this</code> Valid true values are "on" or "true". Anything else
     * sets the flag to false. The default value is false.
     * @param strictprops a <code>boolean</code> value.
     */
    public void setStrictprops(boolean strictprops) {
        this.strictprops = strictprops;
    }


    /**
     * Whether the compiler should force catching of exceptions by explicitly
     * named types.
     * @param strictsignal a <code>boolean</code> value.
     */
    public void setStrictsignal(boolean strictsignal) {
        this.strictsignal = strictsignal;
    }


    /**
     * Sets whether debug symbols should be generated into the class file
     * Valid true values are "on" or "true". Anything else sets the flag to
     * false. The default value is false.
     * @param symbols a <code>boolean</code> value.
     */
    public void setSymbols(boolean symbols) {
        this.symbols = symbols;
    }


    /**
     * Asks the NetRexx compiler to print compilation times to the console
     * Valid true values are "on" or "true". Anything else sets the flag to
     * false. The default value is false.
     * @param time a <code>boolean</code> value.
     */
    public void setTime(boolean time) {
        this.time = time;
    }

    /**
     * Turns on or off tracing and directs the resultant trace output Valid
     * values are: "trace", "trace1", "trace2" and "notrace". "trace" and
     * "trace2".
     * @param trace the value to set.
     */
    public void setTrace(TraceAttr trace) {
        this.trace = trace.getValue();
    }

    /**
     * Turns on or off tracing and directs the resultant trace output Valid
     * values are: "trace", "trace1", "trace2" and "notrace". "trace" and
     * "trace2".
     * @param trace the value to set.
     */
    public void setTrace(String trace) {
        TraceAttr t = new TraceAttr();

        t.setValue(trace);
        setTrace(t);
    }


    /**
     * Tells the NetRexx compiler that the source is in UTF8 Valid true values
     * are "on" or "true". Anything else sets the flag to false. The default
     * value is false.
     * @param utf8 a <code>boolean</code> value.
     */
    public void setUtf8(boolean utf8) {
        this.utf8 = utf8;
    }


    /**
     * Whether lots of warnings and error messages should be generated
     * @param verbose the value to set - verbose&lt;level&gt; or noverbose.
     */
    public void setVerbose(VerboseAttr verbose) {
        this.verbose = verbose.getValue();
    }


    /**
     * Whether lots of warnings and error messages should be generated
     * @param verbose the value to set - verbose&lt;level&gt; or noverbose.
     */
    public void setVerbose(String verbose) {
        VerboseAttr v = new VerboseAttr();

        v.setValue(verbose);
        setVerbose(v);
    }

    /**
     * Whether the task should suppress the "Method argument is not used" in
     * strictargs-Mode, which can not be suppressed by the compiler itself.
     * The warning is logged as verbose message, though.
     * @param suppressMethodArgumentNotUsed a <code>boolean</code> value.
     */
    public void setSuppressMethodArgumentNotUsed(boolean suppressMethodArgumentNotUsed) {
        this.suppressMethodArgumentNotUsed = suppressMethodArgumentNotUsed;
    }


    /**
     * Whether the task should suppress the "Private property is defined but
     * not used" in strictargs-Mode, which can be quite annoying while
     * developing. The warning is logged as verbose message, though.
     * @param suppressPrivatePropertyNotUsed a <code>boolean</code> value.
     */
    public void setSuppressPrivatePropertyNotUsed(boolean suppressPrivatePropertyNotUsed) {
        this.suppressPrivatePropertyNotUsed = suppressPrivatePropertyNotUsed;
    }


    /**
     * Whether the task should suppress the "Variable is set but not used" in
     * strictargs-Mode. Be careful with this one! The warning is logged as
     * verbose message, though.
     * @param suppressVariableNotUsed a <code>boolean</code> value.
     */
    public void setSuppressVariableNotUsed(boolean suppressVariableNotUsed) {
        this.suppressVariableNotUsed = suppressVariableNotUsed;
    }


    /**
     * Whether the task should suppress the "FooException is in SIGNALS list
     * but is not signalled within the method", which is sometimes rather
     * useless. The warning is logged as verbose message, though.
     * @param suppressExceptionNotSignalled a <code>boolean</code> value.
     */
    public void setSuppressExceptionNotSignalled(boolean suppressExceptionNotSignalled) {
        this.suppressExceptionNotSignalled = suppressExceptionNotSignalled;
    }


    /**
     * Tells whether we should filter out any deprecation-messages
     * of the compiler out.
     * @param suppressDeprecation a <code>boolean</code> value.
     */
    public void setSuppressDeprecation(boolean suppressDeprecation) {
        this.suppressDeprecation = suppressDeprecation;
    }


    /**
     * init-Method sets defaults from Properties. That way, when ant is called
     * with arguments like -Dant.netrexxc.verbose=verbose5 one can easily take
     * control of all netrexxc-tasks.
     */
    public void init() {
        String p;

        if ((p = getProject().getProperty("ant.netrexxc.binary")) != null) {
            this.binary = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.comments")) != null) {
            this.comments = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.compact")) != null) {
            this.compact = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.compile")) != null) {
            this.compile = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.console")) != null) {
            this.console = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.crossref")) != null) {
            this.crossref = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.decimal")) != null) {
            this.decimal = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.diag")) != null) {
            this.diag = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.explicit")) != null) {
            this.explicit = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.format")) != null) {
            this.format = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.keep")) != null) {
            this.keep = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.logo")) != null) {
            this.logo = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.replace")) != null) {
            this.replace = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.savelog")) != null) {
            this.savelog = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.sourcedir")) != null) {
            this.sourcedir = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictargs")) != null) {
            this.strictargs = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictassign")) != null) {
            this.strictassign = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictcase")) != null) {
            this.strictcase = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictimport")) != null) {
            this.strictimport = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictprops")) != null) {
            this.strictprops = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictsignal")) != null) {
            this.strictsignal = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.symbols")) != null) {
            this.symbols = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.time")) != null) {
            this.time = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.trace")) != null) {
            setTrace(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.utf8")) != null) {
            this.utf8 = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.verbose")) != null) {
            setVerbose(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressMethodArgumentNotUsed")) != null) {
            this.suppressMethodArgumentNotUsed = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressPrivatePropertyNotUsed")) != null) {
            this.suppressPrivatePropertyNotUsed = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressVariableNotUsed")) != null) {
            this.suppressVariableNotUsed = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressExceptionNotSignalled")) != null) {
            this.suppressExceptionNotSignalled = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressDeprecation")) != null) {
            this.suppressDeprecation = Project.toBoolean(p);
        }
    }


    /**
     * Executes the task - performs the actual compiler call.
     * @throws BuildException on error.
     */
    public void execute() throws BuildException {

        if (srcDir == null || destDir == null) {
            throw new BuildException("srcDir and destDir attributes must be set!");
        }

        DirectoryScanner ds = getDirectoryScanner(srcDir);

        String[] files = ds.getIncludedFiles();

        scanDir(srcDir, destDir, files);

        copyFilesToDestination();

        if (compileList.size() > 0) {
            log("Compiling " + compileList.size() + " source file"
                 + (compileList.size() == 1 ? "" : "s")
                 + " to " + destDir);
            doNetRexxCompile();
        }
    }


    /**
     * Scans the directory looking for source files to be compiled and support
     * files to be copied.
     */
    private void scanDir(File srcDir, File destDir, String[] files) {
        for (int i = 0; i < files.length; i++) {
            File srcFile = new File(srcDir, files[i]);
            File destFile = new File(destDir, files[i]);
            String filename = files[i];
            if (filename.toLowerCase().endsWith(".nrx")) {
                File classFile =
                    new File(destDir,
                    filename.substring(0, filename.lastIndexOf('.')) + ".class");

                if (!compile || srcFile.lastModified() > classFile.lastModified()) {
                    filecopyList.put(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                    compileList.addElement(destFile.getAbsolutePath());
                }
            } else {
                if (srcFile.lastModified() > destFile.lastModified()) {
                    filecopyList.put(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                }
            }
        }
    }


    /** Copy eligible files from the srcDir to destDir  */
    private void copyFilesToDestination() {
        if (filecopyList.size() > 0) {
            log("Copying " + filecopyList.size() + " file"
                 + (filecopyList.size() == 1 ? "" : "s")
                 + " to " + destDir.getAbsolutePath());

            Enumeration e = filecopyList.keys();

            while (e.hasMoreElements()) {
                String fromFile = (String) e.nextElement();
                String toFile = (String) filecopyList.get(fromFile);

                try {
                    FileUtils.getFileUtils().copyFile(fromFile, toFile);
                } catch (IOException ioe) {
                    String msg = "Failed to copy " + fromFile + " to " + toFile
                         + " due to " + ioe.getMessage();

                    throw new BuildException(msg, ioe);
                }
            }
        }
    }


    /** Performs a compile using the NetRexx 1.1.x compiler  */
    private void doNetRexxCompile() throws BuildException {
        log("Using NetRexx compiler", Project.MSG_VERBOSE);

        String classpath = getCompileClasspath();
        StringBuffer compileOptions = new StringBuffer();

        String[] compileOptionsArray = getCompileOptionsAsArray();
        String[] fileListArray = new String[compileList.size()];
        Enumeration e = compileList.elements();
        int j = 0;

        while (e.hasMoreElements()) {
            fileListArray[j] = (String) e.nextElement();
            j++;
        }
        String[] compileArgs = new String[compileOptionsArray.length + fileListArray.length];

        for (int i = 0; i < compileOptionsArray.length; i++) {
            compileArgs[i] = compileOptionsArray[i];
        }
        for (int i = 0; i < fileListArray.length; i++) {
            compileArgs[i + compileOptionsArray.length] = fileListArray[i];
        }

        compileOptions.append("Compilation args: ");
        for (int i = 0; i < compileOptionsArray.length; i++) {
            compileOptions.append(compileOptionsArray[i]);
            compileOptions.append(" ");
        }
        log(compileOptions.toString(), Project.MSG_VERBOSE);

        String eol = System.getProperty("line.separator");
        StringBuffer niceSourceList = new StringBuffer("Files to be compiled:" + eol);

        for (int i = 0; i < compileList.size(); i++) {
            niceSourceList.append("    ");
            niceSourceList.append(compileList.elementAt(i).toString());
            niceSourceList.append(eol);
        }

        log(niceSourceList.toString(), Project.MSG_VERBOSE);

        String currentClassPath = System.getProperty("java.class.path");
        Properties currentProperties = System.getProperties();

        currentProperties.put("java.class.path", classpath);

        try {
            StringWriter out = new StringWriter();
            int rc =
                COM.ibm.netrexx.process.NetRexxC.main(new Rexx(compileArgs), new PrintWriter(out));
            String sdir = srcDir.getAbsolutePath();
            String ddir = destDir.getAbsolutePath();
            boolean doReplace = !(sdir.equals(ddir));
            int dlen = ddir.length();
            String l;
            BufferedReader in = new BufferedReader(new StringReader(out.toString()));

            log("replacing destdir '" + ddir + "' through sourcedir '"
                + sdir + "'", Project.MSG_VERBOSE);
            while ((l = in.readLine()) != null) {
                int idx;

                while (doReplace && ((idx = l.indexOf(ddir)) != -1)) {
                    l = (new StringBuffer(l)).replace(idx, idx + dlen, sdir).toString();
                }
                if (suppressMethodArgumentNotUsed
                    && l.indexOf(MSG_METHOD_ARGUMENT_NOT_USED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressPrivatePropertyNotUsed
                    && l.indexOf(MSG_PRIVATE_PROPERTY_NOT_USED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressVariableNotUsed
                    && l.indexOf(MSG_VARIABLE_NOT_USED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressExceptionNotSignalled
                    && l.indexOf(MSG_EXCEPTION_NOT_SIGNALLED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressDeprecation
                    && l.indexOf(MSG_DEPRECATION) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (l.indexOf("Error:") != -1) {
                    log(l, Project.MSG_ERR);
                } else if (l.indexOf("Warning:") != -1) {
                    log(l, Project.MSG_WARN);
                } else {
                }
            }
            if (rc > 1) {
                throw new BuildException("Compile failed, messages should "
                    + "have been provided.");
            }
        } catch (IOException ioe) {
            throw new BuildException("Unexpected IOException while "
                + "playing with Strings", ioe);
        } finally {
            currentProperties = System.getProperties();
            currentProperties.put("java.class.path", currentClassPath);
        }

    }


    /** Builds the compilation classpath.  */
    private String getCompileClasspath() {
        StringBuffer classpath = new StringBuffer();

        classpath.append(destDir.getAbsolutePath());

        if (this.classpath != null) {
            addExistingToClasspath(classpath, this.classpath);
        }

        return classpath.toString();
    }


    /** This  */
    private String[] getCompileOptionsAsArray() {
        Vector options = new Vector();

        options.addElement(binary ? "-binary" : "-nobinary");
        options.addElement(comments ? "-comments" : "-nocomments");
        options.addElement(compile ? "-compile" : "-nocompile");
        options.addElement(compact ? "-compact" : "-nocompact");
        options.addElement(console ? "-console" : "-noconsole");
        options.addElement(crossref ? "-crossref" : "-nocrossref");
        options.addElement(decimal ? "-decimal" : "-nodecimal");
        options.addElement(diag ? "-diag" : "-nodiag");
        options.addElement(explicit ? "-explicit" : "-noexplicit");
        options.addElement(format ? "-format" : "-noformat");
        options.addElement(keep ? "-keep" : "-nokeep");
        options.addElement(logo ? "-logo" : "-nologo");
        options.addElement(replace ? "-replace" : "-noreplace");
        options.addElement(savelog ? "-savelog" : "-nosavelog");
        options.addElement(sourcedir ? "-sourcedir" : "-nosourcedir");
        options.addElement(strictargs ? "-strictargs" : "-nostrictargs");
        options.addElement(strictassign ? "-strictassign" : "-nostrictassign");
        options.addElement(strictcase ? "-strictcase" : "-nostrictcase");
        options.addElement(strictimport ? "-strictimport" : "-nostrictimport");
        options.addElement(strictprops ? "-strictprops" : "-nostrictprops");
        options.addElement(strictsignal ? "-strictsignal" : "-nostrictsignal");
        options.addElement(symbols ? "-symbols" : "-nosymbols");
        options.addElement(time ? "-time" : "-notime");
        options.addElement("-" + trace);
        options.addElement(utf8 ? "-utf8" : "-noutf8");
        options.addElement("-" + verbose);

        String[] results = new String[options.size()];

        options.copyInto(results);
        return results;
    }


    /**
     * Takes a classpath-like string, and adds each element of this string to
     * a new classpath, if the components exist. Components that don't exist,
     * aren't added. We do this, because jikes issues warnings for
     * non-existant files/dirs in his classpath, and these warnings are pretty
     * annoying.
     *
     * @param target - target classpath
     * @param source - source classpath to get file objects.
     */
    private void addExistingToClasspath(StringBuffer target, String source) {
        StringTokenizer tok = new StringTokenizer(source,
            System.getProperty("path.separator"), false);

        while (tok.hasMoreTokens()) {
            File f = getProject().resolveFile(tok.nextToken());

            if (f.exists()) {
                target.append(File.pathSeparator);
                target.append(f.getAbsolutePath());
            } else {
                log("Dropping from classpath: "
                    + f.getAbsolutePath(), Project.MSG_VERBOSE);
            }
        }

    }


    /**
     * Enumerated class corresponding to the trace attribute.
     */
    public static class TraceAttr extends EnumeratedAttribute {
        /** {@inheritDoc}. */
        public String[] getValues() {
            return new String[]{"trace", "trace1", "trace2", "notrace"};
        }
    }

    /**
     * Enumerated class corresponding to the verbose attribute.
     */
    public static class VerboseAttr extends EnumeratedAttribute {
        /** {@inheritDoc}. */
        public String[] getValues() {
            return new String[]{"verbose", "verbose0", "verbose1",
                "verbose2", "verbose3", "verbose4",
                "verbose5", "noverbose"};
        }
    }
}
