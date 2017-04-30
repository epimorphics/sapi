/******************************************************************
 * File:        ViewMapReference.java
 * Created by:  Dave Reynolds
 * Created on:  30 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.views;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.util.EpiException;

/**
 * Variant on a view map which is a indirect reference to a named view
 */
public class ViewMapReference extends ViewMap {
    protected String viewReference;
    protected ViewMap reference;
    
    public ViewMapReference(API api, String viewReference) {
        super(api);
        this.viewReference = viewReference;
        this.api = api;
    }
    
    @Override
    public ClassSpec getTree() {
        ViewMap view = api.getView(viewReference);
        if (tree == null || view.hasChangedFrom(reference)) {
            if (view == null) {
                throw new EpiException("Cannot find view: " + viewReference);
            }
            tree = view.getTree();
            csvmap = view.getCsvMap();
            reference = view;
        }
        return tree;
    }
    
}
