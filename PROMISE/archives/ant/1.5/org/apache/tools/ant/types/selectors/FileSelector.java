package org.apache.tools.ant.types.selectors;

import java.io.File;

import org.apache.tools.ant.BuildException;

/**
 * This is the interface to be used by all selectors.
 *
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @since 1.5
 */
public interface FileSelector {

    /**
     * Method that each selector will implement to create their
     * selection behaviour. If there is a problem with the setup
     * of a selector, it can throw a BuildException to indicate
     * the problem.
     *
     * @param basedir A java.io.File object for the base directory
     * @param filename The name of the file to check
     * @param file A File object for this filename
     * @return whether the file should be selected or not
     * @exception BuildException if the selector was not configured correctly
     */
    public boolean isSelected(File basedir, String filename, File file)
            throws BuildException;

}

