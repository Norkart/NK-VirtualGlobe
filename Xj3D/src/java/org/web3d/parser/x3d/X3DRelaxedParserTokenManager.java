/* Generated By:JavaCC: Do not edit this line. X3DRelaxedParserTokenManager.java */
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
package org.web3d.parser.x3d;
// Standard imports
import java.io.Reader;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.sav.*;
import org.web3d.util.ErrorReporter;
import org.web3d.util.StringArray;

public class X3DRelaxedParserTokenManager implements X3DRelaxedParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x40000000L) != 0L)
            return 8;
         if ((active0 & 0x100L) != 0L)
            return 1;
         if ((active0 & 0x7fbffe0000L) != 0L)
         {
            jjmatchedKind = 39;
            return 20;
         }
         return -1;
      case 1:
         if ((active0 & 0x778ffe0000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 1;
            return 20;
         }
         if ((active0 & 0x830000000L) != 0L)
            return 20;
         return -1;
      case 2:
         if ((active0 & 0x778ff80000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 2;
            return 20;
         }
         if ((active0 & 0x60000L) != 0L)
            return 20;
         return -1;
      case 3:
         if ((active0 & 0x578fe00000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 3;
            return 20;
         }
         if ((active0 & 0x2000180000L) != 0L)
            return 20;
         return -1;
      case 4:
         if ((active0 & 0x1787c00000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 4;
            return 20;
         }
         if ((active0 & 0x4008200000L) != 0L)
            return 20;
         return -1;
      case 5:
         if ((active0 & 0x187c00000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 5;
            return 20;
         }
         if ((active0 & 0x1600000000L) != 0L)
            return 20;
         return -1;
      case 6:
         if ((active0 & 0x107c00000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 6;
            return 20;
         }
         if ((active0 & 0x80000000L) != 0L)
            return 20;
         return -1;
      case 7:
         if ((active0 & 0x107c00000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 7;
            return 20;
         }
         return -1;
      case 8:
         if ((active0 & 0x7800000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 8;
            return 20;
         }
         if ((active0 & 0x100400000L) != 0L)
            return 20;
         return -1;
      case 9:
         if ((active0 & 0x7000000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 9;
            return 20;
         }
         if ((active0 & 0x800000L) != 0L)
            return 20;
         return -1;
      case 10:
         if ((active0 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 10;
            return 20;
         }
         if ((active0 & 0x6000000L) != 0L)
            return 20;
         return -1;
      case 11:
         if ((active0 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 11;
            return 20;
         }
         return -1;
      case 12:
         if ((active0 & 0x1000000L) != 0L)
         {
            jjmatchedKind = 39;
            jjmatchedPos = 12;
            return 20;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 35:
         return jjMoveStringLiteralDfa1_0(0x100L);
      case 46:
         return jjStartNfaWithStates_0(0, 30, 8);
      case 65:
         return jjMoveStringLiteralDfa1_0(0x800000000L);
      case 67:
         return jjMoveStringLiteralDfa1_0(0x100000000L);
      case 68:
         return jjMoveStringLiteralDfa1_0(0x20000L);
      case 69:
         return jjMoveStringLiteralDfa1_0(0x204000000L);
      case 70:
         return jjMoveStringLiteralDfa1_0(0x4000000000L);
      case 73:
         return jjMoveStringLiteralDfa1_0(0x420000000L);
      case 77:
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 78:
         return jjMoveStringLiteralDfa1_0(0x80000L);
      case 80:
         return jjMoveStringLiteralDfa1_0(0x80200000L);
      case 82:
         return jjMoveStringLiteralDfa1_0(0x8000000L);
      case 83:
         return jjMoveStringLiteralDfa1_0(0x1000000000L);
      case 84:
         return jjMoveStringLiteralDfa1_0(0x2010000000L);
      case 85:
         return jjMoveStringLiteralDfa1_0(0x40000L);
      case 91:
         return jjStopAtPos(0, 15);
      case 93:
         return jjStopAtPos(0, 16);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x3400000L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x800000L);
      case 123:
         return jjStopAtPos(0, 13);
      case 125:
         return jjStopAtPos(0, 14);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 47:
         return jjMoveStringLiteralDfa2_0(active0, 0x100L);
      case 65:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000000L);
      case 69:
         return jjMoveStringLiteralDfa2_0(active0, 0x120000L);
      case 77:
         return jjMoveStringLiteralDfa2_0(active0, 0x400000000L);
      case 79:
         if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_0(1, 28, 20);
         return jjMoveStringLiteralDfa2_0(active0, 0x108000000L);
      case 82:
         return jjMoveStringLiteralDfa2_0(active0, 0x2080200000L);
      case 83:
         if ((active0 & 0x20000000L) != 0L)
            return jjStartNfaWithStates_0(1, 29, 20);
         else if ((active0 & 0x800000000L) != 0L)
            return jjStartNfaWithStates_0(1, 35, 20);
         return jjMoveStringLiteralDfa2_0(active0, 0x40000L);
      case 85:
         return jjMoveStringLiteralDfa2_0(active0, 0x80000L);
      case 88:
         return jjMoveStringLiteralDfa2_0(active0, 0x204000000L);
      case 99:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000000000L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x3400000L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x800000L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x100L) != 0L)
            return jjStopAtPos(2, 8);
         break;
      case 69:
         if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(2, 18, 20);
         break;
      case 70:
         if ((active0 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(2, 17, 20);
         break;
      case 76:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000080000L);
      case 77:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000000L);
      case 79:
         return jjMoveStringLiteralDfa3_0(active0, 0x80200000L);
      case 80:
         return jjMoveStringLiteralDfa3_0(active0, 0x600000000L);
      case 84:
         return jjMoveStringLiteralDfa3_0(active0, 0x4100000L);
      case 85:
         return jjMoveStringLiteralDfa3_0(active0, 0x2008000000L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x2400000L);
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000000L);
      case 116:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 65:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(3, 20, 20);
         break;
      case 69:
         if ((active0 & 0x2000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 37, 20);
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000L);
      case 70:
         return jjMoveStringLiteralDfa4_0(active0, 0x80000000L);
      case 76:
         if ((active0 & 0x80000L) != 0L)
            return jjStartNfaWithStates_0(3, 19, 20);
         break;
      case 79:
         return jjMoveStringLiteralDfa4_0(active0, 0x600000000L);
      case 80:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000L);
      case 83:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000000L);
      case 84:
         return jjMoveStringLiteralDfa4_0(active0, 0x8200000L);
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x1000000000L);
      case 112:
         return jjMoveStringLiteralDfa4_0(active0, 0x800000L);
      case 116:
         return jjMoveStringLiteralDfa4_0(active0, 0x1000000L);
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x2400000L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x8000000L) != 0L)
            return jjStartNfaWithStates_0(4, 27, 20);
         else if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 38, 20);
         break;
      case 73:
         return jjMoveStringLiteralDfa5_0(active0, 0x80000000L);
      case 79:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(4, 21, 20);
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000L);
      case 82:
         return jjMoveStringLiteralDfa5_0(active0, 0x604000000L);
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x1000000L);
      case 112:
         return jjMoveStringLiteralDfa5_0(active0, 0x1000000000L);
      case 116:
         return jjMoveStringLiteralDfa5_0(active0, 0x2400000L);
      case 117:
         return jjMoveStringLiteralDfa5_0(active0, 0x800000L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 76:
         return jjMoveStringLiteralDfa6_0(active0, 0x80000000L);
      case 78:
         return jjMoveStringLiteralDfa6_0(active0, 0x104000000L);
      case 79:
         return jjMoveStringLiteralDfa6_0(active0, 0x2400000L);
      case 84:
         if ((active0 & 0x200000000L) != 0L)
            return jjStartNfaWithStates_0(5, 33, 20);
         else if ((active0 & 0x400000000L) != 0L)
            return jjStartNfaWithStates_0(5, 34, 20);
         break;
      case 97:
         return jjMoveStringLiteralDfa6_0(active0, 0x1000000L);
      case 116:
         if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 36, 20);
         return jjMoveStringLiteralDfa6_0(active0, 0x800000L);
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x80000000L) != 0L)
            return jjStartNfaWithStates_0(6, 31, 20);
         return jjMoveStringLiteralDfa7_0(active0, 0x100000000L);
      case 79:
         return jjMoveStringLiteralDfa7_0(active0, 0x800000L);
      case 80:
         return jjMoveStringLiteralDfa7_0(active0, 0x4000000L);
      case 108:
         return jjMoveStringLiteralDfa7_0(active0, 0x1000000L);
      case 110:
         return jjMoveStringLiteralDfa7_0(active0, 0x400000L);
      case 117:
         return jjMoveStringLiteralDfa7_0(active0, 0x2000000L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 78:
         return jjMoveStringLiteralDfa8_0(active0, 0x100000000L);
      case 82:
         return jjMoveStringLiteralDfa8_0(active0, 0x4000000L);
      case 105:
         return jjMoveStringLiteralDfa8_0(active0, 0x1000000L);
      case 108:
         return jjMoveStringLiteralDfa8_0(active0, 0x400000L);
      case 110:
         return jjMoveStringLiteralDfa8_0(active0, 0x800000L);
      case 116:
         return jjMoveStringLiteralDfa8_0(active0, 0x2000000L);
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(6, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0);
      return 8;
   }
   switch(curChar)
   {
      case 79:
         return jjMoveStringLiteralDfa9_0(active0, 0x4000000L);
      case 84:
         if ((active0 & 0x100000000L) != 0L)
            return jjStartNfaWithStates_0(8, 32, 20);
         break;
      case 108:
         return jjMoveStringLiteralDfa9_0(active0, 0x800000L);
      case 112:
         return jjMoveStringLiteralDfa9_0(active0, 0x2000000L);
      case 121:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(8, 22, 20);
         break;
      case 122:
         return jjMoveStringLiteralDfa9_0(active0, 0x1000000L);
      default :
         break;
   }
   return jjStartNfa_0(7, active0);
}
private final int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(7, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(8, active0);
      return 9;
   }
   switch(curChar)
   {
      case 84:
         return jjMoveStringLiteralDfa10_0(active0, 0x4000000L);
      case 101:
         return jjMoveStringLiteralDfa10_0(active0, 0x1000000L);
      case 117:
         return jjMoveStringLiteralDfa10_0(active0, 0x2000000L);
      case 121:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(9, 23, 20);
         break;
      default :
         break;
   }
   return jjStartNfa_0(8, active0);
}
private final int jjMoveStringLiteralDfa10_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(8, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(9, active0);
      return 10;
   }
   switch(curChar)
   {
      case 79:
         if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_0(10, 26, 20);
         return jjMoveStringLiteralDfa11_0(active0, 0x1000000L);
      case 116:
         if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(10, 25, 20);
         break;
      default :
         break;
   }
   return jjStartNfa_0(9, active0);
}
private final int jjMoveStringLiteralDfa11_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(9, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(10, active0);
      return 11;
   }
   switch(curChar)
   {
      case 110:
         return jjMoveStringLiteralDfa12_0(active0, 0x1000000L);
      default :
         break;
   }
   return jjStartNfa_0(10, active0);
}
private final int jjMoveStringLiteralDfa12_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(10, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(11, active0);
      return 12;
   }
   switch(curChar)
   {
      case 108:
         return jjMoveStringLiteralDfa13_0(active0, 0x1000000L);
      default :
         break;
   }
   return jjStartNfa_0(11, active0);
}
private final int jjMoveStringLiteralDfa13_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(11, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(12, active0);
      return 13;
   }
   switch(curChar)
   {
      case 121:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(13, 24, 20);
         break;
      default :
         break;
   }
   return jjStartNfa_0(12, active0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0x7ffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 21;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xfc00877200000000L & l) != 0L)
                  {
                     if (kind > 39)
                        kind = 39;
                     jjCheckNAdd(20);
                  }
                  else if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 11)
                        kind = 11;
                     jjCheckNAdd(9);
                  }
                  else if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  else if (curChar == 46)
                     jjCheckNAdd(8);
                  else if (curChar == 45)
                     jjCheckNAddTwoStates(7, 8);
                  else if (curChar == 35)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if ((0xffff7fffffffffffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 2:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 3:
                  if ((0x2400L & l) != 0L && kind > 7)
                     kind = 7;
                  break;
               case 4:
                  if (curChar == 10 && kind > 7)
                     kind = 7;
                  break;
               case 5:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 6:
                  if (curChar == 45)
                     jjCheckNAddTwoStates(7, 8);
                  break;
               case 7:
                  if (curChar == 46)
                     jjCheckNAdd(8);
                  break;
               case 8:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  jjCheckNAdd(9);
                  break;
               case 9:
                  if ((0x3ff680000000000L & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  jjCheckNAdd(9);
                  break;
               case 10:
                  if (curChar == 34)
                     jjCheckNAddStates(0, 2);
                  break;
               case 11:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 13:
                  if ((0x8400000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 14:
                  if (curChar == 34 && kind > 12)
                     kind = 12;
                  break;
               case 15:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(6, 9);
                  break;
               case 16:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 17:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 18:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAdd(16);
                  break;
               case 19:
                  if ((0xfc00877200000000L & l) == 0L)
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(20);
                  break;
               case 20:
                  if ((0xffffaf7200000000L & l) == 0L)
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(20);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7ffffffc7ffffffL & l) == 0L)
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(20);
                  break;
               case 1:
               case 2:
                  jjCheckNAddStates(3, 5);
                  break;
               case 9:
                  if ((0x100007e0000007eL & l) == 0L)
                     break;
                  if (kind > 11)
                     kind = 11;
                  jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 12:
                  if (curChar == 92)
                     jjAddStates(10, 12);
                  break;
               case 13:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 20:
                  if ((0x57ffffffc7ffffffL & l) == 0L)
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(20);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 20:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 39)
                     kind = 39;
                  jjCheckNAdd(20);
                  break;
               case 1:
               case 2:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(3, 5);
                  break;
               case 11:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(0, 2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 21 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_1(0x200L);
      default :
         return 1;
   }
}
private final int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 47:
         return jjMoveStringLiteralDfa2_1(active0, 0x200L);
      default :
         return 2;
   }
}
private final int jjMoveStringLiteralDfa2_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return 2;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 2;
   }
   switch(curChar)
   {
      case 35:
         if ((active0 & 0x200L) != 0L)
            return jjStopAtPos(2, 9);
         break;
      default :
         return 3;
   }
   return 3;
}
static final int[] jjnextStates = {
   11, 12, 14, 2, 3, 5, 11, 12, 16, 14, 13, 15, 17, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
"\173", "\175", "\133", "\135", "\104\105\106", "\125\123\105", "\116\125\114\114", 
"\115\105\124\101", "\120\122\117\124\117", "\151\156\160\165\164\117\156\154\171", 
"\157\165\164\160\165\164\117\156\154\171", "\151\156\151\164\151\141\154\151\172\145\117\156\154\171", 
"\151\156\160\165\164\117\165\164\160\165\164", "\105\130\124\105\122\116\120\122\117\124\117", "\122\117\125\124\105", 
"\124\117", "\111\123", "\56", "\120\122\117\106\111\114\105", 
"\103\117\115\120\117\116\105\116\124", "\105\130\120\117\122\124", "\111\115\120\117\122\124", "\101\123", 
"\123\143\162\151\160\164", "\124\122\125\105", "\106\101\114\123\105", null, null, null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
   "IN_MULTI_LINE_COMMENT", 
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0xfffffff801L, 
};
static final long[] jjtoSkip = {
   0x2feL, 
};
static final long[] jjtoSpecial = {
   0x280L, 
};
static final long[] jjtoMore = {
   0x500L, 
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[21];
private final int[] jjstateSet = new int[42];
StringBuffer image;
int jjimageLen;
int lengthOfMatch;
protected char curChar;
public X3DRelaxedParserTokenManager(JavaCharStream stream){
   if (JavaCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public X3DRelaxedParserTokenManager(JavaCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 21; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 2 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }
   image = null;
   jjimageLen = 0;

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         try { input_stream.backup(0);
            while (curChar <= 44 && (0x100100003600L & (1L << curChar)) != 0L)
               curChar = input_stream.BeginToken();
         }
         catch (java.io.IOException e1) { continue EOFLoop; }
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         break;
       case 1:
         try { input_stream.backup(0);
            while (curChar <= 44 && (0x100100003600L & (1L << curChar)) != 0L)
               curChar = input_stream.BeginToken();
         }
         catch (java.io.IOException e1) { continue EOFLoop; }
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 10)
         {
            jjmatchedKind = 10;
         }
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken.specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == null)
                 specialToken = matchedToken;
              else
              {
                 matchedToken.specialToken = specialToken;
                 specialToken = (specialToken.next = matchedToken);
              }
              SkipLexicalActions(matchedToken);
           }
           else 
              SkipLexicalActions(null);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
        jjimageLen += jjmatchedPos + 1;
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
  }
}

void SkipLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      default :
         break;
   }
}
}
