//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import java.util.Iterator;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ImageFileFilter extends FileFilter {
  public static String getExt(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');
    if (i > 0 &&  i < s.length() - 1)
      ext = s.substring(i+1).toLowerCase();
    if (ext == null) return "";
    return ext;
  }

  public boolean accept(File f) {
    if (f.isDirectory()) return true;

    String ext = getExt(f);
    if (ext == null || ext.length() == 0) return false;
    Iterator wr_it = ImageIO.getImageWritersBySuffix(ext);
    if (wr_it.hasNext() && wr_it.next() != null)
      return true;
    return false;
  }
  public String getDescription() {
    String[] formats = ImageIO.getWriterFormatNames();
    if (formats.length == 0) return "";
    for (int i=0; i<formats.length; ++i)
      formats[i] = formats[i].toLowerCase();

    String fs = formats[0];

    for (int i=1; i< formats.length; ++i) {
      boolean drop_this = false;
      for (int j=0; j<i && !drop_this; ++j)
        if (formats[i].equals(formats[j]))
          drop_this = true;
      if (!drop_this)
        fs += ", " + formats[i];
    }
    return fs;
  }
}