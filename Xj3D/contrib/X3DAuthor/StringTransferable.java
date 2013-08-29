//
//   StringTransferable.java
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
import java.io.*;
import java.awt.datatransfer.*;

public class StringTransferable
    implements Transferable {

    static final DataFlavor flavors[] = {DataFlavor.stringFlavor};

    String data;

    public StringTransferable(String data) {
	super();
	this.data = data;
    }

    public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
	return data;
    }

    public DataFlavor[] getTransferDataFlavors() {
	return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(DataFlavor.stringFlavor);
    }
}
