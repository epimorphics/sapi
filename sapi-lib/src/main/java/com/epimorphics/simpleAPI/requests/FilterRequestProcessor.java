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
import com.epimorphics.util.NameUtils;

/**
 * Handle filter queries. Treats any request parameter that isn't a directive (begins with "_")
 * as a candidate filter.
 */
public class FilterRequestProcessor implements RequestProcessor {

    @Override
    public ListQueryBuilder process(Request request, ListQueryBuilder builder,
            EndpointSpec spec) {
        for (String parameter : request.getRemainingParameters()) {
            if ( !parameter.startsWith("_") ) {
                ViewMap view = spec.getView( request.getViewName() );
                if (view != null) {
                    ViewPath path = view.pathTo(parameter);
                    if (path != null) {
                        // A legal filter
                        request.consume(parameter);
                        String varname = path.asVariableName();
                        ViewEntry entry = view.findEntry(path);
                        if (entry != null && entry.isFilterable()) {
                            String type = entry.getTypeURI();
                            if (type != null) {
                                type = spec.getPrefixes().expandPrefix(type);
                            }
                            String valueBase = entry.getValueBase();
                            if (valueBase != null) {
                                valueBase = spec.getPrefixes().expandPrefix(valueBase);
                            }
                            List<String> rawargs = request.get(parameter);
                            if (rawargs.size() == 1) {
                                if (!rawargs.get(0).equals("*")) {
                                    builder = builder.filter(varname, asValue(rawargs.get(0), type, valueBase));
                                } else {
                                    // Wildcard filter, silently ignore
                                }
                            } else {
                                List<RDFNode> args = new ArrayList<>(rawargs.size());
                                for (int i = 0; i < rawargs.size(); i++) {
                                    args.add(i, asValue(rawargs.get(i), type, valueBase));
                                }
                                builder = builder.filter(varname, args);
                            }
                        }
                    }
                }
            }
        }
        return builder;
    }
    
    private static RDFNode asValue(String value, String type, String valueBase) {
        if (valueBase != null && ! NameUtils.isURI(value)) {
            value = NameUtils.ensureLastSlash(valueBase) + value;
        }
        // TODO have a configurable default language, currently this will default to "@en"
        return TypeUtil.asTypedValue(value, type);
    }
    
}
