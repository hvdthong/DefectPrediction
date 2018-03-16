package org.apache.xpath.functions;


import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XObject;

/**
 * <meta name="usage" content="advanced"/>
 * This is a superclass of all XPath functions.  This allows two
 * ways for the class to be called. One method is that the
 * super class processes the arguments and hands the results to
 * the derived class, the other method is that the derived
 * class may process it's own arguments, which is faster since
 * the arguments don't have to be added to an array, but causes
 * a larger code footprint.
 */
public abstract class Function extends Expression
{

  /**
   * Set an argument expression for a function.  This method is called by the 
   * XPath compiler.
   *
   * @param arg non-null expression that represents the argument.
   * @param argNum The argument number index.
   *
   * @throws WrongNumberArgsException If the argNum parameter is beyond what 
   * is specified for this function.
   */
  public void setArg(Expression arg, int argNum)
          throws WrongNumberArgsException
  {
      reportWrongNumberArgs();
  }

  /**
   * Check that the number of arguments passed to this function is correct.
   * This method is meant to be overloaded by derived classes, to check for 
   * the number of arguments for a specific function type.  This method is 
   * called by the compiler for static number of arguments checking.
   *
   * @param argNum The number of arguments that is being passed to the function.
   *
   * @throws WrongNumberArgsException
   */
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException
  {
    if (argNum != 0)
      reportWrongNumberArgs();
  }

  /**
   * Constructs and throws a WrongNumberArgException with the appropriate
   * message for this function object.  This method is meant to be overloaded
   * by derived classes so that the message will be as specific as possible.
   *
   * @throws WrongNumberArgsException
   */
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
      throw new WrongNumberArgsException(XSLMessages.createXPATHMessage("zero", null));
  }

  /**
   * Execute an XPath function object.  The function must return
   * a valid object.
   * @param xctxt The execution current context.
   * @return A valid XObject.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {

    System.out.println("Error! Function.execute should not be called!");

    return null;
  }
  
  /**
   * Call the visitors for the function arguments.
   */
  public void callArgVisitors(XPathVisitor visitor)
  {
  }

  
  /**
   * @see XPathVisitable#callVisitors(ExpressionOwner, XPathVisitor)
   */
  public void callVisitors(ExpressionOwner owner, XPathVisitor visitor)
  {
  	if(visitor.visitFunction(owner, this))
  	{
  		callArgVisitors(visitor);
  	}
  }
  
  /**
   * @see Expression#deepEquals(Expression)
   */
  public boolean deepEquals(Expression expr)
  {
  	if(!isSameClass(expr))
  		return false;
  		
  	return true;
  }

  /**
   * This function is currently only being used by Position()
   * and Last(). See respective functions for more detail.
   */
  public void postCompileStep(Compiler compiler)
  {
  }
}