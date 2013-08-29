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

package org.xj3d.ui.swt.view;

// External imports
// none

// Local imports
// none

/**
 * Constant identifiers of the components used in this view
 * 
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface BrowserViewConstants {

    /** Base identifier of components in this package */
    public static final String VIEW_ID = "org.xj3d.ui.swt.view";
    
    /** Identifier of the main browser view */
    public static final String BROWSER_VIEW_ID = VIEW_ID + ".BrowserView";
    
    /** Identifier of the frame rate contribution to the status bar */
    public static final String FRAME_RATE_CONTRIBUTION_ITEM_ID = 
        VIEW_ID + ".FrameRateContributionItem";
}

