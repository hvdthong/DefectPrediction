import org.apache.velocity.runtime.RuntimeServices;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

/**
 * Class to manage the text resource for the Velocity
 * Runtime.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ResourceManager.java 463298 2006-10-12 16:10:32Z henning $
 */
public interface ResourceManager
{
    /**
     * A template resources.
     */
    public static final int RESOURCE_TEMPLATE = 1;

    /**
     * A static content resource.
     */
    public static final int RESOURCE_CONTENT = 2;

    /**
     * Initialize the ResourceManager.
     * @param rs
     * @throws Exception
     */
    public void initialize( RuntimeServices rs ) throws Exception;

    /**
     * Gets the named resource.  Returned class type corresponds to specified type
     * (i.e. <code>Template</code> to <code>RESOURCE_TEMPLATE</code>).
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>,
     *                     <code>RESOURCE_CONTENT</code>, etc.).
     * @param encoding  The character encoding to use.
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if a problem in parse
     */
    public Resource getResource(String resourceName, int resourceType, String encoding )
        throws ResourceNotFoundException, ParseErrorException, Exception;

    /**
     *  Determines is a template exists, and returns name of the loader that
     *  provides it.  This is a slightly less hokey way to support
     *  the Velocity.templateExists() utility method, which was broken
     *  when per-template encoding was introduced.  We can revisit this.
     *
     *  @param resourceName Name of template or content resource
     *  @return class name of loader than can provide it
     */
    public String getLoaderNameForResource(String resourceName );

}

