/******************************************************************
 * File:        Engine.java
 * Created by:  Dave Reynolds
 * Created on:  27 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.requests.Request;

/**
 * Plug point for adding new types of query processor.
 * <p>
 * The query generation is largely handled by the EndpointSpec and the Engine just acts
 * as a factory for EndpointSpecs. However, it also holds cross-endpoint processing elements
 * such as RequestProcessors or rendering engines.
 * </p>
 */
public interface Engine {

    /**
     * Parse the specification for and endpoint processable by this engine
     */
    public EndpointSpec parse(API api, String filename, JsonValue json);
    
    /**
     * Apply any default RequestProcessors or other preparation steps to 
     * complete the query builder. Called by the EndpointSpecs instances.
     */
    public QueryBuilder finalizeQueryBuilder(Request request, QueryBuilder builder, EndpointSpec spec);
    
    // TODO support for registering RequestProcessors?    
    // TODO support for fetching associated renderers?
    // TODO support for registering plugin renderers?
}
