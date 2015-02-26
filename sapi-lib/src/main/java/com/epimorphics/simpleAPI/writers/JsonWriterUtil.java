/******************************************************************
 * File:        JsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.List;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodeDescription;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Utility for writing RDF descriptions or result sets to JSON.
 */
public class JsonWriterUtil {

    public static void writeValueSet(JSONMap map, ValueSet values,
            JSFullWriter out) {
        out.startObject();
        String id = values.getStringID();
        if (id != null) {
            out.pair("@id", id);
        }
        // for (KeyValues kv : values.listKeyValues()) {
        for (String key : values.listSortedKeys()) {
            KeyValues kv = values.getKeyValues(key);
            writeKeyValues(map, kv, out);
        }
        out.finishObject();
    }

    protected static void writeKeyValues(JSONMap map, KeyValues vals,
            JSFullWriter out) {
        String key = vals.getKey();
        JSONNodeDescription policy = map.getEntry(key);
        List<Object> nodevals = vals.getValues();
        if (nodevals.isEmpty()) {
            return;
        } else if (nodevals.size() > 1 || policy.isMultivalued()) {
            nodevals = vals.getSortedValues(); // Make this controllable through
                                               // a mapping option?
            out.key(key);
            out.startArray();
            for (Object n : nodevals) {
                writeNode(map, policy, key, n, out, true);
            }
            out.finishArray();
        } else {
            writeNode(map, policy, key, nodevals.get(0), out, false);
        }
    }

    protected static void writeNode(JSONMap map, JSONNodeDescription policy,
            String key, Object value, JSFullWriter writer, boolean isArrayElt) {
        if (value instanceof ValueSet) {
            if (isArrayElt)
                writer.arrayElementProcess();
            else
                writer.key(key);
            writeValueSet(map, (ValueSet) value, writer);
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
            } else {
                Literal l = n.asLiteral();
                String lex = l.getLexicalForm();
                if (l.getDatatype() == null) {
                    String lang = l.getLanguage();
                    if (lang == null || lang.isEmpty()
                            || !policy.showLangTag(lang)) {
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
                        writer.pair("@value", lex);
                        writer.pair("@language", lang);
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
