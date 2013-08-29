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

package org.xj3d.ui.swt.device;

// External imports
import java.util.List;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

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
 * specific to the SWT UI toolkit. Devices created through
 * this factory are initialized with the appropriate SWT
 * event listeners.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class SWTDeviceFactory extends DeviceFactory {

    /**
     * Constructor.
     *
     * <p>
     * Note that the canvas argument requires an instance of
     * org.eclipse.swt.widgets.Control. Control is the lowest
     * level swt class that implements the device listeners
     * utilized in this class. Specifically:
     * <ul>
     * <li>KeyListener</li>
     * <li>MouseListener</li>
     * <li>MouseMoveListener</li>
     * </ul>
     *
     * @param canvas - The Object representing the ui toolkit
     * specific graphical component. Must be an instance of
     * org.eclipse.swt.widgets.Control.
     * @param rendererID - Identifier String of the renderer type.
     * @param surface - The Object representing the renderer
     * specific drawing surface.
     * @throws IllegalArgumentException if the canvas Object is not
     * an instance of org.eclipse.swt.widgets.Control.
     * @param reporter - The instance to use or null. A value of null
     * will clear the currently set reporter and causes the factory
     * to use the DefaultErrorReporter.
     */
    public SWTDeviceFactory( Object canvas, String rendererID,
        Object surface, ErrorReporter reporter ) {

        if ( !( canvas instanceof Control ) ) {
            throw new IllegalArgumentException(
                "canvas object is not a subclass of org.eclipse.swt.Control" );
        }
        errorReporter = ( reporter == null ) ?
            DefaultErrorReporter.getDefaultReporter( ) : reporter;

        toolkitID = Xj3DConstants.SWT_ID;

        this.canvas = canvas;
        // do we want to check the rendererID ?
        this.rendererID = rendererID;
        // can we check the surface ? what's valid ?
        this.surface = surface;
    }

    /**
     * Return the array of DeviceManagers that are available per the
     * constructor parameters. The InputDevices instantiated will be
     * initialized with SWT UI toolkit event listeners.
     *
     * @return the array of DeviceManagers. If no DeviceManagers are
     * available, an empty (size 0) array is returned.
     */
    public DeviceManager[] getDeviceManagers( ) {
        List managerList = createDevices( );
        DeviceManager[] manager =
            (DeviceManager[])managerList.toArray( new DeviceManager[managerList.size( )] );
        Control control = (Control)canvas;
        for ( int i = 0; i < manager.length; i++ ) {
            InputDevice[] device = manager[i].getDevices( );
            for ( int j = 0; j < device.length; j++ ) {
                InputDevice dev = device[j];
                if ( dev instanceof MouseListener ) {
                    control.addMouseListener( (MouseListener)dev );
                }
                if ( dev instanceof MouseMoveListener ) {
                    control.addMouseMoveListener( (MouseMoveListener)dev );
                }
                if ( dev instanceof Listener ) {
                    control.addListener( SWT.MouseWheel, (Listener)dev );
                }
                if ( dev instanceof KeyListener ) {
                    control.addKeyListener( (KeyListener)dev );
                }
            }
        }
        return( manager );
    }

    /**
     * Return the KeySensorDevice associated with the rendering surface
     * initialized with the SWT key event listener.
     *
     * @return the KeySensorDevice
     */
    public KeySensorDevice getKeySensorDevice( ) {
        SWTKeySensorDevice keyDevice = new SWTKeySensorDevice( );
        Control control = (Control)canvas;
        control.addKeyListener( keyDevice );
        return( keyDevice );
    }
}

