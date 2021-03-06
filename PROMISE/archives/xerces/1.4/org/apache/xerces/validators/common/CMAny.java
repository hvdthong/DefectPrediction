package org.apache.xerces.validators.common;

import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.utils.ImplementationMessages;
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;

/**
 * Content model any node.
 *
 * @version $Id: CMAny.java 317309 2001-06-25 14:47:19Z sandygao $
 */
public class CMAny
    extends CMNode {


    /**
     * The any content model type. This value is one of the following:
     * XMLContentSpec.CONTENTSPECNODE_ANY,
     * XMLContentSpec.CONTENTSPECNODE_ANY_OTHER,
     * XMLContentSpec.CONTENTSPECNODE_ANY_NS.
     */
    private int fType;

    /**
     * URI of the any content model. This value is set if the type is
     * of the following:
     * XMLContentSpec.CONTENTSPECNODE_ANY,
     * XMLContentSpec.CONTENTSPECNODE_ANY_OTHER.
     */
    private int fURI;

    /**
     * Part of the algorithm to convert a regex directly to a DFA
     * numbers each leaf sequentially. If its -1, that means its an
     * epsilon node. Zero and greater are non-epsilon positions.
     */
    private int fPosition = -1;


    /** Constructs a content model any. */
    public CMAny(int type, int uri, int position) throws CMException {
        super(type);

        fType = type;
        fURI = uri;
        fPosition = position;
    }


    final int getType() {
        return fType;
    }

    final int getURI() {
        return fURI;
    }

    final int getPosition()
    {
        return fPosition;
    }

    final void setPosition(int newPosition)
    {
        fPosition = newPosition;
    }



    boolean isNullable() throws CMException
    {
        return (fPosition == -1);
    }

    String toString(StringPool stringPool)
    {
        StringBuffer strRet = new StringBuffer();
        strRet.append("(");
        switch (fType & 0x0f) {
        case XMLContentSpec.CONTENTSPECNODE_ANY:
            strRet.append("##any");
            break;
        case XMLContentSpec.CONTENTSPECNODE_ANY_NS:
            strRet.append("##any:uri=" + stringPool.toString(fURI));
            break;
        case XMLContentSpec.CONTENTSPECNODE_ANY_OTHER:
            strRet.append("##other:uri=" + stringPool.toString(fURI));
            break;
        }
        strRet.append(stringPool.toString(fURI));
        strRet.append(')');
        if (fPosition >= 0)
        {
            strRet.append
            (
                " (Pos:"
                + new Integer(fPosition).toString()
                + ")"
            );
        }
        return strRet.toString();
    }


    protected void calcFirstPos(CMStateSet toSet) throws CMException
    {
        if (fPosition == -1)
            toSet.zeroBits();

        else
            toSet.setBit(fPosition);
    }

    protected void calcLastPos(CMStateSet toSet) throws CMException
    {
        if (fPosition == -1)
            toSet.zeroBits();

        else
            toSet.setBit(fPosition);
    }

