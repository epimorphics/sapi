/******************************************************************
 * File:        Main.java
 * Created by:  Dave Reynolds
 * Created on:  4 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.sapi.doctool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.RDFDataMgr;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.EndpointSpec;
import com.epimorphics.simpleAPI.core.EndpointSpecFactory;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.impl.JSONMapEntry;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Generate raw documentation for a set of API specs plus a vocabulary.
 * <p>
 * Generates an index.html listing the terms in the vocabulary (very crude) and a {spec}.html
 * table for each specification file in a directory of specification files.
 * </p>
 * <p>
 * Note that the prefixes declared in the vocabulary file must match the
 * prefixes used in the specification files.
 * </p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Doctool {
    public static final String VOCAB_INDEX_FILE = "index.html";
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage:  doctool vocab-file  spec-directory");
            System.exit(1);
        }
        
        Model vocabulary = RDFDataMgr.loadModel( args[0] );
        generateVocabIndex(vocabulary);
        
        File specDir = new File( args[1] );
        for (String specfn : specDir.list()) {
            EndpointSpec spec = EndpointSpecFactory.read(null, new File(specDir, specfn).getPath());
            generateDataTable(NameUtils.removeExtension(specfn), spec, vocabulary);
        }
    }
    
    public static void generateVocabIndex(Model vocabulary) throws IOException {
        FileWriter out = new FileWriter( VOCAB_INDEX_FILE );
        List<Statement> terms = vocabulary.listStatements(null, RDFS.comment, (RDFNode)null).toList();
        Collections.sort(terms, new Comparator<Statement>() {
            @Override
            public int compare(Statement o1, Statement o2) {
                return asJsonName(o1).compareTo( asJsonName(o2) );
            }
        });
        
        // Output the index
        out.write( "<div class=\"vocab-index\">\n" );
        out.write( "  <ul>\n" );
        for (Statement s : terms) {
            out.write( String.format("    <li><a href=\"#definition-%s\">%s</a></li>\n", asJsonName(s), asJsonName(s)) );
        }
        out.write( "  </ul>\n" );
        out.write( "</div>\n" );
        
        // Output the definitions
        out.write( "<div class=\"vocab\">\n" );
        for (Statement s : terms) {
            out.write( String.format("    <div class='term' id='definition-%s'>%s (%s)</div>\n", asJsonName(s), asJsonName(s), asRDFName(s)) );
            out.write( String.format("    <div class='definition'>%s</div>\n", asComment(s)) );
        }
        out.write( "</div>\n" );
        out.close();
    }
    
    private static String asJsonName(Statement s) {
        return RDFUtil.getLocalname( s.getSubject() );
    }
    
    private static String asRDFName(Statement s) {
        Resource r = s.getSubject();
        return r.getModel().shortForm( r.getURI() );
    }
    
    private static String asComment(Statement s) {
        return s.getObject().asLiteral().getLexicalForm();
    }

    public static void generateDataTable(String specname, EndpointSpec spec, Model vocabulary) throws IOException {
        JSONMap mapping = spec.getMap();
        if (mapping == null) return;
        
        System.out.println("Processing " + specname);
        FileWriter out = new FileWriter( specname + ".html" );
        Map<String, JSONMap> nested = writeMapping(out, mapping, vocabulary);
        while (! nested.isEmpty() ) {
            Map<String, JSONMap> processing = nested;
            nested = new HashMap<String, JSONMap>();
            for (String field : processing.keySet()) {
                out.write( String.format("<p>Structure of nested field <code>%s</code>:</p>", field) );
                nested.putAll( writeMapping(out, processing.get(field), vocabulary));
            }
        }
        out.close();
    }
    
    private static Map<String, JSONMap> writeMapping(FileWriter out, JSONMap mapping, Model vocabulary) throws IOException {
        Map<String, JSONMap> nested = new HashMap<String, JSONMap>();
        
        out.write( "<table class='table table-condensed table-bordered'>\n" );
        out.write( "  <thead>\n" );
        out.write( "    <tr><th>Field</th><th>Meaning</th><th>Type</th><th>Occurs</th></tr>\n" );
        out.write( "  </thead>\n" );
        out.write( "  <tbody>\n" );
        for (JSONMapEntry entry : mapping.getMapping()) {
            String jsonname = entry.getJsonName();
            if (entry.isParent()) {
                nested.put(jsonname, entry.getNestedMap());
            }
            Resource prop = vocabulary.getResource( vocabulary.expandPrefix( entry.getProperty() ) );
            String meaning = entry.getComment();
            if (meaning == null) {
                meaning = RDFUtil.getStringValue(prop, RDFS.comment, "No definition found");
            }
            String type = entry.getType();
            if (type == null) {
                Resource typeR = RDFUtil.getResourceValue(prop, RDFS.range);
                if (typeR != null && typeR.isURIResource()) {
                    type = vocabulary.shortForm( typeR.getURI() );
                }
            }
            if (type == null) {
                type = "";
            } else if (type.equals("rdf:PlainLiteral")) {
                type = "string";
            }
            String occurs = entry.isOptional() ? "optional" : "";
            if (entry.isMultivalued()) {
                occurs = "multi-valued";
            }
            out.write( String.format("    <tr><td><code>%s</code></td><td>%s</td><td>%s</td><td>%s</td></tr>\n", jsonname, meaning, type, occurs) );
        }
        out.write( "  </tbody>\n" );
        out.write( "</table>\n" );
        
        return nested;
    }
}

