/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.device;

// External imports
import java.awt.Component;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import java.util.List;

// Local imports
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.device.DeviceManager;
import org.xj3d.device.InputDevice;

import org.web3d.vrml.util.KeySensorDevice;

import org.xj3d.core.eventmodel.DeviceFactory;

/**
 * A concrete implementation of DeviceFactory that is
 * specific to the AWT UI toolkit. Devices created through
 * this factory are initialized with the appropriate AWT
 * event listeners.
 *
 * @author Rex Melton
 * @version $Revision: 1.7 $
 */
public class AWTDeviceFactory extends DeviceFactory {

    /**
     * Constructor.
     *
     * @param canvas - The Object representing the ui toolkit
     * specific graphical component. Must be an instance of
     * java.awt.Component.
     * @param rendererID - Identifier String of the renderer type.
     * @param surface - The Object representing the renderer
     * specific drawing surface.
     * @throws IllegalArgumentException if the canvas Object is not
     * an instance of java.awt.Component.
     * @param reporter - The instance to use or null. A value of null
     * will clear the currently set reporter and causes the factory
     * to use the DefaultErrorReporter.
     */
    public AWTDeviceFactory( Object canvas, String rendererID,
        Object surface, ErrorReporter reporter ) {
        if ( !( canvas instanceof Component ) ) {
            throw new IllegalArgumentException(
                "canvas object is not a subclass of java.awt.Component" );
        }
        errorReporter = ( reporter == null ) ?
            DefaultErrorReporter.getDefaultReporter( ) : reporter;

        toolkitID = Xj3DConstants.AWT_ID;

        this.canvas = canvas;
        // do we want to check the rendererID ?
        this.rendererID = rendererID;
        // can we check the surface ? what's valid ?
        this.surface = surface;
    }

    /**
     * Return the array of DeviceManagers that are available per the
     * constructor parameters. The InputDevices instantiated will be
     * initialized with the AWT ui toolkit event listeners.
     *
     * @return the array of DeviceManagers. If no DeviceManagers are
     * available, an empty (size 0) array is returned.
     */
    public DeviceManager[] getDeviceManagers( ) {
        List managerList = createDevices( );
        DeviceManager[] manager =
            (DeviceManager[])managerList.toArray( new DeviceManager[managerList.size( )] );
        Component cmp = (Component)canvas;
        for( int i = 0; i < manager.length; i++ ) {
            InputDevice[] device = manager[i].getDevices( );
            for( int j = 0; j < device.length; j++ ) {
                InputDevice dev = device[j];
                if( dev instanceof MouseListener ) {
                    cmp.addMouseListener( (MouseListener)dev );
                }
                if( dev instanceof MouseMotionListener ) {
                    cmp.addMouseMotionListener( (MouseMotionListener)dev );
                }
                if( dev instanceof MouseWheelListener ) {
                    cmp.addMouseWheelListener( (MouseWheelListener)dev );
                }
                if( dev instanceof KeyListener ) {
                    cmp.addKeyListener( (KeyListener)dev );
                }
            }
        }
        return( manager );
    }

    /**
     * Return the KeySensorDevice associated with the rendering surface
     * initialized with the AWT key event listener.
     *
     * @return the KeySensorDevice
     */
    public KeySensorDevice getKeySensorDevice( ) {
        AWTKeySensorDevice keyDevice = new AWTKeySensorDevice( );
        Component cmp = (Component)canvas;
        cmp.addKeyListener( keyDevice );
        return( keyDevice );
    }
}

