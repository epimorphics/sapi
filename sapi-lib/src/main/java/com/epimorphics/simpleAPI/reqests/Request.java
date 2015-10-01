/******************************************************************
 * File:        RequestParameters.java
 * Created by:  Dave Reynolds
 * Created on:  1 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.reqests;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Encapsulates a query request, whether from query parameters, path parameters
 * or a POSTed json request.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Request {
    protected String requestedURI;
    protected MultivaluedMap<String, String> parameters;
    
    // TODO constructors
    
    // TODO remove parameters
}
