/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.parser;

// Standard imports
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

// Application specific imports
// none

/**
 * General convenience utilities for handling parsing of VRML files.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class VRMLParserUtils {

    /**
     * Private constructor to prevent direct instantiation
     */
    private VRMLParserUtils() {
    }

    /**
     * Take the stream and strip the first line out of it to check that it is
     * a vrml file. If it is, return an array of the contents of the first
     * line. The array is arranged as:<br>
     * ret_val[0] = "#VRML" or "#X3D" <br>
     * ret_val[1] = version string ("V2.0", "V3.0" etc)<br>
     * ret_val[2] = encoding type ("utf8" usually)<br>
     * ret_val[3] = The rest of the line - a comment
     * <p>
     * If the first two characters are not "#V" then this will terminate and
     * not read the characters any further. The first two characters will be
     * returned as a string in the first item and only in the array. The items
     * in the array are built based on whitespace separators. That is, we don't
     * explicitly look for these strings, but rather just give the next
     * whitespace separated token. If we run out of tokens then any remaining
     * items in the array are set to null.
     *
     * @param stream The input stream to source characters from.
     * @return An array of the items of the header
     * @throws IOException Something happened while reading the stream
     */
    public static String[] parseFileHeader(InputStream stream)
        throws IOException {

        char[] ch = new char[2];
        String[] ret_val = null;

        ch[0] = (char)stream.read();
        ch[1] = (char)stream.read();

        if((ch[0] != '#') || ((ch[1] != 'V') && (ch[1] != 'X'))) {
            ret_val = new String[1];
            ret_val[0] = new String(ch);
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append(ch);
            char c;

            while(((c = (char)stream.read()) != '\n')  &&
                  (c != '\r') &&
                  (c != -1)) {
                buf.append(c);
            }

            ret_val = parseHeaderString(buf.toString());
        }

        return ret_val;
    }

    /**
     * Take the reader and strip the first line out of it to check that it is
     * a vrml file. If it is, return an array of the contents of the first
     * line. The output and assumptions are the same as the version of this
     * method that takes an InputStream.
     *
     * @param stream The input stream to source characters from.
     * @return An array of the items of the header
     * @throws IOException Something happened while reading the stream
     * @see #parseFileHeader(java.io.InputStream)
     */
    public static String[] parseFileHeader(Reader stream) throws IOException {
        char[] ch = new char[2];
        String[] ret_val = null;

        ch[0] = (char)stream.read();
        ch[1] = (char)stream.read();

        if((ch[0] != '#') || ((ch[1] != 'V') && (ch[1] != 'X'))) {
            ret_val = new String[1];
            ret_val[0] = new String(ch);
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append(ch);
            char c;

            while(((c = (char)stream.read()) != '\n')  &&
                  (c != '\r') &&
                  (c != -1)) {
                buf.append(c);
            }

            ret_val = parseHeaderString(buf.toString());
        }

        return ret_val;
    }

    /**
     * From the given string, parse it and return the values.
     *
     * @param stream The input stream to source characters from.
     * @return An array of the items of the header
     */
    private static String[] parseHeaderString(String line) throws IOException {
        String[] ret_val = new String[4];

        // just to make sure
        ret_val[0] = null;
        ret_val[1] = null;
        ret_val[2] = null;
        ret_val[3] = null;

        StringTokenizer strtok = new StringTokenizer(line);

        try {
            ret_val[0] = strtok.nextToken();
            ret_val[1] = strtok.nextToken();
            ret_val[2] = strtok.nextToken();
            ret_val[3] = strtok.nextToken(""); // want the rest of the string
        } catch(NoSuchElementException nsee) {
            // ignore it and just leave them all set to null
        }

        return ret_val;
    }
}
