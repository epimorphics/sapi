/******************************************************************
 * File:        ViewMap.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigItem;

/**
 * Represents a singled configured tree view over the data which can be directly mapped to JSON.
 */
public class ViewMap extends ConfigItem {
    protected ViewTree tree;
    protected API api;
    
    public ViewMap(API api) {
        this.api = api;
    }
    
    public ViewMap(API api, ViewTree tree) {
        this.tree = tree;
        this.api = api;
    }
    
    public API getAPI() {
        return api;
    }
    
    // TODO constructor to clone an existing tree/map?
    
    public ViewTree getTree() {
        return tree;
    }
    
    /**
     * Return a SPARQL query string representing the bindings for this map
     */
    public String asQuery() {
        StringBuffer query = new StringBuffer();
        tree.renderAsQuery(query, "id", "");
        return query.toString();
    }

    // TODO indexes for looking up entry from across tree?
    
    public static ViewMap parseFromJson(API api, JsonValue list) {        
        // TODO allow indirection to named map
        return new ViewMap(api, ViewTree.parseFromJson(api, list) );
    }

    @Override
    public String toString() {
        return tree.toString();
    }
}
