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
 * Geometry nodes produce renderable geometry and are contained by a Shape
 * node.
 * <p>
 *
 * All geometry has two common properties that indicate whether to render
 * both sides of the geometry (solid), and the winding of the vertices of
 * the triangle. By default, VRML97/X3D use counter-clockwise ordering, but
 * any of the polygonal nodes may elect to reverse the order.
 *
 * @author Alan Hudson
 * @version $Revision: 1.9 $
 */
public interface VRMLGeometryNodeType extends VRMLNodeType {

    /**
     * Get the value of the solid field.
     *
     * @return true This object is solid (ie single sided)
     */
    public boolean isSolid();

    /**
     * Get the value of the CCW field. If the node does not have one, this will
     * return true.
     *
     * @return true if the vertices are CCW ordered
     */
    public boolean isCCW();

    /**
     * Specifies whether this node requires lighting.
     *
     * @return true Should lighting be enabled
     */
    public boolean isLightingEnabled();

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets();

    /**
     * Get the texture coordinate generation mode.  The values are constants
     * defined in the X3D Spec under TextureCoordinateGenerator. NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers.
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum);

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    public boolean hasLocalColors();

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    public boolean hasLocalColorAlpha();

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addLocalColorsListener(LocalColorsListener l);

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeLocalColorsListener(LocalColorsListener l);

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l);

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l);
}
