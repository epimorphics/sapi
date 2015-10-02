/******************************************************************
 * File:        RequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  1 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;

/**
 * General interface for processors that extract parts of the request and 
 * update the query-under-construction. The Request object is side-effected
 * to remove those request parameters that have been handled.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface RequestProcessor {

    public QueryBuilder process(Request request, QueryBuilder builder, EndpointSpec spec);
}
