package org.apache.tools.ant.util.optional;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFEngine;

import java.util.Iterator;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import org.apache.tools.ant.util.ReflectUtil;
import org.apache.tools.ant.util.ScriptRunnerBase;

/**
 * This class is used to run BSF scripts
 *
 */
public class ScriptRunner extends ScriptRunnerBase {
    static {
        BSFManager.registerScriptingEngine(
            "groovy",
            "org.codehaus.groovy.bsf.GroovyEngine",
            new String[] {"groovy", "gy"});
    }

    private BSFEngine  engine;
    private BSFManager manager;

    /**
     * Get the name of the manager prefix.
     * @return "bsf"
     */
    public String getManagerName() {
        return "bsf";
    }

    /**
     * Check if bsf supports the language.
     * @return true if bsf can create an engine for this language.
     */
    public boolean supportsLanguage() {
        Hashtable table = (Hashtable) ReflectUtil.getField(
            new BSFManager(), "registeredEngines");
        String engineClassName = (String) table.get(getLanguage());
        if (engineClassName == null) {
            getProject().log(
                "This is no BSF engine class for language '"
                + getLanguage() + "'",
                Project.MSG_VERBOSE);
            return false;
        }
        try {
            getScriptClassLoader().loadClass(engineClassName);
            return true;
        } catch (Throwable ex) {
            getProject().log(
                "unable to create BSF engine class for language '"
                + getLanguage() + "'",
                ex,
                Project.MSG_VERBOSE);
            return false;
        }
    }

    /**
     * Do the work.
     *
     * @param execName the name that will be passed to BSF for this script execution.
     * @exception BuildException if something goes wrong executing the script.
     */
    public void executeScript(String execName) throws BuildException {
        checkLanguage();
        ClassLoader origLoader = replaceContextLoader();
        try {
            BSFManager m = createManager();
            declareBeans(m);
            if (engine == null) {
                m.exec(getLanguage(), execName, 0, 0, getScript());
            } else {
                engine.exec(execName, 0, 0, getScript());
            }
        } catch (BSFException be) {
            throw getBuildException(be);
        } finally {
            restoreContextLoader(origLoader);
        }
    }

    /**
     * Evaluate the script.
     *
     * @param execName the name that will be passed to BSF for this script execution.
     * @return the result of the evaluation
     * @exception BuildException if something goes wrong executing the script.
     */
    public Object evaluateScript(String execName) throws BuildException {
        checkLanguage();
        ClassLoader origLoader = replaceContextLoader();
        try {
            BSFManager m = createManager();
            declareBeans(m);
            if (engine == null) {
                return m.eval(getLanguage(), execName, 0, 0, getScript());
            }
            return engine.eval(execName, 0, 0, getScript());
        } catch (BSFException be) {
            throw getBuildException(be);
        } finally {
            restoreContextLoader(origLoader);
        }
    }

    /**
     * Get/create a BuildException from a BSFException.
     * @param be BSFException to convert.
     * @return BuildException the converted exception.
     */
    private BuildException getBuildException(BSFException be) {
        Throwable t = be;
        Throwable te = be.getTargetException();
        if (te instanceof BuildException) {
            return (BuildException) te;
        }
        return new BuildException(te == null ? t : te);
    }

    private void declareBeans(BSFManager m) throws BSFException {
        for (Iterator i = getBeans().keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            Object value = getBeans().get(key);
            if (value != null) {
                m.declareBean(key, value, value.getClass());
            } else {
                m.undeclareBean(key);
            }
        }
    }

    private BSFManager createManager() throws BSFException {
        if (manager != null) {
            return manager;
        }
        BSFManager m = new BSFManager();
        m.setClassLoader(getScriptClassLoader());
        if (getKeepEngine()) {
            BSFEngine e = manager.loadScriptingEngine(getLanguage());
            this.manager = m;
            this.engine  = e;
        }
        return m;
    }
}
