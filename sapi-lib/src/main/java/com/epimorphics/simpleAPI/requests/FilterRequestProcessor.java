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

import javax.ws.rs.core.Response.Status;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.rdfutil.TypeUtil;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.query.impl.NestedSparqlQueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.sparql.exprs.Infix;
import com.epimorphics.sparql.exprs.Op;
import com.epimorphics.sparql.graphpatterns.Basic;
import com.epimorphics.sparql.terms.Filter;
import com.epimorphics.sparql.terms.IsExpr;
import com.epimorphics.sparql.terms.TermUtils;
import com.epimorphics.sparql.terms.Var;
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
                Op relation = null;
                String pname = parameter;
                if (parameter.startsWith("min-")) {
                    pname = parameter.substring(4); relation = Op.opGreaterEq;
                } else if (parameter.startsWith("minEx-")) {
                    pname = parameter.substring(6); relation = Op.opGreater;
                } else if (parameter.startsWith("max-")) {
                    pname = parameter.substring(4); relation = Op.opLessEq;
                } else if (parameter.startsWith("maxEx-")) {
                    pname = parameter.substring(6); relation = Op.opLess;
                }
                ViewMap view = spec.getView( request.getViewName() );
                if (view != null) {
                    ViewPath path = view.pathTo(pname);
                    if (path != null) {
                        // A legal filter
                        PropertySpec entry = view.findEntry(path);
                        if (entry != null && entry.isFilterable()) {
                            request.consume(parameter);
                            String type = entry.getRange();
                            if (type != null) {
                                type = spec.getPrefixes().expandPrefix(type);
                            }
                            String valueBase = entry.getValueBase();
                            if (valueBase != null) {
                                valueBase = spec.getPrefixes().expandPrefix(valueBase);
                            }
                            List<String> rawargs = request.get(parameter);
                            if (rawargs.size() == 1) {
                                String arg = rawargs.get(0);
                                if (!arg.equals("*") && !arg.isEmpty()) {
                                    RDFNode value = asValue(arg, type, valueBase);
                                    if (relation == null) {
                                        builder = builder.filter(path, view, value);
                                    } else {
                                        Var var = new Var( path.asVariableName() );
                                        IsExpr val = TermUtils.nodeToTerm(value);
                                        Basic pattern = new Basic( new Filter(new Infix(var, relation, val)) );
                                        if (builder instanceof NestedSparqlQueryBuilder) {
                                            builder = ((NestedSparqlQueryBuilder)builder).pathAndFilter(path, view, pattern);
                                        } else if (builder instanceof SparqlQueryBuilder) {
                                            builder = ((SparqlQueryBuilder)builder).filter(pattern);
                                        } else {
                                            throw new WebApiException(Status.BAD_REQUEST, "Range filters only supported on sparql list endpoints");
                                        }
                                    }
                                } else {
                                    // Wildcard filter, silently ignore
                                }
                            } else {
                                if (relation != null) {
                                    throw new WebApiException(Status.BAD_REQUEST, "Multiple range restrictions on same parameter not yet supported");
                                }
                                List<RDFNode> args = new ArrayList<>(rawargs.size());
                                for (int i = 0; i < rawargs.size(); i++) {
                                    args.add(i, asValue(rawargs.get(i), type, valueBase));
                                }
                                builder = builder.filter(path, view, args);
                            }
                        }
                    }
                }
            }
        }
        return builder;
    }
    
    private static RDFNode asValue(String value, String type, String valueBase) {
        boolean isIRI = NameUtils.isURI(value);
        if (!isIRI && valueBase != null) { 
            value = valueBase + value;
            isIRI = true;
        }
        if (isIRI) {
            return ResourceFactory.createResource(value);
        } else {
            // TODO have a configurable default language, currently this will default to "@en"
            return TypeUtil.asTypedValue(value, type);
        }
    }
    
}
