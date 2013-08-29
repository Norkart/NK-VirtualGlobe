import java.util.StringTokenizer;

import org.web3d.x3d.sai.*;

/**
 * GenericX3DFieldListener
 * A generic testing field listener which just prints out the values of the
 * events that it sees.
 */

public class GenericTestingX3DFieldListener implements X3DFieldEventListener {

    /** The identifying tag to make events easier to differentiate during
     * testing
     */
	String outputTag;

    /** Make a new generic field listener with specified tag
     * @param aTag The tag to use with all output
     */
    GenericTestingX3DFieldListener(String aTag) {
    	if (aTag != null)
    		outputTag=aTag+":";
    	else
    		outputTag = "";
    }

	/** Make a new generic field listener with no tag */
	GenericTestingX3DFieldListener() {
		this(null);
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(boolean value) {
	  if (value)
		return "true";
	  else
		return "false";
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
    public static String getValueString(boolean value[]) {
    	String valueString="";
    	if (value==null)
    		return "NULL array";
    	else {
    		valueString="[";
    		for (int counter=0; counter<value.length; counter++) {
    			if (counter>0) valueString=valueString+" ";
    			valueString=valueString+value[counter];
    		}
    		valueString=valueString+"]";
    	}
    	return valueString;
    }

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(double value[]) {
	  String valueString="";
	  if (value==null)
		return "NULL array";
	  else {
		valueString="[";
		for (int counter=0; counter<value.length; counter++) {
		  if (counter>0) valueString=valueString+" ";
		  valueString=valueString+value[counter];
		}
		valueString=valueString+"]";
	  }
	  return valueString;
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(float value[]) {
	  String valueString="";
	  if (value==null)
		return "NULL array";
	  else {
		valueString="[";
		for (int counter=0; counter<value.length; counter++) {
		  if (counter>0) valueString=valueString+" ";
		  valueString=valueString+value[counter];
		}
		valueString=valueString+"]";
	  }
	  return valueString;
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(int value[]) {
	  String valueString="";
	  if (value==null)
		return "NULL array";
	  else {
		valueString="[";
		for (int counter=0; counter<value.length; counter++) {
		  if (counter>0) valueString=valueString+" ";
		  valueString=valueString+value[counter];
		}
		valueString=valueString+"]";
	  }
	  return valueString;
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(int value[][]) {
	  if (value==null)
		return "NULL";
	  else {
		String temp="[";
		int counter;
		for (counter=0;counter<value.length;counter++) {
		  if (counter>0)
			temp=temp+",";
		  int innerValue[]=value[counter];
		  if (innerValue==null)
			temp=temp+"NULL";
		  else if (innerValue.length==0)
			temp=temp+"EMPTY";
		  else {
			temp=temp+innerValue[0];
			int i=1;
			for (;i<innerValue.length;i++) {
			  temp=temp+" "+innerValue[i];
			}
		  }
		}
		temp=temp+"]";
		return temp;
	  }
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(double value[][]) {
	  if (value==null)
		return "NULL";
	  else {
		String temp="[";
		int counter;
		for (counter=0;counter<value.length;counter++) {
		  if (counter>0)
			temp=temp+",";
		  double innerValue[]=value[counter];
		  if (innerValue==null)
			temp=temp+"NULL";
		  else if (innerValue.length==0)
			temp=temp+"EMPTY";
		  else {
			temp=temp+innerValue[0];
			int i=1;
			for (;i<innerValue.length;i++) {
			  temp=temp+" "+innerValue[i];
			}
		  }
		}
		temp=temp+"]";
		return temp;
	  }
	}


	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(float value[][]) {
	  if (value==null)
		return "NULL";
	  else {
		String temp="[";
		int counter;
		for (counter=0;counter<value.length;counter++) {
		  if (counter>0)
			temp=temp+",";
		  float innerValue[]=value[counter];
		  if (innerValue==null)
			temp=temp+"NULL";
		  else if (innerValue.length==0)
			temp=temp+"EMPTY";
		  else {
			temp=temp+innerValue[0];
			int i=1;
			for (;i<innerValue.length;i++) {
			  temp=temp+" "+innerValue[i];
			}
		  }
		}
		temp=temp+"]";
		return temp;
	  }
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(String value[]) {
	  String valueString="";
	  if (value==null)
		return "NULL array";
	  else {
		valueString="[";
		for (int counter=0; counter<value.length; counter++) {
		  if (counter>0) valueString=valueString+" ";
		  valueString=valueString+getValueString(value[counter]);
		}
		valueString=valueString+"]";
	  }
	  return valueString;
	}

	/** Returns value formatted as a quoted string
	 * @param value The value to convert.
	 * @return A somewhat nice string representation.
	 */
	public static String getValueString(String value) {
	  StringTokenizer temp = new StringTokenizer(value, "\f\t\n\\\"", true);
	  String rep = "\"";
	  String token;
	  while (temp.hasMoreTokens()) {
		token = temp.nextToken();
		if (token.equals("\\"))
		  rep = rep + "\\\\";
		else if (token.equals("\f"))
		  rep = rep + "\\d";
		else if (token.equals("\t"))
		  rep = rep + "\\t";
		else if (token.equals("\n"))
		  rep = rep + "\\n";
		else if (token.equals("\""))
		  rep = rep + "\\\"";
		else
		  rep = rep + token;
	  }
	  return rep +"\"";
	}


	/** Print out the new field value
	 * @see org.web3d.x3d.sai.X3DFieldEventListener#readableFieldChanged(org.web3d.x3d.sai.X3DFieldEvent)
	 */
	public void readableFieldChanged(X3DFieldEvent evt) {
		X3DField eventSource=(X3DField)evt.getSource();
		double eventTime=evt.getTime();
		Object eventData=evt.getData();
		String fieldValue=readFieldValue(eventSource);
	}
		
	public String readFieldValue(X3DField eventSource) {
		String eventType;
		String eventValue="Unprocessed";
		switch (eventSource.getDefinition().getFieldType()) {
			case X3DFieldTypes.MFBOOL: {
				eventType="MFBool";
				MFBool event=(MFBool)eventSource;
				boolean value[]=new boolean[event.getSize()];
				event.getValue(value);
				eventValue=getValueString(value);
				break;				
			}
			case X3DFieldTypes.MFCOLOR: {
				eventType="MFColor";
				MFColor event=(MFColor)eventSource;
				float value[][]=makeFloatArray(event.getSize(),3);
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFCOLORRGBA: {
				eventType="MFColorRGBA";
				MFColorRGBA event=(MFColorRGBA)eventSource;
				float value[][]=makeFloatArray(event.getSize(),4);
				event.getValue(value);
				eventValue=getValueString(value);				
				break;
			}
			case X3DFieldTypes.MFDOUBLE: {
				eventType="MFDouble";
				MFFloat event=(MFFloat)eventSource;
				float value[]=new float[event.getSize()];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFFLOAT: {
				eventType="MFFloat";
				MFFloat event=(MFFloat)eventSource;
				float value[]=new float[event.getSize()];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFIMAGE: {
				eventType="MFImage";
				MFImage event=(MFImage)eventSource;
				int numImages=event.getSize();
				if (numImages==0)
					eventValue="0 images";
				else {
					eventValue="{";
					for (int counter=0; counter<numImages; counter++) {
						if (counter>0) eventValue=eventValue+",";
					eventValue="Width:"+event.getWidth(counter)+",Height:"+event.getHeight(counter)+",Components:"+event.getComponents(counter);
					}
					eventValue=eventValue+"}";
				}
				break;
			}
			case X3DFieldTypes.MFINT32: {
				eventType="MFInt32";
				MFInt32 event=(MFInt32)eventSource;
				int value[]=new int[event.getSize()];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFNODE: {
				eventType="MFNode";
				MFNode event=(MFNode)eventSource;
				X3DNode value[]=new X3DNode[event.getSize()];
				event.getValue(value);
				if (value==null)
				  eventValue="NULL array of Nodes";
				else {
				  eventValue=value.length+" Nodes of types {";
				  int counter;
				  for (counter=0; counter<value.length; counter++) {
					if (counter>0) eventValue=eventValue+",";
					eventValue=eventValue+value[counter].getNodeName();
				  }
				  eventValue=eventValue+"}";
				}

				break;
			}
			case X3DFieldTypes.MFROTATION: {
				eventType="MFRotation";
				MFRotation event=(MFRotation)eventSource;
				float value[][]=makeFloatArray(event.getSize(),4);
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFSTRING: {
				eventType="MFString";
				MFString event=(MFString)eventSource;
				String value[]=new String[event.getSize()];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFTIME: {
				eventType="MFTime";
				MFTime event=(MFTime)eventSource;
				double value[]=new double[event.getSize()];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFVEC2F: {
				eventType="MFVec2f";
				MFVec2f event=(MFVec2f)eventSource;
				float value[][]=makeFloatArray(event.getSize(),2);
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFVEC3D: {
				eventType="MFVec3d";
				MFVec3d event=(MFVec3d)eventSource;
				double value[][]=makeDoubleArray(event.getSize(),3);
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.MFVEC3F: {
				eventType="MFVec3f";
				MFVec3f event=(MFVec3f)eventSource;
				float value[][]=makeFloatArray(event.getSize(),3);
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.SFBOOL: {
				eventType="SFBool";
				SFBool event=(SFBool)eventSource;
				if (event.getValue())
					eventValue="TRUE";
				else
					eventValue="FALSE";
				break;
			}
			case X3DFieldTypes.SFCOLOR: {
				eventType="SFColor";
				SFColor event=(SFColor)eventSource;
				float value[]=new float[3];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.SFCOLORRGBA: {
				eventType="SFColorRGBA";
				SFColorRGBA event=(SFColorRGBA)eventSource;
				float value[]=new float[4];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.SFDOUBLE: {
				eventType="SFDouble";
				SFDouble event=(SFDouble)eventSource;
				eventValue=String.valueOf(event.getValue());
				break;
			}
			case X3DFieldTypes.SFFLOAT: {
				eventType="SFFloat";
				SFFloat event=(SFFloat)eventSource;
				eventValue =String.valueOf(event.getValue());
				break;
			}
			case X3DFieldTypes.SFIMAGE: {
				eventType="SFImage";
				SFImage event=(SFImage)eventSource;
				eventValue="Width:"+event.getWidth()+",Height:"+event.getHeight()+",Components:"+event.getComponents();
				break;
			}
			case X3DFieldTypes.SFINT32: {
				eventType="SFInt32";
				SFInt32 event=(SFInt32)eventSource;
				eventValue=String.valueOf(event.getValue());
				break;
			}
			case X3DFieldTypes.SFNODE: {
				eventType="SFNode";
				SFNode event=(SFNode)eventSource;
				X3DNode node=event.getValue();
				if (node!=null)
					eventValue="Type:"+node.getNodeName();
				else
					eventValue="NULL";
				break;
			}
			case X3DFieldTypes.SFROTATION: {
				eventType="SFRotation";
				SFRotation event=(SFRotation)eventSource;
				float value[]=new float[4];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.SFSTRING: {
				eventType="SFString";
				SFString event=(SFString)eventSource;
				eventValue=getValueString(event.getValue());
				break;
			}
			case X3DFieldTypes.SFTIME: {
				eventType="SFTime";
				SFTime event=(SFTime)eventSource;
				eventValue=String.valueOf(event.getValue());
				break;
			}
			case X3DFieldTypes.SFVEC2F: {
				eventType="SFVec2f";
				SFVec2f event=(SFVec2f)eventSource;
				float value[]=new float[2];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.SFVEC3D: {
				eventType="SFVec3d";
				SFVec3d event=(SFVec3d)eventSource;
				double value[]=new double[3];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			case X3DFieldTypes.SFVEC3F: {
				eventType="SFVec3f";
				SFVec3f event=(SFVec3f)eventSource;
				float value[]=new float[3];
				event.getValue(value);
				eventValue=getValueString(value);
				break;
			}
			default:
				eventType="UNKNOWN";
		}
		return eventType+":"+eventValue;
	}


	/**
	 * Make a two dimensional array of floats
	 * @param length The number of sub arrays
	 * @param width The lenght of the sub arrays
	 * @return The newly allocated array
	 */
	double[][] makeDoubleArray(int length, int width) {
		double result[][];
		result=new double[length][];
		for (int counter=0; counter<length; counter++)
			result[counter]=new double[width];
		return result;
	}

	/**
	 * Make a two dimensional array of floats
	 * @param length The number of sub arrays
	 * @param width The lenght of the sub arrays
	 * @return The newly allocated array
	 */
    float[][] makeFloatArray(int length, int width) {
    	float result[][];
    	result=new float[length][];
    	for (int counter=0; counter<length; counter++)
    		result[counter]=new float[width];
    	return result;
    }

}
;