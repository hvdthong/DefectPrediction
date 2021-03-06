package org.apache.ivy.plugins.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.repository.Resource;

public interface ModuleDescriptorParser {
    public ModuleDescriptor parseDescriptor(ParserSettings ivySettings, URL descriptorURL,
            boolean validate) throws ParseException, IOException;

    public ModuleDescriptor parseDescriptor(ParserSettings ivySettings, URL descriptorURL,
            Resource res, boolean validate) throws ParseException, IOException;

    /**
     * Convert a module descriptor to an ivy file. This method MUST close the given input stream
     * when job is finished
     * 
     * @param is
     *            input stream with opened on original module descriptor resource
     */
    public void toIvyFile(InputStream is, Resource res, File destFile, ModuleDescriptor md)
            throws ParseException, IOException;

    public boolean accept(Resource res);

    /**
     * Return the 'type' of module artifacts this parser is parsing
     * @return the 'type' of module artifacts this parser is parsing
     */
    public String getType();

    /**
     * Returns the module metadata artifact corresponding to the given module revision id that this
     * parser parses
     * 
     * @param res
     *            the resource for which the module artifact should be returned
     * @param mrid
     *            the module revision id for which the module artifact should be returned
     * @return the module artifact corresponding to the given mrid and resource
     */
    public Artifact getMetadataArtifact(ModuleRevisionId mrid, Resource res);
}
