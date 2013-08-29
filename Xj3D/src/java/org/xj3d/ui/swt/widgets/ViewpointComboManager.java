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

package org.xj3d.ui.swt.widgets;

// External imports
import java.util.ArrayList;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;

import org.web3d.vrml.nodes.VRMLViewpointNodeType;

// Local imports
// none

/**
 * A convenience class for managing the interactions with a Combo object
 * on the managing display thread. This class acts as an object model
 * intermediary between VRMLViewpointNodeTypes that are utilized by the
 * browser and the String objects that are displayed within the Combo.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class ViewpointComboManager implements Runnable {
    
    /** Default description string for viewpoints that have no
     *  description defined */
    private static final String NO_DESCRIPTION = "<No description>";
    
    /** Flag for the display thread indicating the list of items on the
     *  combo have changed*/
    private boolean listHasChanged;
    
    /** Flag for the display thread indicating that the selected item in
     *  the combo has been changed programatically */
    private boolean selectionHasChanged;
    
    /** Store of the actual Node objects that are represented in the Combo's
     *  display */
    private ArrayList nodeList;
    
    /** The viewpoint node whose peer requires 'selecting' in the combo */
    private VRMLViewpointNodeType selectNode;
    
    /** The display object whose thread will process the Combo's interactions */
    private Display display;
    
    /** The combo to manage */
    private Combo combo;
    
    /**
     * Constructor
     * 
     * @param display - The Display ancestor of the combo argument
     * @param combo - The Combo object to handle interactions for.
     */
    public ViewpointComboManager( Display display, Combo combo ) {
        this.display = display;
        this.combo = combo;
        nodeList = new ArrayList( );
        listHasChanged = false;
        selectionHasChanged = false;
    }
    
    //---------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------
    
    /**
     * Method for the display thread to perform the necessary updates
     * to the combo.
     */
    public void run( ) {
        if ( listHasChanged ) {
            combo.removeAll( );
            for ( int i = 0; i < nodeList.size( ); i++ ) {
                VRMLViewpointNodeType node = 
                    (VRMLViewpointNodeType)nodeList.get( i );
                String addString = node.getDescription( );
                if ( addString == null ) {
                    //note: widgets are allergic to null Strings
                    addString = NO_DESCRIPTION;
                }
                combo.add( addString );
            }
            listHasChanged = false;
        }
        if ( selectionHasChanged ) {
            int size = nodeList.size( );
            for ( int i = 0; i < size; i++ ) {
                VRMLViewpointNodeType node = (VRMLViewpointNodeType)nodeList.get( i );
                if ( node.equals( selectNode ) ) {
                    combo.select( i );
                    break;
                }
            }
            selectionHasChanged = false;
        }
    }
    //---------------------------------------------------------
    
    /** 
     * Clear the combo of all items
     */
    public void clear( ) {
        nodeList.clear( );
        if ( !listHasChanged ) {
            listHasChanged = true;
            display.asyncExec( this );
        }
    }
    
    /**
     * Add the specified node to the combo. If the node is null,
     * no action will be performed.
     * 
     * @param node - The node to add to the combo.
     */
    public void add( VRMLViewpointNodeType node ) {
        if ( node != null ) {
            nodeList.add( node );
            if ( !listHasChanged ) {
                listHasChanged = true;
                display.asyncExec( this );
            }
        }
    }
    
    /**
     * Remove the specified node from the combo. If the node does
     * not exist in association with the combo, no action is performed.
     * 
     * @param node - The node to remove from the combo
     */
    public void remove( VRMLViewpointNodeType node ) {
        if ( node != null ) {
            int index = nodeList.indexOf( node );
            if ( index != -1 ) {
                nodeList.remove( index );
                if ( !listHasChanged ) {
                    listHasChanged = true;
                    display.asyncExec( this );
                }
            }
        }
    }
    
    /**
     * Select the specified node in the the combo. If the node
     * is null or does not exist in association with the combo
     * no action is performed.
     * 
     * @param node - The node to select in the combo
     */
    public void select( VRMLViewpointNodeType node ) {
        if ( node != null ) {
            selectNode = node;
            if ( !selectionHasChanged ) {
                selectionHasChanged = true;
                display.asyncExec( this );
            }
        }
    }
    
    /**
     * Return the node associated with the combo at the specified index.
     * If the index is out of bounds, null will be returned.
     * 
     * @param index - The index of the node in the combo to return
     * @return The node, or null if the specified index was invalid.
     */
    public VRMLViewpointNodeType getNode( int index ) {
        if ( ( index > -1 ) && ( index < nodeList.size( ) ) ) {
            return( (VRMLViewpointNodeType)nodeList.get( index ) );
        }
        else { return( null ); }
    }
}

