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

package org.web3d.net.content;

// External imports
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import net.java.games.joal.ALConstants;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.ietf.uri.ContentHandler;
import org.ietf.uri.ResourceConnection;
import org.xj3d.io.StreamContentContainer;

// Local imports


/**
 * Content handler implementation for loading VRML Audio content from a URI
 * resource connection.
 * <P>
 *
 * The returned object type for this loader is an InputStream
 * <P>
 *
 * @author  Guy Carpenter
 * @version $Revision: 1.5 $
 */
class AudioContentHandler extends ContentHandler {

    /**
     * Construct a new instance of the content handler.
     *
     * @param browser The core representation of the browser
     * @param wlm Loader manager for doing async calls
     */
    AudioContentHandler() {
    }

    /**
     * Given a fresh stream from a ResourceConnection,
     * read and create an object instance.
     *
     * @param resc The resource connection to read the data from
     * @return The object read in by the content handler
     * @exception IOException The connection stuffed up.
     */
    public Object getContent(ResourceConnection resc)
        throws IOException {

        InputStream inputStream = resc.getInputStream();
        int format = -1;
        int freq = 0;
        int length = resc.getContentLength();

        BufferedInputStream bis = new BufferedInputStream(inputStream);

        try {
            AudioFormat fmt = AudioSystem.getAudioFileFormat(bis).getFormat();

            int numChannels = fmt.getChannels();
            int bits = fmt.getSampleSizeInBits();
            format = ALConstants.AL_FORMAT_MONO8;

            if ((bits == 8) && (numChannels == 1)) {
                format = ALConstants.AL_FORMAT_MONO8;
            } else if ((bits == 16) && (numChannels == 1)) {
                format = ALConstants.AL_FORMAT_MONO16;
            } else if ((bits == 8) && (numChannels == 2)) {
                format = ALConstants.AL_FORMAT_STEREO8;
            } else if ((bits == 16) && (numChannels == 2)) {
                format = ALConstants.AL_FORMAT_STEREO16;
            }

            freq = Math.round(fmt.getSampleRate());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return new StreamContentContainer(bis, length, format, freq);
    }
}
