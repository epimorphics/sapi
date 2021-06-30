/******************************************************************
 * File:        JsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  1 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewTree;

/**
 * Serialize a result as JSON. Assumes that the shape of the result matches the
 * shape of the given viewTree. This is supposed to be true "by construction".
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JsonWriterUtil {

    public static final String LANGUAGE_FIELD = "@language";
    public static final String VALUE_FIELD = "@value";
    public static final String ID_FIELD = "@id";
    public static final String LABEL_FIELD = "label";
    public static final String[] LABEL_FIELDS = new String[]{"prefLabel", "label", "name"};


    public static void writeResult(TreeResult result, JSFullWriter out) {
        EndpointSpec spec = result.getCall().getEndpoint();
        ViewMap view = result.getCall().getView();
        writeResult(result, view == null ? null : view.getTree(), spec.getAPI(), out);
    }
    
    protected static void writeResult(TreeResult result, ViewTree tree, API api, JSFullWriter out) {
        out.startObject();
        String id = result.getStringID();
        if (id != null) {
            out.pair(ID_FIELD, id);
        }
        for (String key : result.getSortedKeys()) {
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
                out.key(key);
                out.startArray();
                for (Object n : values) {
                    writeNode(tree, policy, api, key, n, out, true);
                }
                out.finishArray();
            } else if (!values.isEmpty()) {
                writeNode(tree, policy, api, key, values.get(0), out, false);
            }
        }
        out.finishObject();
    }


    protected static void writeNode(ViewTree tree, ViewEntry policy, API api,
            String key, Object value, JSFullWriter writer, boolean isArrayElt) {
        if (value instanceof TreeResult) {
            if ( ((TreeResult)value).isSimple() && api.isShowSimpleLinks() ) {
                // Legacy non-jsonld rendering of leaf resources
                value = ((TreeResult)value).asResource();
            }
        }
        if (value instanceof Result) {
            if (isArrayElt)
                writer.arrayElementProcess();
            else
                writer.key(key);
            ViewTree ntree = null;
            if (tree != null) {
                ViewEntry entry = tree.getEntry(key);
                if (entry != null) {
                    ntree = entry.getNested();
                }
            }
            writeResult((TreeResult)value, ntree, api, writer);
        } else if (value instanceof RDFNode) {
            RDFNode n = (RDFNode) value;
            if (n.isURIResource()) {
                String uri = n.asResource().getURI();
                if (isArrayElt) {
                    writer.arrayElement(uri);
                } else {
                    writer.pair(key, uri);
                }
                // if (isArrayElt) writer.arrayElementProcess(); else
                // writer.key(key);
                // writer.startObject();
                // if (n.isURIResource()) {
                // writer.pair("@id", n.asResource().getURI());
                // }
                // writer.finishObject();
            } else if (n.isAnon()) {
                // Do nothing - no nested values since it's not a TreeResult so nothing we can usefully serialize
            } else {
                Literal l = n.asLiteral();
                String lex = l.getLexicalForm();
                if (l.getDatatype() == null || l.getDatatypeURI().equals(RDF.langString.getURI())) {
                    String lang = l.getLanguage();
                    if (lang == null || lang.isEmpty() || !api.isShowLangTag()) {
                        if (isArrayElt)
                            writer.arrayElement(lex);
                        else
                            writer.pair(key, lex);
                    } else {
                        if (isArrayElt)
                            writer.arrayElementProcess();
                        else
                            writer.key(key);
                        writer.startObject();
                        writer.pair(VALUE_FIELD, lex);
                        writer.pair(LANGUAGE_FIELD, lang);
                        writer.finishObject();
                    }
                } else {
                    Object jv = l.getValue();
                    if (jv instanceof Number) {
                        if (lex.equals("NaN") || lex.contains("INF")) {
                            // legal in RDF and XSD but not legal in JSON, omit
                        } else {
                            if (isArrayElt)
                                writer.arrayElement((Number) jv);
                            else
                                writer.pair(key, (Number) jv);
                        }
                    } else if (jv instanceof Boolean) {
                        if (isArrayElt)
                            writer.arrayElement((Boolean) jv);
                        else
                            writer.pair(key, (Boolean) jv);
                    } else {
                        // TODO handle other typed literals
                        if (isArrayElt)
                            writer.arrayElement(lex);
                        else
                            writer.pair(key, lex);
                    }
                }
            }
        }
    }

}
