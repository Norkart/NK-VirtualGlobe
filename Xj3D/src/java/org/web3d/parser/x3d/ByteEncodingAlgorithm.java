/*
 * Fast Infoset ver. 0.1 software ("Software")
 *
 * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Software is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations.
 *
 *    Sun supports and benefits from the global community of open source
 * developers, and thanks the community for its important contributions and
 * open standards-based technology, which Sun has adopted into many of its
 * products.
 *
 *    Please note that portions of Software may be provided with notices and
 * open source licenses from such communities and third parties that govern the
 * use of those portions, and any licenses granted hereunder do not alter any
 * rights and obligations you may have under such open source licenses,
 * however, the disclaimer of warranty and limitation of liability provisions
 * in this License will apply to all Software in this distribution.
 *
 *    You acknowledge that the Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 */


package org.web3d.parser.x3d;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jvnet.fastinfoset.EncodingAlgorithmException;

import org.jvnet.fastinfoset.EncodingAlgorithm;

/**
 * An encoder for handling Byte values.
 *
 * @author Alan Hudson
 * @version
 */
public class ByteEncodingAlgorithm implements EncodingAlgorithm {
    /** The URI to use for FI tables */
    public static final String ALGORITHM_URI = "http://www.web3d.org/binary/BYTE";

    protected final static Pattern SPACE_PATTERN = Pattern.compile("\\s");
    public final static int BYTE_SIZE    = 1;

    public final int getPrimtiveLengthFromOctetLength(int octetLength) throws EncodingAlgorithmException {
        if (octetLength % BYTE_SIZE != 0) {
            throw new EncodingAlgorithmException("'length' is not a multiple of " +
                    BYTE_SIZE +
                    " bytes correspond to the size of the 'byte' primitive type");
        }

        return octetLength / BYTE_SIZE;
    }

    public int getOctetLengthFromPrimitiveLength(int primitiveLength) {
        return primitiveLength * BYTE_SIZE;
    }

    public final Object decodeFromBytes(byte[] b, int start, int length) throws EncodingAlgorithmException {
        byte[] data = new byte[getPrimtiveLengthFromOctetLength(length)];
        decodeFromBytesToByteArray(data, 0, b, start, length);

        return data;
    }

    public final Object decodeFromInputStream(InputStream s) throws IOException {
        return decodeFromInputStreamToByteArray(s);
    }


    public void encodeToOutputStream(Object data, OutputStream s) throws IOException {
        if (!(data instanceof byte[])) {
            throw new IllegalArgumentException("'data' not an instance of byte[]");
        }

        final byte[] idata = (byte[])data;

        encodeToOutputStreamFromByteArray(idata, s);
    }


    public final Object convertFromCharacters(char[] ch, int start, int length) {
        final CharBuffer cb = CharBuffer.wrap(ch, start, length);
        final List byteList = new ArrayList();

        matchWhiteSpaceDelimnatedWords(cb,
                new WordListener() {
            public void word(int start, int end) {
                String iStringValue = cb.subSequence(start, end).toString();
                byteList.add(Byte.valueOf(iStringValue));
            }
        }
        );

        return generateArrayFromList(byteList);
    }

    public final void convertToCharacters(Object data, StringBuffer s) {
        if (!(data instanceof byte[])) {
            throw new IllegalArgumentException("'data' not an instance of byte[]");
        }

        final byte[] idata = (byte[])data;

        convertToCharactersFromByteArray(idata, s);
    }


    public final void decodeFromBytesToByteArray(byte[] sdata, int istart, byte[] b, int start, int length) {
        final int size = length / BYTE_SIZE;
        for (int i = 0; i < size; i++) {
//            sdata[istart++] = (byte) (b[start++] & 0xFF));
            sdata[istart++] = (byte) (b[start++]);
        }
    }

    public final byte[] decodeFromInputStreamToByteArray(InputStream s) throws IOException {
        final List byteList = new ArrayList();
        final byte[] b = new byte[BYTE_SIZE];

        while (true) {
            int n = s.read(b);
            if (n != 1) {
                if (n == -1) {
                    break;
                }

                while(n != 1) {
                    final int m = s.read(b, n, BYTE_SIZE - n);
                    if (m == -1) {
                        throw new EOFException();
                    }
                    n += m;
                }
            }

            final int i = (b[0] & 0xFF);
            byteList.add(new Byte((byte)i));
        }

        return generateArrayFromList(byteList);
    }


    public final void encodeToOutputStreamFromByteArray(byte[] idata, OutputStream s) throws IOException {
        for (int i = 0; i < idata.length; i++) {
            final int bits = idata[i];
//            s.write(bits & 0xFF);

            //TODO: not sure which one we should do
            s.write(bits);
        }
    }

    public final void encodeToBytes(Object array, int astart, int alength, byte[] b, int start) {
        encodeToBytesFromByteArray((byte[])array, astart, alength, b, start);
    }

    public final void encodeToBytesFromByteArray(byte[] sdata, int istart, int ilength, byte[] b, int start) {
        final int iend = istart + ilength;
        for (int i = istart; i < iend; i++) {
            final byte bits = sdata[i];
//            b[start++] = (byte)(bits & 0xFF);
            b[start++] = (byte)(bits);
        }
    }


    public final void convertToCharactersFromByteArray(byte[] sdata, StringBuffer s) {
        for (int i = 0; i < sdata.length; i++) {
            s.append(Byte.toString(sdata[i]));
            if (i != sdata.length) {
                s.append(' ');
            }
        }
    }


    public final byte[] generateArrayFromList(List array) {
        byte[] sdata = new byte[array.size()];
        for (int i = 0; i < sdata.length; i++) {
            sdata[i] = ((Byte)array.get(i)).byteValue();
        }

        return sdata;
    }

    public interface WordListener {
        public void word(int start, int end);
    }

    public void matchWhiteSpaceDelimnatedWords(CharBuffer cb, WordListener wl) {
        Matcher m = SPACE_PATTERN.matcher(cb);
        int i = 0;
        while(m.find()) {
            int s = m.start();
            if (s != i) {
                wl.word(i, s);
            }
            i = m.end();
        }
    }
}
