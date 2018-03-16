package org.apache.xalan.processor;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemTemplateElement;

import org.xml.sax.Attributes;

/**
 * This class processes parse events for an XSLT template element.
 */
public class ProcessorTemplateElem extends XSLTElementProcessor
{
    static final long serialVersionUID = 8344994001943407235L;

  /**
   * Receive notification of the start of an element.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, or an empty string.
   * @param localName The local name (without prefix), or empty string if not namespace processing.
   * @param rawName The qualified name (with prefix).
   * @param attributes The specified or defaulted attributes.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {

    super.startElement(handler, uri, localName, rawName, attributes);
    try
    {
      XSLTElementDef def = getElemDef();
      Class classObject = def.getClassObject();
      ElemTemplateElement elem = null;

      try
      {
        elem = (ElemTemplateElement) classObject.newInstance();

        elem.setDOMBackPointer(handler.getOriginatingNode());
        elem.setLocaterInfo(handler.getLocator());
        elem.setPrefixes(handler.getNamespaceSupport());
      }
      catch (InstantiationException ie)
      {
      }
      catch (IllegalAccessException iae)
      {
      }

      setPropertiesFromAttributes(handler, rawName, attributes, elem);
      appendAndPush(handler, elem);
    }
    catch(TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }

  /**
   * Append the current template element to the current
   * template element, and then push it onto the current template
   * element stack.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param elem non-null reference to a the current template element.
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  protected void appendAndPush(
          StylesheetHandler handler, ElemTemplateElement elem)
            throws org.xml.sax.SAXException
  {

    ElemTemplateElement parent = handler.getElemTemplateElement();
    {
      parent.appendChild(elem);
      handler.pushElemTemplateElement(elem);
    }
  }

  /**
   * Receive notification of the end of an element.
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
    handler.popElemTemplateElement().setEndLocaterInfo(handler.getLocator());
  }
}