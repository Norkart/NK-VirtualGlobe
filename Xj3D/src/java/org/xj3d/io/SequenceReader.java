/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.io;

// External imports
import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Local imports
// None

/**
 * Represents the logical concatenation of other readers.
 * <p>
 * The standard java.io package does not provide this capability so this
 * class does the same as {@link java.io.SequenceInputStream} except for
 * {@link java.io.Reader} derived classes.
 * <p>
 * It starts out with an ordered collection of input streams and reads from
 * the first one until end of file is reached,whereupon it reads from the
 * second one, and so on, until end of file is reached
 * on the last of the contained input streams.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class SequenceReader extends Reader {

    /** The list of current readers */
    private Iterator readerList;

    /** The reader that we are getting bytes from */
    private Reader currentReader;

    /**
     * Initializes a newly created <code>SequenceReader</code> with the list of
     * be a {@link java.util.List} that produces objects whose run-time type is
     * {@link java.io.Reader}. The readers that are  produced by the list will
     * be read in order defined in the list to provide the bytes to be read from
     * this <code>SequenceReader</code>. After each input stream from the list
     * is exhausted, it is closed by calling its <code>close()</code> method.
     *
     * @param readers A list of input streams to process
     * @see java.util.List
     */
    public SequenceReader(List readers) {
        readerList = readers.iterator();

        try {
            nextStream();
        } catch (IOException ex) {
            // This should never happen
            throw new Error("panic");
        }
    }

    /**
     * Initializes a newly created <code>SequenceReader</code> by remembering
     * the two arguments, which will be read as s1 then s2 order to provide the
     * bytes to be read from this <code>SequenceReader</code>.
     *
     * @param s1 The first input stream to read.
     * @param s2 The second input stream to read.
     */
    public SequenceReader(Reader s1, Reader s2) {
        ArrayList list = new ArrayList(2);
        list.add(s1);
        list.add(s2);
        readerList = list.iterator();

        try {
            nextStream();
        } catch (IOException ex) {
            // This should never happen
            throw new Error("panic");
        }
    }

    //----------------------------------------------------------
    // Methods defined by Reader
    //----------------------------------------------------------

    /**
     * Reads the next char of data from this input stream. If no char is
     * available because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data is
     * available, the end of the stream is detected, or an exception is thrown.
     * <p>
     * This method tries to read one character from the current substream. If
     * it reaches the end of the stream, it calls the <code>close</code>
     * method of the current substream and begins reading from the next
     * substream.
     *
     * @return The next char of data, or <code>-1</code> if the end of the
     *    stream is reached.
     * @exception IOException  if an I/O error occurs.
     */
    public int read() throws IOException {
        if (currentReader == null) {
            return -1;
        }

        int c = currentReader.read();

        if (c == -1) {
            nextStream();
            return read();
        }

        return c;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. This method blocks until at least 1 byte
     * of input is available. If the first argument is <code>null</code>,
     * up to <code>len</code> bytes are read and discarded.
     * <p>
     * The <code>read</code> method of <code>SequenceReader</code>
     * tries to read the data from the current substream. If it fails to
     * read any characters because the substream has reached the end of
     * the stream, it calls the <code>close</code> method of the current
     * substream and begins reading from the next substream.
     *
     * @param b   The buffer into which the data is read.
     * @param off The start offset of the data.
     * @param len The maximum number of bytes read.
     * @return The number of bytes read.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(char b[], int off, int len) throws IOException {
        if (currentReader == null) {
            return -1;
        } else if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int n = currentReader.read(b, off, len);
        if (n <= 0) {
            nextStream();
            return read(b, off, len);
        }
        return n;
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream. A closed <code>SequenceReader</code>
     * cannot  perform input operations and cannot be reopened.
     * <p>
     * If this stream was created from an list, all remaining elements
     * are requested from the list and closed before the <code>close</code>
     * method returns. of <code>Reader</code> .
     *
     * @exception IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        do {
            nextStream();
        } while (currentReader != null);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     *  Continues reading currentReader the next stream if an EOF is reached.
     */
    private final void nextStream() throws IOException {
        if (currentReader != null) {
            currentReader.close();
        }

        if (readerList.hasNext()) {
            currentReader = (Reader)readerList.next();

            if (currentReader == null)
                throw new NullPointerException();
        } else
            currentReader = null;
    }
}
