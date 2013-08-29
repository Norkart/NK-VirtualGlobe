/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.xj3d.ui.construct.ogl;

// External Imports
import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.j3d.aviatrix3d.management.RenderManager;

// Internal Imports

/**
 * A JFrameNotifyWrapper wrapper that handles calling setEnabled on the renderManager
 * when the frame's addNotify is called.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class JFrameNotifyWrapper extends JFrame {
    /** The scene Manager */
    private RenderManager sceneManager;

    /**
     * Constructs a new frame that is initially invisible.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @see JComponent#getDefaultLocale
     */
    public JFrameNotifyWrapper(RenderManager manager) throws HeadlessException {
        super();

        init(manager);
    }

    /**
     * Creates a <code>Frame</code> in the specified
     * <code>GraphicsConfiguration</code> of
     * a screen device and a blank title.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param gc the <code>GraphicsConfiguration</code> that is used
     *      to construct the new <code>Frame</code>;
     *      if <code>gc</code> is <code>null</code>, the system
     *      default <code>GraphicsConfiguration</code> is assumed
     * @exception IllegalArgumentException if <code>gc</code> is not from
     *      a screen device.  This exception is always thrown when
     *      GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * @since     1.3
     */
    public JFrameNotifyWrapper(GraphicsConfiguration gc, RenderManager manager) {
        super(gc);

        init(manager);
    }

    /**
     * Creates a new, initially invisible <code>Frame</code> with the
     * specified title.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param title the title for the frame
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see Component#setSize
     * @see Component#setVisible
     * @see JComponent#getDefaultLocale
     */
    public JFrameNotifyWrapper(String title, RenderManager manager) throws HeadlessException {
        super(title);

        init(manager);
    }

    /**
     * Creates a <code>JFrameNotifyWrapper</code> with the specified title and the
     * specified <code>GraphicsConfiguration</code> of a screen device.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param title the title to be displayed in the
     *      frame's border. A <code>null</code> value is treated as
     *      an empty string, "".
     * @param gc the <code>GraphicsConfiguration</code> that is used
     *      to construct the new <code>JFrameNotifyWrapper</code> with;
     *      if <code>gc</code> is <code>null</code>, the system
     *      default <code>GraphicsConfiguration</code> is assumed
     * @exception IllegalArgumentException if <code>gc</code> is not from
     *      a screen device.  This exception is always thrown when
     *      GraphicsEnvironment.isHeadless() returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see JComponent#getDefaultLocale
     * @since     1.3
     */
    public JFrameNotifyWrapper(String title, GraphicsConfiguration gc, RenderManager manager) {
        super(title, gc);

        init(manager);
    }

    /**
     * Common initialization.
     */
    private void init(RenderManager manager) {
        sceneManager = manager;
    }

    /**
     * Override addNotify so we know we have peer before calling setEnabled for Aviatrix3D.
     */
    public void addNotify() {
        super.addNotify();

        sceneManager.setEnabled(true);
    }
}
