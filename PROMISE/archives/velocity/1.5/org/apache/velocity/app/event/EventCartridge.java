import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;

/**
 * Stores the event handlers. Event handlers can be assigned on a per
 * VelocityEngine instance basis by specifying the class names in the
 * velocity.properties file. Event handlers may also be assigned on a per-page
 * basis by creating a new instance of EventCartridge, adding the event
 * handlers, and then calling attachToContext. For clarity, it's recommended
 * that one approach or the other be followed, as the second method is primarily
 * presented for backwards compatibility.
 *
 * <P>
 * Note that Event Handlers follow a filter pattern, with multiple event
 * handlers allowed for each event. When the appropriate event occurs, all the
 * appropriate event handlers are called in the sequence they were added to the
 * Event Cartridge. See the javadocs of the specific event handler interfaces
 * for more details.
 *
 * @author <a href="mailto:wglass@wglass@forio.com">Will Glass-Husain </a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr. </a>
 * @author <a href="mailto:j_a_fernandez@yahoo.com">Jose Alberto Fernandez </a>
 * @version $Id: EventCartridge.java 470256 2006-11-02 07:20:36Z wglass $
 */
public class EventCartridge
  {
    private List referenceHandlers = new ArrayList();
    private List nullSetHandlers = new ArrayList();
    private List methodExceptionHandlers = new ArrayList();
    private List includeHandlers = new ArrayList();
    private List invalidReferenceHandlers = new ArrayList();

    /**
     * Ensure that handlers are not initialized more than once.
     */
    Set initializedHandlers = new HashSet();

    /**
     *  Adds an event handler(s) to the Cartridge.  This method
     *  will find all possible event handler interfaces supported
     *  by the passed in object.
     *
     *  @param ev object impementing a valid EventHandler-derived interface
     *  @return true if a supported interface, false otherwise or if null
     */
    public boolean addEventHandler( EventHandler ev )
    {
        if (ev == null)
        {
            return false;
        }

        boolean found = false;

        if ( ev instanceof ReferenceInsertionEventHandler)
        {
            addReferenceInsertionEventHandler( (ReferenceInsertionEventHandler) ev );
            found = true;
        }

        if ( ev instanceof NullSetEventHandler )
        {
            addNullSetEventHandler( (NullSetEventHandler) ev );
            found = true;
        }

        if ( ev instanceof MethodExceptionEventHandler )
        {
            addMethodExceptionHandler( (MethodExceptionEventHandler) ev );
            found = true;
        }

        if ( ev instanceof IncludeEventHandler )
        {
            addIncludeEventHandler( (IncludeEventHandler) ev );
            found = true;
        }

        if ( ev instanceof InvalidReferenceEventHandler )
        {
            addInvalidReferenceEventHandler( (InvalidReferenceEventHandler) ev );
            found = true;
        }

        return found;
    }

    /**
      *  Add a reference insertion event handler to the Cartridge.
      *
      *  @param ev ReferenceInsertionEventHandler
     */
     public void addReferenceInsertionEventHandler( ReferenceInsertionEventHandler ev )
     {
         referenceHandlers.add( ev );
     }

    /**
      *  Add a null set event handler to the Cartridge.
      *
      *  @param ev NullSetEventHandler
      */
     public void addNullSetEventHandler( NullSetEventHandler ev )
     {
         nullSetHandlers.add( ev );
     }

    /**
     *  Add a method exception event handler to the Cartridge.
     *
     *  @param ev MethodExceptionEventHandler
     */
    public void addMethodExceptionHandler( MethodExceptionEventHandler ev )
    {
        methodExceptionHandlers.add( ev );
    }

    /**
     *  Add an include event handler to the Cartridge.
     *
     *  @param ev IncludeEventHandler
     */
    public void addIncludeEventHandler( IncludeEventHandler ev )
    {
        includeHandlers.add( ev );
    }

    /**
     *  Add an invalid reference event handler to the Cartridge.
     *
     *  @param ev InvalidReferenceEventHandler
     */
    public void addInvalidReferenceEventHandler( InvalidReferenceEventHandler ev )
    {
        invalidReferenceHandlers.add( ev );
    }


    /**
     * Removes an event handler(s) from the Cartridge. This method will find all
     * possible event handler interfaces supported by the passed in object and
     * remove them.
     *
     * @param ev  object impementing a valid EventHandler-derived interface
     * @return true if event handler was previously registered, false if not
     *         found
     */
    public boolean removeEventHandler( EventHandler ev )
    {
        if ( ev == null )
        {
            return false;
        }

        boolean found = false;

        if ( ev instanceof ReferenceInsertionEventHandler )
            return referenceHandlers.remove( ev );

        if ( ev instanceof NullSetEventHandler )
            return nullSetHandlers.remove( ev );

        if ( ev instanceof MethodExceptionEventHandler )
            return methodExceptionHandlers.remove(ev );

        if ( ev instanceof IncludeEventHandler )
            return includeHandlers.remove( ev );

        if ( ev instanceof InvalidReferenceEventHandler )
            return invalidReferenceHandlers.remove( ev );

        return found;
    }

    /**
     * Iterate through all the stored ReferenceInsertionEventHandler objects
     * 
     * @return iterator of handler objects
     */
    public Iterator getReferenceInsertionEventHandlers()
    {
        return referenceHandlers.iterator();
    }

    /**
     * Iterate through all the stored NullSetEventHandler objects
     * 
     * @return iterator of handler objects
     */
    public Iterator getNullSetEventHandlers()
    {
        return nullSetHandlers.iterator();
    }

    /**
     * Iterate through all the stored MethodExceptionEventHandler objects
     * 
     * @return iterator of handler objects
     */
    public Iterator getMethodExceptionEventHandlers()
    {
        return methodExceptionHandlers.iterator();
    }

    /**
     * Iterate through all the stored IncludeEventHandlers objects
     * 
     * @return iterator of handler objects
     */
    public Iterator getIncludeEventHandlers()
    {
        return includeHandlers.iterator();
    }

    /**
     * Iterate through all the stored InvalidReferenceEventHandlers objects
     * 
     * @return iterator of handler objects
     */
    public Iterator getInvalidReferenceEventHandlers()
    {
        return invalidReferenceHandlers.iterator();
    }

    /**
     *  Attached the EventCartridge to the context
     *
     *  Final because not something one should mess with lightly :)
     *
     *  @param context context to attach to
     *  @return true if successful, false otherwise
     */
    public final boolean attachToContext( Context context )
    {
        if (  context instanceof InternalEventContext )
        {
            InternalEventContext iec = (InternalEventContext) context;

            iec.attachEventCartridge( this );

            /**
             * while it's tempting to call setContext on each handler from here,
             * this needs to be done before each method call.  This is
             * because the specific context will change as inner contexts
             * are linked in through macros, foreach, or directly by the user.
             */

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Initialize the handlers.  For global handlers this is called when Velocity
     * is initialized. For local handlers this is called when the first handler
     * is executed.  Handlers will not be initialized more than once.
     * 
     * @param rs
     * @throws Exception
     */
    public void initialize (RuntimeServices rs) throws Exception
    {

        for ( Iterator i = referenceHandlers.iterator(); i.hasNext(); )
        {
            EventHandler eh = ( EventHandler ) i.next();
            if ( (eh instanceof RuntimeServicesAware) &&
                    !initializedHandlers.contains(eh) )
            {
                ((RuntimeServicesAware) eh).setRuntimeServices ( rs );
                initializedHandlers.add( eh );
            }
        }

        for ( Iterator i = nullSetHandlers.iterator(); i.hasNext(); )
        {
            EventHandler eh = ( EventHandler ) i.next();
            if ( (eh instanceof RuntimeServicesAware) &&
                    !initializedHandlers.contains(eh) )
            {
                ((RuntimeServicesAware) eh).setRuntimeServices ( rs );
                initializedHandlers.add( eh );
            }
        }

        for ( Iterator i = methodExceptionHandlers.iterator(); i.hasNext(); )
        {
            EventHandler eh = ( EventHandler ) i.next();
            if ( (eh instanceof RuntimeServicesAware) &&
                    !initializedHandlers.contains(eh) )
            {
                ((RuntimeServicesAware) eh).setRuntimeServices ( rs );
                initializedHandlers.add( eh );
            }
        }

        for ( Iterator i = includeHandlers.iterator(); i.hasNext(); )
        {
            EventHandler eh = ( EventHandler ) i.next();
            if ( (eh instanceof RuntimeServicesAware) &&
                    !initializedHandlers.contains(eh) )
            {
                ((RuntimeServicesAware) eh).setRuntimeServices ( rs );
                initializedHandlers.add( eh );
            }
        }

        for ( Iterator i = invalidReferenceHandlers.iterator(); i.hasNext(); )
        {
            EventHandler eh = ( EventHandler ) i.next();
            if ( (eh instanceof RuntimeServicesAware) &&
                    !initializedHandlers.contains(eh) )
            {
                ((RuntimeServicesAware) eh).setRuntimeServices ( rs );
                initializedHandlers.add( eh );
            }
        }

    }


}