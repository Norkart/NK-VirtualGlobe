//
//   JTreeTransferHandler.java
//
//------------------------------------------------------------------------
//
//      Portions Copyright (c) 2000 SURVICE Engineering Company.
//      All Rights Reserved.
//      This file contains Original Code and/or Modifications of Original
//      Code as defined in and that are subject to the SURVICE Public
//      Source License (Version 1.3, dated March 12, 2002)
//
//      A copy of this license can be found in the doc directory
//------------------------------------------------------------------------
//
//      Developed by SURVICE Engineering Co. (www.survice.com)
//      April 2002
//
//      Authors:
//              Bob Parker
//------------------------------------------------------------------------
import java.io.IOException;

import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import javax.swing.*;

public class JTreeTransferHandler extends TransferHandler {
    X3DImportData dataHandler = null;

    public JTreeTransferHandler(X3DImportData handler) {
	super();
	dataHandler = handler;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavor) {
	if (false) {
	for (int i = 0; i < flavor.length; ++i)
	    System.out.println("flavor[" + new Integer(i) + "] - " + flavor[i].toString());
	}

	System.out.println("canImport:");
	return true;
    }

    /*
    public Transferable createTransferable(JComponent c) {
	System.out.println("createTransferable:");
	return null;
    }

    public void exportAsDrag(JComponent c, InputEvent e, int action) {
	System.out.println("exportAsDrag:");
    }

    public void exportDone(JComponent c, Transferable t, int action) {
	System.out.println("exportDone:");
    }

    public void exportToClipboard(JComponent c, Clipboard cp, int action) {
	System.out.println("exportToClipboard:");
    }

    public static Action getCopyAction() {
	System.out.println("getCopyAction");
	return TransferHandler.getCopyAction();
    }

    public static Action getCutAction() {
	System.out.println("getCutAction");
	return TransferHandler.getCutAction();
    }

    public static Action getPasteAction() {
	System.out.println("getPasteAction");
	return TransferHandler.getPasteAction();
    }

    public int getSourceActions(JComponent c) {
	System.out.println("getSourceActions");
	return 1;
    }

    public Icon getVisualRepresentation(Transferable t) {
	System.out.println("getVisualRepresentation:");
	return null;
    }
    */

    public boolean importData(JComponent c, Transferable t) {
	if (dataHandler == null)
	    return false;

	return dataHandler.importData(c, t);
    }
}
