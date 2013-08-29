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
 * Provides for implementation of a X3D browser than runs as a
 * component and able to extract a Browser reference from it.
 * <P>
 * Generally this is used to provide a definition of an AWT component with a
 * VRML display capability. There is no reason why this could not be used for
 * other browser representations such as off screen renderers or file savers.
 *
 * @version 2.0 29 August 1998
 */
public interface X3DComponent {

    /**
     * Get a browser reference from this component that represents the
     * internals of this browser.
     *
     * @return A reference to the browser object represented by this component.
     */
    public ExternalBrowser getBrowser();

    /**
     * Get a reference to the component implementation. For example, if this
     * is an AWT component, it would return an instance
     * of {@link java.awt.Component}.
     */
    public Object getImplementation();

    /**
     * Shutdown the component because it will no longer be needed. If the
     * component has already had this method called, it will silently ignore
     * any further requests.
     */
    public void shutdown();
}





