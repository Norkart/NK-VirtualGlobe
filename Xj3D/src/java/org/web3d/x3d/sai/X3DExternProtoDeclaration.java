/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;


/**
 * The representation of an EXTERNPROTO declaration.
 * <p>
 *
 * This is the representation of the declaration, not of a runtime node. For
 * this reason you cannot access the internals, nor can you work with the
 * individual field values. You can, however, perform basic introspection
 * tasks such as looking at the available field definitions and seeing the
 * basic node type.
 * <p>
 *
 * The implementation extends the basic proto declaration class to represent
 * externally represented information. While all the basic method calls are
 * supported, they do need to have thier behaivour modified somewhat to deal
 * with the external nature of this structure. The following modifications to
 * the behaviours are made:
 * <p>
 *
 * <i>createInstance()</i><br>
 * <i>getNodeType()</i><br>
 * If the instance has not loaded yet or failed to load, this will generate
 * an InvalidNodeException in addition to the normal reasons. A user should
 * check the load state first before trying to create an instance if they
 * wish to avoid this error.
 *
 * <p>
 * <i>getFieldDeclarations()</i><br>
 * When queried, this will return the definitions of the fields as declared
 * in the externproto, not the underlying proto definition.
 * <p>
 *
 * No access is provided to the underlying proto declaration that fulfills
 * this external representation. If the user wishes to access that, then
 * they may make use of the createVrmlFromURL feature of the browser to load
 * the individual file as needed.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface X3DExternProtoDeclaration extends X3DProtoDeclaration {

    /**
     * Check to see if the underlying definition has been loaded yet.
     * There are 4 load states - not loaded, in-progress, failed and
     * complete. If an externproto is never used in the containing scene
     * then the browser may never even attempt to load it. This is the
     * not-loaded state. A user may force the browser to load the defintion
     * through the loadNow() method later on.
     *
     * @return The current load state
     * @see X3DLoadStateTypes
     */
    public int getLoadState();

    /**
     * Ask the browser to load the defintion now. If the load state is
     * already in progress, failed or completed, then this request will be
     * ignored.
     */
    public void loadNow();

    /**
     * Get the URLs used to describe this external prototype. If no URLs
     * are defined in the file, this will return null.
     *
     * @return The URLs used to describe this node's contents
     */
    public String[] getURLs();
}
