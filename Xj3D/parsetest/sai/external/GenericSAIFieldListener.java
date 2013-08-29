import java.util.StringTokenizer;
import org.web3d.x3d.sai.*;
/**
 * Created on 2004/05/04
 */
/**
 * GenericSAIFieldListener
 * 
 * Generic diagnostic field listener. Prints out timestamp and field value data
 * in response to being notified of a field change.
 * 
 * @author Brad Vender
 */
public class GenericSAIFieldListener implements X3DFieldEventListener {
	String tag;
	public GenericSAIFieldListener() {
	}
	public GenericSAIFieldListener(String aTag) {
		tag = aTag;
	}
	/**
	 * @see org.web3d.x3d.sai.X3DFieldEventListener#readableFieldChanged(org.web3d.x3d.sai.X3DFieldEvent)
	 */
	public void readableFieldChanged(X3DFieldEvent evt) {
		String valueString = "<not processed>";
		try {
			if (evt != null) {
				Object fieldData = evt.getData();
				X3DField eventSource = (X3DField) evt.getSource();
				double eventTime = evt.getTime();
				Object userData = evt.getData();
				String fieldType = "";
				switch (eventSource.getDefinition().getFieldType()) {
					case X3DFieldTypes.MFBOOL :
						fieldType = "MFBool";
						{
							MFBool realType = (MFBool) eventSource;
							boolean value[] = new boolean[realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.MFCOLOR :
						fieldType = "MFColor";
						{
							MFColor realType = (MFColor) eventSource;
							float value[] = new float[3 * realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value, 3);
						}
						break;
					case X3DFieldTypes.MFCOLORRGBA :
						fieldType = "MFColorRGBA";
						{
							MFColorRGBA realType = (MFColorRGBA) eventSource;
							float value[] = new float[4 * realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value,4);
						}
						break;
					case X3DFieldTypes.MFDOUBLE :
						fieldType = "MFDouble";
						{
							MFDouble realType = (MFDouble) eventSource;
							double value[] = new double[realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.MFFLOAT :
						fieldType = "MFFloat";
						{
							MFFloat realType = (MFFloat) eventSource;
							float value[] = new float[realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.MFIMAGE:
						fieldType = "MFImage";
						{
							MFImage realType = (MFImage) eventSource;
							int size=realType.getSize();
							valueString = "Length: "+size;
							for (int counter=0; counter<size; counter++) {
							    valueString = valueString
							    	+"{ #"
							    	+counter
							    	+" height="
							    	+realType.getHeight(counter)
							    	+" width="
							    	+realType.getWidth(counter)
							    	+" components="
							    	+realType.getComponents(counter)
							    	+"}";
							}    
						}
						break;
					case X3DFieldTypes.MFINT32 :
						fieldType = "MFInt32";
						{
							MFInt32 realType = (MFInt32) eventSource;
							int value[] = new int[realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.MFNODE :
						fieldType = "MFNode";
						{
							MFNode realType = (MFNode) eventSource;
							X3DNode value[] = new X3DNode[realType.getSize()];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array of Nodes";
							else {
								valueString = value.length
										+ " Nodes of types {";
								int counter;
								for (counter = 0; counter < value.length; counter++) {
									if (counter > 0)
										valueString = valueString + ",";
									if (value[counter] instanceof X3DProtoInstance)
										valueString = valueString + "ProtoInstance ";
									valueString = valueString
											+ value[counter].getNodeName()+"("+value[counter].getNodeType()[0]+")";
								}
								valueString = valueString + "}";
							}
						}
						break;
					case X3DFieldTypes.MFROTATION :
						fieldType = "MFRotation";
						{
							MFRotation realType = (MFRotation) eventSource;
							float value[] = new float[4 * realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value, 4);
						}
						break;
					case X3DFieldTypes.MFSTRING :
						fieldType = "MFString";
						{
							MFString realType = (MFString) eventSource;
							String value[] = new String[realType.getSize()];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array";
							else {
								valueString = "Array of " + value.length
										+ " strings";
							}
						}
						break;
					case X3DFieldTypes.MFTIME :
						fieldType = "MFTime";
						{
							MFTime realType = (MFTime) eventSource;
							double value[] = new double[realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.MFVEC2D:
						fieldType = "MFVec2D";
						{
							MFVec2d realType=(MFVec2d) eventSource;
							double value[]=new double[2*realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value,2);
						}
						break;
					case X3DFieldTypes.MFVEC2F :
						fieldType = "MFVec2f";
						{
							MFVec2f realType = (MFVec2f) eventSource;
							float value[] = new float[2 * realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value, 2);
						}
						break;
					case X3DFieldTypes.MFVEC3F :
						fieldType = "MFVec3f";
						{
							MFVec3f realType = (MFVec3f) eventSource;
							float value[] = new float[3 * realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value, 3);
						}
						break;
					case X3DFieldTypes.MFVEC3D :
						fieldType = "MFVec3d";
						{
							MFVec3d realType = (MFVec3d) eventSource;
							double value[] = new double[3 * realType.getSize()];
							realType.getValue(value);
							valueString = getValueString(value, 3);
						}
						break;
					case X3DFieldTypes.SFBOOL :
						fieldType = "SFBool";
						{
							SFBool realType = (SFBool) eventSource;
							valueString = getValueString(realType.getValue());
						}
						break;
					case X3DFieldTypes.SFCOLOR :
						fieldType = "SFColor";
						{
							SFColor realType = (SFColor) eventSource;
							float value[] = new float[3];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.SFCOLORRGBA :
						fieldType = "SFColorRGBA";
						{
							SFColorRGBA realType = (SFColorRGBA) eventSource;
							float value[] = new float[4];
							realType.getValue(value);
							valueString = getValueString(value);
						}
						break;
					case X3DFieldTypes.SFDOUBLE :
						fieldType = "SFDouble";
						{
							SFDouble realType = (SFDouble) eventSource;
							valueString = Double.toString(realType.getValue());
						}
						break;
					case X3DFieldTypes.SFFLOAT :
						fieldType = "SFFloat";
						{
							SFFloat realType = (SFFloat) eventSource;
							valueString = Float.toString(realType.getValue());
						}
						break;
					case X3DFieldTypes.SFIMAGE :
						fieldType = "SFImage";
						{
							SFImage realType = (SFImage) eventSource;
							int pixelBuffer[] = new int[realType.getHeight()
									* realType.getWidth()];
							realType.getPixels(pixelBuffer);
							valueString = "h=" + realType.getHeight() + " w="
									+ realType.getWidth() + " #components= "
									+ realType.getComponents() + " pixels="
									+ getHexValueString(pixelBuffer);
						}
						break;
					case X3DFieldTypes.SFINT32 :
						fieldType = "SFInt32";
						{
							SFInt32 realType = (SFInt32) eventSource;
							valueString = Integer.toString(realType.getValue());
						}
						break;
					case X3DFieldTypes.SFNODE :
						fieldType = "SFNode";
						{
							SFNode realType = (SFNode) eventSource;
							X3DNode value = realType.getValue();
							if (value != null)
								if (value instanceof X3DProtoInstance)
									valueString = "ProtoInstance of type "+value.getNodeName();
								else
									valueString = "Node of type "
										+ value.getNodeName();
							else
								valueString = "NULL Node";
						}
						break;
					case X3DFieldTypes.SFROTATION :
						fieldType = "SFRotation";
						{
							SFRotation realType = (SFRotation) eventSource;
							float value[] = new float[4];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array";
							else
								valueString = value[0] + " " + value[1] + " "
										+ value[2] + " " + value[3];
						}
						break;
					case X3DFieldTypes.SFSTRING :
						fieldType = "SFString";
						{
							SFString realType = (SFString) eventSource;
							valueString = realType.getValue();
						}
						break;
					case X3DFieldTypes.SFTIME :
						fieldType = "SFTime";
						{
							SFTime realType = (SFTime) eventSource;
							valueString = Double.toString(realType.getValue());
						}
						break;
					case X3DFieldTypes.SFVEC2F :
						fieldType = "SFVec2f";
						{
							SFVec2f realType = (SFVec2f) eventSource;
							float value[] = new float[2];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array";
							else
								valueString = value[0] + " " + value[1];
						}
						break;
					case X3DFieldTypes.SFVEC2D :
						fieldType = "SFVec2d";
						{
							SFVec2d realType = (SFVec2d) eventSource;
							double value[] = new double[2];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array";
							else
								valueString = value[0] + " " + value[1];
						}
						break;
					case X3DFieldTypes.SFVEC3F :
						fieldType = "SFVec3f";
						{
							SFVec3f realType = (SFVec3f) eventSource;
							float value[] = new float[3];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array";
							else
								valueString = value[0] + " " + value[1] + " "
										+ value[2];
						}
						break;
					case X3DFieldTypes.SFVEC3D :
						fieldType = "SFVec3d";
						{
							SFVec3d realType = (SFVec3d) eventSource;
							double value[] = new double[3];
							realType.getValue(value);
							if (value == null)
								valueString = "NULL array";
							else
								valueString = value[0] + " " + value[1] + " "
										+ value[2];
						}
						break;
					default :
						fieldType = "unknown";
				}
				if (tag != null)
					System.out.println("(" + tag
							+ ")Received an eventOut of type:" + fieldType
							+ " and timestamp:" + eventTime + " and value:"
							+ valueString + " with user data of " + userData);
				else
					System.out.println("Received an eventOut of type:"
							+ fieldType + " and timestamp:" + eventTime
							+ " and value:" + valueString
							+ " with user data of " + userData);
			} else {
				System.out.println("NULL event generated.");
			}
		} catch (Exception e) {
			System.out
					.println("Error processing GenericFieldListener callback.");
			e.printStackTrace(System.out);
		}
	}
	public static String getValueString(boolean value) {
		if (value)
			return "true";
		else
			return "false";
	}
	public static String getValueString(boolean value[]) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					valueString = valueString + " ";
				valueString = valueString + (value[counter]? "true":"false");
			}
			valueString = valueString + "]";
		}
		return valueString;
	}

	public static String getValueString(double value[]) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					valueString = valueString + " ";
				valueString = valueString + value[counter];
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	public static String getValueString(float value[]) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					valueString = valueString + " ";
				valueString = valueString + value[counter];
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	public static String getValueString(float value[], int elemPerGroup) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					if (counter % elemPerGroup == 0
							&& counter != value.length - 1)
						valueString = valueString + ",";
					else if (counter != value.length - 1)
						valueString = valueString + " ";
				valueString = valueString + value[counter];
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	public static String getValueString(double value[], int elemPerGroup) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					if (counter % elemPerGroup == 0
							&& counter != value.length - 1)
						valueString = valueString + ",";
					else if (counter != value.length - 1)
						valueString = valueString + " ";
				valueString = valueString + value[counter];
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	public static String getValueString(int value[]) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					valueString = valueString + " ";
				valueString = valueString + value[counter];
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	public static String getHexValueString(int value[]) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					valueString = valueString + " ";
				valueString = valueString + Integer.toString(value[counter],16);
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	public static String getValueString(int value[][]) {
		if (value == null)
			return "NULL";
		else {
			String temp = "[";
			int counter;
			for (counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					temp = temp + ",";
				int innerValue[] = value[counter];
				if (innerValue == null)
					temp = temp + "NULL";
				else if (innerValue.length == 0)
					temp = temp + "EMPTY";
				else {
					temp = temp + innerValue[0];
					int i = 1;
					for (; i < innerValue.length; i++) {
						temp = temp + " " + innerValue[i];
					}
				}
			}
			temp = temp + "]";
			return temp;
		}
	}
	public static String getValueString(float value[][]) {
		if (value == null)
			return "NULL";
		else {
			String temp = "[";
			int counter;
			for (counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					temp = temp + ",";
				float innerValue[] = value[counter];
				if (innerValue == null)
					temp = temp + "NULL";
				else if (innerValue.length == 0)
					temp = temp + "EMPTY";
				else {
					temp = temp + innerValue[0];
					int i = 1;
					for (; i < innerValue.length; i++) {
						temp = temp + " " + innerValue[i];
					}
				}
			}
			temp = temp + "]";
			return temp;
		}
	}
	/** Returns value formated as a list of quoted strings */
	public static String getValueString(String value[]) {
		String valueString = "";
		if (value == null)
			return "NULL array";
		else {
			valueString = "[";
			for (int counter = 0; counter < value.length; counter++) {
				if (counter > 0)
					valueString = valueString + " ";
				valueString = valueString + getValueString(value[counter]);
			}
			valueString = valueString + "]";
		}
		return valueString;
	}
	/** Returns value formatted as a quoted string */
	public static String getValueString(String value) {
		StringTokenizer temp = new StringTokenizer((String) value,
				"\f\t\n\\\"", true);
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
		return rep + "\"";
	}
}
