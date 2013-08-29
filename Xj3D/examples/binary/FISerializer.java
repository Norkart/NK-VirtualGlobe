/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

// External imports
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.net.URI;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.sax.SAXResult;
import com.sun.xml.fastinfoset.sax.SAXDocumentSerializer;
import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.algorithm.FloatEncodingAlgorithm;
import org.jvnet.fastinfoset.sax.PrimitiveTypeContentHandler;
import org.jvnet.fastinfoset.EncodingAlgorithmIndexes;
import org.jvnet.fastinfoset.sax.EncodingAlgorithmAttributes;
import org.jvnet.fastinfoset.sax.FastInfosetDefaultHandler;

import org.xml.sax.*;

import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.fastinfoset.sax.Properties;
import com.sun.xml.fastinfoset.sax.AttributesHolder;
import com.sun.xml.fastinfoset.algorithm.FloatEncodingAlgorithm;

import com.sun.xml.fastinfoset.algorithm.BuiltInEncodingAlgorithmFactory;

/**
 *  Serializes an XML encoded document using FastInfoset.
 *  Tries to determine the type of data and store in the least bits.
 *
 * TODO: Stats collection for non builtins isn't written
 *
 * @author Alan Hudson
 * @version
 */
public class FISerializer {
    // Encode files for fastest parsing
    public static final int METHOD_FASTEST_PARSING = 0;

    // Encode files for smallest size using non lossy techniques
    public static final int METHOD_SMALLEST_NONLOSSY = 1;

    // Encode files for smallest size using lossy techniques
    public static final int METHOD_SMALLEST_LOSSY = 2;

    // Encode files as strings
    public static final int METHOD_STRINGS = 3;

    // Smallest float difference for equality
    private static final float FLOAT_EPS = 0.0000009f;

    // Largest acceptable error for float quantization
    private static float PARAM_FLOAT_LOSSY = 0.000001f;

    // Usage docs
    private static final String USAGE = "Usage:  FISerializer [options] <input> <output>\n" +
                                        "options:  -method [fastest, smallest, lossy]\n" +
                                        "          -quantizeParam n\n" +
                                        "          -savedefaults";
    protected static final int BYTE_ALGORITHM_ID = 32;
    protected static final int DELTA_ZLIB_INT_ARRAY_ALGORITHM_ID = 33;
    protected static final int QUANTIZED_ZLIB_FLOAT_ARRAY_ALGORITHM_ID = 34;
    protected static final String EXTERNAL_VOCABULARY_URI_STRING = "urn:external-vocabulary";

    private Transformer _transformer;
    private DocumentBuilder _docBuilder;
    private Source _source = null;
    private SAXResult _result = null;

    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    protected static final String LOAD_DTD_ID = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";

    /** Cache of nodes used for type information. NodeName --> Node*/
    private HashMap nodeCache;

    private boolean collectStats = true;

    /** What method should we use to compress */
    private int method;

    /** Should we remove defaults */
    private boolean removeDefaults;

    /** How many default values where removed */
    private int defaults;

    // Stats information
    private int[] types;
    private int strLen;
    private int totalValues;

    /**
     * Creates a new instance of FISerializer
     *
     * compMethod What
     */
    public FISerializer(int compMethod, boolean rd) {
        method = compMethod;
        removeDefaults = rd;

        types = new int[6];

        try {
            // get a transformer and document builder
            _transformer = TransformerFactory.newInstance().newTransformer();
            _docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void writeFiltered(File input, File output) {

        try {

            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(output));
            SAXDocumentSerializer serializer = new SAXDocumentSerializer();
            serializer.setOutputStream(fos);

            SerializerVocabulary externalVocabulary = new SerializerVocabulary();
//            externalVocabulary.encodingAlgorithm.add(ByteEncodingAlgorithm.ALGORITHM_URI);
//            externalVocabulary.encodingAlgorithm.add(DeltazlibIntArrayAlgorithm.ALGORITHM_URI);
//            externalVocabulary.encodingAlgorithm.add(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI);

            SerializerVocabulary initialVocabulary = new SerializerVocabulary();
            initialVocabulary.setExternalVocabulary(
                    new URI(EXTERNAL_VOCABULARY_URI_STRING),
                    externalVocabulary, false);

            serializer.setVocabulary(initialVocabulary);

            Map algorithms = new HashMap();
//            algorithms.put(ByteEncodingAlgorithm.ALGORITHM_URI, new ByteEncodingAlgorithm());
//            algorithms.put(DeltazlibIntArrayAlgorithm.ALGORITHM_URI, new DeltazlibIntArrayAlgorithm());
            if (method == METHOD_SMALLEST_LOSSY) {
                // Only global control available currently
//                algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI,
//                    new QuantizedzlibFloatArrayAlgorithm(PARAM_FLOAT_LOSSY));
            } else {
                // Default is no loss
//                algorithms.put(QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI, new QuantizedzlibFloatArrayAlgorithm());
            }
            serializer.setRegisteredEncodingAlgorithms(algorithms);

            // Obtain an instance of an XMLReader implementation
            // from a system property
            XMLReader
                parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();

            parser.setFeature(VALIDATION_FEATURE_ID, false);

            //parser.setFeature(LOAD_DTD_ID, false);

            //parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, false);

            // Create a new instance and register it with the parser
            ContentHandler contentHandler = new X3DFilter(serializer, method);
            parser.setContentHandler(contentHandler);

            FileInputStream fis = new FileInputStream(input);

            InputSource is = new InputSource(fis);
            // Don't worry about this for now -- we'll get to it later
            parser.parse(is);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /** Starts the sample
     * @param args XML input file name and FI output document name
     */
    public static void main(String[] args) {
        try {
            int pnum = 0;
            int method = FISerializer.METHOD_SMALLEST_NONLOSSY;
            boolean removeDefaults = true;

            if (args.length < 2) {
                System.out.println(FISerializer.USAGE);
                return;
            }

            for(int i=0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("-fastest")) {
                        System.out.println("Fasting parsing method");
                        method = FISerializer.METHOD_FASTEST_PARSING;
                    } else if (args[i].equals("-smallest")) {
                        System.out.println("Smallest parsing method");
                        method = FISerializer.METHOD_SMALLEST_NONLOSSY;
                    } else if (args[i].equals("-lossy")) {
                        System.out.println("Lossy parsing method");
                        method = FISerializer.METHOD_SMALLEST_LOSSY;
                    } else if (args[i].equals("-strings")) {
                        System.out.println("String method");
                        method = FISerializer.METHOD_STRINGS;
                    } else if (args[i].equals("-savedefaults")) {
                        removeDefaults = false;
                    } else if (args[i].equals("-quantizeParam")) {
                        pnum++;
                        i++;

                        String st = args[i];
                        PARAM_FLOAT_LOSSY = Float.parseFloat(st);
                    } else {
                        System.out.println("Unknown option: " + args[i]);
                    }

                    pnum++;
                } else
                    break;
            }

            if (pnum + 2 < args.length) {
                System.out.println(FISerializer.USAGE);
                return;
            }

            File input = new File(args[pnum++]);
            File ouput = new File(args[pnum++]);
            FISerializer docSerializer = new FISerializer(method, removeDefaults);
            docSerializer.writeFiltered(input, ouput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


public class X3DFilter implements ContentHandler {

    private ContentHandler parent;
    private PrimitiveTypeContentHandler primHandler;
    private int method;

    public X3DFilter(ContentHandler parent, int method) {
      System.out.println("Parent: " + parent);

      this.parent = parent;
      this.method = method;
      primHandler = (PrimitiveTypeContentHandler) parent;
    }

    public void startElement(String namespaceURI, String localName,
      String qualifiedName, Attributes atts) throws SAXException {

        int len = atts.getLength();

//System.out.println("SE: " + localName + " atts: " + atts.getLength());
        if (atts.getLength() > 0) {

                AttributesHolder aholder = new AttributesHolder();

                for(int i=0; i < len; i++) {

                    // Look for float types
                    String data = atts.getValue(i).trim();

                    int dpos = data.indexOf(".");

                    String expStr = null;
                    String mantStr = null;
                    boolean list = false;
                    boolean floatData = true;
                    boolean anyFloats = false;
                    boolean intData = true;
                    boolean shortData = true;
//                    boolean byteData = true;
                    boolean byteData = false;
                    boolean booleanData = true;
                    boolean stringData = false;

                    float ftmp;
                    int itmp;
                    boolean btmp;

                    // Determine if this is a list.  Look for multiple seperators in the first 50 characters.
                    int dlen = data.length();
                    String small = data.substring(0,dlen > 50 ? 50 : dlen);

                    StringTokenizer stok = new StringTokenizer(small);
                    String tok;
                    int tokCnt = 0;
                    int cnt = 0;

                    if (stok.countTokens() > 1)
                        list = true;

                    stok = new StringTokenizer(data);
                    if (list) {
                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            if (floatData) {
                                try {
                                    ftmp = Float.parseFloat(tok);
                                } catch(Exception e) {
                                    floatData = false;
                                }
                            }

                            if (intData) {
                                try {
                                    itmp = Integer.parseInt(tok);

                                    if (itmp < Byte.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                                        byteData = false;
                                    } else if (itmp < Short.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                                        shortData = false;
                                    }
                                } catch(Exception e2) {
                                    intData = false;
                                    shortData = false;
                                    byteData = false;
                                }
                            }

                            if (booleanData) {
                                if (!tok.equalsIgnoreCase("true") || !tok.equalsIgnoreCase("false"))
                                    booleanData = false;
                            }

                            if (!floatData && !intData && !byteData && !booleanData) {
                                stringData = true;
                                break;
                            }

                            tokCnt++;
                        }

                    } else {
                        tokCnt = 1;
                        tok = data;

                        if (floatData) {
                            try {
                                ftmp = Float.parseFloat(tok);

                                if (tok.length() < 4)
                                    floatData = false;
                            } catch(Exception e) {
                                floatData = false;
                            }
                        }

                        if (intData) {
                            try {
                                itmp = Integer.parseInt(tok);

                                if (itmp < Byte.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                                    byteData = false;
                                } else if (itmp < Short.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                                    shortData = false;
                                }
                            } catch(Exception e2) {
                                intData = false;
                                shortData = false;
                                byteData = false;
                            }
                        }

                        if (booleanData) {
                            if (!tok.equalsIgnoreCase("true") || !tok.equalsIgnoreCase("false"))
                                booleanData = false;
                        }

                        if (!floatData && !intData && !booleanData && !byteData) {
                            stringData = true;
                        }

                    }

                    if (stringData || method == FISerializer.METHOD_STRINGS) {
                        types[0]++;
                        strLen += atts.getValue(i).length();

//System.out.println(atts.getLocalName(i) + " is String");
                        aholder.addAttribute(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), atts.getValue(i));
                    } else if (byteData) {
//System.out.println(atts.getLocalName(i) + " is byte list: " + tokCnt);
                        types[1]++;
                        stok = new StringTokenizer(data);
                        byte[] bval = new byte[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            bval[cnt++] = Byte.parseByte(tok);
                        }
                        aholder.addAttributeWithAlgorithmData(
                            new QualifiedName("", "", atts.getLocalName(i), atts.getQName(i)),
                            null,
                            BYTE_ALGORITHM_ID,
                            bval);
                    } else if (shortData) {
                        types[2]++;
//System.out.println(atts.getLocalName(i) + " is Short list: " + tokCnt);
                        stok = new StringTokenizer(data);
                        short[] sval = new short[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            try {
                                sval[cnt++] = Short.parseShort(tok);
                            } catch(Exception e) {
                                System.out.println("Error converting short?: " + tok);
                            }
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.SHORT, sval);
                    } else if (intData) {
                        types[3]++;
//System.out.println(atts.getLocalName(i) + " is Int list: " + tokCnt);
                        stok = new StringTokenizer(data);
                        int[] ival = new int[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            try {
                                ival[cnt++] = Integer.parseInt(tok);
                            } catch(Exception e) {
                                System.out.println("Error converting integer?: " + tok);
                            }
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.INT, ival);

                    } else if (floatData) {
                        types[4]++;
//System.out.println(atts.getLocalName(i) + " is a Float List: " + tokCnt);
                        stok = new StringTokenizer(data);
                        float[] fval = new float[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            try {
                                fval[cnt++] = Float.parseFloat(tok);
                            } catch(Exception e) {
                                System.out.println("Error converting float?: " + tok);
                            }
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.FLOAT, fval);
                    } else if (booleanData) {
                        types[5]++;
//System.out.println(atts.getLocalName(i) + " is boolean list: " + tokCnt);
                        stok = new StringTokenizer(data);
                        boolean[] bval = new boolean[tokCnt];

                        while(stok.hasMoreTokens()) {
                            tok = stok.nextToken();

                            bval[cnt++] = tok.equalsIgnoreCase("true");
                        }

                        aholder.addAttributeWithAlgorithmData(new QualifiedName("", "", atts.getLocalName(i),
                            atts.getQName(i)), null, EncodingAlgorithmIndexes.BOOLEAN, bval);
                    } else {
                        System.out.println("Unhandled: " + data);
                    }


                }
                parent.startElement(namespaceURI, localName, qualifiedName, aholder);
        } else {
            parent.startElement(namespaceURI, localName, qualifiedName, atts);
        }
    }

  public void endElement(String namespaceURI, String localName,
   String qualifiedName) throws SAXException {

      parent.endElement(namespaceURI, localName, qualifiedName);
  }

  // Methods that pass data along unchanged:
  public void startDocument() throws SAXException {
    parent.startDocument();
  }

  public void startPrefixMapping(String prefix, String uri)
   throws SAXException {
    parent.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix)
   throws SAXException {
    parent.endPrefixMapping(prefix);
  }

  public void setDocumentLocator(Locator locator) {
    parent.setDocumentLocator(locator);
  }

    public void endDocument() throws SAXException {
        parent.endDocument();

        System.out.println("Datatype stats");
        System.out.println("String: " + types[0] + " avg: " + (strLen / types[0]));
        System.out.println("Byte: " + types[1]);
        System.out.println("Short: " + types[2]);
        System.out.println("Int: " + types[3]);
        System.out.println("Float: " + types[4]);
        System.out.println("Boolean: " + types[5]);
  }

int chars = 0;
  public void characters(char[] text, int start, int length)
   throws SAXException {

   // TODO: Can we ignore some of this?
//System.out.println("chars: " + length + " tot: " + chars);
chars += length;

    // Look for float types
    String data = new String(text, start, length);
    int dpos = data.indexOf(".");

    String expStr = null;
    String mantStr = null;
    boolean list = false;
    boolean floatData = true;
    boolean anyFloats = false;
    boolean intData = true;
    boolean shortData = true;
//    boolean byteData = true;
    boolean byteData = false;
    boolean booleanData = true;
    boolean stringData = false;

    float ftmp;
    int itmp;
    boolean btmp;

    // Determine if this is a list.  Look for multiple seperators in the first 50 characters.
    int dlen = data.length();
    String small = data.substring(0,dlen > 50 ? 50 : dlen);

    StringTokenizer stok = new StringTokenizer(small);
    String tok;
    int tokCnt = 0;
    int cnt = 0;

    if (stok.countTokens() > 1)
        list = true;

    stok = new StringTokenizer(data);
    if (list) {
        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();

            if (floatData) {
                try {
                    ftmp = Float.parseFloat(tok);
                } catch(Exception e) {
                    floatData = false;
                }
            }

            if (intData) {
                try {
                    itmp = Integer.parseInt(tok);

                    if (itmp < Byte.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                        byteData = false;
                    } else if (itmp < Short.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                        shortData = false;
                    }
                } catch(Exception e2) {
                    intData = false;
                    shortData = false;
                    byteData = false;
                }
            }

            if (booleanData) {
                if (!tok.equalsIgnoreCase("true") || !tok.equalsIgnoreCase("false"))
                    booleanData = false;
            }

            if (!floatData && !intData && !byteData && !booleanData) {
                stringData = true;
                break;
            }

            tokCnt++;
        }

    } else {
        tokCnt = 1;
        tok = data;

        if (floatData) {
            try {
                ftmp = Float.parseFloat(tok);
                if (tok.length() < 4)
                    floatData = false;

            } catch(Exception e) {
                floatData = false;
            }
        }

        if (intData) {
            try {
                itmp = Integer.parseInt(tok);

                if (itmp < Byte.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                    byteData = false;
                } else if (itmp < Short.MIN_VALUE || itmp > Byte.MAX_VALUE) {
                    shortData = false;
                }
            } catch(Exception e2) {
                intData = false;
                shortData = false;
                byteData = false;
            }
        }

        if (booleanData) {
            if (!tok.equalsIgnoreCase("true") || !tok.equalsIgnoreCase("false"))
                booleanData = false;
        }

        if (!floatData && !intData && !booleanData && !byteData) {
            stringData = true;
        }

    }

    if (stringData || method == FISerializer.METHOD_STRINGS) {
        types[0]++;
        strLen += length;

        parent.characters(text, start, length);

//System.out.println(data + " is String");
    } else if (byteData) {
System.out.println(data + " is byte list: " + tokCnt);
        types[1]++;
        stok = new StringTokenizer(data);
        byte[] bval = new byte[tokCnt];

        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();

            bval[cnt++] = Byte.parseByte(tok);
        }

        primHandler.bytes(bval,0, bval.length);
    } else if (shortData) {
        types[2]++;
System.out.println(data + " is Short list: " + tokCnt);
        stok = new StringTokenizer(data);
        short[] sval = new short[tokCnt];

        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();

            try {
                sval[cnt++] = Short.parseShort(tok);
            } catch(Exception e) {
                System.out.println("Error converting short?: " + tok);
            }
        }

        primHandler.shorts(sval,0, sval.length);
    } else if (intData) {
        types[3]++;
System.out.println(data + " is Int list: " + tokCnt);
        stok = new StringTokenizer(data);
        int[] ival = new int[tokCnt];

        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();

            try {
                ival[cnt++] = Integer.parseInt(tok);
            } catch(Exception e) {
                System.out.println("Error converting integer?: " + tok);
            }
        }

        primHandler.ints(ival,0, ival.length);

    } else if (floatData) {
        types[4]++;
System.out.println(data + " is a Float List: " + tokCnt);
        stok = new StringTokenizer(data);
        float[] fval = new float[tokCnt];

        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();

            try {
                fval[cnt++] = Float.parseFloat(tok);
            } catch(Exception e) {
                System.out.println("Error converting float?: " + tok);
            }
        }

        primHandler.floats(fval,0, fval.length);
    } else if (booleanData) {
        types[5]++;
System.out.println(data + " is boolean list: " + tokCnt);
        stok = new StringTokenizer(data);
        boolean[] bval = new boolean[tokCnt];

        while(stok.hasMoreTokens()) {
            tok = stok.nextToken();

            bval[cnt++] = tok.equalsIgnoreCase("true");
        }

        primHandler.booleans(bval,0, bval.length);
    } else {
        System.out.println("Unhandled: " + data);
        parent.characters(text, start, length);
    }
  }

  public void ignorableWhitespace(char[] text, int start,
   int length) throws SAXException {

   // TODO: Should we get rid of this?
//System.out.println("iws: " + length);
    parent.ignorableWhitespace(text, start, length);
  }

  public void processingInstruction(String target, String data)
   throws SAXException {
    parent.processingInstruction(target, data);
  }

  public void skippedEntity(String name)
   throws SAXException {
    parent.skippedEntity(name);
  }

}
}
