//
//   MyScript.java
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
import vrml.*;
import vrml.field.*;
import vrml.ConstField.*;
import vrml.node.*;

public class MyScript extends Script {
    SFInt32 touchChoice = null;

    LmsNotify lms = null;

    public MyScript() {
	super();
    }

    public void initialize() {
	touchChoice = (SFInt32)getEventOut("touchChoice");
    }

    public void processEvent(Event e) {
	String s;

	if (((ConstSFBool)e.getValue()).getValue()) {
	    touchChoice.setValue(-1);
	} else {
	    touchChoice.setValue(0);
	}
    }
}
