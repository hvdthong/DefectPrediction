package org.apache.xalan.extensions;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xpath.XPathProcessorException;
import org.apache.xpath.functions.FuncExtFunction;

/**
 * <meta name="usage" content="internal"/>
 * Class holding a table registered extension namespace handlers
 */
public class ExtensionsTable
{  
  /**
   * <meta name="usage" content="internal"/>
   * Table of extensions that may be called from the expression language
   * via the call(name, ...) function.  Objects are keyed on the call
   * name.
   */
  public Hashtable m_extensionFunctionNamespaces = new Hashtable();
  
  /**
   * The StylesheetRoot associated with this extensions table.
   */
  private StylesheetRoot m_sroot;
  
  /**
   * <meta name="usage" content="advanced"/>
   * The constructor (called from TransformerImpl) registers the
   * StylesheetRoot for the transformation and instantiates an
   * ExtensionHandler for each extension namespace.
   */
  public ExtensionsTable(StylesheetRoot sroot)
    throws javax.xml.transform.TransformerException
  {
    m_sroot = sroot;
    Vector extensions = m_sroot.getExtensions();
    for (int i = 0; i < extensions.size(); i++)
    {
      ExtensionNamespaceSupport extNamespaceSpt = 
                 (ExtensionNamespaceSupport)extensions.elementAt(i);
      ExtensionHandler extHandler = extNamespaceSpt.launch();
        if (extHandler != null)
          addExtensionNamespace(extNamespaceSpt.getNamespace(), extHandler);
      }
    }
       
  /**
   * Get an ExtensionHandler object that represents the
   * given namespace.
   * @param extns A valid extension namespace.
   *
   * @return ExtensionHandler object that represents the
   * given namespace.
   */
  public ExtensionHandler get(String extns)
  {
    return (ExtensionHandler) m_extensionFunctionNamespaces.get(extns);
  }

  /**
   * <meta name="usage" content="advanced"/>
   * Register an extension namespace handler. This handler provides
   * functions for testing whether a function is known within the
   * namespace and also for invoking the functions.
   *
   * @param uri the URI for the extension.
   * @param extNS the extension handler.
   */
  public void addExtensionNamespace(String uri, ExtensionHandler extNS)
  {
    m_extensionFunctionNamespaces.put(uri, extNS);
  }

  /**
   * Execute the function-available() function.
   * @param ns       the URI of namespace in which the function is needed
   * @param funcName the function name being tested
   *
   * @return whether the given function is available or not.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean functionAvailable(String ns, String funcName)
          throws javax.xml.transform.TransformerException
  {
    boolean isAvailable = false;
    
    if (null != ns)
    {
      ExtensionHandler extNS = 
           (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (extNS != null)
        isAvailable = extNS.isFunctionAvailable(funcName);
    }
    return isAvailable;
  }
  
  /**
   * Execute the element-available() function.
   * @param ns       the URI of namespace in which the function is needed
   * @param elemName name of element being tested
   *
   * @return whether the given element is available or not.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean elementAvailable(String ns, String elemName)
          throws javax.xml.transform.TransformerException
  {
    boolean isAvailable = false;
    if (null != ns)
    {
      ExtensionHandler extNS = 
               (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
        isAvailable = extNS.isElementAvailable(elemName);
    } 
    return isAvailable;        
  }  
  
  /**
   * Handle an extension function.
   * @param ns        the URI of namespace in which the function is needed
   * @param funcName  the function name being called
   * @param argVec    arguments to the function in a vector
   * @param methodKey a unique key identifying this function instance in the
   *                  stylesheet
   * @param exprContext a context which may be passed to an extension function
   *                  and provides callback functions to access various
   *                  areas in the environment
   *
   * @return result of executing the function
   *
   * @throws javax.xml.transform.TransformerException
   */
  public Object extFunction(String ns, String funcName, 
                            Vector argVec, Object methodKey, 
                            ExpressionContext exprContext)
            throws javax.xml.transform.TransformerException
  {
    Object result = null;
    if (null != ns)
    {
      ExtensionHandler extNS =
        (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (null != extNS)
      {
        try
        {
          result = extNS.callFunction(funcName, argVec, methodKey,
                                      exprContext);
        }
        catch (javax.xml.transform.TransformerException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else
      {
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, new Object[]{ns, funcName })); 
      }
    }
    return result;    
  }
  
  /**
   * Handle an extension function.
   * @param extFunction  the extension function
   * @param argVec    arguments to the function in a vector
   * @param exprContext a context which may be passed to an extension function
   *                  and provides callback functions to access various
   *                  areas in the environment
   *
   * @return result of executing the function
   *
   * @throws javax.xml.transform.TransformerException
   */
  public Object extFunction(FuncExtFunction extFunction, Vector argVec, 
                            ExpressionContext exprContext)
         throws javax.xml.transform.TransformerException
  {
    Object result = null;
    String ns = extFunction.getNamespace();
    if (null != ns)
    {
      ExtensionHandler extNS =
        (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (null != extNS)
      {
        try
        {
          result = extNS.callFunction(extFunction, argVec, exprContext);
        }
        catch (javax.xml.transform.TransformerException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else
      {
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, 
                                          new Object[]{ns, extFunction.getFunctionName()})); 
      }
    }
    return result;        
  }
}
