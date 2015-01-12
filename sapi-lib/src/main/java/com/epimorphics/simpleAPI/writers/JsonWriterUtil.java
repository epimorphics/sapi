/******************************************************************
 * File:        JsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.JSONOldMap;
import com.epimorphics.simpleAPI.core.JSONNodePolicy;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility for writing RDF descriptions or result sets to JSON. 
 */
public class JsonWriterUtil {

    public static void writeKeyValues(JSONMap map, ValueSet values, JSFullWriter out) {
        writeKeyValues(map, values, values.getStringID(), out, new HashSet<String>()); 
    }
    
    protected static void writeKeyValues(JSONMap map, ValueSet values, String id, JSFullWriter out, Set<String> seen) {
        out.startObject();
        if (id != null) {
            out.pair("@id", id);
            seen.add( id );
        }
        
        List<String> keys = map.listKeys();
        if (keys == null) {
            keys = values.listSortedKeys();
        }
        for (String key : keys) {
            KeyValues vals = values.getKeyValues(key);
            JSONNodePolicy policy = map.policyFor(key);
            if (vals != null) {
                List<RDFNode> nodevals = vals.getValues();
                if (nodevals.isEmpty()) {
                    continue;
                } else if (nodevals.size() > 1 || policy.isMultivalued()) {
                    out.key(key);
                    out.startArray();
                    for (RDFNode n : nodevals) {
                        writeNode(map, policy, key, n, values, out, seen, true);
                    }
                    out.finishArray();
                } else {
                    writeNode(map, policy, key, nodevals.get(0), values, out, seen, false);
                }
            }
        }
        out.finishObject();        
    }

    protected static void writeNode(
            JSONOldMap map, 
            JSONNodePolicy policy, 
            String key, 
            RDFNode value, 
            ValueSet values, 
            JSFullWriter writer, 
            Set<String> seen, 
            boolean isArrayElt) {
   
        if (value.isResource()) {
            Resource r = (Resource)value;
            JSONOldMap nestedMap = policy.getNestedMap();
            String id = r.getURI();
            if ( policy.isNested() && (nestedMap != null || r.listProperties().hasNext()) && !seen.contains(id) ) {
                if (isArrayElt)  writer.arrayElementProcess(); else writer.key(key);
                if (nestedMap == null) {
                    writeKeyValues( map, ValueSet.fromResource(map, r), id, writer, seen );
                } else {
                    writeKeyValues( nestedMap, values, id, writer, seen );
                }
            } else {
                if (value.isURIResource()) {
                    String root = value.asResource().getURI();
                    if (isArrayElt) writer.arrayElement(root); else writer.pair(key, root);
                }
                // No output in anon case if we have no nesting
            }
            
        } else {
            Literal l = value.asLiteral();
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
                    if (isArrayElt) writer.arrayElement( (Number)jv ); else writer.pair(key, (Number)jv);
                } else if (jv instanceof Boolean) {
                    if (isArrayElt) writer.arrayElement( (Boolean)jv ); else writer.pair(key, (Boolean)jv);
                } else {
                    if (isArrayElt) writer.arrayElement( lex ); else writer.pair(key, lex);
                }
            }
        }        
    }    

}

