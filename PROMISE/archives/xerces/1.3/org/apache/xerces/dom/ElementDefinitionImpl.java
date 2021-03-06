package org.apache.xerces.dom;

import org.w3c.dom.*;

/**
 * NON-DOM CLASS: Describe one of the Elements (and its associated
 * Attributes) defined in this Document Type.
 * <p>
 * I've included this in Level 1 purely as an anchor point for default
 * attributes. In Level 2 it should enable the ChildRule support.
 *
 * @version
 */
public class ElementDefinitionImpl 
    extends ParentNode {


    /** Serialization version. */
    static final long serialVersionUID = -8373890672670022714L;
    

    /** Element definition name. */
    protected String name;

    /** Default attributes. */
    protected NamedNodeMapImpl attributes;


    /** Factory constructor. */
    public ElementDefinitionImpl(DocumentImpl ownerDocument, String name) {
    	super(ownerDocument);
        this.name = name;
        attributes = new NamedNodeMapImpl(ownerDocument);
    }


    /** 
     * A short integer indicating what type of node this is. The named
     * constants for this value are defined in the org.w3c.dom.Node interface.
     */
    public short getNodeType() {
        return NodeImpl.ELEMENT_DEFINITION_NODE;
    }

    /**
     * Returns the element definition name
     */
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return name;
    }

    /**
     * Replicate this object.
     */
    public Node cloneNode(boolean deep) {

    	ElementDefinitionImpl newnode =
            (ElementDefinitionImpl) super.cloneNode(deep);
    	newnode.attributes = attributes.cloneMap(newnode);
    	return newnode;


    /**
     * Query the attributes defined on this Element.
     * <p>
     * In the base implementation this Map simply contains Attribute objects
     * representing the defaults. In a more serious implementation, it would
     * contain AttributeDefinitionImpl objects for all declared Attributes,
     * indicating which are Default, DefaultFixed, Implicit and/or Required.
     * 
     * @return org.w3c.dom.NamedNodeMap containing org.w3c.dom.Attribute
     */
    public NamedNodeMap getAttributes() {

        if (needsSyncChildren()) {
            synchronizeChildren();
        }
    	return attributes;


