package org.apache.tools.ant.types;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * The assertion datatype. This type describes
 * assertion settings for the &lt;java&gt; task and others.
 * One can set the system assertions, and enable/disable those in
 * packages and classes.
 * Assertions can only be enabled or disabled when forking Java.
 *
 * Example: set system assertions and all org.apache packages except
 * for ant, and the class org.apache.tools.ant.Main.
 * <pre>
 * &lt;assertions enableSystemAssertions="true" &gt;
 *   &lt;enable package="org.apache" /&gt;
 *   &lt;disable package="org.apache.ant" /&gt;
 *   &lt;enable class="org.apache.tools.ant.Main"/&gt;
 * &lt;/assertions&gt;
 *</pre>
 * Disable system assertions; enable those in the anonymous package
 * <pre>
 * &lt;assertions enableSystemAssertions="false" &gt;
 *   &lt;enable package="..." /&gt;
 * &lt;/assertions&gt;
 * </pre>
 * enable assertions in a class called Test
 * <pre>
 * &lt;assertions &gt;
 *   &lt;enable class="Test" /&gt;
 * &lt;/assertions&gt;
 * </pre>
 * This type is a datatype, so you can declare assertions and use them later
 *
 * <pre>
 * &lt;assertions id="project.assertions" &gt;
 *   &lt;enable project="org.apache.test" /&gt;
 * &lt;/assertions&gt;
 *
 * &lt;assertions refid="project.assertions" /&gt;
 *
 * </pre>
 * @since Ant 1.6
 */
public class Assertions extends DataType implements Cloneable {

    /**
     * enable/disable sys assertions; null means undefined
     */
    private Boolean enableSystemAssertions;

    /**
     * list of type BaseAssertion
     */
    private ArrayList assertionList = new ArrayList();


    /**
     * enable assertions
     * @param assertion an enable assertion nested element
     */
    public void addEnable(EnabledAssertion assertion) {
        checkChildrenAllowed();
        assertionList.add(assertion);
    }

    /**
     * disable assertions
     * @param assertion a disable assertion nested element
     */
    public void addDisable(DisabledAssertion assertion) {
        checkChildrenAllowed();
        assertionList.add(assertion);
    }

    /**
     * enable or disable system assertions.
     * Default is not set (neither -enablesystemassersions or -disablesytemassertions
     * are used on the command line).
     * @param enableSystemAssertions if true enable system assertions
     */
    public void setEnableSystemAssertions(Boolean enableSystemAssertions) {
        checkAttributesAllowed();
        this.enableSystemAssertions = enableSystemAssertions;
    }

    /**
     * Set the value of the refid attribute.
     *
     * <p>Subclasses may need to check whether any other attributes
     * have been set as well or child elements have been created and
     * thus override this method. if they do the must call
     * <code>super.setRefid</code>.</p>
     * @param ref the reference to use
     */
    public void setRefid(Reference ref) {
        if (assertionList.size() > 0 || enableSystemAssertions != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * get whatever we are referencing to. This could be ourself.
     * @return the object that contains the assertion info
     */
    private Assertions getFinalReference() {
        if (getRefid() == null) {
            return this;
        } else {
            Object o = getRefid().getReferencedObject(getProject());
            if (!(o instanceof Assertions)) {
                throw new BuildException("reference is of wrong type");
            }
            return (Assertions) o;
        }
    }

    /**
     * how many assertions are made...will resolve references before returning
     * @return total # of commands to make
     */
    public int size() {
        Assertions clause = getFinalReference();
        return clause.getFinalSize();
    }


    /**
     * what is the final size of this object
     * @return
     */
    private int getFinalSize() {
        return assertionList.size() + (enableSystemAssertions != null ? 1 : 0);
    }

    /**
     * add the assertions to a list in a format suitable
     * for adding to a command line
     * @param commandList the command line to format
     */
    public void applyAssertions(List commandList) {
        getProject().log("Applying assertions", Project.MSG_DEBUG);
        Assertions clause = getFinalReference();
        if (Boolean.TRUE.equals(clause.enableSystemAssertions)) {
            getProject().log("Enabling system assertions", Project.MSG_DEBUG);
            commandList.add("-enablesystemassertions");
        } else if (Boolean.FALSE.equals(clause.enableSystemAssertions)) {
            getProject().log("disabling system assertions", Project.MSG_DEBUG);
            commandList.add("-disablesystemassertions");
        }

        Iterator it = clause.assertionList.iterator();
        while (it.hasNext()) {
            BaseAssertion assertion = (BaseAssertion) it.next();
            String arg = assertion.toCommand();
            getProject().log("adding assertion " + arg, Project.MSG_DEBUG);
            commandList.add(arg);
        }
    }

    /**
     * apply all the assertions to the command.
     * @param command the command line to format
     */
    public void applyAssertions(CommandlineJava command) {
        Assertions clause = getFinalReference();
        if (Boolean.TRUE.equals(clause.enableSystemAssertions)) {
            addVmArgument(command, "-enablesystemassertions");
        } else if (Boolean.FALSE.equals(clause.enableSystemAssertions)) {
            addVmArgument(command, "-disablesystemassertions");
        }

        Iterator it = clause.assertionList.iterator();
        while (it.hasNext()) {
            BaseAssertion assertion = (BaseAssertion) it.next();
            String arg = assertion.toCommand();
            addVmArgument(command, arg);
        }
    }

    /**
     * add the assertions to a list in a format suitable
     * for adding to a command line
     * @param commandIterator list of commands
     */
    public void applyAssertions(final ListIterator commandIterator) {
        getProject().log("Applying assertions", Project.MSG_DEBUG);
        Assertions clause = getFinalReference();
        if (Boolean.TRUE.equals(clause.enableSystemAssertions)) {
            getProject().log("Enabling system assertions", Project.MSG_DEBUG);
            commandIterator.add("-enablesystemassertions");
        } else if (Boolean.FALSE.equals(clause.enableSystemAssertions)) {
            getProject().log("disabling system assertions", Project.MSG_DEBUG);
            commandIterator.add("-disablesystemassertions");
        }

        Iterator it = clause.assertionList.iterator();
        while (it.hasNext()) {
            BaseAssertion assertion = (BaseAssertion) it.next();
            String arg = assertion.toCommand();
            getProject().log("adding assertion " + arg, Project.MSG_DEBUG);
            commandIterator.add(arg);
        }
    }

    /**
     * helper method to add a string JVM argument to a command
     * @param command
     * @param arg
     */
    private static void addVmArgument(CommandlineJava command, String arg) {
        Commandline.Argument argument;
        argument = command.createVmArgument();
        argument.setValue(arg);
    }

    /**
     * clone the objects.
     * This is not a full depth clone; the list of assertions is cloned,
     * but it does not clone the underlying assertions.
     * @return a cli
     * @throws CloneNotSupportedException if the super class does not support cloning
     */
    public Object clone() throws CloneNotSupportedException {
        Assertions that = (Assertions) super.clone();
        that.assertionList = (ArrayList) assertionList.clone();
        return that;
    }

    /**
     * base class for our assertion elements.
     */

    public abstract static class BaseAssertion {
        private String packageName;
        private String className;

        /**
         * name a class
         * @param className a class name
         */
        public void setClass(String className) {
            this.className = className;
        }

        /**
         * name a package
         * @param packageName a package name
         */
        public void setPackage(String packageName) {
            this.packageName = packageName;
        }

        /**
         * what is the class name?
         * @return classname or null
         * @see #setClass
         */
        protected String getClassName() {
            return className;
        }

        /**
         * what is the package name?
         * @return package name or null
         * @see #setPackage
         */
        protected String getPackageName() {
            return packageName;
        }

        /**
         * get the prefix used to begin the command; -ea or -da.
         * @return prefix
         */
        public abstract String getCommandPrefix();

        /**
         * create a full command string from this class
         * @throws BuildException in case of trouble
         * @return The command string
         */
        public String toCommand() {
            if (getPackageName() != null && getClassName() != null) {
                throw new BuildException("Both package and class have been set");
            }
            StringBuffer command = new StringBuffer(getCommandPrefix());
            if (getPackageName() != null) {
                command.append(':');
                command.append(getPackageName());
                if (!command.toString().endsWith("...")) {
                    command.append("...");
                }
            } else if (getClassName() != null) {
                command.append(':');
                command.append(getClassName());
            }
            return command.toString();
        }
    }


    /**
     * an enabled assertion enables things
     */
    public static class EnabledAssertion extends BaseAssertion {
        /**
         * get the prefix used to begin the command; -ea or -da.
         * @return prefix
         */
        public String getCommandPrefix() {
            return "-ea";
        }

    }

    /**
     * A disabled assertion disables things
     */
    public static class DisabledAssertion extends BaseAssertion {
        /**
         * get the prefix used to begin the command; -ea or -da.
         * @return prefix
         */
        public String getCommandPrefix() {
            return "-da";
        }

    }
}
