/******************************************************************
 * File:        SortRequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import javax.ws.rs.core.Response.Status;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;

/**
 * Handle _sort directive, allows "." path notation to sorting on
 * elements of nested view. If the API wants to avoid that then pre-process the request.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SortRequestProcessor extends RequestProcessorBase {
    public static final String SORT = "_sort";

    @Override
    public QueryBuilder process(Request request, QueryBuilder builder,
            EndpointSpec spec) {
        if (request.hasParameter(SORT)) {
            for (String sort : request.get(SORT)) {
                boolean down = false;
                if (sort.startsWith("+")) {
                    sort = sort.substring(1).trim();
                } else if (sort.startsWith("-")) {
                    down = true;
                    sort = sort.substring(1).trim();
                }
                String sortVar = spec.getView().asVariableName(sort);
                if (sortVar == null) {
                    throw new WebApiException(Status.BAD_REQUEST, "Did not recognize parameter to sort on: " + sort);
                }
                builder = builder.sort( sortVar, down );
            }
            request.remove(SORT);
            return builder;
        }
        return builder;
    }
    
}
