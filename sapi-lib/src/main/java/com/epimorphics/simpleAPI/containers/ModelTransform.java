/******************************************************************
 * File:        ModelTransform.java
 * Created by:  Dave Reynolds
 * Created on:  15 Jan 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.containers;

import org.apache.jena.rdf.model.Resource;

/**
 * Signature for a class which supplies a method
 * to pre-process a payload model before it is put in a container
 */
public interface ModelTransform {

    public void transform(Resource rootOfModel);
    
}
