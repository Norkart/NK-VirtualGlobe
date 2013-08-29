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

import vrml.eai.Browser;
import vrml.eai.Node;
import vrml.eai.field.*;


/**
 *   A test to try out createVrmlFromString, and tries to read a
 *   variety of PROTO fields which have not had events sent on them.
 *   <P>
 */

public class ProtoFieldReading {
  public static void main(String[] args) {
    Browser browser=TestFactory.getBrowser();

    browser.addBrowserListener(new GenericBrowserListener());
    /* Test one */
    Node nodes[]=browser.createVrmlFromString(
      "PROTO test [ "+
	  " eventOut SFBool a "+
	  " eventOut SFColor b "+
	  " eventOut SFFloat c "+
	  " eventOut SFImage d "+
	  " eventOut SFInt32 e "+
	  " eventOut SFNode f  "+
	  " eventOut SFRotation g "+
	  " eventOut SFString h " +
	  " eventOut SFTime i " +
	  " eventOut SFVec2f j " +
	  " eventOut SFVec3f k " +
	  " eventOut MFColor l " +
	  " eventOut MFFloat m " +
	  " eventOut MFInt32 n " +
	  " eventOut MFNode o " +
	  " eventOut MFRotation p " +
	  " eventOut MFString q " +
	  " eventOut MFTime r " +
	  " eventOut MFVec2f s " +
	  " eventOut MFVec3f t " +
	  "]{ Group {}} test {}"
    );

    EventOutSFBool a = (EventOutSFBool)nodes[0].getEventOut("a");
    EventOutSFColor b = (EventOutSFColor)nodes[0].getEventOut("b");
    EventOutSFFloat c = (EventOutSFFloat)nodes[0].getEventOut("c");
    EventOutSFImage d = (EventOutSFImage)nodes[0].getEventOut("d");
    EventOutSFInt32 e = (EventOutSFInt32)nodes[0].getEventOut("e");
    EventOutSFNode f = (EventOutSFNode)nodes[0].getEventOut("f");
    EventOutSFRotation g = (EventOutSFRotation)nodes[0].getEventOut("g");
    EventOutSFString h = (EventOutSFString)nodes[0].getEventOut("h");
    EventOutSFTime i = (EventOutSFTime)nodes[0].getEventOut("i");
    EventOutSFVec2f j = (EventOutSFVec2f)nodes[0].getEventOut("j");
    EventOutSFVec3f k = (EventOutSFVec3f)nodes[0].getEventOut("k");
    EventOutMFColor l = (EventOutMFColor)nodes[0].getEventOut("l");
    EventOutMFFloat m = (EventOutMFFloat)nodes[0].getEventOut("m");
    EventOutMFInt32 n = (EventOutMFInt32)nodes[0].getEventOut("n");
    EventOutMFNode o = (EventOutMFNode)nodes[0].getEventOut("o");
    EventOutMFRotation p = (EventOutMFRotation)nodes[0].getEventOut("p");
    EventOutMFString q = (EventOutMFString)nodes[0].getEventOut("q");
    EventOutMFTime r = (EventOutMFTime)nodes[0].getEventOut("r");
    EventOutMFVec2f s = (EventOutMFVec2f)nodes[0].getEventOut("s");
    EventOutMFVec3f t = (EventOutMFVec3f)nodes[0].getEventOut("t");

    System.out.println("Now getting values.");
    
		try {
			a.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			b.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			c.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			d.getPixels();
			d.getHeight();
			d.getWidth();
			d.getComponents();
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
			g.getValue();
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
			j.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			k.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			l.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			l.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			m.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			m.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			m.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			n.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			n.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			n.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			o.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			o.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			o.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			p.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			p.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			p.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			q.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			q.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			q.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			r.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			r.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			r.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			s.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			s.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			s.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			t.getValue();
		} catch (RuntimeException re) {
			re.printStackTrace();
		}
		try {
			t.get1Value(0);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			t.get1Value(-1);
		} catch (ArrayIndexOutOfBoundsException aio) {
			// Expected to happen--its okay
		} catch (Exception ex) {
			ex.printStackTrace();
		}

    System.out.println("Done getting values.");
  }
}
