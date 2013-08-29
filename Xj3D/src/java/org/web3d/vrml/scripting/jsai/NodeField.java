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
package org.web3d.vrml.scripting.jsai;

// Standard imports
// none

// Application specific imports
import vrml.Browser;

/**
 * Marker interface for all field object instances that represent a node.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
interface NodeField {

    /**
     * Initialise this instance with the extra information needed.
     *
     * @param b The browser to use with this node
     * @param fac The Factory used to create fields
     */
    void initialize(Browser b, FieldFactory fac);
}
