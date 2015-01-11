/******************************************************************
 * File:        SelectEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.List;

import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.writers.KeyValueSetStream;
import com.hp.hpl.jena.query.ResultSet;

public interface ListEndpointSpec extends EndpointSpec {

    /**
     * Return a streaming writer which serializes the results of a coalesced select query
     * using any mapping information supplied by the endpoint 
     */
    public JSONWritable getWriter(KeyValueSetStream results, RequestParameters request);

    /**
     * Return a streaming writer which serializes the results of a select query
     * using any mapping information supplied by the endpoint. Creates a wrapping
     * KeyValueSetStream to perform any coalescing of adjacent rows.
     */
    public JSONWritable getWriter(ResultSet results, RequestParameters request);
    
    /**
     * Return a list of parameter names which should be used to ground variables
     * in the query template. A request is not valid unless all of these are provided
     */
    public List<String> listBindingParams();
}
