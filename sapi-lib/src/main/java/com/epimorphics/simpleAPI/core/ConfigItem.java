/******************************************************************
 * File:        ConfigBase.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.concurrent.atomic.AtomicLong;

import com.epimorphics.appbase.monitor.ConfigInstance;

/**
 * Base for configuration objects.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConfigItem implements ConfigInstance {
    protected static AtomicLong versionCount = new AtomicLong(0);
    
    protected String name;
    protected long version;

    public ConfigItem() {
        version = versionCount.incrementAndGet();
    }
    
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public long getVersion() {
        return version;
    }
    
    public boolean hasChangedFrom(ConfigItem current) {
        return current == null || version != current.getVersion();
    }
}
