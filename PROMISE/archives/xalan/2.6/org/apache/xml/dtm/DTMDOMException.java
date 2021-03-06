package org.apache.xml.dtm;

/**
 * Simple implementation of DOMException.
 *
 * %REVIEW% Several classes were implementing this internally;
 * it makes more sense to have one shared version.
 * @xsl.usage internal
 */
public class DTMDOMException extends org.w3c.dom.DOMException
{
  /**
   * Constructs a DOM/DTM exception.
   *
   * @param code
   * @param message
   */
  public DTMDOMException(short code, String message)
  {
    super(code, message);
  }

  /**
   * Constructor DTMDOMException
   *
   *
   * @param code
   */
  public DTMDOMException(short code)
  {
    super(code, "");
  }
}
