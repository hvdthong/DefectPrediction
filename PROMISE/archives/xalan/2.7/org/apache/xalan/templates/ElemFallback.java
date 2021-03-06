package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;

/**
 * Implement xsl:fallback.
 * <pre>
 * <!ELEMENT xsl:fallback %template;>
 * <!ATTLIST xsl:fallback %space-att;>
 * </pre>
 * @xsl.usage advanced
 */
public class ElemFallback extends ElemTemplateElement
{
    static final long serialVersionUID = 1782962139867340703L;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_FALLBACK;
  }

  /**
   * Return the node name.
   *
   * @return The Element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_FALLBACK_STRING;
  }

  /**
   * This is the normal call when xsl:fallback is instantiated.
   * In accordance with the XSLT 1.0 Recommendation, chapter 15,
   * "Normally, instantiating an xsl:fallback element does nothing."
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
  }

  /**
   * Execute the fallback elements.  This must be explicitly called to
   * instantiate the content of an xsl:fallback element.
   * When an XSLT transformer performs fallback for an instruction
   * element, if the instruction element has one or more xsl:fallback
   * children, then the content of each of the xsl:fallback children
   * must be instantiated in sequence; otherwise, an error must
   * be signaled. The content of an xsl:fallback element is a template.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void executeFallback(
          TransformerImpl transformer)
            throws TransformerException
  {

    int parentElemType = m_parentNode.getXSLToken();
    if (Constants.ELEMNAME_EXTENSIONCALL == parentElemType 
        || Constants.ELEMNAME_UNDEFINED == parentElemType)
    {

      if (transformer.getDebug())
        transformer.getTraceManager().fireTraceEvent(this);

      transformer.executeChildTemplates(this, true);

      if (transformer.getDebug())
	    transformer.getTraceManager().fireTraceEndEvent(this); 
    }
    else
    {

      System.out.println(
        "Error!  parent of xsl:fallback must be an extension or unknown element!");
    }
  }
}
