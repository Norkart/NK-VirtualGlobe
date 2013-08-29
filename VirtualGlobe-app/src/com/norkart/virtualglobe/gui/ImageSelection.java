//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ImageSelection  implements Transferable, ClipboardOwner {
  static public DataFlavor imageFlavor = DataFlavor.imageFlavor;
  private DataFlavor[] flavors = {imageFlavor};
  private BufferedImage image;


  public ImageSelection(BufferedImage image) {
    this.image = image;
  }
  public synchronized DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(imageFlavor);
  }
  public synchronized Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if(flavor.equals(imageFlavor)) {
      return image;
    }
    else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  public void lostOwnership(Clipboard c, Transferable t) {
  }
}