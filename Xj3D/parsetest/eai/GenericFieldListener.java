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

// Java imports
import java.util.StringTokenizer;

// Application specific imports
import vrml.eai.event.VrmlEvent;
import vrml.eai.event.VrmlEventListener;
import vrml.eai.field.*;
import vrml.eai.Node;

/** A generic field event listener with copious debugging text.
  * Only really useful to make sure that things are working correctly in
  * development of the system, and not really intended for real life use.
  * <P>
  * The eventOutChanged method is intended for verifying that the
  *  eventOut has the correct type, class and eventually value, and
  *  will probably end up too verbose for practical uses.
  */

class GenericFieldListener implements VrmlEventListener {
  String tag;

  public GenericFieldListener() {
  }

  public GenericFieldListener(String aTag) {
    tag=aTag;
  }

  public void eventOutChanged(VrmlEvent evt) {
    String valueString="<not processed>";
    try {
      if (evt!=null) {
        Object fieldData=evt.getData();
        BaseField eventSource=evt.getSource();
        double eventTime=evt.getTime();
        Object userData=evt.getData();
        String fieldType="";
        switch(eventSource.getType()) {
          case BaseField.MFColor:
            fieldType="MFColor";
            {
              EventOutMFColor realType=(EventOutMFColor)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.MFFloat:
            fieldType="MFFloat";
            {
              EventOutMFFloat realType=(EventOutMFFloat)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.MFInt32:
            fieldType="MFInt32";
            {
              EventOutMFInt32 realType=(EventOutMFInt32)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.MFNode:
            fieldType="MFNode";
            {
              EventOutMFNode realType=(EventOutMFNode)eventSource;
              Node value[]=realType.getValue();
              if (value==null)
                valueString="NULL array of Nodes";
              else {
                valueString=value.length+" Nodes of types {";
                int counter;
                for (counter=0; counter<value.length; counter++) {
                  if (counter>0) valueString=valueString+",";
                  valueString=valueString+value[counter].getType();
                }
                valueString=valueString+"}";
              }
            }
            break;
          case BaseField.MFRotation:
            fieldType="MFRotation";
            {
              EventOutMFRotation realType=(EventOutMFRotation)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.MFString:
            fieldType="MFString";
            {
              EventOutMFString realType=(EventOutMFString)eventSource;
              String value[]=realType.getValue();
              if (value==null)
                valueString="NULL array";
              else {
                valueString="Array of "+value.length+" strings";
              }
            }
            break;
          case BaseField.MFTime:
            fieldType="MFTime";
            {
              EventOutMFTime realType=(EventOutMFTime)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.MFVec2f:
            fieldType="MFVec2f";
            {
              EventOutMFVec2f realType=(EventOutMFVec2f)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.MFVec3f:
            fieldType="MFVec3f";
            {
              EventOutMFVec3f realType=(EventOutMFVec3f)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.SFBool:
            fieldType="SFBool";
            {
              EventOutSFBool realType=(EventOutSFBool)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.SFColor:
            fieldType="SFColor";
            {
              EventOutSFColor realType=(EventOutSFColor)eventSource;
              valueString=getValueString(realType.getValue());
            }
            break;
          case BaseField.SFFloat:
            fieldType="SFFloat";
            {
              EventOutSFFloat realType=(EventOutSFFloat)eventSource;
              valueString=Float.toString(realType.getValue());
            }
            break;
          case BaseField.SFImage:
            fieldType="SFImage";
            {
              EventOutSFImage realType=(EventOutSFImage)eventSource;
              valueString="h="+realType.getHeight()+
                          " w="+realType.getWidth()+
                          " #components= "+realType.getComponents()+
                          " pixels="+getValueString(realType.getPixels());
            }
            break;
          case BaseField.SFInt32:
            fieldType="SFInt32";
            {
              EventOutSFInt32 realType=(EventOutSFInt32)eventSource;
              valueString=Integer.toString(realType.getValue());
            }
            break;
          case BaseField.SFNode:
            fieldType="SFNode";
            {
              EventOutSFNode realType=(EventOutSFNode)eventSource;
              Node value=realType.getValue();
              if (value!=null)
                valueString="Node of type "+value.getType();
              else
                valueString="NULL Node";
            }
            break;
          case BaseField.SFRotation:
            fieldType="SFRotation";
            {
              EventOutSFRotation realType=(EventOutSFRotation)eventSource;
              float value[]=realType.getValue();
              if (value==null)
                  valueString="NULL array";
              else 
                  valueString=value[0]+" "+value[1]+" "+value[2]+" "+value[3];
            }
            break;
          case BaseField.SFString:
            fieldType="SFString";
            {
              EventOutSFString realType=(EventOutSFString)eventSource;
              valueString=realType.getValue();
            }
            break;
          case BaseField.SFTime:
            fieldType="SFTime";
            {
              EventOutSFTime realType=(EventOutSFTime)eventSource;
              valueString=Double.toString(realType.getValue());
            }
            break;
          case BaseField.SFVec2f:
            fieldType="SFVec2f";
            {
              EventOutSFVec2f realType=(EventOutSFVec2f)eventSource;
              float value[]=realType.getValue();
              if (value==null)
                  valueString="NULL array";
              else
                  valueString=value[0]+" "+value[1];
            }
            break;
          case BaseField.SFVec3f:
            fieldType="SFVec3f";
            {
              EventOutSFVec3f realType=(EventOutSFVec3f)eventSource;
              float value[]=realType.getValue();
              if (value==null)
                  valueString="NULL array";
              else 
                  valueString=value[0]+" "+value[1]+" "+value[2];
            }
            break;
          default:
            fieldType="unknown";
        }
        if (tag!=null)
          System.out.println("("+tag+")Received an eventOut of type:"+fieldType+
            " and timestamp:"+eventTime+" and value:"+valueString+" with user data of "+userData);
        else 
          System.out.println("Received an eventOut of type:"+fieldType+
            " and timestamp:"+eventTime+" and value:"+valueString+" with user data of "+userData);
      } else {
        System.out.println("NULL event generated.");
      }
    } catch (Exception e) {
      System.out.println("Error processing GenericFieldListener callback.");
      e.printStackTrace(System.out);
    }
  }

  public static String getValueString(boolean value) {
    if (value)
      return "true";
    else
      return "false";
  }

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

  /** Returns value formated as a list of quoted strings */
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

  /** Returns value formatted as a quoted string */
  public static String getValueString(String value) {
    StringTokenizer temp = new StringTokenizer((String)value, "\f\t\n\\\"", true);
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

}

