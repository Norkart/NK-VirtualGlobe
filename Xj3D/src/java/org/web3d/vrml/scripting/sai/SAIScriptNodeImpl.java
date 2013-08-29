/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;

import org.web3d.x3d.sai.X3DScriptNode;
import org.web3d.x3d.sai.X3DChildNode;
import org.web3d.x3d.sai.X3DUrlObject;

/**
 * Shell representation of a script node.
 * <p>
 *
 * The script is different to all the other nodes. While it represents
 * a script, it doesn't have the normal content of a node. This will be an
 * interface to interact between the script and an external scripting engine.
 * Quite how we are going to do this remains an interesting thing to consider.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
class SAIScriptNodeImpl extends BaseNode implements X3DScriptNode {

    /** The internal script representation */
    private VRMLScriptNodeType localScript;

    /**
     * Create a new script node implementation.
     *
     * @param node The source node this is wrapping
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    public SAIScriptNodeImpl(VRMLNodeType node,
                             ReferenceQueue refQueue,
                             FieldFactory fac,
                             FieldAccessListener fal,
                             BaseNodeFactory bnf ) {
        super(node, refQueue, fac, fal, bnf);

        localScript = (VRMLScriptNodeType)node;
    }

    //----------------------------------------------------------
    // Methods defined by X3DUrlObject
    //----------------------------------------------------------

    /**
     * Get the number of valid URL values that have been defined for this
     * node instance.
     *
     * @return A non-negative number
     */
    public int getNumUrl() {
        String[] urls = localScript.getUrl();
        return (urls == null) ? 0 : urls.length;
    }

    /**
     * Get the list of URLs defined by this node.
     *
     * @param urls The array to copy values into
     */
    public void getUrl(String[] urls) {
        String[] src_urls = localScript.getUrl();

        System.arraycopy(src_urls, 0, urls, 0, src_urls.length);
    }

    /**
     * Set the URl list to the new value. A zero length or null value will
     * empty the current list.
     *
     * @param urls The list of new values to set
     */
    public void setUrl(String[] urls) {

        // setUrl() method is not currently available on
        // VRMLSingleExternalNodeType, so do it the hard way.
        int field = localScript.getFieldIndex("url");
        localScript.setValue(field, urls, urls.length);
    }
}
