/******************************************************************
 * File:        JsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.EndpointSpecFactory;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONNodeDescription;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Utility for writing RDF descriptions or result sets to JSON. 
 */
public class JsonWriterUtil {

    public static void writeValueSet(JSONMap map, ValueSet values, JSFullWriter out) {
        out.startObject();
        String id = values.getStringID();
        if (id != null) {
            out.pair("@id", id);
        }
//        for (KeyValues kv : values.listKeyValues()) {
        for (String key : values.listSortedKeys()) {
            KeyValues kv = values.getKeyValues(key);
            writeKeyValues(map, kv, out);
        }
        out.finishObject();
    }
    
    protected static void writeKeyValues(JSONMap map, KeyValues vals, JSFullWriter out) {
        String key = vals.getKey();
        JSONNodeDescription policy = map.getEntry(key);
        List<Object> nodevals = vals.getValues();
        if (nodevals.isEmpty()) {
            return;
        } else if (nodevals.size() > 1 || policy.isMultivalued()) {
            nodevals = vals.getSortedValues();   // Make this controllable through a mapping option?
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
    
    protected static void writeNode(JSONMap map, JSONNodeDescription policy, String key, Object value, JSFullWriter writer, boolean isArrayElt) {
        if (value instanceof ValueSet) {
            if (isArrayElt)  writer.arrayElementProcess(); else writer.key(key);
            writeValueSet(map, (ValueSet)value, writer);
        } else if (value instanceof RDFNode) {
            RDFNode n = (RDFNode)value;
            if (n.isURIResource()) {
                String uri = n.asResource().getURI();
                if (isArrayElt) {
                    writer.arrayElement( uri ); 
                } else {
                    writer.pair(key, uri);
                }
//                if (isArrayElt)  writer.arrayElementProcess(); else writer.key(key);
//                writer.startObject();
//                if (n.isURIResource()) {
//                    writer.pair("@id", n.asResource().getURI());
//                }
//                writer.finishObject();
            } else {
                Literal l = n.asLiteral();
                String lex = l.getLexicalForm();
                if (l.getDatatype() == null) {
                    String lang = l.getLanguage();
                    if (lang == null || lang.isEmpty() || !policy.showLangTag(lang)) {
                        if (isArrayElt) writer.arrayElement(lex); else writer.pair(key, lex);
                    } else {
                        if (isArrayElt)  writer.arrayElementProcess(); else writer.key(key);
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
                            if (isArrayElt) writer.arrayElement( (Number)jv ); else writer.pair(key, (Number)jv);
                        }
                    } else if (jv instanceof Boolean) {
                        if (isArrayElt) writer.arrayElement( (Boolean)jv ); else writer.pair(key, (Boolean)jv);
                    } else {
                        if (isArrayElt) writer.arrayElement( lex ); else writer.pair(key, lex);
                    }
                }
            }
        }        
    }    

    /** Output and array of alternative hasFormat values */
    public static void writeFormats(JsonObject config, String request, JSFullWriter out) {
        if (config.hasKey(EndpointSpecFactory.HAS_FORMAT)) {
            writeFormat(config.get(EndpointSpecFactory.HAS_FORMAT), request, out);
        }        
    }
    
    private static void writeFormat(JsonValue format, String request, JSFullWriter out) {
        List<String> formats = new ArrayList<>();
        if (format.isString()) {
            addFormat(formats, format.getAsString().value(), request, out);
        } else if (format.isArray()) {
            for (Iterator<JsonValue> i = format.getAsArray().iterator(); i.hasNext();) {
                addFormat(formats,  i.next().getAsString().value(), request, out);
            }
        }
        if ( ! formats.isEmpty() ) {
            out.key("hasFormat");
            out.startArray();
            for (String f : formats) {
                out.arrayElement(f);
            }
            out.finishArray();
        }
    }
    
    private static void addFormat(List<String> formats, String format,  String request, JSFullWriter out) {
        if ("json".equals(format)) return;
        
        Matcher m = URIPAT.matcher(request);
        if (m.matches()) {
            formats.add( m.group(1) + "." + format + (m.group(3) == null ? "" : m.group(3)) );
        } else {
            formats.add( request + "." + format );
        }
    }
    
    protected static final Pattern URIPAT = Pattern.compile("([^?]*)(\\.[a-z]*)?(\\?.*)?");
    
    public static void main(String[] args) {
        Matcher m = URIPAT.matcher("http://localhost:8080/api/id/stations?_limit=10&qualifier=Groundwater");
        System.out.println( String.format("Matches %b 1=%s 2=%s 3=%s", m.matches(), m.group(1), m.group(2), m.group(3)) );
    }
}

