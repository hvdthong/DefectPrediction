package org.apache.tools.ant.taskdefs.optional.junit;

import java.text.NumberFormat;
import java.io.IOException;
import java.io.OutputStream;
import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.apache.tools.ant.BuildException;

/**
 * Prints short summary output of the test to Ant's logging system.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 */
 
public class SummaryJUnitResultFormatter implements JUnitResultFormatter {

    /**
     * Formatter for timings.
     */
    private NumberFormat nf = NumberFormat.getInstance();
    /**
     * OutputStream to write to.
     */
    private OutputStream out;

    private boolean withOutAndErr = false;
    private String systemOutput = null;
    private String systemError = null;

    /**
     * Empty
     */
    public SummaryJUnitResultFormatter() {}
    /**
     * Empty
     */
    public void startTestSuite(JUnitTest suite) {}
    /**
     * Empty
     */
    public void startTest(Test t) {}
    /**
     * Empty
     */
    public void endTest(Test test) {}
    /**
     * Empty
     */
    public void addFailure(Test test, Throwable t) {}
    /**
     * Interface TestListener for JUnit &gt; 3.4.
     *
     * <p>A Test failed.
     */
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    /**
     * Empty
     */
    public void addError(Test test, Throwable t) {}
    
    public void setOutput(OutputStream out) {
        this.out = out;
    }

    public void setSystemOutput( String out ) {
        systemOutput = out;
    }

    public void setSystemError( String err ) {
        systemError = err;
    }

    /**
     * Should the output to System.out and System.err be written to
     * the summary.
     */
    public void setWithOutAndErr( boolean value ) {
        withOutAndErr = value;
    }

    /**
     * The whole testsuite ended.
     */
    public void endTestSuite(JUnitTest suite) throws BuildException {
        String newLine = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer("Tests run: ");
        sb.append(suite.runCount());
        sb.append(", Failures: ");
        sb.append(suite.failureCount());
        sb.append(", Errors: ");
        sb.append(suite.errorCount());
        sb.append(", Time elapsed: ");
        sb.append(nf.format(suite.getRunTime()/1000.0));
        sb.append(" sec");
        sb.append(newLine);

        if (withOutAndErr) {
            if (systemOutput != null && systemOutput.length() > 0) {
                sb.append( "Output:" ).append(newLine).append(systemOutput)
                    .append(newLine);
            }
            
            if (systemError != null && systemError.length() > 0) {
                sb.append( "Error: " ).append(newLine).append(systemError)
                    .append(newLine);
            }
        }

        try {
            out.write(sb.toString().getBytes());
            out.flush();
        } catch (IOException ioex) {
            throw new BuildException("Unable to write summary output", ioex);
        } finally {
            if (out != System.out && out != System.err) {
                try {
                    out.close();
                } catch (IOException e) {}
            }
        }
    }
}
