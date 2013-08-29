/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2007 - 2008
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package xj3d.filter.importer;

// External imports
import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// Local imports
// None

/**
 * Utility methods supporting file imports
 *
 * @author Rex Melton
 * @version $Revision: 1.6 $
 */
public abstract class ImportUtils {
    
    /**
     * Create and return a new xml DOM object parsed from the argument <code>URL</code>
     *
     * @param url the document <code>URL</code>
     * @return a new xml DOM object parsed from the argument <code>URL</code>, or 
     * <code>null</code> if one cannot be created.
     */
    public static Document getDocument(URL url) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(url.openStream());
        }
        // unable to get a document builder factory
        catch (FactoryConfigurationError fce) { 
            fce.printStackTrace(); 
        }
        // Parser with specified options can't be built
        catch (ParserConfigurationException pce) { 
            pce.printStackTrace(); 
        }
        // Errors generated during parsing
        catch (SAXException sxe) { 
            sxe.printStackTrace(); 
        } 
        catch (IOException ioe) { 
            ioe.printStackTrace(); 
        }
        return(document);
    }
    
    /**
     * Create and return a new xml DOM object parsed from the argument <code>File</code>
     *
     * @param file the document <code>File</code>
     * @return a new xml DOM object parsed from the argument <code>File</code>, or 
     * <code>null</code> if one cannot be created.
     */
    public static Document getDocument(File file) {
        Document document = null;
        try {
            URL url = file.toURI().toURL();
            document = getDocument(url);
        } catch (MalformedURLException murle) {
            System.out.println(murle.getMessage());
        }
        return(document);
    }
    
    /**
     * Search the children of the argument node for elements.
     *
     * @param node The parent node element.
     * @return The list of elements.
     */
    public static ArrayList<Element> getElements(Element node) {
        
        ArrayList<Element> list = new ArrayList<Element>();
        // note that we need to preserve the order of the child elements,
        // transformational elements of a Collada node must be processed
        // in order.
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)n;
                list.add(e);
            }
        }
        return(list);
    }
    
    /**
     * Search the children of the argument node for named elements.
     *
     * @param node The parent node element.
     * @param tagName The child element's tagName.
     * @return The list of elements.
     */
    public static ArrayList<Element> getElementsByTagName(Element node, String tagName) {
        
        ArrayList<Element> list = new ArrayList<Element>();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)n;
                if (e.getTagName().equals(tagName)) {
                    list.add(e);
                }
            }
        }
        return(list);
    }
    
    /**
     * Search the children of the argument node for first instance of the named element.
     *
     * @param node The parent node element.
     * @param tagName The child element's tagName.
     * @return The named element, or null if it could not be found.
     */
    public static Element getFirstElementByTagName(Element node, String tagName) {
        
        ArrayList<Element> list = new ArrayList<Element>();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)n;
                if (e.getTagName().equals(tagName)) {
                    return((Element)n);
                }
            }
        }
        return(null);
    }
    
    /**
     * Search the children of the argument node for elements, return the 
     * first one found.
     *
     * @param node The parent node element.
     * @return The first child element of the parent.
     */
    public static Element getFirstElement(Element node) {
        
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return((Element)n);
            }
        }
        return(null);
    }
    
    /**
     * Return the named attribute of the argument element, or
     * null if it does not exist.
     *
     * @param element The element to search for a named attribute
     * @param attr The attribute name
     * @return The attribute String, or null if it does not exist.
     */
    public static String getAttribute(Element element, String attr) {
        String value = element.getAttribute(attr);
        if (value.equals("")) {
            value = null;
        }
        return(value);
    }
}
