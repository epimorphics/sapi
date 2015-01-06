/******************************************************************
 * File:        SelectEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.writers.KeyValueSetStream;
import com.hp.hpl.jena.query.ResultSet;

public interface SelectEndpointSpec extends EndpointSpec {

    /**
     * Return a streaming writer which serializes the results of a coalesced select query
     * using any mapping information supplied by the endpoint 
     */
    public JSONWritable getWriter(KeyValueSetStream results);


    /**
     * Return a streaming writer which serializes the results of a select query
     * using any mapping information supplied by the endpoint. Creates a wrapping
     * KeyValueSetStream to perform any coalescing of adjacent rows.
     */
    public JSONWritable getWriter(ResultSet results);    
}
