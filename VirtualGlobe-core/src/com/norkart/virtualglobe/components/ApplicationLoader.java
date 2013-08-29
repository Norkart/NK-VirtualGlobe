//-----------------------------------------------------------------------------
//
//                   Copyright (c) Norkart AS 2006-2007
//
//             This source code is the property of Norkart AS.
// Its use by other parties is regulated by license or agreement with Norkart.
//
//-----------------------------------------------------------------------------
package com.norkart.virtualglobe.components;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import com.norkart.virtualglobe.cache.CacheManager;
import javax.swing.JOptionPane;


/**
 * <p>Title: Virtual Globe</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SINTEF</p>
 * @author Rune Aasgaard
 * @version 1.0
 */

public class ApplicationLoader extends Thread {
    private DocumentBuilder documentBuilder;
    private LinkedList requestList = new LinkedList();
    
    protected class Rec {
        DomLoadable obj;
        URL         url;
        InputStream in;
        
        Rec(DomLoadable obj, URL url, boolean noopen) throws IOException {
            this.obj = obj;
            this.url = url;
            if (!noopen)
                open();
        }
        
        private void open()  throws IOException {
            if (in != null)
                return;
            // Create cache name
            CacheManager cache = null;
            if (!("localhost".equals(url.getHost()) || "file".equals(url.getProtocol()))) {
                if (obj instanceof Universe)
                    cache = ((Universe)obj).getCacheManager();
                else if (obj instanceof WorldComponent)
                    cache = ((WorldComponent)obj).getCacheManager();
            }
            
            if (cache != null)
                in = cache.getInputStream(url);
            else
                in = url.openStream();
            
            if (in != null)
                in = new BufferedInputStream(in);
            else
                throw new IOException("unable to open: " + url);
        }
    }
    
    public ApplicationLoader() {
        super("Application Loader");
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {}
    }
    
    public void requestLoading(DomLoadable comp, URL url, boolean noopen) throws IOException {
        synchronized (requestList) {
            requestList.add(new Rec(comp, url, noopen));
            requestList.notifyAll();
        }
    }
    
    public boolean waitForLoading(long wait) {
        synchronized (requestList) {
            if (!requestList.isEmpty()) {
                try {
                    requestList.wait(wait);
                } catch (InterruptedException ex) {}
            }
            return requestList.isEmpty();
        }
    }
    
    public Document newDocument() {
        return documentBuilder.newDocument();
    }
    
    public void load(DomLoadable obj, InputStream in, URL url) {
        // Parse the document
        int rounds = 0;
        while (++rounds <= 2) {
            try {
                Document doc = documentBuilder.parse(in);
                
                // Load the document
                Element ele = doc.getDocumentElement();
                if (!ele.getNodeName().equals("vgml"))
                    throw new WorldComponent.LoadException("Invalid element name " +
                            ele.getNodeName() + " found vgml expected");
                for (Node n = ele.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if (n instanceof Element) {
                        ele = (Element)n;
                        obj.load(ele);
                    }
                }
                break;
            } catch (Exception ex) {
                if (url == null) {
                    ex.printStackTrace();
                    break;
                } else {
                    System.err.println("Error during loading of model: " + url);
                    ex.printStackTrace();
                    CacheManager cache = null;
                    if (!("localhost".equals(url.getHost()) || "file".equals(url.getProtocol()))) {
                        if (obj instanceof Universe)
                            cache = ((Universe)obj).getCacheManager();
                        else if (obj instanceof WorldComponent)
                            cache = ((WorldComponent)obj).getCacheManager();
                    }
                    
                    in = null;
                    try {
                        if (cache != null) {
                            cache.deleteFile(url);
                            in = cache.getInputStream(url);
                            
                        } else
                            in = url.openStream();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                    if (in != null)
                        in = new BufferedInputStream(in);
                }
            }
        }
    }
    
    public void run() {
        while (true) {
            
            Rec currRec = null;
            while (currRec == null) {
                synchronized (requestList) {
                    while (requestList.isEmpty()){
                        try { requestList.wait(); } catch (java.lang.InterruptedException ex) {}
                    }
                    currRec = (Rec)requestList.getFirst();
                }
            }
            System.out.println("Loading " + currRec.url);
            try {
                currRec.open();
                load(currRec.obj, currRec.in, currRec.url);
                currRec.in.close();
            } catch (java.io.IOException ex) { ex.printStackTrace(); }
            
            synchronized (requestList) {
                requestList.remove(currRec);
                requestList.notifyAll();
            }
        }
    }
}