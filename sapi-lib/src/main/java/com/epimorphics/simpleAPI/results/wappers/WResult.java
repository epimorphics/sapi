/******************************************************************
 * File:        WResults.java
 * Created by:  Dave Reynolds
 * Created on:  1 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results.wappers;

import static com.epimorphics.simpleAPI.writers.JsonWriterUtil.ID_FIELD;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.results.RDFResult;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewTree;
import com.epimorphics.util.EpiException;

/**
 * Wrapped result value for us in scripting environments, such as velocity, for HTML rendering
 */
public class WResult {
    protected Result result;
    
    public WResult(Result result) {
        this.result = result;
    }
    
    public Result asResult() {
        return result;
    }
    
    public WJSONObject asJson() {
        if (result instanceof TreeResult) {
            return wrap((TreeResult)result);
        } else {
            return wrap( ((RDFResult)result).asTreeResult() );
        }
    }

    public RDFNodeWrapper asRDF() {
        Resource resource = result.asResource();
        resource.getModel().setNsPrefixes( API.get().getPrefixes() );
        ModelWrapper modelw = new ModelWrapper(resource.getModel());
        return new RDFNodeWrapper(modelw, resource);
    }
    
    // TODO property name lookup (json name to RDF URI)
    
    // TODO This is a near clone of JsonWriterUtil - replace with a generic Visitor pattern shared implementation?
    public static WJSONObject wrap(TreeResult result) {
        EndpointSpec spec = result.getCall().getEndpoint();
        ViewMap view = result.getCall().getView();
        return wrap(result, view == null ? null : view.getTree(), spec.getAPI());
    }
    
    protected static WJSONObject wrap(TreeResult result, ViewTree tree, API api) {
        WJSONObject w = new WJSONObject();
        w.put(ID_FIELD, result.getStringID());
        for (String key : result.getKeys()) {
            List<Object> values = result.getSortedValues(key);
            ViewEntry policy = null;
            if (tree != null) {
                policy = tree.getEntry(key);
            }
            if (policy == null) {
                policy = api.getDefaultViewFor(key);
            }
            if (values.size() > 1 || policy.isMultivalued()) {
                // TODO handle case where we have a showOnlyLang setting and there's multiple different language values here
                WJSONArray array = new WJSONArray();
                for (Object n : values) {
                    array.add( wrap(tree, policy, api, key, n) );
                }
                w.put(key, array);
            } else if (!values.isEmpty()) {
                w.put(key, wrap(tree, policy, api, key, values.get(0)));
            }
        }
        return w;
    }


    protected static Object wrap(ViewTree tree, ViewEntry policy, API api, String key, Object value) {
        if (value instanceof Result) {
            ViewTree ntree = null;
            if (tree != null) {
                ViewEntry entry = tree.getEntry(key);
                if (entry != null) {
                    ntree = entry.getNested();
                }
            }
            return wrap((TreeResult)value, ntree, api);
        } else if (value instanceof RDFNode) {
            RDFNode n = (RDFNode) value;
            if (n.isResource()) {
                if (n.isURIResource()) {
                    return new WJSONObject( (Resource)n );
                } else {
                    return new WJSONObject();
                }
            } else {
                Literal l = n.asLiteral();
                String lex = l.getLexicalForm();
                if (l.getDatatype() == null || l.getDatatypeURI().equals(RDF.langString.getURI())) {
                    String lang = l.getLanguage();
                    if (lang == null || lang.isEmpty() || !api.isShowLangTag()) {
                        return lex;
                    } else {
                        return new WJSONLangString(lex, lang);
                    }
                } else {
                    // TODO handle other typed literals
                    Object jv = l.getValue();
                    if (jv instanceof Number || jv instanceof Boolean) {
                        return jv;
                    } else {
                        return lex;
                    }
                }
            }
        } else {
            throw new EpiException("Internal error: unexpected value type in result wrapper");
        }
    }
}
