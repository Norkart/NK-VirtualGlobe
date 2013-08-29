import org.web3d.x3d.sai.*;

import java.util.Random;

/** A demo utility for MFVec3f and Coordinate.
  * All this class does is wait for an event on its MFVec3f input,
  * do some preselected manipulation on that event's value, and then
  * send the event off to its output.  A thread.sleep is thrown in
  * to avoid running too fast, but it would be much better to do this work
  * in a seperate process.
  * <P>
  * This class is mostly concerned with the set1Value testing.
  */
public class BasicSAICoordinateMutator implements X3DFieldEventListener {
  ExternalBrowser theBrowser;
  Random r;
  int changeType;
  int loopCounter;
  int loopTop;
  float positions[];
  float originalPositions[];
  float raised_positions[][];
  float buffer[];
  MFVec3f theDest;
  MFVec3f theSource;
  
  float lastEventTime;

  public BasicSAICoordinateMutator(
    ExternalBrowser b, MFVec3f source, MFVec3f dest
  ) {
    r=new Random();
    theDest=dest;
    theSource=source;
    loopCounter=0;
    loopTop=20;
    theBrowser=b;
    buffer=new float[3];
  }

  public BasicSAICoordinateMutator(
    ExternalBrowser b, MFVec3f source, MFVec3f dest, int type
  ) {
    this(b,source,dest);
    changeType=type;
    lastEventTime=System.currentTimeMillis()/1000;
  }

  public void readableFieldChanged(X3DFieldEvent evt) {
	  System.out.println("Getting an eventOutChanged at "+System.currentTimeMillis());
	  System.out.println("Timestamp"+evt.getTime());
      loopCounter++;
      if (loopCounter==loopTop) {
         //System.out.println(".*.");
         loopCounter=0;
      }
      /** Get the event, change it, and then send a new value. */
      MFVec3f src=(MFVec3f)(evt.getSource());
      int size=src.getSize();
      switch (changeType) {
        default:
          {
            // Random movement of two points.
            theBrowser.beginUpdate();
            int index=Math.abs(r.nextInt() % size);
            src.get1Value(index,buffer);
            buffer[0]=buffer[0]-0.5f+r.nextFloat();
            buffer[1]=buffer[0]-0.5f+r.nextFloat();
            buffer[2]=buffer[0]-0.5f+r.nextFloat();
            theDest.set1Value(index,buffer);
            theDest.set1Value(index,buffer);
            index=Math.abs(r.nextInt() % size);
            src.get1Value(index,buffer);
            buffer[0]=buffer[0]-0.5f+r.nextFloat();
            buffer[1]=buffer[0]-0.5f+r.nextFloat();
            buffer[2]=buffer[0]-0.5f+r.nextFloat();
            theDest.set1Value(index,buffer);
            theBrowser.endUpdate();
          }
          break;
        case 2:
          {
          // Cyclic manipulation of all endpoints.
          if (positions==null) {
            positions=new float[3*src.getSize()];
            raised_positions=new float[src.getSize()][];
            for (int counter=0; counter<src.getSize(); counter++)
              raised_positions[counter]=new float[3];
          }
          src.getValue(positions);
          for (int counter=0; counter<raised_positions.length; counter++) {
            raised_positions[counter][0]=positions[counter*3]+(float)(Math.sin(0.1f*(float)(loopCounter-loopTop/2)));
            raised_positions[counter][1]=positions[counter*3+1]+(float)(Math.sin(0.05f*(float)(loopCounter-loopTop/2)));
            raised_positions[counter][2]=positions[counter*3+2]+(float)(Math.sin(0.05f*(float)(loopCounter-loopTop/2)));
          }
          theDest.setValue(raised_positions.length,raised_positions);
          }
          break;
        case 3:
          break;
      }
	  

  }

}


