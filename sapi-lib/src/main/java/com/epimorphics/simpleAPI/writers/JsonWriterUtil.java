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
import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.NodeWriterPolicy;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class JsonWriterUtil {

    public static void writeKeyValues(NodeWriterPolicy policy, KeyValueSet values, JSFullWriter out) {
        writeKeyValues(policy, values, out, new HashSet<String>()); 
    }

    public static void writeKeyValues(NodeWriterPolicy policy, KeyValueSet values, JSFullWriter out, Set<String> seen) {
        out.startObject();
        if (values.getId() != null) {
            out.pair("@id", values.getId());
            seen.add( values.getId());
        }
        for (KeyValues kv : values.listSortedKeyValues()) {
            String key = kv.getKey();
            List<RDFNode> nodevals = kv.getValues();
            if (nodevals.isEmpty()) {
                continue;
            } else if (nodevals.size() == 1) {   // TODO: policy to force structured values?
                writePair(policy, key, nodevals.get(0), out, seen);
            } else {
                out.key(key);
                out.startArray();
                for (RDFNode n : nodevals) {
                    writeArrayElement(policy, key, n, out, seen);
                }
                out.finishArray();
            }
        }
        out.finishObject();
    }

    /**
     * Return serializable object for a JSON node
     */
    protected static Object formatRDFNode(NodeWriterPolicy policy, String key, RDFNode value) {
        if (value.isURIResource()) {
            Resource root = value.asResource();
            if (policy.allowNesting(key) && root.listProperties().hasNext()) {
                return KeyValueSet.fromResource(policy, root);
            } else {
                return root.getURI();
            }
        } else if (value.isAnon()) {
            return KeyValueSet.fromResource(policy, value.asResource());
        } else {
            Literal l = value.asLiteral();
            String lex = l.getLexicalForm();
            if (l.getDatatype() == null) {
                String lang = l.getLanguage();
                if (lang == null || lang.isEmpty() || !policy.showLangTag(key, lang)) {
                    return lex;
                } else {
                    return JsonUtil.makeJson("@value", lex, "@language", lang);
                }
            } else {
                Object jv = l.getValue();
                if (jv instanceof Number || jv instanceof Boolean) {
                    return jv;
                } else {
                    return lex;   // TODO optional structured values for unrecognised types?
                }
            } 
        }
    }
    
    protected static void writePair(NodeWriterPolicy policy, String key, RDFNode value, JSFullWriter writer, Set<String> seen) {
        if (value.isURIResource()) {
            Resource root = value.asResource();
            if (policy.allowNesting(key) && root.listProperties().hasNext() && !seen.contains(root.getURI())) {
                writer.key(key);
                writeKeyValues(policy, KeyValueSet.fromResource(policy, root), writer, seen);
            } else {
                writer.pair(key, policy.uriValue(root));
            }
        } else if (value.isAnon()) {
            writer.key(key);
            writeKeyValues(policy, KeyValueSet.fromResource(policy, value.asResource()), writer, seen);
        } else {
            Literal l = value.asLiteral();
            String lex = l.getLexicalForm();
            if (l.getDatatype() == null) {
                String lang = l.getLanguage();
                if (lang == null || lang.isEmpty() || !policy.showLangTag(key, lang)) {
                    writer.pair(key, lex);
                } else {
                    writer.key(key);
                    writer.startObject();
                    writer.pair("@value", lex);
                    writer.pair("@language", lang);
                    writer.finishObject();
                }
            } else {
                Object jv = l.getValue();
                if (jv instanceof Number) {
                    writer.pair(key, (Number)jv);
                } else if (jv instanceof Boolean) {
                    writer.pair(key, (Boolean)jv);
                } else {
                    writer.pair(key, lex);
                }
            }
        }        
    }
    
    protected static void writeArrayElement(NodeWriterPolicy policy, String key, RDFNode value, JSFullWriter writer, Set<String> seen) {
        if (value.isURIResource()) {
            Resource root = value.asResource();
            if (policy.allowNesting(key) && root.listProperties().hasNext() && !seen.contains(root.getURI())) {
                writer.arrayElementProcess();
                writeKeyValues(policy, KeyValueSet.fromResource(policy, root), writer, seen);
            } else {
                writer.arrayElement(policy.uriValue(root));
            }
        } else if (value.isAnon()) {
            writer.arrayElementProcess();
            writeKeyValues(policy, KeyValueSet.fromResource(policy, value.asResource()), writer, seen);
        } else {
            Literal l = value.asLiteral();
            String lex = l.getLexicalForm();
            if (l.getDatatype() == null) {
                String lang = l.getLanguage();
                if (lang == null || lang.isEmpty() || !policy.showLangTag(key, lang)) {
                    writer.arrayElement(lex);
                } else {
                    writer.arrayElementProcess();
                    writer.startObject();
                    writer.pair("@value", lex);
                    writer.pair("@language", lang);
                    writer.finishObject();
                }
            } else {
                Object jv = l.getValue();
                if (jv instanceof Number) {
                    writer.arrayElement((Number)jv);
                } else if (jv instanceof Boolean) {
                    writer.arrayElement((Boolean)jv);
                } else {
                    writer.arrayElement(lex);
                }
            }
        }        
    }

}

