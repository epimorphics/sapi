/******************************************************************
 * File:        ViewMapProjection.java
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
 * Variant of a ViewMap defined as a projection from some other named view.
 */
public class ViewMapProjection extends ViewMap {
    protected String viewReference;
    protected ViewMap reference;
    protected Projection projection;
    
    public ViewMapProjection(API api, String viewReference, String projection) {
        super(api);
        this.viewReference = viewReference;
        this.api = api;
        this.projection = new Projection(projection);
    }
    
    @Override
    public ClassSpec getTree() {
        ViewMap view = api.getView(viewReference);
        if (tree == null || view.hasChangedFrom(reference)) {
            if (view == null) {
                throw new EpiException("Cannot find view: " + viewReference);
            }
            tree = view.getTree().project(projection);
            csvmap = view.getCsvMap();
            reference = view;
        }
        return tree;
    }
    
}
