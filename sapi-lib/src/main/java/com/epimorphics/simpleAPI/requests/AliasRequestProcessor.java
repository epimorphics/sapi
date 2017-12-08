/******************************************************************
 * File:        AliasRequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  4 May 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.Map;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;

/**
 * Handle alias which allow shortened form for query filter parameter names. 
 * Must be placed in the filter stack ahead of the FilterRequestProcessor
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class AliasRequestProcessor implements RequestProcessor {

    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder,
            EndpointSpec spec) {
        for (Map.Entry<String, String> alias : spec.getAliases().entrySet()) {
            String key = alias.getKey();
            if (request.hasAvailableParameter(key)) {
                for (String value: request.get(key)) {
                    request.add(alias.getValue(), value);
                }
                request.consume(key);                
            }
        }
        return builder;
    }

}
