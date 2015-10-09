/******************************************************************
 * File:        FilterRequestProcessor.java
 * Created by:  Dave Reynolds
 * Created on:  4 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

import com.epimorphics.rdfutil.TypeUtil;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;

/**
 * Handle filter queries. Treats any request parameter that isn't a directive (begins with "_")
 * as a candidate filter.
 */
public class FilterRequestProcessor implements RequestProcessor {

    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder,
            EndpointSpec spec) {
        for (String parameter : request.getParameters()) {
            if ( !parameter.startsWith("_") ) {
                ViewMap view = spec.getView();
                ViewPath path = view.pathTo(parameter);
                if (path != null) {
                    // A legal filter
                    String varname = path.asVariableName();
                    ViewEntry entry = view.findEntry(path);
                    String type = entry.getTypeURI();
                    if (type != null) {
                        type = spec.getPrefixes().expandPrefix(type);
                    }
                    List<String> rawargs = request.get(parameter);
                    if (rawargs.size() == 1) {
                        builder = builder.filter(varname, TypeUtil.asTypedValue(rawargs.get(0), type));
                    } else {
                        List<RDFNode> args = new ArrayList<>(rawargs.size());
                        for (int i = 0; i < rawargs.size(); i++) {
                            args.set(i, TypeUtil.asTypedValue(rawargs.get(i), type));
                        }
                        builder = builder.filter(varname, args);
                    }
                }
            }
        }
        return builder;
    }
    
}
