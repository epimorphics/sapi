/******************************************************************
 * File:        WResults.java
 * Created by:  Dave Reynolds
 * Created on:  1 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results.wappers;

import org.apache.jena.rdf.model.Resource;

import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.simpleAPI.results.Result;

/**
 * Wrapped result value for us in scripting enviornments, such as velocity, for HTML rendering
 */
public class WResult {
    protected Result result;
    
    public Result asResult() {
        return result;
    }
    
    public WJSONObject asJson() {
        // TODO implement
        return null;
    }

    public RDFNodeWrapper asRDF() {
        Resource resource = result.asResource();
        ModelWrapper modelw = new ModelWrapper(resource.getModel());
        return new RDFNodeWrapper(modelw, resource);
    }
    
    // TODO property name lookup (json name to RDF URI)
}
