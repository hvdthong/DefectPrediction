package org.apache.tools.ant.types.selectors;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Reference;

/**
 * Selector that selects files by forwarding the request on to other classes.
 *
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @since 1.5
 */
public class ExtendSelector extends BaseSelector {

    private String classname = null;
    private FileSelector dynselector = null;
    private Vector paramVec = new Vector();
    private Path classpath = null;

    /**
     * Default constructor.
     */
    public ExtendSelector() {
    }

    /**
     * Sets the classname of the custom selector.
     *
     * @param classname is the class which implements this selector
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    /**
     * Instantiates the identified custom selector class.
     */
    public void selectorCreate() {
        if (classname != null && classname.length() > 0) {
            try {
                Class c = null;
                if (classpath == null) {
                    c = Class.forName(classname);
                } else {
                    AntClassLoader al = new AntClassLoader(getProject(),
                                                           classpath);
                    c = al.loadClass(classname);
                    AntClassLoader.initializeClass(c);
                }
                dynselector = (FileSelector) c.newInstance();
            }
            catch (ClassNotFoundException cnfexcept) {
                setError("Selector " + classname +
                        " not initialized, no such class");
            }
            catch (InstantiationException iexcept) {
                setError("Selector " + classname +
                        " not initialized, could not create class");
            }
            catch (IllegalAccessException iaexcept) {
                setError("Selector " + classname +
                        " not initialized, class not accessible");
            }
        } else {
            setError("There is no classname specified");
        }
    }

    /**
     * Create new parameters to pass to custom selector.
     *
     * @param p The new Parameter object
     */
    public void addParam(Parameter p) {
        paramVec.addElement(p);
    }


    /**
     * Set the classpath to load the classname specified using an attribute.
     */
    public final void setClasspath(Path classpath) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    /**
     * Specify the classpath to use to load the Selector (nested element).
     */
    public final Path createClasspath() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }

    /**
     * Get the classpath
     */
    public final Path getClasspath() {
        return classpath;
    }

    /**
     * Set the classpath to use for loading a custom selector by using
     * a reference.
     */
    public void setClasspathref(Reference r) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        createClasspath().setRefid(r);
    }

    /**
     * These are errors specific to ExtendSelector only. If there are
     * errors in the custom selector, it should throw a BuildException
     * when isSelected() is called.
     */
    public void verifySettings() {
        if (dynselector == null) {
            selectorCreate();
        }
        if (classname == null || classname.length() < 1) {
            setError("The classname attribute is required");
        }
        else if (dynselector == null) {
            setError("Internal Error: The custom selector was not created");
        }
        else if (!(dynselector instanceof ExtendFileSelector) &&
                (paramVec.size() > 0)) {
            setError("Cannot set parameters on custom selector that does not "
                    + "implement ExtendFileSelector");
        }
    }


    /**
     * Allows the custom selector to choose whether to select a file. This
     * is also where the Parameters are passed to the custom selector,
     * since we know we must have them all by now. And since we must know
     * both classpath and classname, creating the class is deferred to here
     * as well.
     */
    public boolean isSelected(File basedir, String filename, File file)
            throws BuildException {
        validate();
        if (paramVec.size() > 0 && dynselector instanceof ExtendFileSelector) {
            Parameter[] paramArray = new Parameter[paramVec.size()];
            paramVec.copyInto(paramArray);
            ((ExtendFileSelector)dynselector).setParameters(paramArray);
        }
        return dynselector.isSelected(basedir,filename,file);
    }

}

