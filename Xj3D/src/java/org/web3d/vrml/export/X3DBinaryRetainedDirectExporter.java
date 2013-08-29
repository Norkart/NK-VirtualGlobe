/****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.export;

// External imports
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.web3d.parser.x3d.*;
import org.xml.sax.SAXException;

import com.sun.xml.fastinfoset.sax.SAXDocumentSerializer;
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.algorithm.FloatEncodingAlgorithm;

import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.fastinfoset.sax.Properties;
import com.sun.xml.fastinfoset.sax.AttributesHolder;
import com.sun.xml.fastinfoset.algorithm.*;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.proto.*;
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;

import org.xj3d.core.eventmodel.*;
import org.xj3d.core.loading.*;

import org.web3d.vrml.export.compressors.*;

import org.web3d.util.IntHashMap;
import org.web3d.util.XMLTools;
import org.web3d.vrml.renderer.CRExternPrototypeDecl;
import org.web3d.vrml.renderer.CRProtoInstance;
import org.web3d.vrml.renderer.common.nodes.AbstractDynamicFieldNode;

/**
 * X3D binary exporter using a retained Scenegraph.
 * This will directly write to X3D binary instead of
 * reusing the XML code to generate an XML file in memory.
 *
 * Known Issues:
 *
 *    Proto node fields are copied into instances
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public class X3DBinaryRetainedDirectExporter extends X3DRetainedSAXExporter
    implements SceneGraphTraversalSimpleObserver {

    // Smallest float difference for equality
    private static final float FLOAT_EPS = 0.0000009f;

    // Largest acceptable error for float quantization
    private static float PARAM_FLOAT_LOSSY = 0.001f;

    protected static final int BYTE_ALGORITHM_ID = 32;
    protected static final int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;
    protected static final int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID = 34;
    protected static final String EXTERNAL_VOCABULARY_URI_STRING = "urn:external-vocabulary";

    /** The output stream to write to */
    private OutputStream os;

    /**
     * Create a new exporter for the given spec version
     *
     * @param os The stream to export the code to
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param errorReporter The error reporter to use
     */
    public X3DBinaryRetainedDirectExporter(OutputStream os, int major, int minor,
        ErrorReporter errorReporter, int compressionMethod, float quantizeParam) {

        super(major, minor, errorReporter, compressionMethod, quantizeParam);

        this.os = os;
        binary = true;

/*      Node Compressors don't work yet
        if (compressionMethod == X3DBinarySerializer.METHOD_SMALLEST_LOSSY) {
            useNC = true;
        }
*/

        encodingTo = ".x3db";
        printDocType = false;

        stripWhitespace = false;

        // Setup Fast InfoSet

        SAXDocumentSerializer serializer = new SAXDocumentSerializer();

        //  TODO: We'd like to catch most DEF name limits
        serializer.setAttributeValueSizeLimit(32);
        serializer.setOutputStream(os);

        SerializerVocabulary initialVocabulary = new SerializerVocabulary();

        // TODO: Does this do a copy?
        initialVocabulary.setExternalVocabulary(EXTERNAL_VOCABULARY_URI_STRING,
                X3DBinaryVocabulary.serializerVoc, false);

        serializer.setVocabulary(initialVocabulary);

        Map algorithms = new HashMap();
        algorithms.put(ByteEncodingAlgorithm.ALGORITHM_URI, new ByteEncodingAlgorithm());
        algorithms.put(DeltazlibIntArrayAlgorithm.ALGORITHM_URI, new DeltazlibIntArrayAlgorithm());
        if (compressionMethod == METHOD_SMALLEST_LOSSY) {
            // Only global control available currently
            algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                new QuantizedzlibFloatArrayAlgorithm(PARAM_FLOAT_LOSSY));
        } else {
            // Default is no loss
            algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm());
        }
        serializer.setRegisteredEncodingAlgorithms(algorithms);

        handler = serializer;
    }

    /**
     * Encode double array data.
     * This base version will just use a string rep.
     *
     * @param fval The parsed double array value
     * @param numElements The number of elements per item
     * @param decl The field declaration.
     * @param qName The qualified name
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    protected void encodeDoubleArray(double[] fval, int numElements, VRMLFieldDeclaration decl,
        String qName, AttributesHolder aholder, int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            switch(compressionMethod) {
                case METHOD_STRINGS:
                    String st = createX3DString(decl, fval, numElements);

                    aholder.addAttribute(new QualifiedName("", "",
                        qName), st);
                    break;
                case METHOD_SMALLEST_LOSSY:
                case METHOD_SMALLEST_NONLOSSY:
                default:
                    clen = BuiltInEncodingAlgorithmFactory.doubleEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                    String st2 = createX3DString(decl, fval, numElements);

                    if (clen <= st2.length()) {
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", qName),
                            null,
                            EncodingAlgorithmIndexes.DOUBLE,
                            fval);
                    } else {
                        aholder.addAttribute(new QualifiedName("", "",
                            qName), st2);
                    }
                    break;
            }
        }
    }

    /**
     * Encode float array data.
     * This base version will just use a string rep
     *
     * @param fval The parsed float array value
     * @param numElements The number of elements per item
     * @param decl The field declaration.
     * @param qName The qualified name
     * @param aholder The current attributes holder
     * @param ftype The X3D field type, defined in FieldConstants
     */
    protected void encodeFloatArray(float[] fval, int numElements, VRMLFieldDeclaration decl,
        String qName, AttributesHolder aholder, int ftype) {

        int clen;

        if (fval != null && fval.length != 0) {
            switch(compressionMethod) {
                case METHOD_SMALLEST_LOSSY:
                    aholder.addAttributeWithAlgorithmData(
                        new QualifiedName("", "", qName),
                        QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                        QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                        fval);
                    break;
                case METHOD_SMALLEST_NONLOSSY:
                    aholder.addAttributeWithAlgorithmData(
                        new QualifiedName("", "", qName),
                        QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
                        QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID,
                        fval);
                    break;
                case METHOD_STRINGS:
                    String st = createX3DString(decl, fval, numElements);

                    aholder.addAttribute(new QualifiedName("", "",
                        qName), st);
                    break;
                default:
                    clen = BuiltInEncodingAlgorithmFactory.floatEncodingAlgorithm.getOctetLengthFromPrimitiveLength(fval.length);

                    String st2 = createX3DString(decl, fval, numElements);

                    if (clen <= st2.length()) {
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", qName),
                            null,
                            EncodingAlgorithmIndexes.FLOAT,
                            fval);
                    } else {
                        aholder.addAttribute(new QualifiedName("", "",
                            qName), st2);
                    }
                    break;
            }
        }
    }
}
