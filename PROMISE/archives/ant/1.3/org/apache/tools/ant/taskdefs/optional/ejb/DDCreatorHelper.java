package org.apache.tools.ant.taskdefs.optional.ejb;

import java.io.File;

/**
 * A helper class which performs the actual work of the ddcreator task.
 *
 * This class is run with a classpath which includes the weblogic tools and the home and remote
 * interface class files referenced in the deployment descriptors being built.
 *
 * @author <a href="mailto:conor@cortexebusiness.com.au">Conor MacNeill</a>, Cortex ebusiness Pty Limited
 */
public class DDCreatorHelper {
    /**
     * The root directory of the tree containing the textual deployment desciptors. 
     */
    private File descriptorDirectory;
    
    /**
     * The directory where generated serialised desployment descriptors are written.
     */
    private File generatedFilesDirectory;

    /**
     * The descriptor text files for which a serialised descriptor is to be created.
     */
    String[] descriptors; 

    /**
     * The main method.
     *
     * The main method creates an instance of the DDCreatorHelper, passing it the 
     * args which it then processes.
     */    
    public static void main(String[] args) throws Exception {
        DDCreatorHelper helper = new DDCreatorHelper(args);
        helper.process();
    }
  
    /**
     * Initialise the helper with the command arguments.
     *
     */
    private DDCreatorHelper(String[] args) {
        int index = 0;
        descriptorDirectory = new File(args[index++]);
        generatedFilesDirectory = new File(args[index++]);
        
        descriptors = new String[args.length - index];
        for (int i = 0; index < args.length; ++i) {
            descriptors[i] = args[index++];
        }
    }
    
    /**
     * Do the actual work.
     *
     * The work proceeds by examining each descriptor given. If the serialised
     * file does not exist or is older than the text description, the weblogic
     * DDCreator tool is invoked directly to build the serialised descriptor.
     */    
    private void process() throws Exception {
        for (int i = 0; i < descriptors.length; ++i) {
            String descriptorName = descriptors[i];
            File descriptorFile = new File(descriptorDirectory, descriptorName);

            int extIndex = descriptorName.lastIndexOf(".");
            String serName = null;
            if (extIndex != -1) {
                serName = descriptorName.substring(0, extIndex) + ".ser";
            }
            else {
                serName = descriptorName + ".ser";
            }
            File serFile = new File(generatedFilesDirectory, serName);
                
            if (!serFile.exists() || serFile.lastModified() < descriptorFile.lastModified()) {
                
                String[] args = {"-noexit", 
                                 "-d", serFile.getParent(),
                                 "-outputfile", serFile.getName(),
                                 descriptorFile.getPath()};
                try {
                    weblogic.ejb.utils.DDCreator.main(args);
                }
                catch (Exception e) {
                    String[] newArgs = {"-d", generatedFilesDirectory.getPath(),
                                 "-outputfile", serFile.getName(),
                                 descriptorFile.getPath()};
                    weblogic.ejb.utils.DDCreator.main(newArgs);
                }
            }
        }
    }
}
