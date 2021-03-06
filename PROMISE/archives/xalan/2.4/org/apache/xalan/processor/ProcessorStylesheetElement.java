package org.apache.xalan.processor;

import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetComposed;

import javax.xml.transform.TransformerException;
import org.xml.sax.Attributes;

import javax.xml.transform.TransformerConfigurationException;

/**
 * TransformerFactory for xsl:stylesheet or xsl:transform markup.
 */
class ProcessorStylesheetElement extends XSLTElementProcessor
{

  /**
   * Receive notification of the start of an strip-space element.
   *
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param rawName The raw XML 1.0 name (with prefix), or the
   *        empty string if raw names are not available.
   * @param attributes The attributes attached to the element.  If
   *        there are no attributes, it shall be an empty
   *        Attributes object.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {

		super.startElement(handler, uri, localName, rawName, attributes);
    try
    {
      int stylesheetType = handler.getStylesheetType();
      Stylesheet stylesheet;

      if (stylesheetType == StylesheetHandler.STYPE_ROOT)
      {
        try
        {
          stylesheet = new StylesheetRoot(handler.getSchema(), handler.getStylesheetProcessor().getErrorListener());
        }
        catch(TransformerConfigurationException tfe)
        {
          throw new TransformerException(tfe);
        }
      }
      else
      {
        Stylesheet parent = handler.getStylesheet();

        if (stylesheetType == StylesheetHandler.STYPE_IMPORT)
        {
          StylesheetComposed sc = new StylesheetComposed(parent);

          parent.setImport(sc);

          stylesheet = sc;
        }
        else
        {
          stylesheet = new Stylesheet(parent);

          parent.setInclude(stylesheet);
        }
      }

      stylesheet.setDOMBackPointer(handler.getOriginatingNode());
      stylesheet.setLocaterInfo(handler.getLocator());

      stylesheet.setPrefixes(handler.getNamespaceSupport());
      handler.pushStylesheet(stylesheet);
      setPropertiesFromAttributes(handler, rawName, attributes,
                                  handler.getStylesheet());
      handler.pushElemTemplateElement(handler.getStylesheet());
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Receive notification of the end of an element.
   *
   * @param name The element type name.
   * @param attributes The specified or defaulted attributes.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   */
  public void endElement(
          StylesheetHandler handler, String uri, String localName, String rawName)
            throws org.xml.sax.SAXException
  {
		super.endElement(handler, uri, localName, rawName);
    handler.popElemTemplateElement();
    handler.popStylesheet();
  }
}
