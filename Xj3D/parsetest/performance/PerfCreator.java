/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

/**
 * Peformance testing creator.  Creates large files for performance testing.
 * Simple creator for now.
 *
 * @author Alan Hudson
 * @version
 */
public class PerfCreator {
    public static void main(String[] args) {
        System.out.println("#X3D V3.0 utf8\n\nPROFILE Immersive\n");

        int num = 2500;

        for(int i=0; i < num; i++) {
            printLOD();
        }
    }

    public static void printLOD() {
        System.out.println("LOD {");
        System.out.println("\trange[5]");
        System.out.println("\tchildren [");
        printBox("\t\t");
        printSphere("\t\t");
        System.out.println("\t\t]\n}");
    }

    public static void printBox(String indent) {
        System.out.print(indent);
        System.out.println("Shape { geometry Box {} }");
    }

    public static void printSphere(String indent) {
        System.out.print(indent);
        System.out.println("Shape { geometry Sphere {} }");
    }
}