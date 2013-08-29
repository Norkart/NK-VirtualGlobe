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
package org.web3d.vrml.nodes;

/**
 * A node which emits sound information.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface VRMLSoundNodeType extends VRMLChildNodeType
{
    /**
     * Get node content for the current source.
     *
     * @return The current source or null if not set
     */
    public VRMLAudioClipNodeType getSource();

    /**
     * Set node content as replacement for the source. Setting a value of
     * null will act like delete.
     *
     * @param source The new source instance to use
     */
    public void setSource(VRMLNodeType source);

    /**
     * Delete all contained source content
     */
    public void deleteSource();
}
