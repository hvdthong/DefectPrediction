package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XRTreeFrag;
import org.apache.xpath.objects.XString;

/**
 * Implement xsl:with-param.  xsl:with-param is allowed within
 * both xsl:call-template and xsl:apply-templates.
 * <pre>
 * <!ELEMENT xsl:with-param %template;>
 * <!ATTLIST xsl:with-param
 *   name %qname; #REQUIRED
 *   select %expr; #IMPLIED
 * >
 * </pre>
 * @xsl.usage advanced
 */
public class ElemWithParam extends ElemTemplateElement
{
  /**
   * This is the index to the stack frame being called, <emph>not</emph> the 
   * stack frame that contains this element.
   */
  int m_index;

  /**
   * The "select" attribute, which specifies the value of the
   * argument, if element content is not specified.
   * @serial
   */
  private XPath m_selectPattern = null;

  /**
   * Set the "select" attribute.
   * The "select" attribute specifies the value of the
   * argument, if element content is not specified.
   *
   * @param v Value to set for the "select" attribute. 
   */
  public void setSelect(XPath v)
  {
    m_selectPattern = v;
  }

  /**
   * Get the "select" attribute.
   * The "select" attribute specifies the value of the
   * argument, if element content is not specified.
   *
   * @return Value of the "select" attribute. 
   */
  public XPath getSelect()
  {
    return m_selectPattern;
  }

  /**
   * The required name attribute specifies the name of the
   * parameter (the variable the value of whose binding is
   * to be replaced). The value of the name attribute is a QName,
   * which is expanded as described in [2.4 Qualified Names].
   * @serial
   */
  private QName m_qname = null;
  
  int m_qnameID;

  /**
   * Set the "name" attribute.
   * DJD
   *
   * @param v Value to set for the "name" attribute.
   */
  public void setName(QName v)
  {
    m_qname = v;
  }

  /**
   * Get the "name" attribute.
   * DJD
   *
   * @return Value of the "name" attribute.
   */
  public QName getName()
  {
    return m_qname;
  }

  /**
   * Get an integer representation of the element type.
   *
   * @return An integer representation of the element, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_WITHPARAM;
  }


  /**
   * Return the node name.
   *
   * @return the node name.
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_WITHPARAM_STRING;
  }
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    if(null == m_selectPattern  
       && org.apache.xalan.processor.TransformerFactoryImpl.m_optimize)
    {
      XPath newSelect = ElemVariable.rewriteChildToExpression(this);
      if(null != newSelect)
        m_selectPattern = newSelect;
    }
    m_qnameID = sroot.getComposeState().getQNameID(m_qname);
    super.compose(sroot);
    
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if(null != m_selectPattern)
      m_selectPattern.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
      
  }
  
  /**
   * Set the parent as an ElemTemplateElement.
   *
   * @param parent This node's parent as an ElemTemplateElement
   */
  public void setParentElem(ElemTemplateElement p)
  {
    super.setParentElem(p);
    p.m_hasVariableDecl = true;
  }
  
  /**
   * Get the XObject representation of the variable.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @return the XObject representation of the variable.
   *
   * @throws TransformerException
   */
  public XObject getValue(TransformerImpl transformer, int sourceNode)
          throws TransformerException
  {

    XObject var;
    XPathContext xctxt = transformer.getXPathContext();

    xctxt.pushCurrentNode(sourceNode);

    try
    {
      if (null != m_selectPattern)
      {
        var = m_selectPattern.execute(xctxt, sourceNode, this);

        var.allowDetachToRelease(false);

        if (TransformerImpl.S_DEBUG)
          transformer.getTraceManager().fireSelectedEvent(sourceNode, this,
                  "select", m_selectPattern, var);
      }
      else if (null == getFirstChildElem())
      {
        var = XString.EMPTYSTRING;
      }
      else
      {

        int df = transformer.transformToRTF(this);

        var = new XRTreeFrag(df, xctxt, this);
      }
    }
    finally
    {
      xctxt.popCurrentNode();
    }

    return var;
  }
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs && (null != m_selectPattern))
  		m_selectPattern.getExpression().callVisitors(m_selectPattern, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }
  
  /**
   * Add a child to the child list. If the select attribute
   * is present, an error will be raised.
   *
   * @param elem New element to append to this element's children list
   *
   * @return null if the select attribute was present, otherwise the 
   * child just added to the child list 
   */
  public ElemTemplateElement appendChild(ElemTemplateElement elem)
  {
    if (m_selectPattern != null)
    {
      error(XSLTErrorResources.ER_CANT_HAVE_CONTENT_AND_SELECT, 
          new Object[]{"xsl:" + this.getNodeName()});
      return null;
    }
    return super.appendChild(elem);
  }


}
