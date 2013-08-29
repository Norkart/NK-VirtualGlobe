/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003-2005
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

// Standard library imports
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

// Application specific

/**
 * A listener for drop events.
 *
 * @author Alan Hudson
 * @version
 */
public class DropListener implements DropTargetListener {
    public int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private Xj3DFrame browser;

    public DropListener(Xj3DFrame browser) {
        this.browser = browser;
    }

    /**
     * Notification of drag enter.
     *
     * @param e The drag event
     */
    public void dragEnter(DropTargetDragEvent e) {
        if(isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }

        e.acceptDrag(e.getDropAction());
    }

    /**
     * Notification of drag over.
     *
     * @param e The drag event
     */
    public void dragOver(DropTargetDragEvent e) {
        if(isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }

        e.acceptDrag(e.getDropAction());
    }

    /**
     * Notificationn of drop action changed.
     *
     * @param e The drag event
     */
    public void dropActionChanged(DropTargetDragEvent e) {
        if(isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }
        e.acceptDrag(e.getDropAction());
    }

    /**
     * The drag has left the building.
     *
     * @param e The drag queen
     */
    public void dragExit(DropTargetEvent e) {
    }

    /**
     * Drop an object on this.
     *
     * @param e The drop event
     */
    public void drop(DropTargetDropEvent e) {

        DataFlavor chosen = chooseDropFlavor(e);
        if (chosen == null) {
            e.rejectDrop();
            return;
        }
        int da = e.getDropAction();
        int sa = e.getSourceActions();

        if ((sa & acceptableActions) == 0) {
            e.rejectDrop();
            return;
        }

        Object data = null;
        try {
            e.acceptDrop(acceptableActions);

            data = e.getTransferable().getTransferData(chosen);

            if (data == null) {
                System.out.println("Drop returned null?");
                return;
            }
        } catch ( Throwable t ) {
            System.err.println( "Couldn't transfer data: " + t.getMessage());
            t.printStackTrace();
            e.dropComplete(false);
            return;
        }

        if (data instanceof java.util.List) {
            Iterator itr = ((java.util.List)data).iterator();

            File file = (File) itr.next();

            browser.gotoLocation(file);
        } else {
            System.out.println("Unknown data type for drop: " + data);
        }

        e.dropComplete(true);
    }

    // local methods

    /**
     * Is this drag flavor supported
     *
     * @param e the DropTargetDragEvent object
     * @return whether the flavor is acceptable
     */
    private boolean isDragFlavorSupported(DropTargetDragEvent e) {
        boolean ok = false;
            if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                ok = true;

        return ok;
    }

    /**
     * Choose between drop flavors.
     *
     * @return the chosen DataFlavor or null if none match
     */
    private DataFlavor chooseDropFlavor(DropTargetDropEvent e) {
        if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            return DataFlavor.javaFileListFlavor;
        else
            return null;
    }

    /**
     * Is a drag operation ok?
     *
     * @param e the event object
     * @return whether the flavor and operation is ok
     */
    private boolean isDragOk(DropTargetDragEvent e) {
        if(isDragFlavorSupported(e) == false) {
            return false;
        }

        int da = e.getDropAction();

        // we're saying that these actions are necessary
        if ((da & acceptableActions) == 0)
            return false;

        return true;
    }
}