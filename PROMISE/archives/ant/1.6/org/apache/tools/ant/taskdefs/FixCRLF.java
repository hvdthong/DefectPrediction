package org.apache.tools.ant.taskdefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileUtils;

/**
 * Converts text source files to local OS formatting conventions, as
 * well as repair text files damaged by misconfigured or misguided editors or
 * file transfer programs.
 * <p>
 * This task can take the following arguments:
 * <ul>
 * <li>srcdir
 * <li>destdir
 * <li>include
 * <li>exclude
 * <li>cr
 * <li>eol
 * <li>tab
 * <li>eof
 * <li>encoding
 * </ul>
 * Of these arguments, only <b>sourcedir</b> is required.
 * <p>
 * When this task executes, it will scan the srcdir based on the include
 * and exclude properties.
 * <p>
 * This version generalises the handling of EOL characters, and allows
 * for CR-only line endings (which I suspect is the standard on Macs.)
 * Tab handling has also been generalised to accommodate any tabwidth
 * from 2 to 80, inclusive.  Importantly, it will leave untouched any
 * literal TAB characters embedded within string or character constants.
 * <p>
 * <em>Warning:</em> do not run on binary files.
 * <em>Caution:</em> run with care on carefully formatted files.
 * This may sound obvious, but if you don't specify asis, presume that
 * your files are going to be modified.  If "tabs" is "add" or "remove",
 * whitespace characters may be added or removed as necessary.  Similarly,
 * for CR's - in fact "eol"="crlf" or cr="add" can result in cr
 * characters being removed in one special case accommodated, i.e.,
 * CRCRLF is regarded as a single EOL to handle cases where other
 * programs have converted CRLF into CRCRLF.
 *
 * @since Ant 1.1
 *
 * @ant.task category="filesystem"
 */

public class FixCRLF extends MatchingTask {

    private static final int UNDEF = -1;
    private static final int NOTJAVA = 0;
    private static final int LOOKING = 1;
    private static final int IN_CHAR_CONST = 2;
    private static final int IN_STR_CONST = 3;
    private static final int IN_SINGLE_COMMENT = 4;
    private static final int IN_MULTI_COMMENT = 5;

    private static final int ASIS = 0;
    private static final int CR = 1;
    private static final int LF = 2;
    private static final int CRLF = 3;
    private static final int ADD = 1;
    private static final int REMOVE = -1;
    private static final int SPACES = -1;
    private static final int TABS = 1;

    private static final int INBUFLEN = 8192;
    private static final int LINEBUFLEN = 200;

    private static final char CTRLZ = '\u001A';

    private int tablength = 8;
    private String spaces = "        ";
    private StringBuffer linebuf = new StringBuffer(1024);
    private StringBuffer linebuf2 = new StringBuffer(1024);
    private int eol;
    private String eolstr;
    private int ctrlz;
    private int tabs;
    private boolean javafiles = false;
    private boolean fixlast = true;

    private File srcDir;
    private File destDir = null;

    private FileUtils fileUtils = FileUtils.newFileUtils();

    /**
     * Encoding to assume for the files
     */
    private String encoding = null;

    /**
     * Defaults the properties based on the system type.
     * <ul><li>Unix: eol="LF" tab="asis" eof="remove"
     *     <li>Mac: eol="CR" tab="asis" eof="remove"
     *     <li>DOS: eol="CRLF" tab="asis" eof="asis"</ul>
     */
    public FixCRLF () {
        tabs = ASIS;
        if (Os.isFamily("mac")) {
            ctrlz = REMOVE;
            eol = CR;
            eolstr = "\r";
        } else if (Os.isFamily("dos")) {
            ctrlz = ASIS;
            eol = CRLF;
            eolstr = "\r\n";
        } else {
            ctrlz = REMOVE;
            eol = LF;
            eolstr = "\n";
        }
    }

    /**
     * Set the source dir to find the source text files.
     */
    public void setSrcdir(File srcDir) {
        this.srcDir = srcDir;
    }

    /**
     * Set the destination where the fixed files should be placed.
     * Default is to replace the original file.
     */
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Set to true if modifying Java source files.
     */
    public void setJavafiles(boolean javafiles) {
        this.javafiles = javafiles;
    }


    /**
     * Specify how EndOfLine characters are to be handled.
     *
     * @param attr valid values:
     * <ul>
     * <li>asis: leave line endings alone
     * <li>cr: convert line endings to CR
     * <li>lf: convert line endings to LF
     * <li>crlf: convert line endings to CRLF
     * </ul>
     */
    public void setEol(CrLf attr) {
        String option = attr.getValue();
        if (option.equals("asis")) {
            eol = ASIS;
        } else if (option.equals("cr") || option.equals("mac")) {
            eol = CR;
            eolstr = "\r";
        } else if (option.equals("lf") || option.equals("unix")) {
            eol = LF;
            eolstr = "\n";
        } else {
            eol = CRLF;
            eolstr = "\r\n";
        }
    }

    /**
     * Specify how carriage return (CR) characters are to be handled.
     *
     * @param attr valid values:
     * <ul>
     * <li>add: ensure that there is a CR before every LF
     * <li>asis: leave CR characters alone
     * <li>remove: remove all CR characters
     * </ul>
     *
     * @deprecated use {@link #setEol setEol} instead.
     */
    public void setCr(AddAsisRemove attr) {
        log("DEPRECATED: The cr attribute has been deprecated,",
            Project.MSG_WARN);
        log("Please use the eol attribute instead", Project.MSG_WARN);
        String option = attr.getValue();
        CrLf c = new CrLf();
        if (option.equals("remove")) {
            c.setValue("lf");
        } else if (option.equals("asis")) {
            c.setValue("asis");
        } else {
            c.setValue("crlf");
        }
        setEol(c);
    }

    /**
     * Specify how tab characters are to be handled.
     *
     * @param attr valid values:
     * <ul>
     * <li>add: convert sequences of spaces which span a tab stop to tabs
     * <li>asis: leave tab and space characters alone
     * <li>remove: convert tabs to spaces
     * </ul>
     */
    public void setTab(AddAsisRemove attr) {
        String option = attr.getValue();
        if (option.equals("remove")) {
            tabs = SPACES;
        } else if (option.equals("asis")) {
            tabs = ASIS;
        } else {
            tabs = TABS;
        }
    }

    /**
     * Specify tab length in characters.
     *
     * @param tlength specify the length of tab in spaces,
     */
    public void setTablength(int tlength) throws BuildException {
        if (tlength < 2 || tlength > 80) {
            throw new BuildException("tablength must be between 2 and 80",
                                     getLocation());
        }
        tablength = tlength;
        StringBuffer sp = new StringBuffer();
        for (int i = 0; i < tablength; i++) {
            sp.append(' ');
        }
        spaces = sp.toString();
    }

    /**
     * Specify how DOS EOF (control-z) characters are to be handled.
     *
     * @param attr valid values:
     * <ul>
     * <li>add: ensure that there is an eof at the end of the file
     * <li>asis: leave eof characters alone
     * <li>remove: remove any eof character found at the end
     * </ul>
     */
    public void setEof(AddAsisRemove attr) {
        String option = attr.getValue();
        if (option.equals("remove")) {
            ctrlz = REMOVE;
        } else if (option.equals("asis")) {
            ctrlz = ASIS;
        } else {
            ctrlz = ADD;
        }
    }

    /**
     * Specifies the encoding Ant expects the files to be in -
     * defaults to the platforms default encoding.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Specify whether a missing EOL will be added
     * to the final line of a file.
     */
    public void setFixlast(boolean fixlast) {
        this.fixlast = fixlast;
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException {

        if (srcDir == null) {
            throw new BuildException("srcdir attribute must be set!");
        }
        if (!srcDir.exists()) {
            throw new BuildException("srcdir does not exist!");
        }
        if (!srcDir.isDirectory()) {
            throw new BuildException("srcdir is not a directory!");
        }
        if (destDir != null) {
            if (!destDir.exists()) {
                throw new BuildException("destdir does not exist!");
            }
            if (!destDir.isDirectory()) {
                throw new BuildException("destdir is not a directory!");
            }
        }

        log("options:"
            + " eol="
            + (eol == ASIS ? "asis" : eol == CR ? "cr" : eol == LF ? "lf" : "crlf")
            + " tab=" + (tabs == TABS ? "add" : tabs == ASIS ? "asis" : "remove")
            + " eof=" + (ctrlz == ADD ? "add" : ctrlz == ASIS ? "asis" : "remove")
            + " tablength=" + tablength
            + " encoding=" + (encoding == null ? "default" : encoding),
            Project.MSG_VERBOSE);

        DirectoryScanner ds = super.getDirectoryScanner(srcDir);
        String[] files = ds.getIncludedFiles();

        for (int i = 0; i < files.length; i++) {
            processFile(files[i]);
        }
    }

    /**
     * Creates a Reader reading from a given file an taking the user
     * defined encoding into account.
     */
    private Reader getReader(File f) throws IOException {
        return (encoding == null) ? new FileReader(f)
            : new InputStreamReader(new FileInputStream(f), encoding);
    }


    private void processFile(String file) throws BuildException {
        File srcFile = new File(srcDir, file);
        File destD = destDir == null ? srcDir : destDir;
        File tmpFile = null;
        BufferedWriter outWriter;
        OneLiner.BufferLine line;

        OneLiner lines = new OneLiner(srcFile);

        try {
            try {
                tmpFile = fileUtils.createTempFile("fixcrlf", "", null);
                tmpFile.deleteOnExit();
                Writer writer = (encoding == null) ? new FileWriter(tmpFile)
                    : new OutputStreamWriter(new FileOutputStream(tmpFile),
                                             encoding);
                outWriter = new BufferedWriter(writer);
            } catch (IOException e) {
                throw new BuildException(e);
            }

            while (lines.hasMoreElements()) {
                int endComment;

                try {
                    line = (OneLiner.BufferLine) lines.nextElement();
                } catch (NoSuchElementException e) {
                    throw new BuildException(e);
                }

                String lineString = line.getLineString();
                int linelen = line.length();


                if (tabs == ASIS) {
                    try {
                        outWriter.write(lineString);
                    } catch (IOException e) {
                        throw new BuildException(e);


                    while (line.getNext() < linelen) {

                        switch (lines.getState()) {

                        case NOTJAVA:
                            notInConstant(line, line.length(), outWriter);
                            break;

                        case IN_MULTI_COMMENT:
                            endComment
                                = lineString.indexOf("*/", line.getNext());
                            if (endComment >= 0) {
                                lines.setState(LOOKING);
                            } else {
                                endComment = linelen;
                            }

                            notInConstant(line, endComment, outWriter);
                            break;

                        case IN_SINGLE_COMMENT:
                            notInConstant(line, line.length(), outWriter);
                            lines.setState(LOOKING);
                            break;

                        case IN_CHAR_CONST:
                        case IN_STR_CONST:

                            int begin = line.getNext();
                            char terminator = (lines.getState() == IN_STR_CONST
                                               ? '\"'
                                               : '\'');
                            endOfCharConst(line, terminator);
                            while (line.getNext() < line.getLookahead()) {
                                if (line.getNextCharInc() == '\t') {
                                    line.setColumn(line.getColumn()
                                        + tablength
                                        - (line.getColumn() % tablength));
                                } else {
                                    line.incColumn();
                                }
                            }

                            try {
                                outWriter.write(line.substring(begin,
                                                               line.getNext()));
                            } catch (IOException e) {
                                throw new BuildException(e);
                            }

                            lines.setState(LOOKING);

                            break;


                        case LOOKING:
                            nextStateChange(line);
                            notInConstant(line, line.getLookahead(), outWriter);
                            break;




                if (!("".equals(line.getEol())) || fixlast) {
                    try {
                        outWriter.write(eolstr);
                    } catch (IOException e) {
                        throw new BuildException(e);


            try {
                if (ctrlz == ASIS) {
                    outWriter.write(lines.getEofStr());
                } else if (ctrlz == ADD) {
                    outWriter.write(CTRLZ);
                }
            } catch (IOException e) {
                throw new BuildException(e);
            } finally {
                try {
                    outWriter.close();
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }


            try {
                lines.close();
                lines = null;
            } catch (IOException e) {
                throw new BuildException("Unable to close source file "
                                         + srcFile);
            }

            File destFile = new File(destD, file);

            boolean destIsWrong = true;
            if (destFile.exists()) {
                log("destFile exists", Project.MSG_DEBUG);
                if (!fileUtils.contentEquals(destFile, tmpFile)) {
                    log(destFile + " is being written", Project.MSG_DEBUG);
                } else {
                    log(destFile + " is not written, as the contents "
                        + "are identical", Project.MSG_DEBUG);
                    destIsWrong = false;
                }
            }

            if (destIsWrong) {
                fileUtils.rename(tmpFile, destFile);
                tmpFile = null;
            }

        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            try {
                if (lines != null) {
                    lines.close();
                }
            } catch (IOException io) {
                log("Error closing " + srcFile, Project.MSG_ERR);

            if (tmpFile != null) {
                tmpFile.delete();
            }
    }

    /**
     * Scan a BufferLine for the next state changing token: the beginning
     * of a single or multi-line comment, a character or a string constant.
     *
     * As a side-effect, sets the buffer state to the next state, and sets
     * field lookahead to the first character of the state-changing token, or
     * to the next eol character.
     *
     * @param bufline       BufferLine containing the string
     *                                 to be processed
     * @exception org.apache.tools.ant.BuildException
     *                                 Thrown when end of line is reached
     *                                 before the terminator is found.
     */
    private void nextStateChange(OneLiner.BufferLine bufline)
        throws BuildException {
        int eol = bufline.length();
        int ptr = bufline.getNext();


        while (ptr < eol) {
            switch (bufline.getChar(ptr++)) {
            case '\'':
                bufline.setState(IN_CHAR_CONST);
                bufline.setLookahead(--ptr);
                return;
            case '\"':
                bufline.setState(IN_STR_CONST);
                bufline.setLookahead(--ptr);
                return;
            case '/':
                if (ptr < eol) {
                    if (bufline.getChar(ptr) == '*') {
                        bufline.setState(IN_MULTI_COMMENT);
                        bufline.setLookahead(--ptr);
                        return;
                    } else if (bufline.getChar(ptr) == '/') {
                        bufline.setState(IN_SINGLE_COMMENT);
                        bufline.setLookahead(--ptr);
                        return;
                    }
                }
                break;

        bufline.setLookahead(ptr);
    }


    /**
     * Scan a BufferLine forward from the 'next' pointer
     * for the end of a character constant.  Set 'lookahead' pointer to the
     * character following the terminating quote.
     *
     * @param bufline       BufferLine containing the string
     *                                 to be processed
     * @param terminator          The constant terminator
     *
     * @exception org.apache.tools.ant.BuildException
     *                                 Thrown when end of line is reached
     *                                 before the terminator is found.
     */
    private void endOfCharConst(OneLiner.BufferLine bufline, char terminator)
        throws BuildException {
        int ptr = bufline.getNext();
        int eol = bufline.length();
        char c;
        while (ptr < eol) {
            if ((c = bufline.getChar(ptr++)) == '\\') {
                ptr++;
            } else {
                if (c == terminator) {
                    bufline.setLookahead(ptr);
                    return;
                }
            }
        throw new BuildException("endOfCharConst: unterminated char constant");
    }


    /**
     * Process a BufferLine string which is not part of a string constant.
     * The start position of the string is given by the 'next' field.
     * Sets the 'next' and 'column' fields in the BufferLine.
     *
     * @param bufline       BufferLine containing the string
     *                                 to be processed
     * @param end                  Index just past the end of the
     *                                 string
     * @param outWriter Sink for the processed string
     */
    private void notInConstant(OneLiner.BufferLine bufline, int end,
                                BufferedWriter outWriter) {
        int nextTab;
        int nextStop;
        int tabspaces;
        String line = bufline.substring(bufline.getNext(), end);

        linebuf = new StringBuffer();
        while ((nextTab = line.indexOf((int) '\t', place)) >= 0) {
            col += nextTab - place;
            tabspaces = tablength - (col % tablength);
            linebuf.append(spaces.substring(0, tabspaces));
            col += tabspaces;
            place = nextTab + 1;
        linebuf.append(line.substring(place, line.length()));
        String linestring = new String(linebuf.substring(0));
        if (tabs == REMOVE) {
            try {
                outWriter.write(linestring);
            } catch (IOException e) {
                throw new BuildException(e);
            int tabCol;
            linebuf2 = new StringBuffer();
            place = 0;
            col = bufline.getColumn();
            int placediff = col - 0;
            nextStop = col + (tablength - col % tablength);
            if (nextStop - col < 2) {
                linebuf2.append(linestring.substring(
                                        place, nextStop - placediff));
                place = nextStop - placediff;
                nextStop += tablength;
            }

            for (; nextStop - placediff <= linestring.length();
                    nextStop += tablength) {
                for (tabCol = nextStop;
                             --tabCol - placediff >= place
                             && linestring.charAt(tabCol - placediff) == ' ';) {
                }
                if (nextStop - tabCol > 2) {
                    linebuf2.append(linestring.substring(
                                    place, ++tabCol - placediff));
                    linebuf2.append('\t');
                } else {
                    linebuf2.append(linestring.substring(
                                    place, nextStop - placediff));

                place = nextStop - placediff;

            linebuf2.append(linestring.substring(place, linestring.length()));

            try {
                outWriter.write(linebuf2.substring(0));
            } catch (IOException e) {
                throw new BuildException(e);


        bufline.setColumn(bufline.getColumn() + linestring.length());
        bufline.setNext(end);

    }


    class OneLiner implements Enumeration {

        private int state = javafiles ? LOOKING : NOTJAVA;

        private StringBuffer eolStr = new StringBuffer(LINEBUFLEN);
        private StringBuffer eofStr = new StringBuffer();

        private BufferedReader reader;
        private StringBuffer line = new StringBuffer();
        private boolean reachedEof = false;
        private File srcFile;

        public OneLiner(File srcFile)
            throws BuildException {
            this.srcFile = srcFile;
            try {
                reader = new BufferedReader
                        (getReader(srcFile), INBUFLEN);
                nextLine();
            } catch (IOException e) {
                throw new BuildException(srcFile + ": " + e.getMessage(),
                                         e, getLocation());
            }
        }

        protected void nextLine()
            throws BuildException {
            int ch = -1;
            int eolcount = 0;

            eolStr = new StringBuffer();
            line = new StringBuffer();

            try {
                ch = reader.read();
                while (ch != -1 && ch != '\r' && ch != '\n') {
                    line.append((char) ch);
                    ch = reader.read();
                }

                if (ch == -1 && line.length() == 0) {
                    reachedEof = true;
                    return;
                }

                switch ((char) ch) {
                case '\r':
                    ++eolcount;
                    eolStr.append('\r');
                    reader.mark(2);
                    switch ((ch = reader.read())) {
                    case '\r':
                        if ((char) (ch = reader.read()) == '\n') {
                            eolcount += 2;
                            eolStr.append("\r\n");
                        } else {
                            reader.reset();
                        }
                        break;
                    case '\n':
                        ++eolcount;
                        eolStr.append('\n');
                        break;
                    case -1:
                        break;
                    default:
                        reader.reset();
                        break;
                    break;

                case '\n':
                    ++eolcount;
                    eolStr.append('\n');
                    break;


                if (eolcount == 0) {
                    int i = line.length();
                    while (--i >= 0 && line.charAt(i) == CTRLZ) {
                    }
                    if (i < line.length() - 1) {
                        eofStr.append(line.toString().substring(i + 1));
                        if (i < 0) {
                            line.setLength(0);
                            reachedEof = true;
                        } else {
                            line.setLength(i + 1);
                        }
                    }


            } catch (IOException e) {
                throw new BuildException(srcFile + ": " + e.getMessage(),
                                         e, getLocation());
            }
        }

        public String getEofStr() {
            return eofStr.substring(0);
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public boolean hasMoreElements() {
            return !reachedEof;
        }

        public Object nextElement()
            throws NoSuchElementException {
            if (!hasMoreElements()) {
                throw new NoSuchElementException("OneLiner");
            }
            BufferLine tmpLine =
                    new BufferLine(line.toString(), eolStr.substring(0));
            nextLine();
            return tmpLine;
        }

        public void close() throws IOException {
            if (reader != null) {
                reader.close();
            }
        }

        class BufferLine {
            private int next = 0;
            private int column = 0;
            private int lookahead = UNDEF;
            private String line;
            private String eolStr;

            public BufferLine(String line, String eolStr)
                throws BuildException {
                next = 0;
                column = 0;
                this.line = line;
                this.eolStr = eolStr;
            }

            public int getNext() {
                return next;
            }

            public void setNext(int next) {
                this.next = next;
            }

            public int getLookahead() {
                return lookahead;
            }

            public void setLookahead(int lookahead) {
                this.lookahead = lookahead;
            }

            public char getChar(int i) {
                return line.charAt(i);
            }

            public char getNextChar() {
                return getChar(next);
            }

            public char getNextCharInc() {
                return getChar(next++);
            }

            public int getColumn() {
                return column;
            }

            public void setColumn(int col) {
                column = col;
            }

            public int incColumn() {
                return column++;
            }

            public int length() {
                return line.length();
            }

            public int getEolLength() {
                return eolStr.length();
            }

            public String getLineString() {
                return line;
            }

            public String getEol() {
                return eolStr;
            }

            public String substring(int begin) {
                return line.substring(begin);
            }

            public String substring(int begin, int end) {
                return line.substring(begin, end);
            }

            public void setState(int state) {
                OneLiner.this.setState(state);
            }

            public int getState() {
                return OneLiner.this.getState();
            }
        }
    }

    /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     */
    public static class AddAsisRemove extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"add", "asis", "remove"};
        }
    }

    /**
     * Enumerated attribute with the values "asis", "cr", "lf" and "crlf".
     */
    public static class CrLf extends EnumeratedAttribute {
        /**
         * @see EnumeratedAttribute#getValues
         */
        public String[] getValues() {
            return new String[] {"asis", "cr", "lf", "crlf",
                                 "mac", "unix", "dos"};
        }
    }

}
