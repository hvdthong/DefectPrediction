import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MathException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.util.TemplateNumber;

/**
 * Helps handle math<br><br>
 *
 * Please look at the Parser.jjt file which is
 * what controls the generation of this class.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:pero@antaramusic.de">Peter Romianowski</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author Nathan Bubna
 * @version $Id: ASTMathNode.java 517553 2007-03-13 06:09:58Z wglass $
 */
public abstract class ASTMathNode extends SimpleNode
{
    protected boolean strictMode = false;

    public ASTMathNode(int id)
    {
        super(id);
    }

    public ASTMathNode(Parser p, int id)
    {
        super(p, id);
    }

    /**
     * {@inheritDoc}
     */
    public Object init(InternalContextAdapter context, Object data) throws TemplateInitException
    {
        super.init(context, data);
        strictMode = rsvc.getBoolean(RuntimeConstants.STRICT_MATH, false);
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     * gets the two args and performs the operation on them
     *
     * @param context
     * @return result or null
     * @throws MethodInvocationException
     */
    public Object value(InternalContextAdapter context) throws MethodInvocationException
    {
        Object left = jjtGetChild(0).value(context);
        Object right = jjtGetChild(1).value(context);

        /*
         * should we do anything special here?
         */
        Object special = handleSpecial(left, right, context);
        if (special != null)
        {
            return special;
        }

        /*
         * convert to Number if applicable
         */
        if (left instanceof TemplateNumber)
        {
           left = ((TemplateNumber)left).getAsNumber();
        }
        if (right instanceof TemplateNumber)
        {
           right = ((TemplateNumber)right).getAsNumber();
        }

        /*
         * if not a Number, not much we can do
         */
        if (!(left instanceof Number) || !(right instanceof Number))
        {
            boolean wrongright = (left instanceof Number);
            boolean wrongtype = wrongright ? right != null : left != null;
            String msg = (wrongright ? "Right" : "Left")
                        + " side of math operation ("
                        + jjtGetChild(wrongright ? 1 : 0).literal() + ") "
                        + (wrongtype ? "is not a Number. " : "has a null value. ")
                        + getLocation(context);
            if (strictMode)
            {
                log.error(msg);
                throw new MathException(msg);
            }
            else
            {
                log.debug(msg);
                return null;
            }
        }

        return perform((Number)left, (Number)right, context);
    }

    /**
     * Extension hook to allow special behavior by subclasses
     * If this method returns a non-null value, that is returned,
     * rather than the result of the math operation.
     * @see ASTAddNode#handleSpecial
     */
    protected Object handleSpecial(Object left, Object right, InternalContextAdapter context)
    {
        return null;
    }

    /**
     * Performs the math operation represented by this node.
     */
    public abstract Number perform(Number left, Number right, InternalContextAdapter context);

}




