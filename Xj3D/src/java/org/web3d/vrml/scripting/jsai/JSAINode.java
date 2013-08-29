/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.jsai;

// External imports
import java.util.HashMap;

// Local imports
import vrml.field.*;

import vrml.ConstField;
import vrml.Browser;
import vrml.Field;
import vrml.node.Node;

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Local wrapper for the JSAI Node class.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public class JSAINode extends Node {

    /** Mapping of the currently created field names to Field instances */
    private HashMap fieldMap;

    /** Mapping of the current created field names to ConstField instances */
    private HashMap constMap;

    /** The factory to create field references from */
    private FieldFactory fieldFactory;

    /**
     * Construct a new node instance based on the underlying VRML node
     *
     * @param node The node to use
     * @param b The browser to use with this node
     * @param factory The Factory used to create fields
     * @throws NullPointerException The node reference was null
     */
    public JSAINode(VRMLNodeType node, Browser b, FieldFactory factory) {
        if(node == null)
            throw new NullPointerException("Node reference null");

        if(b == null)
            throw new NullPointerException("Browser reference is null");

        realNode = node;
        browser = b;
        fieldFactory = factory;

        nodeName = node.getVRMLNodeName();

        fieldMap = new HashMap();
        constMap = new HashMap();
    }



    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the factory reference used by this node.
     *
     * @return the current field factory instance
     */
    FieldFactory getFieldFactory() {
        return fieldFactory;
    }

    /**
     * Create a field given a name.
     *
     * @param name The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventIn
     * @throws InvalidExposedFieldException The field is not an exposedField
     */
    protected Field createField(String name, boolean checkEventIn) {
        Field ret_val = (Field)fieldMap.get(name);

        if(ret_val == null) {
            ret_val = fieldFactory.createField(realNode, name, checkEventIn);
            fieldMap.put(name, ret_val);

            if(ret_val instanceof NodeField)
                ((NodeField)ret_val).initialize(browser, fieldFactory);
        }

        return ret_val;
    }

    /**
     * Create a constant field that represents an eventOut.
     *
     * @param name The name of the field to fetch
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventOut
     */
    protected ConstField createConstField(String name) {
        ConstField ret_val = (ConstField)constMap.get(name);

        if(ret_val == null) {
            ret_val = fieldFactory.createConstField(realNode, name);
            constMap.put(name, ret_val);

            if(ret_val instanceof NodeField)
                ((NodeField)ret_val).initialize(browser, fieldFactory);
        }

        return ret_val;
    }
}
