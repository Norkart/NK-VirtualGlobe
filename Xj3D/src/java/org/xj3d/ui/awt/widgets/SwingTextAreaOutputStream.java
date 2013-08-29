/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.io.*;

import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

// Local imports
// None

/**
 * An output stream that redirects to a text area.
 * <p>
 *
 * Typically this class is used to redirect <code>System.out</code>
 * and <code>System.err</code>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class SwingTextAreaOutputStream extends OutputStream {

    /** The text area that we're listening to */
    private JTextArea textArea;

    /** The label of the stream to append to the output message */
    private String name;

    /**
     * Create a new stream instance that uses the given title and
     * writes to the text area.
     *
     * @param title The title that could be used, may be null
     * @param ta The text area to dump the output to
     */
    public SwingTextAreaOutputStream(String title, JTextArea ta) {
        name = title;
        textArea = ta;
    }

    //----------------------------------------------------------
    // Methods defined by OutputStream
    //----------------------------------------------------------

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    public void write(int b) throws IOException {

        textArea.append(String.valueOf((char) b));

        autoScroll();
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * <p>
     * The <code>write</code> method of <code>OutputStream</code> calls
     * the write method of one argument on each of the bytes to be
     * written out. Subclasses are encouraged to override this method and
     * provide a more efficient implementation.
     * <p>
     * If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (len > 2)
            textArea.append(name);

        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Autoscroll the console window so the latest message is visible.
     */
    private void autoScroll() {
        try {
            int length = textArea.getLineCount();

            Rectangle rect = textArea.modelToView(length - 1);

            if (rect != null) {
                rect.y = length * rect.height;
                textArea.scrollRectToVisible(rect);
            }
        } catch(BadLocationException ble) {
            System.out.println("Bad location in console window");
        }

    }

}
