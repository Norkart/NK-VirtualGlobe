/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

import java.io.InputStream;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

/**
 * A utility class for mapping nodes by name to their component.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class X3DNodeComponentMapper {

    /** The config file for initializing the inheritance map */
    private static final String NODE_MAP_FILENAME = "config/3.2/profiles.xml";

    /** The instance */
    private static X3DNodeComponentMapper instance;

    /** Node to component map, key = (String)nodeName, value = (String)componentPackageName */
    private Map<String,String> nodeMap;

    /** Node to component level map, key = (String)nodeName, value = (Integer)componentLevel */
    private Map<String,Integer> levelMap;

    /** Protected Constructor */
    protected X3DNodeComponentMapper() {
        initializeNodeMap();
    }

    /**
     * Return the instance of the X3DNodeComponentMapper
     *
     * @return the instance of the X3DNodeComponentMapper
     */
    public static X3DNodeComponentMapper getInstance() {
        if (instance == null) {
            instance = new X3DNodeComponentMapper();
        }
        return(instance);
    }

    /**
     * Return the component package name for the named argument node
     *
     * @param node_name The node for which to determine the interfaces
     * @return The component package name. If the named node is unknown,
     * null is returned.
     */
    public String getComponentName(String node_name) {
        return(nodeMap.get(node_name));
    }

    /**
     * Return the component level for the named argument node
     *
     * @param node_name The node for which to determine the interfaces
     * @return The component level. If the named node is unknown,
     * null is returned.
     */
    public Integer getComponentLevel(String node_name) {
        return(levelMap.get(node_name));
    }

    /**
     * Retrieve the configuration file containing the node component
     * data and initialize the local map with it.
     */
    private void initializeNodeMap() {

        InputStream is = (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return ClassLoader.getSystemResourceAsStream(NODE_MAP_FILENAME);
                }
            }
           );

        // Fallback mechanism for WebStart
        if(is == null) {
            ClassLoader cl = X3DNodeComponentMapper.class.getClassLoader();
            is = (InputStream)cl.getResourceAsStream(NODE_MAP_FILENAME);
        }

        Document nodemap_doc = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(false);
            fac.setNamespaceAware(false);
            DocumentBuilder builder = fac.newDocumentBuilder();
            nodemap_doc = builder.parse(is);
        } catch(FactoryConfigurationError fce) {
            //System.out.println("X3DNodeComponentMapper: FactoryConfigurationError: "+ fce.getMessage());
        } catch(ParserConfigurationException pce) {
            //System.out.println("X3DNodeComponentMapper: ParserConfigurationException: "+ pce.getMessage());
        } catch(SAXException se) {
            //System.out.println("X3DNodeComponentMapper: SAXException: "+ se.getMessage());
        } catch(IOException ioe) {
            //System.out.println("X3DNodeComponentMapper: IOException: "+ ioe.getMessage());
        }

        if (nodemap_doc == null) {
            System.out.println("X3DNodeComponentMapper: node to component mapping is unavailable");
            nodeMap = new HashMap<String,String>(0);
            levelMap = new HashMap<String,Integer>(0);
        } else {
            nodeMap = new HashMap<String,String>();
            levelMap = new HashMap<String,Integer>();
            Element root_element = nodemap_doc.getDocumentElement();

            // get the set of defined components
            NodeList cmpConfigList = root_element.getElementsByTagName("componentConfig");
            Element cmpConfig_element = (Element)cmpConfigList.item(0);
            NodeList cmpList = cmpConfig_element.getElementsByTagName("component");
            int numCmp = cmpList.getLength();
            for (int i = 0; i < numCmp; i++) {
                Element cmp_element = (Element)cmpList.item(i);
                String cmp_name = cmp_element.getAttribute("name");
                // note the package name is transformed, all lower case, no dashes
                String cmp_package_name = cmp_name.toLowerCase().replaceAll("-", "");
                // get the set of levels per component
                NodeList cmpLevelList = cmp_element.getElementsByTagName("componentLevel");
                int numLevel = cmpLevelList.getLength();
                Integer level;

                for (int j = 0; j < numLevel; j++) {
                    level = new Integer(j) + 1;
                    Element cmpLevel_element = (Element)cmpLevelList.item(j);
                    // get the set of nodes per level
                    NodeList nodeList = cmpLevel_element.getElementsByTagName("node");
                    int numNode = nodeList.getLength();
                    for (int k = 0; k < numNode; k++) {
                        Element node_element = (Element)nodeList.item(k);
                        String node_name = node_element.getAttribute("name");
                        nodeMap.put(node_name, cmp_package_name);
                        levelMap.put(node_name, level);
                    }
                }
            }
        }
    }
}
