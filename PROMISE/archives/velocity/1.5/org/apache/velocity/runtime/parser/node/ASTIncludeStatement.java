import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParserVisitor;

/**
 *
 */
public class ASTIncludeStatement extends SimpleNode
{
    /**
     * @param id
     */
    public ASTIncludeStatement(int id)
    {
        super(id);
    }

    /**
     * @param p
     * @param id
     */
    public ASTIncludeStatement(Parser p, int id)
    {
        super(p, id);
    }


    /**
     * @see org.apache.velocity.runtime.parser.node.SimpleNode#jjtAccept(org.apache.velocity.runtime.parser.ParserVisitor, java.lang.Object)
     */
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
