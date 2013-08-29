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

import gl4java.drawable.GLDrawable;
import gl4java.drawable.GLEventListener;

// Application specific imports
import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.mobile.browser.PerFrameManager;

public class SGManager implements GLEventListener {

    /** The GLDrawables this SG manages */
    private GLDrawable channel;

    /** The Scene Graph root */
    private Group root;

    /** The cull threads */
    private Cull cull;

    /** The draw threads */
    private Draw draw;

    /** The current active Viewpoint */
    private Viewpoint viewpoint;

    /** Handles the VRML Event model */
    private PerFrameManager pfManager;

    /**
     * Constructs a new SGManager for a single channel
     *
     * @param channel The channel to render to
     */
    public SGManager(GLDrawable channel) {
        this.channel = channel;

        channel.addGLEventListener(this);
        cull = new Cull();
        draw = new Draw(channel);
    }

    public void setPerFrameManager(PerFrameManager pfm) {
        pfManager = pfm;
    }

    public void setScene(Group scene) {
        root = scene;

        // Tell the nodes their SGManager
        root.setSGManager(this);
    }

    protected void drawFrameFinished(int channel) {
    }

    /**
     * Set the active viewpoint. A null viewpoint is invalid.
     */
    public void setActiveViewpoint(Viewpoint vp) {
        if (viewpoint != null)
            viewpoint.setActive(false);
        viewpoint = vp;

        vp.setActive(true);
    }

    //---------------------------------------------------------------
    // Methods required by GLEventListener
    //---------------------------------------------------------------

    public void init(GLDrawable drawable) {
        GLFunc gl = drawable.getGL();
        GLUFunc glu = drawable.getGLU();
        GLContext glj = drawable.getGLContext();

        // Need to move headlight to Viewpoint
        float pos[] = { 5.0f, 5.0f, 10.0f, 0.0f };

        gl.glClearColor(0, 0, 0, 0);
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

    public void reshape(GLDrawable gld,int width,int height) {

        float h = (float)width / (float)height;
        GLFunc gl = gld.getGL();
        GLUFunc glu = gld.getGLU();

        gl.glViewport(0,0,width,height);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45, h, 1, 1000);
        gl.glMatrixMode(GL_MODELVIEW);
//        gl.glLoadIdentity();
//        gl.glTranslatef(0.0f, 0.0f, -10.0f);

    }

    public void display(GLDrawable gld) {
        if(root == null || pfManager == null)
            return;

        GLFunc gl = gld.getGL();

        pfManager.simTick();
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        cull.cull(root);

        if (viewpoint != null)
            viewpoint.setupView(gld);

        draw.draw(cull.getRenderList(), cull.getRenderOp(), cull.getRenderListSize());
    }

    public void preDisplay(GLDrawable drawable) {
    }

    public void postDisplay(GLDrawable drawable) {
    }
}