/* Generated By:JavaCC: Do not edit this line. VRML97FieldParserConstants.java */
/*****************************************************************************
 *                        Web3d Consortium Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

package org.web3d.parser.vrml97;

public interface VRML97FieldParserConstants {

  int EOF = 0;
  int NUMBER_LITERAL = 1;
  int STRING_LITERAL = 2;
  int LBRACKET = 3;
  int RBRACKET = 4;
  int TRUE = 5;
  int FALSE = 6;
  int COMMENT = 13;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "<NUMBER_LITERAL>",
    "<STRING_LITERAL>",
    "\"[\"",
    "\"]\"",
    "\"TRUE\"",
    "\"FALSE\"",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "\",\"",
    "<COMMENT>",
  };

}
