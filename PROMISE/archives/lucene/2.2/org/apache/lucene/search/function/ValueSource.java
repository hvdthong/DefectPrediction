import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * Expert: source of values for basic function queries.
 * <P>At its default/simplest form, values - one per doc - are used as the score of that doc.
 * <P>Values are instantiated as 
 * {@link org.apache.lucene.search.function.DocValues DocValues} for a particular reader.
 * <P>ValueSource implementations differ in RAM requirements: it would always be a factor
 * of the number of documents, but for each document the number of bytes can be 1, 2, 4, or 8. 
 *
 * <p><font color="#FF0000">
 * WARNING: The status of the <b>search.function</b> package is experimental. 
 * The APIs introduced here might change in the future and will not be 
 * supported anymore in such a case.</font>
 *
 * @author yonik
 */
public abstract class ValueSource implements Serializable {

  /**
   * Return the DocValues used by the function query.
   * @param reader the IndexReader used to read these values.
   * If any caching is involved, that caching would also be IndexReader based.  
   * @throws IOException for any error.
   */
  public abstract DocValues getValues(IndexReader reader) throws IOException;

  /** 
   * description of field, used in explain() 
   */
  public abstract String description();

  /* (non-Javadoc) @see java.lang.Object#toString() */
  public String toString() {
    return description();
  }

  /**
   * Needed for possible caching of query results - used by {@link ValueSourceQuery#equals(Object)}.
   * @see Object#equals(Object)
   */
  public abstract boolean equals(Object o);

  /**
   * Needed for possible caching of query results - used by {@link ValueSourceQuery#hashCode()}.
   * @see Object#hashCode()
   */
  public abstract int hashCode();
  
}
