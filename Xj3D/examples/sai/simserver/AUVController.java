/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import mil.navy.nps.dis.*;
import mil.navy.nps.net.*;

import java.net.*;
import java.util.*;

/**
 * Moves an AUV around a world.  Also moves a target around in random
 * places for the AUV to collide with.
 *
 * @author Alan Hudson
 * @version
 */

public class AUVController extends Object implements BehaviorConsumerIF
{
    public static int numEntities = 1;
    public static int pauseTime = 100;

    public static final String DATA_LAYOUT =
      "# EntityXLocation EntityYLocation EntityZLocation velocityX velocityY velocityZ Psi Theta Phi AngVelX AngVelY AngVelZ\n"
    + " 4   14  1.5 3  0   0   0   -.1 0   0   0   0 \n"
    + " 7   14  1.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "10   14  2   3   0   .25 0   -.1 0   0   0   0 \n"
    + "13   14  2.25    3   0   .25 0   -.1 0   0   0   0 \n"
    + "16   14  2.5 3   0   .25 0   -.1 0   0   0   0 \n"
    + "19   14  2.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "22   14  3   3   0   .25 0   -.1 0   0   0   0 \n"
    + "25   14  3.25    3   0   .25 0   -.1 0   0   0   0 \n"
    + "28   14  3.5 3   0   .25 0   -.1 0   0   0   0 \n"
    + "31   14  3.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "34   14  4   1   1   .25 0.785   -.1 0   0   0   0 \n"
    + "35   15  4.25    0   1   .25 1.5708  -.1 0   0   0   0 \n"
    + "35   16  4.5 0   1   .25 1.5708  -.1 0   0   0   0 \n"
    + "35   17  4.75    -1  1   .25 2.355   -.1 0   0   0   0 \n"
    + "34   18  5   -3  0   0   3.1416  .1  0   0   0   0 \n"
    + "31   18  4.75    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "28   18  4.5 -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "25   18  4.25    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "22   18  4   -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "19   18  3.75    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "16   18  3.5 -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "13   18  3.25    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "10   18  3   -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "7    18  2.75    -3  0   -.25    3.9221  .1  0   0   0   0 \n"
    + "4    18  2.5 -1  -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    17  2.25    0   -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    16  2   0   -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    15  1.75    1   -1  -.25    5.497   .1  0   0   0   0 \n";


  private static final int PORT = 62040;
  private static final String GROUP = "224.2.181.145";

  public static void main(String args[])
  {
    if (args.length > 1) {
        numEntities = Integer.parseInt(args[0]);
        pauseTime = 1000 / Integer.parseInt(args[1]);
    }

    System.out.println("Number entities: " + numEntities + " pauseTime: " + pauseTime);

    AUVController controller = new AUVController();
    controller.test();
  }

    public void test() {
System.out.println("Starting Controller");
        DatagramSocket socket = null;
        InetAddress address;
        int pause = 500 / numEntities;
        float offset = -10;

        try {
            try {
                socket = new MulticastSocket(PORT);
                address = InetAddress.getByName(GROUP);
                ((MulticastSocket)socket).joinGroup(address);
            } catch(Exception e) {
                System.out.println("Unicast fallback");
                socket.close();

                socket = new DatagramSocket();
                address = InetAddress.getByName("localhost");
            }

            for(int i=0; i < numEntities; i++) {
                if (i % 10 == 0)
                    offset += 2.5;
                launchEntity(socket, address, PORT, DATA_LAYOUT, 0,1,i,0,0,offset);

                try { Thread.sleep(pause); } catch(Exception e) {}
            }
System.out.println("Launching Target");
            launchTarget(socket, address, PORT, DATA_LAYOUT, 0, 1, numEntities, 0, 0, offset);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    private void launchEntity(DatagramSocket socket, InetAddress address, int port, String path,
        int siteID, int appID, int entityID,
        float xoff, float yoff, float zoff) {

        Thread thread;

        BehaviorProducerUDP writer1 = new BehaviorProducerUDP(socket);
        writer1.setDefaultDestination(address, new Integer(port));
        thread = new Thread(writer1);
        thread.start();

        Runner2 e1 = new Runner2(writer1, path, siteID, appID, entityID, pauseTime);
        e1.setOffset(xoff,yoff,zoff);
        thread = new Thread(e1);
        thread.start();
    }

    private void launchTarget(DatagramSocket socket, InetAddress address, int port, String path,
        int siteID, int appID, int entityID,
        float xoff, float yoff, float zoff) {

        Thread thread;

        BehaviorProducerUDP writer1 = new BehaviorProducerUDP(socket);
        writer1.setDefaultDestination(address, new Integer(port));
        thread = new Thread(writer1);
        thread.start();

        TargetRunner e1 = new TargetRunner(writer1, path, siteID, appID, entityID, 500);
        e1.setOffset(xoff,yoff,zoff);
        thread = new Thread(e1);
        thread.start();
    }

  /**
   * Receives a PDU from the BehaviorProducer.
   *
   * @param pdu the ProtocolDataUnit generated by the BehaviorProducer
   * @param producer the producer that generated the call
   */
  public void receivePdu(ProtocolDataUnit pdu, BehaviorProducerIF producer)
  {
    System.out.println("Got PDU in tester");
  }

  /**
   * Does the same as receivePdu(), but can also pass in an arbitrary
   * object with the other information.
   */
  public void receivePdu(ProtocolDataUnit pdu, BehaviorProducerIF producer, Object data)
  {
    this.receivePdu(pdu, producer);
  }
}

class Runner2 implements Runnable {
    EntityID id;
    BehaviorProducerUDP        writer;
    String data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int pauseTime;

    public Runner2(BehaviorProducerUDP writer, String data, int siteID, int appID, int entityID, int pauseTime) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.pauseTime = pauseTime;
        id = new EntityID(siteID, appID, entityID);
        espdu = new EntityStatePdu();
        espdu.setEntityID(id);
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    public void run() {
        int totalPacketsSent = 0;
        boolean sendingPDUs = true;

        while(sendingPDUs)
        {
            String          lineString;                   // one line from the string
            StringTokenizer lineTokenizer;
            StringTokenizer itemTokenizer;

            // Check to see if we're beyond the time-out limit. If so, generate a fake event
            // that presses the "stop" button. This saves us having to write a bunch of
            // duplicate code.

            lineTokenizer = new StringTokenizer(data, "\r\n");

            // while we have more lines....
            while(lineTokenizer.hasMoreTokens() && sendingPDUs)
            {
              float           pduValues[] = new float[12];  //holds x,y,z; dx,dy,dz; psi,theta,phi; angX,angY,angZ
              int             valueCount = 0;

              // get one line of input, then decode each token in that string
              lineString = lineTokenizer.nextToken();
              itemTokenizer = new StringTokenizer(lineString);
              valueCount = 0;

              while(itemTokenizer.hasMoreTokens())
              {
                float  value;
                String token;

                token = itemTokenizer.nextToken();

                // got a hash mark somewhere in the token; ignore all the rest
                if(token.indexOf('#') != -1)
                  break;

              // Read the value into a float
                value = Float.valueOf(token).floatValue();
                valueCount++;

              // prevents array out of bounds if extra values present
                if(valueCount > 12)
                  break;

                pduValues[valueCount-1] = value;
              }

          if(valueCount == 0)     // got a blank line; skip it, and don't send out a zero PDU
            continue;

          espdu.reset();

          if (entityID == 0) {
              ArticulationParameter p = new ArticulationParameter();
              p.setParameterValue(10.0);
              espdu.addArticulationParameter(p);
          }

          // location
          espdu.setEntityLocationX(pduValues[0] + xoff);
          espdu.setEntityLocationY(pduValues[1] + yoff);
          espdu.setEntityLocationZ(pduValues[2] + zoff);

          // velocity
          espdu.setEntityLinearVelocityX(pduValues[3]);
          espdu.setEntityLinearVelocityY(pduValues[4]);
          espdu.setEntityLinearVelocityZ(pduValues[5]);

          // orientation
          espdu.setEntityOrientationPsi(pduValues[6]);   // h
          espdu.setEntityOrientationTheta(pduValues[7]); // p
          espdu.setEntityOrientationPhi(pduValues[8]);   // r

          // angular velocity
          espdu.setEntityAngularVelocityY(pduValues[9]); // h
          espdu.setEntityAngularVelocityX(pduValues[10]); // p
          espdu.setEntityAngularVelocityZ(pduValues[11]); // r

          espdu.makeTimestampCurrent();
          writer.write(espdu);

          try {
            Thread.sleep(pauseTime);
          } catch(Exception e) {}
          }

        }
    }
}

class TargetRunner implements Runnable {
    EntityID id;
    BehaviorProducerUDP        writer;
    String data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int pauseTime;

    public TargetRunner(BehaviorProducerUDP writer, String data, int siteID, int appID, int entityID, int pauseTime) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.pauseTime = pauseTime;
        id = new EntityID(siteID, appID, entityID);
        espdu = new EntityStatePdu();
        espdu.setEntityID(id);
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    public void run() {
        int totalPacketsSent = 0;
        boolean sendingPDUs = true;

        while(sendingPDUs)
        {
            String          lineString;                   // one line from the string
            StringTokenizer lineTokenizer;
            StringTokenizer itemTokenizer;

            // Check to see if we're beyond the time-out limit. If so, generate a fake event
            // that presses the "stop" button. This saves us having to write a bunch of
            // duplicate code.

            lineTokenizer = new StringTokenizer(data, "\r\n");

            // while we have more lines....
            while(lineTokenizer.hasMoreTokens() && sendingPDUs)
            {
              float           pduValues[] = new float[12];  //holds x,y,z; dx,dy,dz; psi,theta,phi; angX,angY,angZ
              int             valueCount = 0;

              // get one line of input, then decode each token in that string
              lineString = lineTokenizer.nextToken();
              itemTokenizer = new StringTokenizer(lineString);
              valueCount = 0;

              while(itemTokenizer.hasMoreTokens())
              {
                float  value;
                String token;

                token = itemTokenizer.nextToken();

                // got a hash mark somewhere in the token; ignore all the rest
                if(token.indexOf('#') != -1)
                  break;

              // Read the value into a float
                value = Float.valueOf(token).floatValue();
                valueCount++;

              // prevents array out of bounds if extra values present
                if(valueCount > 12)
                  break;

                pduValues[valueCount-1] = value;
              }

          float rand = (float) Math.random();

          if(valueCount == 0 || rand > 0.1f) {     // got a blank line; skip it, and don't send out a zero PDU
              try {
                Thread.sleep(pauseTime);
              } catch(Exception e) {}

            continue;
          }

          espdu.reset();
System.out.println("Moving target:" + entityID + " rand: " + rand);
          if (entityID == 0) {
              ArticulationParameter p = new ArticulationParameter();
              p.setParameterValue(10.0);
              espdu.addArticulationParameter(p);
          }

          // location
          espdu.setEntityLocationX(pduValues[0] + xoff);
          espdu.setEntityLocationY(pduValues[1] + yoff);
          espdu.setEntityLocationZ(pduValues[2] + zoff);

          // velocity
          espdu.setEntityLinearVelocityX(0);
          espdu.setEntityLinearVelocityY(0);
          espdu.setEntityLinearVelocityZ(0);

          // orientation
          espdu.setEntityOrientationPsi(pduValues[6]);   // h
          espdu.setEntityOrientationTheta(pduValues[7]); // p
          espdu.setEntityOrientationPhi(pduValues[8]);   // r

          // angular velocity
          espdu.setEntityAngularVelocityY(0); // h
          espdu.setEntityAngularVelocityX(0); // p
          espdu.setEntityAngularVelocityZ(0); // r

          espdu.makeTimestampCurrent();
          writer.write(espdu);

          try {
            Thread.sleep(pauseTime);
          } catch(Exception e) {}
          }

        }
    }
}
