/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d.rendering;

// External imports
import javax.media.opengl.GL;

// Local imports
// None

/**
 * Extended version of the {@link org.j3d.aviatrix3d.rendering.ComponentRenderable}
 * interface that provides additional handling for textures. 
 * <p>
 *
 * Textures can come in several diffferent forms and this interface provides a way
 * to map between the holding {@link org.j3d.aviatrix3d.TextureUnit} class and the
 * rendering/culling stage with addtional information about the form of the
 * contained texture without needing to pull apart the TextureUnit itself. 
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface TextureRenderable extends ComponentRenderable
{
    /**
     * Check to see if the contained texture is an offscreen renderable such as
     * a pbuffer or multipass texture. If is is, then the XXXX method will return
     * the contained cullable. 
     *
     * @return true if the texture contains an offscreen source
     */
    public boolean isOffscreenSource();

    /**
     * Fetch the offscreen texture source that this renderable holds on to.
     *
     * @return The contained offscreen texture or null if none
     */
    public OffscreenCullable getOffscreenSource(); 
}

