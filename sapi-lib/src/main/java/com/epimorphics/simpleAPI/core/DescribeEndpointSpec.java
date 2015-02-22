/******************************************************************
 * File:        APIDescribeEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.rdf.model.Resource;

public interface DescribeEndpointSpec extends EndpointSpec {

    /**
     * Return a streaming writer which serializes a retrieved resource
     * using any mapping information supplied by the endpoint 
     */
    public JSONWritable getWriter(Resource resource, String requestURI);
}
