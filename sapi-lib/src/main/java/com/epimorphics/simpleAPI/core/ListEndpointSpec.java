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

import javax.ws.rs.core.StreamingOutput;

import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.query.ResultSet;

public interface ListEndpointSpec extends EndpointSpec {

    /**
     * Return a streaming writer which serializes the results of a select query
     * using any mapping information supplied by the endpoint. Creates a wrapping
     * ValueStream to perform any coalescing of adjacent rows.
     */
    public JSONWritable getWriter(ResultSet results, RequestParameters request);

    /**
     * Return a streaming writer which serializes the results of a select query to CSV format,
     * no mapping used, coalesces adjacent rows using a wrapping ValueStream.
     */
    public StreamingOutput getCSVWriter(ResultSet results, RequestParameters request, boolean includeID);
    
    /**
     * Return a list of parameter names which should be used to ground variables
     * in the query template. A request is not valid unless all of these are provided
     */
    public List<String> listBindingParams();
}
