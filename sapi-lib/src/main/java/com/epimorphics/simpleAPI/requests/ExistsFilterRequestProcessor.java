/******************************************************************
 * File:        FilterRequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  4 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;

/**
 * Handle filter queries. Treats any request parameter that isn't a directive (begins with "_")
 * as a candidate filter.
 */
public class ExistsFilterRequestProcessor implements RequestProcessor {

    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder,
            EndpointSpec spec) {
        for (String parameter : request.getRemainingParameters()) {
            if ( !parameter.startsWith("_") && parameter.startsWith("exists-") ) {
                ViewMap view = spec.getView( request.getViewName() );
                if (view != null) {
                    String realParam = parameter.replaceFirst("^exists-", "");
                    ViewPath path = view.pathTo(realParam);
                    if (path != null) {
                        // A legal filter
                        PropertySpec entry = view.findEntry(path);
                        if (entry != null && entry.isFilterable()) {
                            request.consume(parameter);
                            boolean mustExist = request.getAsBoolean(parameter, true);
                            builder = builder.filterExists(path, view, mustExist);
                        }
                    }
                }
            }
        }
        return builder;
    }
    
}
