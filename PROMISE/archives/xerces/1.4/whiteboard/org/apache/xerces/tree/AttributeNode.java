package org.apache.xerces.tree;

import java.io.Writer;
import java.io.IOException;

import org.w3c.dom.*;
import org.apache.xerces.tree.XmlNames;

/**
 * Node representing an XML attribute.  Many views of attributes can be
 * useful, but only the first of these is explicitly supported: <DL>
 *
 *	<DT> <em>Logical View</em> </DT><DD>  Attributes always hold a
 *	string created by expanding character and entity references from
 *	source text conforming to the XML specification.  If this
 *	attribute was declared in a DTD, normalization will often be
 *	done to eliminate insignificant whitespace.</DD>
 *
 *	<DT> <em>DTD Validated View</em> </DT><DD>  If the attribute was
 *	declared in a DTD, it will have minimal semantics provided by its
 *	declaration, and checked by validating parsers.  For example, the
 *	logical view may name one (or many) unparsed entities or DOM nodes.
 *	This view could provide direct access to them for any DTD, since
 *	these attribute semantics are defined by XML itself.</DD>
 *
 *	<DT> <em>Semantic View</em> </DT><DD> The person who wrote the
 *	DTD (or other namespace) defined what each attribute's logical
 *	view "means".  For example, that it's a URL, or that the unparsed
 *	entity referred to identifies a particular database to be used.
 *	This view would provide direct access to such values, but would
 *	need to have code specialized to that DTD or namespace.</DD>
 *
 *	<DT> <em>Physical View</em> </DT><DD>  Attributes may have children
 *	to represent text and entity reference nodes found in unexpanded
 *	and unnormalized XML source text.  Such views are mostly of interest
 *	when editing XML text that is not dynamically generated by programs.
 *	This implementation does not currently support physical views of
 *	attributes.  </DD>
 *
 *	</DL>
 *
 *
 * @author David Brownell
 * @author Rajiv Mordani
 * @version $Revision: 315388 $
 */
class AttributeNode extends NamespacedNode
    implements Attr, NamespaceScoped
{
    private String      value;
    private boolean	specified;
    private String	defaultValue;
    private ElementNode ownerElement;

    private ElementNode	nameScope;
        
    /** Constructs a copy of the specified node. */
    public AttributeNode (AttributeNode original)
    throws DOMException
    {
	this (original.name, original.value,
		original.specified, original.defaultValue);
	nameScope = original.nameScope;
	setOwnerDocument ((XmlDocument) original.getOwnerDocument ());
    }

    /** Constructs an attribute node. */
    public AttributeNode (String name, String value,
	boolean specified, String defaultValue)
    throws DOMException
    {
	if (!XmlNames.isName (name))
	    throw new DomEx (DOMException.INVALID_CHARACTER_ERR);

        this.name = name;
        this.value = value;
	this.specified = specified;
	this.defaultValue = defaultValue;
    }

    /** Constructs an attribute node. Used for SAX2 and DOM2 */
    public AttributeNode(String namespaceURI, String qualifiedName,
                         String value, boolean specified, String defaultValue)
        throws DOMException
    {
	if (!XmlNames.isName(qualifiedName)) {
	    throw new DomEx(DOMException.INVALID_CHARACTER_ERR);
        }

        this.name = qualifiedName;
        this.namespaceURI = namespaceURI;
        this.value = value;
	this.specified = specified;
	this.defaultValue = defaultValue;
    }

    void setNameScope (ElementNode e)
    {
	if (e != null && nameScope != null)
	    throw new IllegalStateException (getMessage ("A-000", 
				     new Object [] { e.getTagName () }));
	nameScope = e;
    }

    ElementNode getNameScope ()
    {
	return nameScope;
    }

    String getDefaultValue ()
    {
	return defaultValue;
    }


    public String getNamespace ()
    {
	if (nameScope == null)
	    throw new IllegalStateException (getMessage ("A-001"));

	String	prefix;
	String	value;

	
	if ((prefix = getPrefix()) == null)
	    return nameScope.getNamespace ();

	if ("xml".equals (prefix) || "xmlns".equals (prefix))
	    return null;
	value = nameScope.getInheritedAttribute ("xmlns:" + prefix);

	if (value == null)
	    throw new IllegalStateException ();

	return value;
    }

    
    /**
     * <b>DOM2:</b>
     */
    public Element getOwnerElement() {
        return ownerElement;
    }

    void setOwnerElement(ElementNode element) {
        ownerElement = element;
    }

    
    /** DOM:  Returns the ATTRIBUTE_NODE node type constant. */
    public short getNodeType () { return ATTRIBUTE_NODE; }

    /** DOM:  Returns the attribute name */
    public String getName () { return name; }

    /** DOM:  Returns the attribute name */
    public String getNodeName () { return name; }

    /** DOM:  Returns the attribute value. */
    public String getValue () { return value; }

    /** DOM:  Assigns the value of this attribute. */
    public void setValue (String value) { setNodeValue (value); }

    /** DOM:  Returns the attribute value. */
    public String getNodeValue () { return value; }

    /** DOM:  Returns true if the source text specified the attribute. */
    public boolean getSpecified () { return specified; }

    /** DOM:  Assigns the value of this attribute. */
    public void setNodeValue (String value)
    {
    	if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	this.value = value;
	specified = true;
    }

    /** Flags whether the source text specified the attribute. */
    void setSpecified (boolean specified) { this.specified = specified; }

    /** DOM:  Returns null */
    public Node getParentNode () { return null; }
    
    /** DOM:  Returns null */
    public Node getNextSibling () { return null; }
    
    /** DOM:  Returns null */
    public Node getPreviousSibling () { return null; }

    
    /**
     * Writes the attribute out, as if it were assigned within an
     * element's starting tag (<em>name="value"</em>).
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();

        out.write (name);
	out.write ("=\"");
	writeChildrenXml (context);
	out.write ('"');
    }

    /**
     * Writes the attribute's value.
     */
    public void writeChildrenXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();
	for (int i = 0; i < value.length (); i++) {
	    int c = value.charAt (i);
	    switch (c) {
	      case '<':  out.write ("&lt;"); continue;
	      case '>':  out.write ("&gt;"); continue;
	      case '&':  out.write ("&amp;"); continue;
	      case '\'': out.write ("&apos;"); continue;
	      case '"':  out.write ("&quot;"); continue;
	      default:   out.write (c); continue;
	    }
	}
    }

    /** DOM: returns a copy of this node */
    public Node cloneNode (boolean deep)
    {

	try {
		AttributeNode attr = new AttributeNode (this.name, this.value,
			this.specified, this.defaultValue);
		attr.setOwnerDocument ((XmlDocument)this.getOwnerDocument ());
		attr.ownerElement = ownerElement;
		if (deep) {
	            Node	node;

	            for (int i = 0; (node = item (i)) != null; i++) {
		        node = node.cloneNode (true);
		        attr.appendChild (node);
	            }
		}
		return attr;
	} catch (DOMException e) {
	    throw new RuntimeException (getMessage ("A-002"));
	}
    }

    void checkChildType(int type) throws DOMException {
	switch(type) {
	  case TEXT_NODE:
	  case ENTITY_REFERENCE_NODE:
	    return;
	  default:
	    throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
	}
    }
}
