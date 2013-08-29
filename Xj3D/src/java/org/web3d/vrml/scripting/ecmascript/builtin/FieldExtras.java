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
package org.web3d.vrml.scripting.ecmascript.builtin;

// Standard imports
// none

// Application specific imports
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.sav.VRMLParseException;

/**
 * Factory abstract interface for generating field objects from a given node.
 * <p>
 *
 * The idea of this factory is to break a circular compile dependency
 * between SF/MFNode and all the field classes.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface FieldExtras {

    /**
     * Create a collection of VRML Objects from a string. Used by the SFNode
     * constructor, so we should only ever have one object returned, but an
     * array is returned just in case.
     *
     * @param vrmlString The string containing VRML statements
     * @return A scene containing all the information
     */
    public VRMLNodeType[] parseVrmlString(String vrmlString)
        throws VRMLException, VRMLParseException;

    /**
     * Locate the field factory appropriate to this node and context
     * information. Used so that the field factory can generate the nodes
     * within the correct execution space etc.
     *
     * @return The local field factory instance in use
     */
    public FieldFactory getFieldFactory();

}
