package org.apache.wml;

/**
 * <p>The interface is modeled after DOM1 Spec for HTML from W3C.
 * The DTD used in this DOM model is from 
 *
 * <p>'br' element starts a new line</p>
 *
 * @version $Id: WMLBrElement.java 315461 2000-04-23 18:07:48Z david $
 * @author <a href="mailto:david@topware.com.tw">David Li</a>
 */

public interface WMLBrElement extends WMLElement {
    /**
     * 'xml:lang' specifics the natural or formal language in which
     * the document is written.  
     * (Section 8.8, WAP WML Version 16-Jun-1999) 
     */
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
