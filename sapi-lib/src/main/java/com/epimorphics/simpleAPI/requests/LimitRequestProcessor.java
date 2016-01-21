/******************************************************************
 * File:        LimitRequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.endpoints.ListEndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;

/**
 * Request processor that handles _limit/_offset requests.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class LimitRequestProcessor extends RequestProcessorBase {
    public static final String LIMIT = "_limit";
    public static final String OFFSET = "_offset";
    
    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder, EndpointSpec spec) {
        if (spec instanceof ListEndpointSpec) {
            ListEndpointSpec lspec = (ListEndpointSpec)spec;
            if (request.hasAvailableParameter(LIMIT) || request.hasAvailableParameter(OFFSET)) {
                long limit = Long.MAX_VALUE;
                if (request.hasParameter(LIMIT)) {
                    limit = request.getAsLong(LIMIT);
                    request.consume(LIMIT);
                } else if (lspec.getSoftLimit() != null) {
                    limit = lspec.getSoftLimit();
                }
                if (lspec.getHardLimit() != null) {
                    limit = Math.min(limit, lspec.getHardLimit());
                }
                long offset = 0;
                if (request.hasParameter(OFFSET)) {
                    offset = request.getAsLong(OFFSET);
                    request.consume(OFFSET);
                }
                return builder.limit(limit, offset);
            } else {
                if (lspec.getSoftLimit() != null) {
                    return builder.limit(lspec.getSoftLimit(), 0);
                }
            }
        }
        return builder;
    }

}
