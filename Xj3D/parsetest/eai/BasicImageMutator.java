import vrml.eai.field.*;
import vrml.eai.event.*;

import java.util.Random;

/** A demo utility for PixelTexture.
  * All this class does is wait for an event on its SFImage input,
  * do some preselected manipulation on that event's value, and then
  * send the event off to its output.  A thread.sleep is thrown in
  * to avoid running too fast, but it would be much better to do this work
  * in a seperate process.
  */
public class BasicImageMutator implements VrmlEventListener {
  Random r;
  int changeType;
  int loopCounter;
  int loopTop;
  int pixels[];
  EventInSFImage theDest;
  EventOutSFImage theSource;

  public BasicImageMutator(EventOutSFImage source, EventInSFImage dest) {
    r=new Random();
    theDest=dest;
    theSource=source;
    pixels=new int[64];
    loopCounter=0;
    loopTop=20;
  }

  public BasicImageMutator(EventOutSFImage source, EventInSFImage dest,
    int type) {
    this(source,dest);
    changeType=type;
  }

  public void eventOutChanged(VrmlEvent evt) {
      loopCounter++;
      if (loopCounter==loopTop) {
         //System.out.println(".*.");
         loopCounter=0;
      }
      /** Get the event, change it, and then send a new value. */
      EventOutSFImage src=(EventOutSFImage)(evt.getSource());
      src.getPixels(pixels);
      int height=src.getHeight();
      int width=src.getWidth();
      int components=src.getComponents();
      switch (changeType) {
        default:
          for (int counter=0; counter<64; counter++) {
            pixels[counter]++;
          }
          break;
        case 2:
          int pixelZero=pixels[0];
          for (int counter=1; counter<64; counter++) {
            pixels[counter-1]=pixels[counter];
          }
          pixels[63]=pixelZero;
          break;
        case 3:
          {
          int a = ((r.nextInt()/512) % 9);
          int b = ((r.nextInt()/512) % 9);
          if (a<0) a=-a;
          if (b<0) b=-b;
          width=1 << a;
          height=1 << b;
          }
          components=4;
          pixels=new int[width*height];
          for (int counter=0; counter<pixels.length; counter++)
            pixels[counter]=r.nextInt();
          System.out.println("("+width+" by "+height+" image)");
          break;
      }
      theDest.setValue(height,width,components,pixels);
  }

}


