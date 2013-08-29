//-----------------------------------------------------------------------------
// 
//                   Copyright (c) Norkart AS 2006-2007
// 
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.gui;

import com.norkart.virtualglobe.util.ApplicationSettings;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.HeadlessException;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class HelpDialog extends JDialog {

  public HelpDialog(JFrame frame) throws HeadlessException {
    super(frame, ApplicationSettings.getApplicationSettings().getResourceString("HELP_DIALOG"));
  }
}