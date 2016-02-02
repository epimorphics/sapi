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
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigSpecFactory;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.endpoints.EndpointSpecFactory;
import com.epimorphics.util.NameUtils;
import com.epimorphics.vocabs.SKOS;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

/**
 * Generate raw documentation for a set of API specs plus a set of vocabularies.
 * <p>
 * Generates an index.html listing the terms in the vocabulary (very crude) and a {spec}.html
 * table for each specification file in a directory of specification files.
 * </p>
 * <p>
 * Define the endpoints, prefixes and vocabulary elements in an app.conf file.
 * The vocabulary files are assumed to be defined as a SparqlSource
 * </p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Doctool {
    public static final String VOCAB_INDEX_FILE = "index.html";
    
    protected File outputDir;
    protected Model vocabulary;
    
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage:  doctool output-directory vocab-directory  spec-directory");
            System.exit(1);
        }
        
        Doctool doc = new Doctool( args[0] );
        Model vocabulary = doc.loadVocabularies( args[1] );
        doc.generateVocabIndex(vocabulary);
        
        File specDir = new File( args[2] );
        for (String specfn : specDir.list()) {
            doc.generateDataTable( new File(specDir, specfn), vocabulary );
        }
    }
    
    public Doctool(String outputDir) {
        this.outputDir = new File(outputDir);
    }
    
    public Model loadVocabularies( String vocabDir ) throws IOException {
        File vocabDirF = new File( vocabDir );
        Model vocabulary = ModelFactory.createDefaultModel();
        for (String fname : vocabDirF.list( (dir,fn) -> fn.endsWith(".ttl") )) {
            Model v = RDFDataMgr.loadModel( new File(vocabDirF, fname).getPath() );
            vocabulary.add(v);
            vocabulary.setNsPrefixes(v);
        }
        return vocabulary;
    }
    
    public void generateVocabIndex(Model vocabulary) throws IOException {
        FileWriter out = new FileWriter( new File(outputDir, VOCAB_INDEX_FILE) );
        List<Statement> terms 
            = vocabulary.listStatements(null, RDFS.comment, (RDFNode)null)
            .andThen( vocabulary.listStatements(null, SKOS.definition, (RDFNode)null) )
            .toList();
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
    
    private String asJsonName(Statement s) {
        return RDFUtil.getLocalname( s.getSubject() );
    }
    
    private String asRDFName(Statement s) {
        Resource r = s.getSubject();
        return r.getModel().shortForm( r.getURI() );
    }
    
    private String asComment(Statement s) {
        return s.getObject().asLiteral().getLexicalForm();
    }
    
    public void generateDataTable(File file, Model vocabulary) throws IOException {
        EndpointSpec spec = (EndpointSpec) ConfigSpecFactory.read(new API(), file.getPath());
        generateDataTable( spec.getName(), spec, vocabulary);
    }

    public void generateDataTable(String specname, EndpointSpec spec, Model vocabulary) throws IOException {
        System.out.println("Found " + specname);
//        JSONMap mapping = spec.getMap();
//        if (mapping == null) return;
//        
//        System.out.println("Processing " + specname);
//        FileWriter out = new FileWriter( specname + ".html" );
//        Map<String, JSONMap> nested = writeMapping(out, mapping, vocabulary);
//        while (! nested.isEmpty() ) {
//            Map<String, JSONMap> processing = nested;
//            nested = new HashMap<String, JSONMap>();
//            for (String field : processing.keySet()) {
//                out.write( String.format("<p>Structure of nested field <code>%s</code>:</p>", field) );
//                nested.putAll( writeMapping(out, processing.get(field), vocabulary));
//            }
//        }
//        out.close();
    }
    
    /*
    private static Map<String, JSONMap> writeMapping(FileWriter out, JSONMap mapping, Model vocabulary) throws IOException {
        Map<String, JSONMap> nested = new HashMap<String, JSONMap>();
        
        out.write( "<table class='table table-condensed table-bordered'>\n" );
        out.write( "  <thead>\n" );
        out.write( "    <tr><th>Field</th><th>Meaning</th><th>Type</th><th>Occurs</th></tr>\n" );
        out.write( "  </thead>\n" );
        out.write( "  <tbody>\n" );
        List<JSONMapEntry> entries = mapping.getMapping();
        Collections.sort(entries, new Comparator<JSONMapEntry>() {
            @Override
            public int compare(JSONMapEntry o1, JSONMapEntry o2) {
                return o1.getJsonName().compareTo( o2.getJsonName() );
            }
        });
        for (JSONMapEntry entry : mapping.getMapping()) {
            String jsonname = entry.getJsonName();
            if (entry.isParent()) {
                nested.put(jsonname, entry.getNestedMap());
            }
            Resource prop = vocabulary.getResource( vocabulary.expandPrefix( entry.getProperty() ) );
            String meaning = entry.getComment();
            if (meaning == null) {
                meaning = RDFUtil.getStringValue(prop, RDFS.comment);
            }
            if (meaning == null) {
                meaning = RDFUtil.getStringValue(prop, SKOS.definition, "No definition found");
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
    */
}

