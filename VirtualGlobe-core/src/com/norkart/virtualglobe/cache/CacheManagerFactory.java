/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  CacheManagerFactory.java
 *
 * Created on 21. april 2008, 10:12
 *
 */

package com.norkart.virtualglobe.cache;


/**
 *
 * @author runaas
 */
public abstract class CacheManagerFactory {
    static protected CacheManagerFactory instance;
    
    static public CacheManagerFactory getInstance() {
        return instance;
    }
    static public void setInstance(CacheManagerFactory inst) {
        instance = inst;
    }
    
    public abstract CacheManager createCacheManager();
}
