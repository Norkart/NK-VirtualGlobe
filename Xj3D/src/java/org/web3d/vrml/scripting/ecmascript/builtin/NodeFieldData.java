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
package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Data holder class for describing a node, field and value that has changed.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class NodeFieldData {

    /** The node that this data was sourced from */
    public VRMLNodeType node;

    /** The index of the field value that changed */
    public int fieldIndex;

    /**
     * The new value assigned. Any one of the standard value objects that
     * Rhino can generate.
     */
    public Object value;
}
