
import mil.navy.nps.dis.*;
import mil.navy.nps.net.*;

import java.net.*;
import java.util.*;

public class NetworkSimulator extends Object implements BehaviorConsumerIF
{
    public static int numEntities = 50;
    public static int hz = 5;

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


  private static final int DATA_ITEMS = 28;
  private static float[][] DATA;

  private static final int PORT = 62040;
  private static final String GROUP = "224.2.181.145";

    private static Simulator[] runners;

  public static void main(String args[])
  {
    if (args.length > 1) {
        numEntities = Integer.parseInt(args[0]);
        hz = Integer.parseInt(args[1]);
    }

    System.out.println("Number entities: " + numEntities + " hz: " + hz);

    parseData();
    runners = new Simulator[numEntities];

    NetworkSimulator tester = new NetworkSimulator();
    tester.test();
  }

    public void test() {

        MulticastSocket socket;
        InetAddress address;
        int pause = 2500 / numEntities;
        float offset = -10;

        try {
            socket = new MulticastSocket(PORT);
            address = InetAddress.getByName(GROUP);
            socket.joinGroup(address);


            for(int i=0; i < numEntities; i++) {
                if (i % DATA_ITEMS == 0)
                    offset += 2.5;
                launchEntity(socket, address, PORT, DATA_LAYOUT, 0,1,i,0,0,offset);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        int clock;
        int localClock;
        long startTime;
        long cycleTime;
        long pauseTime = 1000 / (hz * numEntities);
        int cycleInterval = 1000 / hz;

        for(int i=0; i < 50000; i++) {
            clock = i % DATA_ITEMS;
            startTime = System.currentTimeMillis();
            for(int j=0; j < numEntities; j++) {
                localClock = clock + (j % DATA_ITEMS);
                if (localClock > DATA_ITEMS - 1)
                    localClock = localClock - DATA_ITEMS;
//System.out.println("j: " + j + " clock: " + localClock);
                runners[j].clock(localClock);
            }

            cycleTime = System.currentTimeMillis() - startTime;

            pauseTime = cycleInterval - cycleTime;
            if(pauseTime > 0) {
                try {
                    Thread.sleep(pauseTime);
                } catch(Exception e) {}
            } else
                pauseTime = 0;

            System.out.println("processing Time: " + cycleTime + " pauseTime: " + pauseTime + " actual hz: " + (1000.0f / (cycleTime + pauseTime)));
        }
    }


    private void launchEntity(MulticastSocket socket, InetAddress address, int port, String path,
        int siteID, int appID, int entityID,
        float xoff, float yoff, float zoff) {

        Thread thread;

        BehaviorProducerUDP writer1 = new BehaviorProducerUDP(socket);
        writer1.setDefaultDestination(address, new Integer(port));
        thread = new Thread(writer1);
        thread.start();

        Simulator e1 = new Simulator(writer1, DATA, siteID, appID, entityID, hz);
        e1.setOffset(xoff,yoff,zoff);

        runners[entityID] = e1;
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

    private static void parseData() {
        DATA = new float[DATA_ITEMS][12];

        String          lineString;                   // one line from the string
        StringTokenizer lineTokenizer;
        StringTokenizer itemTokenizer;

        lineTokenizer = new StringTokenizer(DATA_LAYOUT, "\r\n");
        int pdu = 0;
        int valueCount = 0;
        float  value;
        String token;

        // while we have more lines....
        while(lineTokenizer.hasMoreTokens()) {
            // get one line of input, then decode each token in that string
            lineString = lineTokenizer.nextToken();
            itemTokenizer = new StringTokenizer(lineString);
            valueCount = 0;

            while(itemTokenizer.hasMoreTokens()) {
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

                DATA[pdu][valueCount-1] = value;
            }

            if(valueCount == 0)     // got a blank line; skip it, and don't send out a zero PDU
            continue;

            pdu++;
        }
    }
}

class Simulator {
    EntityID id;
    BehaviorProducerUDP        writer;
    float[][] data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int hz;
    ArticulationParameter p;

    public Simulator(BehaviorProducerUDP writer, float[][] data, int siteID, int appID, int entityID, int hz) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.hz = hz;
        id = new EntityID(siteID, appID, entityID);
        espdu = new EntityStatePdu();
        espdu.setEntityID(id);
        p = new ArticulationParameter();
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    public void clock(int frame) {
      espdu.reset();

      if (entityID == 0) {
          espdu.addArticulationParameter(p);
      }

      // location
      espdu.setEntityLocationX(data[frame][0] + xoff);
      espdu.setEntityLocationY(data[frame][1] + yoff);
      espdu.setEntityLocationZ(data[frame][2] + zoff);

      // velocity
      espdu.setEntityLinearVelocityX(data[frame][3]);
      espdu.setEntityLinearVelocityY(data[frame][4]);
      espdu.setEntityLinearVelocityZ(data[frame][5]);

      // orientation
      espdu.setEntityOrientationPsi(data[frame][6]);   // h
      espdu.setEntityOrientationTheta(data[frame][7]); // p
      espdu.setEntityOrientationPhi(data[frame][8]);   // r

      // angular velocity
      espdu.setEntityAngularVelocityY(data[frame][9]); // h
      espdu.setEntityAngularVelocityX(data[frame][10]); // p
      espdu.setEntityAngularVelocityZ(data[frame][11]); // r

      espdu.makeTimestampCurrent();
      writer.write(espdu);

    }
}

