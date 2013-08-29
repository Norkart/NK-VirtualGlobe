/*****************************************************************************
 *                    Yumetech, Inc Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

import java.io.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

/**
 * A regression tester.  This will compare a set of references issues to
 * see if rendering has changed between versions.  This will walk a directory
 * tree and take snapshots of all X3D files.  The resultant
 * images will be left in another matching directory tree.
 *
 * This program is designed to be run from the user-install directory.  It will
 * use the operating specific launcher(ie browser.bat or browser.sh) to launch
 * a different browser each time.
 *
 * Typically the class files are copied into the install directory and
 * then run like this: java RegressionTester c:/cygwin/home/giles/Xj3D/parsetest
 *    imagedb images
 *
 * @author Alan Hudson
 * @version
 */
public class RegressionTester {
    private static final int RESULT_BLACK = 0;
    private static final int RESULT_EQUAL = 1;
    private static final int RESULT_NOT_EQUAL = 2;
    private static final int RESULT_NO_SOURCE = 3;
    private static final int PIXEL_TOLERANCE = 10;
    private static final int NUM_PIXELS_TOLERANCE = 20;

    private static final String USAGE_MSG =
      "Usage: RegressionTester [options] [testsDir] [referencesDir] [outputDir]\n";

    /** The directory to test */
    private String testDir;

    /** The directory to get reference images */
    private String referenceImagesDir;

    /** The directory to place current images in */
    private String outputDir;

    /** The file filter */
    private FileFilter filter = new Filter();

    public RegressionTester(String testDir, String refImagesDir, String outputDir) {
        this.testDir = new File(testDir).getAbsolutePath();
        this.referenceImagesDir = refImagesDir;
        this.outputDir = new File(outputDir).getAbsolutePath();
    }

    public static final void main(String[] args) {
        if (args.length < 3) {
            System.out.println(USAGE_MSG);
            return;
        }

        RegressionTester tester = new RegressionTester(args[0], args[1], args[2]);

        tester.run();
    }

    public void run() {
        process(testDir);
    }

    private void process(String dir) {

        // Walk directory tree, look for all X3D files
        File f = new File(dir);

        if (f.isDirectory()) {
            File[] files = f.listFiles(filter);
            int len = files.length;

            String base_name = f.getAbsolutePath().substring(testDir.length());

            File new_dir = new File(outputDir + File.separator + base_name);
            if (!new_dir.exists()) {
                new_dir.mkdir();
            }

            for(int i=0; i < len; i++) {
                process(files[i].getAbsolutePath());
            }
        } else {
            System.out.println("process: " + f);

            String OSName = System.getProperty("os.name").toLowerCase();
            boolean mac = OSName.startsWith("mac os x");
            boolean windows = OSName.indexOf("windows") > -1;

            //System.out.println("Executing: " + " browser.bat -nice -captureViewpoints \"" + f + "\"");
            String cmd;

            if (windows)
                cmd = "browser.bat -nice -captureViewpoints \"" + f + "\"";
            else if (mac) {
                // TODO: I think this is wrong
                cmd = "browser.sh -nice -captureViewpoints \"" + f + "\"";
            } else {
                cmd = "browser.sh -nice -captureViewpoints \"" + f + "\"";
            }

            execute(cmd);

            // Look for png's with VP_ in the name
            PictureFilter pf = new PictureFilter();

            File parent = f.getParentFile();

            File[] images = parent.listFiles(pf);

            for(int j=0; j < images.length; j++) {
                String base_name = images[j].getAbsolutePath().substring(testDir.length()+1);

                String dest_name = outputDir + File.separator + base_name;
                String ref_name = referenceImagesDir + File.separator + base_name;
                File dest_file = new File(dest_name);
                File ref_file = new File(ref_name);

                // move images to destination directory
                dest_file.delete();

                boolean success = images[j].renameTo(dest_file);
                if (!success)
                    System.out.println("Cannot rename snapshot: " + dest_name);

                // compare images to reference images

                int result = compareImages(dest_file, ref_file);

                switch(result) {
                    case RESULT_EQUAL:
                        System.out.println("\tEQUAL");
                        break;
                    case RESULT_BLACK:
                        System.out.println("\t*BLACK");
                        break;
                    case RESULT_NOT_EQUAL:
                        System.out.println("\t*NOT_EQUAL");
                        break;
                    case RESULT_NO_SOURCE:
                        System.out.println("\t*NO SOURCE");
                        break;
                }
            }
        }
    }

    /**
     * Compare two images.  Valid results are BLACK,EQUAL,NOT_EQUAL,NO_SOURCE.
     *
     * @param testFile The newly generated test screenshot.
     * @param refFile The reference file
     */
    private int compareImages(File testFile, File refFile) {
        boolean single_compare = false;

        try {
            FileInputStream test_file = new FileInputStream(testFile);
            FileInputStream ref_file = null;

            BufferedImage test_image = ImageIO.read(test_file);
            BufferedImage ref_image = null;

            if (refFile.exists()) {
                ref_file = new FileInputStream(refFile);
                ref_image = ImageIO.read(ref_file);
            } else {
                single_compare = true;
            }

            int test_width = test_image.getWidth(null);
            int test_height = test_image.getHeight(null);
            int test_imageType = test_image.getType();
            ColorModel test_cm = test_image.getColorModel();

            int ref_width;
            int ref_height;
            int ref_imageType;
            ColorModel ref_cm;

            if (!single_compare) {
                ref_width = ref_image.getWidth(null);
                ref_height = ref_image.getHeight(null);
                ref_imageType = ref_image.getType();
                ref_cm = ref_image.getColorModel();


                if (test_width != ref_width || test_height != ref_height) {
                    System.out.println("Sizes do not match: test: " + test_width + " x " + test_height + " ref: " + ref_width + " x " + ref_height);
                    return RESULT_NOT_EQUAL;
                }

                if (!test_cm.equals(ref_cm)) {
                    System.out.println("Color models do not match: test: " + test_cm + " ref: " + ref_cm);
                    return RESULT_NOT_EQUAL;
                }

                if (test_imageType != ref_imageType) {
                    System.out.println("Image types do not match: test: " + test_imageType + " ref: " + ref_imageType);
                    return RESULT_NOT_EQUAL;
                }
            }

            int[] pixelTmp1 = new int[test_width];
            int[] pixelTmp2 = new int[test_width];

            boolean all_black = true;
            boolean equal = true;
            byte r,g,b;

            for(int i = 0; i < test_height; i++) {
                test_image.getRGB(0, i, test_width, 1, pixelTmp1, 0, test_width);

                if (!single_compare)
                    ref_image.getRGB(0, i, test_width, 1, pixelTmp2, 0, test_width);

                for(int j=0; j < test_width; j++) {
                    r = (byte)((pixelTmp1[j] >> 16) & 0xFF);
                    g = (byte)((pixelTmp1[j] >> 8) & 0xFF);
                    b = (byte)(pixelTmp1[j] & 0xFF);

//if (j==0)
//   System.out.println(r + " " + g + " " + b);
                    if (r != 0 && g != 0 && b != 0)
                        all_black = false;

                    if (!single_compare && pixelTmp1[j] != pixelTmp2[j]) {
                        equal = false;
                    }
                }
            }

//System.out.println("all black: " + all_black);
            if (all_black)
                return RESULT_BLACK;

            if (!single_compare && equal)
                return RESULT_EQUAL;

            return RESULT_NO_SOURCE;
        } catch(IOException ioe) {
            ioe.printStackTrace();

            return RESULT_NOT_EQUAL;
        }
    }

    private void execute(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);

            Thread sysoutThr = new Thread(new readerThread(new BufferedInputStream(p.getInputStream())), "det sysout");
            Thread syserrThr = new Thread(new readerThread(new BufferedInputStream(p.getErrorStream())), "det syserr");
            sysoutThr.setPriority(Thread.NORM_PRIORITY);
            syserrThr.setPriority(Thread.NORM_PRIORITY);

            PrintStream toExec = new PrintStream(p.getOutputStream());

            sysoutThr.start();
            syserrThr.start();

            ProcessKiller pkiller = new ProcessKiller(p, 15000);

            pkiller.start();

            p.waitFor();

            if (pkiller.isDestroyed()) {
                System.out.println("\t*DESTROYED");
            } else {
                pkiller.exit();
                p.destroy();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

   /**
    * A class to read from a BufferedInput stream, write to the text area, then terminate when done.
    */
   class readerThread implements Runnable {
      BufferedInputStream inStr;
      private byte[] buffr;
      private boolean display = false;

      readerThread(BufferedInputStream in) {
         inStr = in;
         buffr = new byte[1024];
      }

      public void run() {
         try {
            while(true) {
               int len = inStr.read(buffr);

               if (len > 0) {
                  if (display)
                     System.out.println(new String(buffr, 0, len));
               } else
                  break;
            }
         } catch (IOException e) {}
      }
   }
}