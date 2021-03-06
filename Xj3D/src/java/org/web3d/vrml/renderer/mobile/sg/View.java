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

package org.web3d.vrml.renderer.mobile.sg;

// Standard imports
import gl4java.*;
import gl4java.awt.*;
import gl4java.drawable.*;

// Application specific imports

/**
 * The View class sets represents the users view into the system.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */

public class View extends Leaf implements GLEventListener{
    /**
     * The default constructor.
     */
    public View() {
    }

    /**
     * This method is called to render this node.  All openGL commands needed
     * to render the node should be executed.  Any transformations needed
     * should be added to the transformation stack premultiplied.  This
     * method must be re-entrant as it can be called from multiple
     * places at once.
     */
    public void render(GLDrawable gld) {
    }

    /*
     * This method is called after a node has been rendered.  This method
     * must be re-entrant.
     */
    public void postRender(GLDrawable gld) {}

    //----------------------------------------------------------
    // Methods required by the GLEventListener interface.
    //----------------------------------------------------------

    public void init(GLDrawable drawable) {
        GLFunc gl = drawable.getGL();
        GLUFunc glu = drawable.getGLU();
        GLContext glj = drawable.getGLContext();

        float pos[] = { 5.0f, 5.0f, 10.0f, 0.0f };

        gl.glLightfv(GL_LIGHT0, GL_POSITION, pos);
        gl.glEnable(GL_CULL_FACE);
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_DEPTH_TEST);

        gl.glEnable(GL_NORMALIZE);

        glj.gljCheckGL();
    }

    public void cleanup(GLDrawable drawable) {
    }

    public void reshape(gl4java.drawable.GLDrawable gld,int width,int height) {
        float h = (float)height / (float)width;
        GLFunc gl = gld.getGL();

        gl.glViewport(0,0,width,height);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 60.0f);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, 10.0f);
    }

    public void display(GLDrawable drawable) {
    }

    public void preDisplay(GLDrawable drawable) {
    }

    public void postDisplay(GLDrawable drawable) {
    }
}