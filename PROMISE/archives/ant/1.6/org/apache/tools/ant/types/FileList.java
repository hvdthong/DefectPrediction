package org.apache.tools.ant.types;

import java.io.File;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * FileList represents an explicitly named list of files.  FileLists
 * are useful when you want to capture a list of files regardless of
 * whether they currently exist.  By contrast, FileSet operates as a
 * filter, only returning the name of a matched file if it currently
 * exists in the file system.
 */
public class FileList extends DataType {

    private Vector filenames = new Vector();
    private File dir;

    /**
     * The default constructor.
     *
     */
    public FileList() {
        super();
    }

    /**
     * A copy constructor.
     *
     * @param filelist a <code>FileList</code> value
     */
    protected FileList(FileList filelist) {
        this.dir       = filelist.dir;
        this.filenames = filelist.filenames;
        setProject(filelist.getProject());
    }

    /**
     * Makes this instance in effect a reference to another FileList
     * instance.
     *
     * <p>You must not set another attribute or nest elements inside
     * this element if you make it a reference.</p>
     * @param r the reference to another filelist.
     * @exception BuildException if an error occurs.
     */
    public void setRefid(Reference r) throws BuildException {
        if ((dir != null) || (filenames.size() != 0)) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }

    /**
     * Set the dir attribute.
     *
     * @param dir the directory this filelist is relative to.
     * @exception BuildException if an error occurs
     */
    public void setDir(File dir) throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.dir = dir;
    }

    /**
     * @param p the current project
     * @return the directory attribute
     */
    public File getDir(Project p) {
        if (isReference()) {
            return getRef(p).getDir(p);
        }
        return dir;
    }

    /**
     * Set the filenames attribute.
     *
     * @param filenames a string contains filenames, separated by , or
     *        by whitespace.
     */
    public void setFiles(String filenames) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (filenames != null && filenames.length() > 0) {
            StringTokenizer tok = new StringTokenizer(
                filenames, ", \t\n\r\f", false);
            while (tok.hasMoreTokens()) {
               this.filenames.addElement(tok.nextToken());
            }
        }
    }

    /**
     * Returns the list of files represented by this FileList.
     * @param p the current project
     * @return the list of files represented by this FileList.
     */
    public String[] getFiles(Project p) {
        if (isReference()) {
            return getRef(p).getFiles(p);
        }

        if (dir == null) {
            throw new BuildException("No directory specified for filelist.");
        }

        if (filenames.size() == 0) {
            throw new BuildException("No files specified for filelist.");
        }

        String[] result = new String[filenames.size()];
        filenames.copyInto(result);
        return result;
    }

    /**
     * Performs the check for circular references and returns the
     * referenced FileList.
     * @param p the current project
     * @return the FileList represented by a referenced filelist.
     */
    protected FileList getRef(Project p) {
        if (!isChecked()) {
            Stack stk = new Stack();
            stk.push(this);
            dieOnCircularReference(stk, p);
        }

        Object o = getRefid().getReferencedObject(p);
        if (!(o instanceof FileList)) {
            String msg = getRefid().getRefId() + " doesn\'t denote a filelist";
            throw new BuildException(msg);
        } else {
            return (FileList) o;
        }
    }

    /**
     * Inner class corresponding to the &lt;file&gt; nested element.
     */
    public static class FileName {
        private String name;

        /**
         * The name attribute of the file element.
         *
         * @param name the name of a file to add to the file list.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the name of the file for this element.
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Add a nested &lt;file&gt; nested element.
     *
     * @param name a configured file element with a name.
     */
    public void addConfiguredFile(FileName name) {
        if (name.getName() == null) {
            throw new BuildException(
                "No name specified in nested file element");
        }
        filenames.addElement(name.getName());
    }
}
