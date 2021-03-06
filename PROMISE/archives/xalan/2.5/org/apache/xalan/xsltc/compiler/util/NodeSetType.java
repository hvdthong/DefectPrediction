package org.apache.xalan.xsltc.compiler.util;

import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.Constants;
import org.apache.xalan.xsltc.compiler.FlowList;

public final class NodeSetType extends Type {
    protected NodeSetType() {}

    public String toString() {
	return "node-set";
    }

    public boolean identicalTo(Type other) {
	return this == other;
    }

    public String toSignature() {
	return NODE_ITERATOR_SIG;
    }

    public org.apache.bcel.generic.Type toJCType() {
	return new org.apache.bcel.generic.ObjectType(NODE_ITERATOR);
    }

    /**
     * Translates a node-set into an object of internal type
     * <code>type</code>. The translation to int is undefined
     * since node-sets are always converted to
     * reals in arithmetic expressions.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    Type type) {
	if (type == Type.String) {
	    translateTo(classGen, methodGen, (StringType) type);
	}
	else if (type == Type.Boolean) {
	    translateTo(classGen, methodGen, (BooleanType) type);
	}
	else if (type == Type.Real) {
	    translateTo(classGen, methodGen, (RealType) type);
	}
	else if (type == Type.Node) {
	    translateTo(classGen, methodGen, (NodeType) type);
	}
	else if (type == Type.Reference) {
	    translateTo(classGen, methodGen, (ReferenceType) type);
	}
	else if (type == Type.Object) {
	    translateTo(classGen, methodGen, (ObjectType) type);
	}
	else {
	    ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
					toString(), type.toString());
	    classGen.getParser().reportError(Constants.FATAL, err);
	}
    }

    /**
     * Translates an external Java Class into an internal type.
     * Expects the Java object on the stack, pushes the internal type
     */
    public void translateFrom(ClassGenerator classGen, 
	MethodGenerator methodGen, Class clazz) 
    {
		
  	InstructionList il = methodGen.getInstructionList();
	ConstantPoolGen cpg = classGen.getConstantPool();
	if (clazz.getName().equals("org.w3c.dom.NodeList")) {
	   final int convert = cpg.addMethodref(BASIS_LIBRARY_CLASS,
					"nodeList2Iterator",
					"("		
					 + "Lorg/w3c/dom/NodeList;"
					 + TRANSLET_INTF_SIG 
					 + DOM_INTF_SIG 
					 + ")" + NODE_ITERATOR_SIG );
	   il.append(new INVOKESTATIC(convert));
	}
	else if (clazz.getName().equals("org.w3c.dom.Node")) {
	   final int convert = cpg.addMethodref(BASIS_LIBRARY_CLASS,
					"node2Iterator",
					"("		
					 + "Lorg/w3c/dom/Node;"
					 + TRANSLET_INTF_SIG 
					 + DOM_INTF_SIG 
					 + ")" + NODE_ITERATOR_SIG );
	   il.append(new INVOKESTATIC(convert));
	}
	else {
	    ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
		toString(), clazz.getName());
	    classGen.getParser().reportError(Constants.FATAL, err);
	} 
    }


    /**
     * Translates a node-set into a synthesized boolean.
     * The boolean value of a node-set is "true" if non-empty
     * and "false" otherwise. Notice that the 
     * function getFirstNode() is called in translateToDesynthesized().
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    BooleanType type) {
	final InstructionList il = methodGen.getInstructionList();
	FlowList falsel = translateToDesynthesized(classGen, methodGen, type);
	il.append(ICONST_1);
	final BranchHandle truec = il.append(new GOTO(null));
	falsel.backPatch(il.append(ICONST_0));
	truec.setTarget(il.append(NOP));
    }

    /**
     * Translates a node-set into a string. The string value of a node-set is
     * value of its first element.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    StringType type) {
	final InstructionList il = methodGen.getInstructionList();
	getFirstNode(classGen, methodGen);
	il.append(DUP);
	final BranchHandle falsec = il.append(new IFLT(null));
	Type.Node.translateTo(classGen, methodGen, type);
	final BranchHandle truec = il.append(new GOTO(null));
	falsec.setTarget(il.append(POP));
	il.append(new PUSH(classGen.getConstantPool(), ""));
	truec.setTarget(il.append(NOP));
    }

    /**
     * Expects a node-set on the stack and pushes a real.
     * First the node-set is converted to string, and from string to real.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    RealType type) {
	translateTo(classGen, methodGen, Type.String);
	Type.String.translateTo(classGen, methodGen, Type.Real);	
    }

    /**
     * Expects a node-set on the stack and pushes a node.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    NodeType type) {
	getFirstNode(classGen, methodGen);
    }

    /**
     * Subsume node-set into ObjectType.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    ObjectType type) {
	    methodGen.getInstructionList().append(NOP);	
    }

    /**
     * Translates a node-set into a non-synthesized boolean. It does not 
     * push a 0 or a 1 but instead returns branchhandle list to be appended 
     * to the false list.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateToDesynthesized
     */
    public FlowList translateToDesynthesized(ClassGenerator classGen, 
					     MethodGenerator methodGen, 
					     BooleanType type) {
	final InstructionList il = methodGen.getInstructionList();
	getFirstNode(classGen, methodGen);
	return new FlowList(il.append(new IFLT(null)));
    }

    /**
     * Expects a node-set on the stack and pushes a boxed node-set.
     * Node sets are already boxed so the translation is just a NOP.
     *
     * @see	org.apache.xalan.xsltc.compiler.util.Type#translateTo
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    ReferenceType type) {
	methodGen.getInstructionList().append(NOP);
    }

    /**
     * Translates a node-set into the Java type denoted by <code>clazz</code>. 
     * Expects a node-set on the stack and pushes an object of the appropriate
     * type after coercion.
     */
    public void translateTo(ClassGenerator classGen, MethodGenerator methodGen, 
			    Class clazz) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	final String className = clazz.getName();

	il.append(methodGen.loadDOM());
	il.append(SWAP);

	if (className.equals("org.w3c.dom.Node")) {
	    int index = cpg.addInterfaceMethodref(DOM_INTF,
						  MAKE_NODE,
						  MAKE_NODE_SIG2);
	    il.append(new INVOKEINTERFACE(index, 2));
	}
        else if (className.equals("org.w3c.dom.NodeList") || 
                 className.equals("java.lang.Object")) {
	    int index = cpg.addInterfaceMethodref(DOM_INTF,
						  MAKE_NODE_LIST,
						  MAKE_NODE_LIST_SIG2);
	    il.append(new INVOKEINTERFACE(index, 2));
	}
        else if (className.equals("java.lang.String")) {
            int next = cpg.addInterfaceMethodref(NODE_ITERATOR,
                                                 "next", "()I");
            int index = cpg.addInterfaceMethodref(DOM_INTF,
                                                 GET_NODE_VALUE,
                                                 "(I)"+STRING_SIG);

            il.append(new INVOKEINTERFACE(next, 1));
            il.append(new INVOKEINTERFACE(index, 2));
                       
        }
	else if (className.equals("int")) {
	    int next = cpg.addInterfaceMethodref(NODE_ITERATOR,
						  "next", "()I");
	    int index = cpg.addInterfaceMethodref(DOM_INTF,
						  GET_NODE_VALUE,
						  "(I)"+STRING_SIG);
	    int str = cpg.addMethodref(BASIS_LIBRARY_CLASS,
					STRING_TO_INT,
					STRING_TO_INT_SIG);

	    il.append(new INVOKEINTERFACE(next, 1));
	    il.append(new INVOKEINTERFACE(index, 2));
	    il.append(new INVOKESTATIC(str));
	}
	else {
	    ErrorMsg err = new ErrorMsg(ErrorMsg.DATA_CONVERSION_ERR,
					toString(), className);
	    classGen.getParser().reportError(Constants.FATAL, err);
	}
    }
    
    /**
     * Some type conversions require gettting the first node from the node-set.
     * This function is defined to avoid code repetition.
     */
    private void getFirstNode(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	il.append(new INVOKEINTERFACE(cpg.addInterfaceMethodref(NODE_ITERATOR,
								NEXT,
								NEXT_SIG), 1));
    }

    /**
     * Translates an object of this type to its boxed representation.
     */ 
    public void translateBox(ClassGenerator classGen,
			     MethodGenerator methodGen) {
	translateTo(classGen, methodGen, Type.Reference);
    }

    /**
     * Translates an object of this type to its unboxed representation.
     */ 
    public void translateUnBox(ClassGenerator classGen,
			       MethodGenerator methodGen) {
	methodGen.getInstructionList().append(NOP);
    }

    /**
     * Returns the class name of an internal type's external representation.
     */
    public String getClassName() {
	return(NODE_ITERATOR);
    }


    public Instruction LOAD(int slot) {
	return new ALOAD(slot);
    }
	
    public Instruction STORE(int slot) {
	return new ASTORE(slot);
    }
}
