/* Generated By:JavaCC: Do not edit this line. VRML97RelaxedParserConstants.java */
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

public interface VRML97RelaxedParserConstants {

  int EOF = 0;
  int COMMENT = 7;
  int NUMBER_LITERAL = 8;
  int STRING_LITERAL = 9;
  int LBRACE = 10;
  int RBRACE = 11;
  int LBRACKET = 12;
  int RBRACKET = 13;
  int DEF = 14;
  int USE = 15;
  int NULL = 16;
  int PROTO = 17;
  int EVENTIN = 18;
  int EVENTOUT = 19;
  int FIELD = 20;
  int EXPOSEDFIELD = 21;
  int EXTERNPROTO = 22;
  int ROUTE = 23;
  int TO = 24;
  int IS = 25;
  int DOT = 26;
  int SCRIPT = 27;
  int TRUE = 28;
  int FALSE = 29;
  int ID = 30;
  int ID_FIRST = 31;
  int ID_REST = 32;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "\",\"",
    "<COMMENT>",
    "<NUMBER_LITERAL>",
    "<STRING_LITERAL>",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\"DEF\"",
    "\"USE\"",
    "\"NULL\"",
    "\"PROTO\"",
    "\"eventIn\"",
    "\"eventOut\"",
    "\"field\"",
    "\"exposedField\"",
    "\"EXTERNPROTO\"",
    "\"ROUTE\"",
    "\"TO\"",
    "\"IS\"",
    "\".\"",
    "\"Script\"",
    "\"TRUE\"",
    "\"FALSE\"",
    "<ID>",
    "<ID_FIRST>",
    "<ID_REST>",
  };

}