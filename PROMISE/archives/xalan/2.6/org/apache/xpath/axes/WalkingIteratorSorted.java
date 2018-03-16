package org.apache.xpath.axes;

import org.apache.xml.dtm.Axis;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.compiler.Compiler;

/**
 * This class iterates over set of nodes that needs to be sorted.
 * @xsl.usage internal
 */
public class WalkingIteratorSorted extends WalkingIterator
{

  
  /** True if the nodes will be found in document order, and this can 
   * be determined statically. */
  protected boolean m_inNaturalOrderStatic = false;

  /**
   * Create a WalkingIteratorSorted object.
   *
   * @param nscontext The namespace context for this iterator,
   * should be OK if null.
   */
  public WalkingIteratorSorted(PrefixResolver nscontext)
  {
    super(nscontext);
  }

  /**
   * Create a WalkingIterator iterator, including creation
   * of step walkers from the opcode list, and call back
   * into the Compiler to create predicate expressions.
   *
   * @param compiler The Compiler which is creating
   * this expression.
   * @param opPos The position of this iterator in the
   * opcode list from the compiler.
   * @param shouldLoadWalkers True if walkers should be
   * loaded, or false if this is a derived iterator and
   * it doesn't wish to load child walkers.
   *
   * @throws javax.xml.transform.TransformerException
   */
  WalkingIteratorSorted(
          Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers)
            throws javax.xml.transform.TransformerException
  {
    super(compiler, opPos, analysis, shouldLoadWalkers);
  }
  
  /**
   * Returns true if all the nodes in the iteration well be returned in document 
   * order.
   * 
   * @return true as a default.
   */
  public boolean isDocOrdered()
  {
    return m_inNaturalOrderStatic;
  }

    
  /**
   * Tell if the nodeset can be walked in doc order, via static analysis. 
   *
   *
   * @return true if the nodeset can be walked in doc order, without sorting.
   */
  boolean canBeWalkedInNaturalDocOrderStatic()
  {

    if (null != m_firstWalker)
    {
      AxesWalker walker = m_firstWalker;
      int prevAxis = -1;
      boolean prevIsSimpleDownAxis = true;

      for(int i = 0; null != walker; i++)
      {
        int axis = walker.getAxis();
        
        if(walker.isDocOrdered())
        {
          boolean isSimpleDownAxis = ((axis == Axis.CHILD)
                                   || (axis == Axis.SELF)
                                   || (axis == Axis.ROOT));
          if(isSimpleDownAxis || (axis == -1))
            walker = walker.getNextWalker();
          else
          {
            boolean isLastWalker = (null == walker.getNextWalker());
            if(isLastWalker)
            {
              if(walker.isDocOrdered() && (axis == Axis.DESCENDANT || 
                 axis == Axis.DESCENDANTORSELF || axis == Axis.DESCENDANTSFROMROOT
                 || axis == Axis.DESCENDANTSORSELFFROMROOT) || (axis == Axis.ATTRIBUTE))
                return true;
            }
            return false;
          }
        }
        else
          return false;
      }
      return true;
    }
    return false;
  }


  
  /**
   * This function is used to perform some extra analysis of the iterator.
   * 
   * @param vars List of QNames that correspond to variables.  This list 
   * should be searched backwards for the first qualified name that 
   * corresponds to the variable reference qname.  The position of the 
   * QName in the vector from the start of the vector will be its position 
   * in the stack frame (but variables above the globalsTop value will need 
   * to be offset to the current stack frame).
   */
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    super.fixupVariables(vars, globalsSize);

    int analysis = getAnalysisBits();
    if(WalkerFactory.isNaturalDocOrder(analysis))
    {
    	m_inNaturalOrderStatic = true;
    }
    else
    {
    	m_inNaturalOrderStatic = false;
    }
    
  }

}