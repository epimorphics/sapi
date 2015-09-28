/******************************************************************
 * File:        ViewMap.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.impl.ConfigItem;

/**
 * Represents a singled configured tree view over the data which can be directly mapped to JSON.
 */
public class ViewMap extends ConfigItem {
    protected ViewTree tree;
    
    public ViewMap() {
    }
    
    public ViewMap(ViewTree tree) {
        this.tree = tree;
    }
    
    // TODO need access to API?
    
    // TODO constructor to clone an existing tree/map?
    
    public ViewTree getTree() {
        return tree;
    }

    // TODO indexes for looking up entry from across tree?
    
    public static ViewMap parseFromJson(API api, JsonValue list) {
        return new ViewMap( ViewTree.parseFromJson(api, list) );
    }

}
