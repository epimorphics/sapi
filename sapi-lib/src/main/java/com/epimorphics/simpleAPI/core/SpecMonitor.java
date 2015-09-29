/******************************************************************
 * File:        SpecMonitor.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import com.epimorphics.appbase.monitor.ConfigMonitor;

public class SpecMonitor extends ConfigMonitor<ConfigItem> {
    protected API api;
    
    public SpecMonitor(API api) {
        this.api = api;
    }

    @Override
    protected Collection<ConfigItem> configure(File file) {
        return Collections.singletonList( ConfigSpecFactory.read(api, file.getPath()) );
    }

}
