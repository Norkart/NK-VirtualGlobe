//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

import com.norkart.geopos.Ellipsoid;
import com.norkart.virtualglobe.util.PathPoint;
import com.norkart.virtualglobe.components.DomLoadable;
import com.norkart.virtualglobe.cache.CacheManager;
import java.io.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class FlyPath  implements DomLoadable {
    public ArrayList list = new ArrayList();
    public URL dataset;
    public boolean loop = false;
    
    public FlyPath() {
    }
    
    private Ellipsoid.DistAz daz;
    public void generate(double [] from, double [] to, Ellipsoid ellps) {
        daz = ellps.inverseGeodesic(from[0], from[1], to[0], to[1], daz);
        
    }
    
    public void clear() {
        list = new ArrayList();
    }
    
    public void add(PathPoint p) {
        list.add(p);
    }
    
    public Iterator iterator() {
        return list.iterator();
    }
    
    public List<PathPoint> getPointList() {
        return list;
    }
    
    public void load(Element domElement) throws LoadException {
        list = new ArrayList();
        if (domElement.getNodeName().equals("fly-path")) {
            String dataset_str= domElement.getAttribute("dataset");
            if (dataset_str != null && dataset_str.length() > 0) {
                try {
                    dataset = new URL(dataset_str);
                } catch (MalformedURLException ex) {}
            }
            String loop_str= domElement.getAttribute("loop");
            if ("true".equalsIgnoreCase(loop_str))
                loop = true;
            for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                if (!(ch instanceof Element)) continue;
                Element chEle = (Element)ch;
                if (chEle.getNodeName().equals("point")) {
                    String line = "";
                    for (Node ch1 = chEle.getFirstChild(); ch1 != null; ch1 = ch1.getNextSibling()) {
                        if (!(ch1 instanceof Text)) continue;
                        Text tEle = (Text)ch1;
                        line += tEle.getNodeValue().trim();
                    }
                    String[] tokens = line.split(",");
                    if (tokens.length != 6)
                        continue;
                    final PathPoint p = new PathPoint();
                    for (int i=0; i < 5; ++i) {
                        if (i ==2)
                            p.data[i] = Double.parseDouble(tokens[i]);
                        else
                            p.data[i] = Math.toRadians(Double.parseDouble(tokens[i]));
                    }
                    p.movetime = Long.parseLong(tokens[5]);
                    list.add(p);
                }
            }
        }
    }
    
    public Element save(Document doc) {
        return null;
    }
    
    public void save(OutputStream o) {
        PrintStream out = null;
        try {
            out = new PrintStream(o, false, "ISO-8859-1");
        } catch (Exception ex) {}
        
        if (out == null || list.isEmpty()) return;
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println("  <vgml>");
        out.print("    <fly-path");
        if (dataset != null)
            out.print("dataset=\""+ dataset +"\"");
        out.println(">");
        Iterator it = list.iterator();
        while (it.hasNext()) {
            PathPoint p = (PathPoint)it.next();
            out.println("      <point>");
            out.println("        " + p);
            out.println("      </point>");
        }
        out.println("    </fly-path>");
        out.println("  </vgml>");
    }
}