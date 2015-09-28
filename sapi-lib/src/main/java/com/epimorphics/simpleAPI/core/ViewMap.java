/******************************************************************
 * File:        ViewMap.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

/**
 * Represents a singled configured tree view over the data which can be directly mapped to JSON.
 */
public class ViewMap {
    protected ViewTree tree;
    
    public ViewMap() {
    }
    
    // TODO constructor to clone an existing tree/map?
    
    public ViewTree getTree() {
        return tree;
    }

    // TODO indexes for looking up entry from across tree?
}
