package org.apache.xml.utils;

import org.xml.sax.*;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.SourceLocator;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
 
import java.io.PrintWriter;
import java.io.PrintStream;


/**
 * <meta name="usage" content="general"/>
 * Implement SAX error handler for default reporting.
 */
public class DefaultErrorHandler implements ErrorHandler, ErrorListener
{
  PrintWriter m_pw;

  /**
   * Constructor DefaultErrorHandler
   */
  public DefaultErrorHandler(PrintWriter pw)
  {
    m_pw = pw;
  }
  
  /**
   * Constructor DefaultErrorHandler
   */
  public DefaultErrorHandler(PrintStream pw)
  {
    m_pw = new PrintWriter(pw, true);
  }
  
  /**
   * Constructor DefaultErrorHandler
   */
  public DefaultErrorHandler()
  {
    m_pw = new PrintWriter(System.err, true);
  }


  /**
   * Receive notification of a warning.
   *
   * <p>SAX parsers will use this method to report conditions that
   * are not errors or fatal errors as defined by the XML 1.0
   * recommendation.  The default behaviour is to take no action.</p>
   *
   * <p>The SAX parser must continue to provide normal parsing events
   * after invoking this method: it should still be possible for the
   * application to process the document through to the end.</p>
   *
   * @param exception The warning information encapsulated in a
   *                  SAX parse exception.
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void warning(SAXParseException exception) throws SAXException
  {
    printLocation(m_pw, exception);
    m_pw.println("Parser warning: " + exception.getMessage());
  }

  /**
   * Receive notification of a recoverable error.
   *
   * <p>This corresponds to the definition of "error" in section 1.2
   * of the W3C XML 1.0 Recommendation.  For example, a validating
   * parser would use this callback to report the violation of a
   * validity constraint.  The default behaviour is to take no
   * action.</p>
   *
   * <p>The SAX parser must continue to provide normal parsing events
   * after invoking this method: it should still be possible for the
   * application to process the document through to the end.  If the
   * application cannot do so, then the parser should report a fatal
   * error even if the XML 1.0 recommendation does not require it to
   * do so.</p>
   *
   * @param exception The error information encapsulated in a
   *                  SAX parse exception.
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void error(SAXParseException exception) throws SAXException
  {

    throw exception;
  }

  /**
   * Receive notification of a non-recoverable error.
   *
   * <p>This corresponds to the definition of "fatal error" in
   * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
   * parser would use this callback to report the violation of a
   * well-formedness constraint.</p>
   *
   * <p>The application must assume that the document is unusable
   * after the parser has invoked this method, and should continue
   * (if at all) only for the sake of collecting addition error
   * messages: in fact, SAX parsers are free to stop reporting any
   * other events once this method has been invoked.</p>
   *
   * @param exception The error information encapsulated in a
   *                  SAX parse exception.
   * @throws SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void fatalError(SAXParseException exception) throws SAXException
  {

    throw exception;
  }
  
  /**
   * Receive notification of a warning.
   *
   * <p>SAX parsers will use this method to report conditions that
   * are not errors or fatal errors as defined by the XML 1.0
   * recommendation.  The default behaviour is to take no action.</p>
   *
   * <p>The SAX parser must continue to provide normal parsing events
   * after invoking this method: it should still be possible for the
   * application to process the document through to the end.</p>
   *
   * @param exception The warning information encapsulated in a
   *                  SAX parse exception.
   * @throws javax.xml.transform.TransformerException Any SAX exception, possibly
   *            wrapping another exception.
   * @see javax.xml.transform.TransformerException
   */
  public void warning(TransformerException exception) throws TransformerException
  {
    printLocation(m_pw, exception);

    m_pw.println(exception.getMessage());
  }

  /**
   * Receive notification of a recoverable error.
   *
   * <p>This corresponds to the definition of "error" in section 1.2
   * of the W3C XML 1.0 Recommendation.  For example, a validating
   * parser would use this callback to report the violation of a
   * validity constraint.  The default behaviour is to take no
   * action.</p>
   *
   * <p>The SAX parser must continue to provide normal parsing events
   * after invoking this method: it should still be possible for the
   * application to process the document through to the end.  If the
   * application cannot do so, then the parser should report a fatal
   * error even if the XML 1.0 recommendation does not require it to
   * do so.</p>
   *
   * @param exception The error information encapsulated in a
   *                  SAX parse exception.
   * @throws javax.xml.transform.TransformerException Any SAX exception, possibly
   *            wrapping another exception.
   * @see javax.xml.transform.TransformerException
   */
  public void error(TransformerException exception) throws TransformerException
  {

    throw exception;
  }

  /**
   * Receive notification of a non-recoverable error.
   *
   * <p>This corresponds to the definition of "fatal error" in
   * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
   * parser would use this callback to report the violation of a
   * well-formedness constraint.</p>
   *
   * <p>The application must assume that the document is unusable
   * after the parser has invoked this method, and should continue
   * (if at all) only for the sake of collecting addition error
   * messages: in fact, SAX parsers are free to stop reporting any
   * other events once this method has been invoked.</p>
   *
   * @param exception The error information encapsulated in a
   *                  SAX parse exception.
   * @throws javax.xml.transform.TransformerException Any SAX exception, possibly
   *            wrapping another exception.
   * @see javax.xml.transform.TransformerException
   */
  public void fatalError(TransformerException exception) throws TransformerException
  {

    throw exception;
  }
  
  public static void ensureLocationSet(TransformerException exception)
  {
    SourceLocator locator = null;
    Throwable cause = exception;
    
    do
    {
      if(cause instanceof SAXParseException)
      {
        locator = new SAXSourceLocator((SAXParseException)cause);
      }
      else if (cause instanceof TransformerException)
      {
        SourceLocator causeLocator = ((TransformerException)cause).getLocator();
        if(null != causeLocator)
          locator = causeLocator;
      }
      
      if(cause instanceof TransformerException)
        cause = ((TransformerException)cause).getCause();
      else if(cause instanceof SAXException)
        cause = ((SAXException)cause).getException();
      else
        cause = null;
    }
    while(null != cause);
    
    exception.setLocator(locator);
  }
  
  public static void printLocation(PrintStream pw, TransformerException exception)
  {
    printLocation(new PrintWriter(pw), exception);
  }
  
  public static void printLocation(java.io.PrintStream pw, org.xml.sax.SAXParseException exception)
  {
    printLocation(new PrintWriter(pw), exception);
  }
  
  public static void printLocation(PrintWriter pw, Throwable exception)
  {
    SourceLocator locator = null;
    Throwable cause = exception;
    
    do
    {
      if(cause instanceof SAXParseException)
      {
        locator = new SAXSourceLocator((SAXParseException)cause);
      }
      else if (cause instanceof TransformerException)
      {
        SourceLocator causeLocator = ((TransformerException)cause).getLocator();
        if(null != causeLocator)
          locator = causeLocator;
      }
      if(cause instanceof TransformerException)
        cause = ((TransformerException)cause).getCause();
      else if(cause instanceof WrappedRuntimeException)
        cause = ((WrappedRuntimeException)cause).getException();
      else if(cause instanceof SAXException)
        cause = ((SAXException)cause).getException();
      else
        cause = null;
    }
    while(null != cause);
        
    if(null != locator)
    {
      String id = (null != locator.getPublicId() )
                  ? locator.getPublicId()
                    : (null != locator.getSystemId())

      pw.print(id + "; " +XSLMessages.createMessage("line", null) + locator.getLineNumber()
                         + "; " +XSLMessages.createMessage("column", null) + locator.getColumnNumber()+"; ");
    }
    else
      pw.print("("+XSLMessages.createMessage(XSLTErrorResources.ER_LOCATION_UNKNOWN, null)+")");
  }
}