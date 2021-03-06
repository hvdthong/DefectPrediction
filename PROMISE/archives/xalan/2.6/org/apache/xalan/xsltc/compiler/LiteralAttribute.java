package org.apache.xalan.xsltc.compiler;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.TypeCheckError;
import org.apache.xalan.xsltc.compiler.util.Util;

import org.apache.xml.serializer.ElemDesc;
import org.apache.xml.serializer.SerializationHandler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class LiteralAttribute extends Instruction {


    /**
     * Creates a new literal attribute (but does not insert it into the AST).
     * @param name the attribute name (incl. prefix) as a String.
     * @param value the attribute value.
     * @param parser the XSLT parser (wraps XPath parser).
     */
    public LiteralAttribute(String name, String value, Parser parser) {
	_name = name;
	_value = AttributeValue.create(this, value, parser);
    }

    public void display(int indent) {
	indent(indent);
	Util.println("LiteralAttribute name=" + _name + " value=" + _value);
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	_value.typeCheck(stable);
	typeCheckContents(stable);
	return Type.Void;
    }

    protected boolean contextDependent() {
	return _value.contextDependent();
    }

    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();

	il.append(methodGen.loadHandler());
	il.append(new PUSH(cpg, _name));
	_value.translate(classGen, methodGen);
	
	SyntaxTreeNode parent = getParent();
	if (parent instanceof LiteralElement
	    && ((LiteralElement)parent).allAttributesUnique()) {	    
	    
	    int flags = 0;
	    boolean isHTMLAttrEmpty = false;
	    ElemDesc elemDesc = ((LiteralElement)parent).getElemDesc();
	    
	    if (elemDesc != null) {
	    	if (elemDesc.isAttrFlagSet(_name, ElemDesc.ATTREMPTY)) {
	    	    flags = flags | SerializationHandler.HTML_ATTREMPTY;
	    	    isHTMLAttrEmpty = true;
	    	}
	    	else if (elemDesc.isAttrFlagSet(_name, ElemDesc.ATTRURL)) {
	    	    flags = flags | SerializationHandler.HTML_ATTRURL;
	    	}
	    }
	    
	    if (_value instanceof SimpleAttributeValue) {
	        String attrValue = ((SimpleAttributeValue)_value).toString();
	        
	        if (!hasBadChars(attrValue) && !isHTMLAttrEmpty) {
	            flags = flags | SerializationHandler.NO_BAD_CHARS;
	        }
	    }
	        
	    il.append(new PUSH(cpg, flags));
	    il.append(methodGen.uniqueAttribute());
	}
	else {
	    il.append(methodGen.attribute());
	}
    }
    
    /**
     * Return true if at least one character in the String is considered to
     * be a "bad" character. A bad character is one whose code is:
     * less than 32 (a space),
     * or greater than 126,
     * or it is one of '<', '>', '&' or '\"'. 
     * This helps the serializer to decide whether the String needs to be escaped.
     */
    private boolean hasBadChars(String value) {
    	char[] chars = value.toCharArray();
    	int size = chars.length;
    	for (int i = 0; i < size; i++) {
    	    char ch = chars[i];
    	    if (ch < 32 || 126 < ch || ch == '<' || ch == '>' || ch == '&' || ch == '\"')
                return true;    	        
    	}
    	return false;
    }
    
    /**
     * Return the name of the attribute
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Return the value of the attribute
     */
    public AttributeValue getValue() {
        return _value;
    }
    
}
