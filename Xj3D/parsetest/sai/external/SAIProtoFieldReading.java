
/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.vrml.scripting.external.sai.SAINode;
import org.web3d.x3d.sai.*;

/**
 * Testing reading from a PROTO node whose fields have not yet generated
 * events.
 *   <P>
 */

public class SAIProtoFieldReading {
  public static void main(String[] args) {
    ExternalBrowser browser=SAITestFactory.getBrowser();

    browser.addBrowserListener(new GenericSAIBrowserListener());
    /* Test one */
    X3DScene scene=browser.createX3DFromString(
      "PROFILE Interactive\n"+
      "PROTO test [ "+
	  " outputOnly SFBool a "+
	  " outputOnly SFColor b "+
	  " outputOnly SFColorRGBA b2 "+
	  " outputOnly SFFloat c "+
	  " outputOnly SFDouble c2 "+
	  " outputOnly SFImage d "+
	  " outputOnly SFInt32 e "+
	  " outputOnly SFNode f  "+
	  " outputOnly SFRotation g "+
	  " outputOnly SFString h " +
	  " outputOnly SFTime i " +
	  " outputOnly SFVec2f j " +
	  " outputOnly SFVec2d j2 " +
	  " outputOnly SFVec3f k " +
	  " outputOnly SFVec3d k2 " +
	  " outputOnly MFBool a2 " +
	  " outputOnly MFColor l " +
	  " outputOnly MFColorRGBA l2 " +
	  " outputOnly MFFloat m " +
	  " outputOnly MFDouble m2 "+
	  " outputOnly MFImage d2 "+
	  " outputOnly MFInt32 n " +
	  " outputOnly MFNode o " +
	  " outputOnly MFRotation p " +
	  " outputOnly MFString q " +
	  " outputOnly MFTime r " +
	  " outputOnly MFVec2f s " +
	  " outputOnly MFVec2d s2 "+
	  " outputOnly MFVec3f t " +
	  " outputOnly MFVec3d t2 "+
	  "]{ Group {}} test {}"
    );

	X3DNode nodes[]=scene.getRootNodes();

    SFBool a = (SFBool)nodes[0].getField("a");
    SFColor b = (SFColor)nodes[0].getField("b");
    SFColorRGBA b2 = (SFColorRGBA)nodes[0].getField("b2");
    SFFloat c = (SFFloat)nodes[0].getField("c");
    SFDouble c2 = (SFDouble)nodes[0].getField("c2");
    SFImage d = (SFImage)nodes[0].getField("d");
    SFInt32 e = (SFInt32)nodes[0].getField("e");
    SFNode f = (SFNode)nodes[0].getField("f");
    SFRotation g = (SFRotation)nodes[0].getField("g");
    SFString h = (SFString)nodes[0].getField("h");
    SFTime i = (SFTime)nodes[0].getField("i");
    SFVec2f j = (SFVec2f)nodes[0].getField("j");
    SFVec2d j2 = (SFVec2d)nodes[0].getField("j2");
    SFVec3f k = (SFVec3f)nodes[0].getField("k");
    SFVec3d k2 = (SFVec3d)nodes[0].getField("k2");
    MFBool a2 = (MFBool)nodes[0].getField("a2");
    MFColor l = (MFColor)nodes[0].getField("l");
    MFColorRGBA l2 = (MFColorRGBA)nodes[0].getField("l2");
    MFFloat m = (MFFloat)nodes[0].getField("m");
    MFDouble m2 = (MFDouble)nodes[0].getField("m2");
    MFImage d2 = (MFImage)nodes[0].getField("d2");
    MFInt32 n = (MFInt32)nodes[0].getField("n");
    MFNode o = (MFNode)nodes[0].getField("o");
    MFRotation p = (MFRotation)nodes[0].getField("p");
    MFString q = (MFString)nodes[0].getField("q");
    MFTime r = (MFTime)nodes[0].getField("r");
    MFVec2f s = (MFVec2f)nodes[0].getField("s");
    MFVec2d s2 = (MFVec2d)nodes[0].getField("s2");
    MFVec3f t = (MFVec3f)nodes[0].getField("t");
    MFVec3d t2 = (MFVec3d)nodes[0].getField("t2");

    System.out.println("Now getting values.");
    
		try {
			a.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			a2.getValue(new boolean[a2.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			a2.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// Expected to happen
		} catch (RuntimeException bad) {
			bad.printStackTrace();
		}
		try {
			a2.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// Expected to happen
		} catch (RuntimeException bad) {
			bad.printStackTrace();
		}
		try {
			a2.set1Value(-1,false);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// Expected to happen
		} catch (RuntimeException bad) {
			bad.printStackTrace();
		}
		try {
			a2.set1Value(0,false);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen
		} catch (RuntimeException bad) {
			bad.printStackTrace();
		}
		try {
			b.getValue(new float[3]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			b2.getValue(new float[4]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			c.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			c2.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			d.getPixels(new int[d.getHeight()*d.getWidth()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			if (0!=d2.getSize())
				System.err.println("MFImage default size not 0");
			// Not much point in doing the for loop at 0.
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			e.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			f.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			g.getValue(new float[4]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			h.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			i.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			j.getValue(new float[2]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			j2.getValue(new double[2]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			k.getValue(new float[3]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			k2.getValue(new double[3]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l.getValue(new float[3*l.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l.get1Value(0,new float[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l.get1Value(-1,new float[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l.set1Value(0,new float[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			l2.getValue(new float[4*l2.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l2.get1Value(0,new float[4]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l2.get1Value(-1,new float[4]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l2.set1Value(0,new float[4]);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}	
		
		try {
			m.getValue(new float[m.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m.set1Value(0,4.5f);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		
		try {
			m2.getValue(new double[m2.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m2.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m2.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m2.set1Value(0,5.6);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		
		try {
			n.getValue(new int[n.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			n.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			n.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			n.set1Value(0,4);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			o.getValue(new SAINode[o.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			o.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			o.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			o.set1Value(0,null);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		
		try {
			p.getValue(new float[4*p.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			p.get1Value(0,new float[4]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			p.set1Value(0,new float[]{3,4,4,5});
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			q.getValue(new String[q.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			q.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			q.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			q.set1Value(0,"Test");
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			r.getValue(new double[r.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			r.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			r.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			r.set1Value(0,5.4);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			s.getValue(new float[2*s.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s.get1Value(0,new float[2]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s.get1Value(-1,new float[2]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s.set1Value(0,new float[]{4,9});
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			s2.getValue(new double[2*s2.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s2.get1Value(0,new double[2]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s2.get1Value(-1,new double[2]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s2.set1Value(0,new double[]{4,7});
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

		try {
			t.getValue(new float[3*t.getSize()]);
			t.getValue(new float[3][t.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t.get1Value(0,new float[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t.get1Value(-1,new float[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t.set1Value(0,new float[]{0,2,4});
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		
		try {
			t2.getValue(new double[3*t2.getSize()]);
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t2.get1Value(0,new double[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t2.get1Value(-1,new double[3]);
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t2.set1Value(0,new double[]{4,2,9});
		} catch (ArrayIndexOutOfBoundsException itsGood) {
			// expected to happen
		} catch (RuntimeException re) {
			re.printStackTrace();
		}

    System.out.println("Done getting values.");
  }
}
