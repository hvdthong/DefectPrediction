package org.apache.lucene.analysis.standard;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/** A grammar-based tokenizer constructed with JFlex
 *
 * <p> This should be a good tokenizer for most European-language documents:
 *
 * <ul>
 *   <li>Splits words at punctuation characters, removing punctuation. However, a 
 *     dot that's not followed by whitespace is considered part of a token.
 *   <li>Splits words at hyphens, unless there's a number in the token, in which case
 *     the whole token is interpreted as a product number and is not split.
 *   <li>Recognizes email addresses and internet hostnames as one token.
 * </ul>
 *
 * <p>Many applications have specific tokenizer needs.  If this tokenizer does
 * not suit your application, please consider copying this source code
 * directory to your project and maintaining your own grammar-based tokenizer.
 */

public class StandardTokenizer extends Tokenizer {
  /** A private instance of the JFlex-constructed scanner */
  private final StandardTokenizerImpl scanner;

  public static final int ALPHANUM          = 0;
  public static final int APOSTROPHE        = 1;
  public static final int ACRONYM           = 2;
  public static final int COMPANY           = 3;
  public static final int EMAIL             = 4;
  public static final int HOST              = 5;
  public static final int NUM               = 6;
  public static final int CJ                = 7;

  /**
   * @deprecated this solves a bug where HOSTs that end with '.' are identified
   *             as ACRONYMs. It is deprecated and will be removed in the next
   *             release.
   */
  public static final int ACRONYM_DEP       = 8;

  /** String token types that correspond to token type int constants */
  public static final String [] TOKEN_TYPES = new String [] {
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<COMPANY>",
    "<EMAIL>",
    "<HOST>",
    "<NUM>",
    "<CJ>",
    "<ACRONYM_DEP>"
  };

  /** @deprecated Please use {@link #TOKEN_TYPES} instead */
  public static final String [] tokenImage = TOKEN_TYPES;

  /**
   * Specifies whether deprecated acronyms should be replaced with HOST type.
   * This is false by default to support backward compatibility.
   *<p/>
   * 
   * @deprecated this should be removed in the next release (3.0).
   */
  private boolean replaceInvalidAcronym = false;
    
  void setInput(Reader reader) {
    this.input = reader;
  }

  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /** Set the max allowed token length.  Any token longer
   *  than this is skipped. */
  public void setMaxTokenLength(int length) {
    this.maxTokenLength = length;
  }

  /** @see #setMaxTokenLength */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

    /**
     * Creates a new instance of the {@link StandardTokenizer}. Attaches the
     * <code>input</code> to a newly created JFlex scanner.
     */
    public StandardTokenizer(Reader input) {
	    this.input = input;
	    this.scanner = new StandardTokenizerImpl(input);
    }

  /**
   * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.  Attaches
   * the <code>input</code> to the newly created JFlex scanner.
   *
   * @param input The input reader
   * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms with HOST.
   *
   */
  public StandardTokenizer(Reader input, boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
    this.input = input;
    this.scanner = new StandardTokenizerImpl(input);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#next()
   */
  public Token next(final Token reusableToken) throws IOException {
      assert reusableToken != null;
      int posIncr = 1;

      while(true) {
	int tokenType = scanner.getNextToken();

	if (tokenType == StandardTokenizerImpl.YYEOF) {
	    return null;
	}

        if (scanner.yylength() <= maxTokenLength) {
          reusableToken.clear();
          reusableToken.setPositionIncrement(posIncr);
          scanner.getText(reusableToken);
          final int start = scanner.yychar();
          reusableToken.setStartOffset(start);
          reusableToken.setEndOffset(start+reusableToken.termLength());
          if (tokenType == StandardTokenizerImpl.ACRONYM_DEP) {
            if (replaceInvalidAcronym) {
              reusableToken.setType(StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.HOST]);
            } else {
              reusableToken.setType(StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.ACRONYM]);
            }
          } else {
            reusableToken.setType(StandardTokenizerImpl.TOKEN_TYPES[tokenType]);
          }
          return reusableToken;
        } else
          posIncr++;
      }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.lucene.analysis.TokenStream#reset()
     */
    public void reset() throws IOException {
	super.reset();
	scanner.yyreset(input);
    }

    public void reset(Reader reader) throws IOException {
        input = reader;
        reset();
    }

  /**
   * when they should have been labeled as hosts instead.
   * @return true if StandardTokenizer now returns these tokens as Hosts, otherwise false
   *
   * @deprecated Remove in 3.X and make true the only valid value
   */
  public boolean isReplaceInvalidAcronym() {
    return replaceInvalidAcronym;
  }

  /**
   *
   * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms as HOST.
   * @deprecated Remove in 3.X and make true the only valid value
   *
   */
  public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
  }
}
