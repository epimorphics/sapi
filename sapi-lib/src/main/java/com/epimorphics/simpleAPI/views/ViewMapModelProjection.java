/******************************************************************
 * File:        ViewMapModelProjection.java
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
 * Variant of a ViewMap defined as a projection from a named model
 */
public class ViewMapModelProjection extends ViewMap {
    protected String modelReference;
    protected ModelSpec reference;
    protected String baseClass;
    protected Projection projection;
    
    public ViewMapModelProjection(API api, String modelReference, String baseClass, String projection) {
        super(api);
        this.modelReference = modelReference;
        this.baseClass = baseClass;
        this.api = api;
        if (projection != null && !projection.isEmpty()) {
            this.projection = new Projection(projection);
        }
    }
    
    @Override
    public ClassSpec getTree() {
        ModelSpec model = modelReference == null ? api.getModel() : api.getModel(modelReference);
        if (model == null || model.hasChangedFrom(reference)) {
            if (model == null) {
                throw new EpiException("Cannot find model: " + modelReference);
            }
            tree = projection == null ? model.projectClass(baseClass) : model.projectClass(baseClass, projection);
            reference = model;
            if (tree == null) {
                throw new EpiException( String.format("Could not find view for model: %s, class %s",
                        modelReference == null ? "defaultModel" : modelReference,
                                baseClass) );
            }
        }
        return tree;
    }
    
}
