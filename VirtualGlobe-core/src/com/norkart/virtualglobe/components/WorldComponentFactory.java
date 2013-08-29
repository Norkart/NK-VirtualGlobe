/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  WorldComponentFactory.java
 *
 * Created on 24. april 2008, 11:50
 *
 */

package com.norkart.virtualglobe.components;

import java.util.*;
import org.w3c.dom.*;

/**
 *
 * @author runaas
 */
public class WorldComponentFactory {
    private static WorldComponentFactory instance = new WorldComponentFactory();
    
    private class Key {
        Class parentClass;
        String elementName;
        
        Key(Class parentClass, String elementName) {
            this.parentClass = parentClass;
            this.elementName = elementName;
        }
        
        public int hashCode() {
            return parentClass.hashCode() & elementName.hashCode();
        }
        
        public boolean equals(Object o) {
            Key k = (Key)o;
            return k.parentClass == parentClass && k.elementName.equals(elementName);
        }
    }
    
    public interface Creator {
        WorldComponent create(WorldComponent parent);
    }
    
    Map<Key, Creator> map = new HashMap();
    
    /** Creates a new instance of WorldComponentFactory */
    public WorldComponentFactory() {
    }
    
    public static WorldComponentFactory getInstance() {
        return instance;
    }
    
    public synchronized WorldComponent createComponent(WorldComponent parent, String element_name) {
        Key key = new Key(parent.getClass(), element_name);
        Creator creator = map.get(key);
        if (creator != null)
            return creator.create(parent);
        return null;
    }
    
    public synchronized void add(Class parentClass, String elementName, Creator creator) {
        Key key = new Key(parentClass, elementName);
        map.put(key, creator);
    }
    
    public void loadChildren(WorldComponent parent, Element domElement) throws DomLoadable.LoadException {
        for (Node ch = domElement.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
            if (ch instanceof Element) {
                Element ch_ele = (Element)ch;
                String ele_name = ch_ele.getNodeName();
                WorldComponent ch_comp = createComponent(parent, ele_name);
                if (ch_comp != null)
                    ch_comp.load(ch_ele);
            }
        }
    }
}
