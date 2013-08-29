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


package org.xj3d.io;

// External imports
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

// Local imports
// None

/**
 * This is a utility class used by content handlers to return an input stream
 * to nodes that require streamed input.
 * <p>
 *
 * Content handlers return objects that are reused by nodes in the scene. This
 * object type can be used to pass an InputStream to the nodes.  Each client
 * will request a new input stream using getInputStream.
 *
 * @author Guy Carpenter
 * @version $Revision: 1.1 $
 */
public class StreamContentContainer {

    /** internal byte-copy of the original input stream. */
    private byte[] buffer;

    /** The format */
    private int format;

    /** The frequency */
    private int freq;

    /**
     * Construct a new content container from an input stream.
     * The content container is a copy of the entire byte stream.
     * Calls to getInputStream will return new InputStreams which
     * are byte-copies of the original input stream.
     * <P>
     * If length bytes cannot be read from the input stream, an IOException
     * will be thrown.
     *
     * @param inputStream input stream to read data from
     * @param length number of bytes of data to read from the input stream
     * @param format The format, defined in ALConstants
     * @param frequency The frequency
     * @throws IOException either too few bytes were read or read failed
     */
    public StreamContentContainer(InputStream inputStream,
                                  int length,
                                  int format,
                                  int frequency)
        throws IOException {

        buffer = new byte[length];
        this.format = format;
        freq = frequency;

        int want = length;
        int have = 0;
        while (want>0) {
            int bytesRead = inputStream.read(buffer, have, want);
            if (bytesRead<1) {
                throw new IOException
                    ("Expected "+length+" bytes, read only "+bytesRead+" bytes");
            }
            have += bytesRead;
            want -= bytesRead;
        }
    }

    /**
     * Returns an input stream associated with this resource.
     * If the resource is cachable (canCache returns true)
     * this method may be called more than once, and each
     * call will return a new stream.
     *
     * @return Returns a new InputStream which will return same byte-stream as original InputStream.
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(buffer);
    }

    /**
     * Returns a direct reference to the underlying buffer.
     *
     * @return The underlying buffer.
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Get the format.  Defined in OpenAL.ALConstants.
     *
     * @return The format of the audio data
     */
    public int getFormat() {
        return format;
    }

    /**
     * Get the frequency of the content.
     *
     * @return The frequency.
     */
    public int getFrequency() {
        return freq;
    }
}
