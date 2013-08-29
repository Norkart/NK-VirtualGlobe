/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  CacheManagerJdbmFactory.java
 *
 * Created on 25. april 2008, 10:27
 *
 */

package com.norkart.virtualglobe.cache.jdbm;

import com.norkart.virtualglobe.cache.CacheManager;
import com.norkart.virtualglobe.cache.CacheManagerFactory;

/**
 *
 * @author runaas
 */
public class CacheManagerJdbmFactory extends CacheManagerFactory { 
    /** Creates a new instance of CacheManagerJdbmFactory */
    public CacheManagerJdbmFactory() {
    }
    
    public CacheManager createCacheManager() {
        return new CacheManagerJdbm();
    }
}
