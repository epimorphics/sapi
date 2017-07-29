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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.sapi2.Sapi2BaseEndpointSpec;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.NameUtils;
import com.epimorphics.vocabs.SKOS;

/**
 * Generate raw documentation for a set of API specs plus a set of vocabularies.
 * <p>
 * Generates an index.html listing the terms in the vocabulary (very crude) and a {spec}.html
 * table for each specification file in a directory of specification files.
 * </p>
 * <p>
 * Define the endpoints, prefixes and vocabulary elements in an app.conf file.
 * The vocabulary files are assumed to be defined as a SparqlSource called vocabulary.
 * </p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Doctool {
    public static final String VOCAB_SOURCE = "vocabulary";
    public static final String VOCAB_INDEX_FILE = "_index.html";
    
    protected File outputDir;
    protected App  app;
    protected API  api;
    protected Model vocabulary;
    
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage:  doctool output-directory spec.conf");
            System.exit(1);
        }
        
        Doctool doc = new Doctool( args[0], args[1] );
        doc.generateVocabIndex();
        doc.generateDataTables();
    }
    
    public Doctool(String outputDir, String specfile) throws IOException {
        this.outputDir = new File(outputDir);
        app  = new App("test", new File(specfile));
        app.startup();
        api = app.getA(API.class);
        if (api == null) {
            System.err.println("No API defined, aborting");
        }
    }
    
    public Model getVocabulary() throws IOException {
        vocabulary = ModelFactory.createDefaultModel();
        SparqlSource vocabSource = app.getComponentAs(VOCAB_SOURCE, SparqlSource.class);
        if (vocabSource == null) {
            System.out.println("Waring: No vocabularies defined");
        } else {
            vocabulary.add( vocabSource.getAccessor().getModel() );
            vocabulary.setNsPrefixes( api.getPrefixes() );
        }
        return vocabulary;
    }
    
    public void generateVocabIndex() throws IOException {
        getVocabulary();
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
        out.write( "<div class=\"apidoc-vocab-index\">\n" );
        out.write( "  <ul>\n" );
        for (Statement s : terms) {
            out.write( String.format("    <li><a href=\"#definition-%s\">%s</a></li>\n", asJsonName(s), asJsonName(s)) );
        }
        out.write( "  </ul>\n" );
        out.write( "</div>\n" );
        
        // Output the definitions
        out.write( "<div class=\"vocab\">\n" );
        for (Statement s : terms) {
            out.write( String.format("    <div class='apidoc-term' id='definition-%s'>%s (%s)</div>\n", asJsonName(s), asJsonName(s), asRDFName(s)) );
            out.write( String.format("    <div class='apidoc-definition'>%s</div>\n", asComment(s)) );
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
    
    public void generateDataTables() throws IOException {
        getVocabulary();
        for (Sapi2BaseEndpointSpec s : api.listSpecs()) {
            System.out.println("Processing: " + s.getName());
            FileWriter out = new FileWriter( new File(outputDir, "_" + s.getName() + ".html") );
            writeMap(out, s);
            out.close();
        }
    }
    
    public void writeMap(FileWriter out, Sapi2BaseEndpointSpec spec) throws IOException {
        ViewSet viewset = new ViewSet(spec);

        out.write( "<table class='apidoc-table table table-condensed table-bordered'>\n" );
        out.write( "  <thead>\n" );
        out.write( "    <tr><th>Field</th><th>Meaning</th><th>Type</th><th>Occurs</th><th>Views</th></tr>\n" );
        out.write( "  </thead>\n" );
        out.write( "  <tbody>\n" );

        for (String path : viewset.listPaths()) {
            PropertySpec entry = viewset.getEntry(path);
            Resource prop = vocabulary.getResource( vocabulary.expandPrefix( entry.getProperty().getURI() ) );
            String meaning = entry.getComment();
            if (meaning == null) {
                meaning = RDFUtil.getStringValue(prop, RDFS.comment);
            }
            if (meaning == null) {
                meaning = RDFUtil.getStringValue(prop, SKOS.definition, "No definition found");
            }
            
            // Expand item name in definition strings
            
            String itemName = path.contains(".") ? NameUtils.splitBeforeLast(path, ".") : spec.getItemName(); 
            if (itemName == null) {
                itemName = "Item";
            }
            meaning = meaning.replace("{x}", itemName);
            
            String type = entry.getRange();
            if (type == null) {
                Resource typeR = RDFUtil.getResourceValue(prop, RDFS.range);
                if (typeR != null && typeR.isURIResource()) {
                    type = vocabulary.shortForm( typeR.getURI() );
                }
            }
            if (type == null) {
                type = "";
            } else {
                type = vocabulary.shortForm( type );
            }
            if (type.equals("rdf:PlainLiteral")) {
                type = "string";
            } else if (type.equals("rdf:langString")) {
                type = "string (langauge specific)";
            }
            String occurs = entry.isOptional() ? "optional" : "";
            if (entry.isMultivalued()) {
                occurs = "multi-valued";
            }
            out.write( String.format("    <tr><td><code>%s</code></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n", 
                    path, meaning, type, occurs, viewset.viewDescription(path)) );
        }
        
        out.write( "  </tbody>\n" );
        out.write( "</table>\n" );
    }
    
    /**
     * Represents the aggregation of all paths across all views for an endpoint
     */
    public static class ViewSet {
        protected List<PathSet> pathsets = new ArrayList<>();
        
        public ViewSet(Sapi2BaseEndpointSpec spec) {
            for (String viewname : spec.listViewNames()) {
                pathsets.add( new PathSet(viewname, spec.getView(viewname)) );
            }
            Collections.sort(pathsets);
        }
        
        public PathSet longestView() {
            if (pathsets.isEmpty()) {
                return null;
            } else {
                return pathsets.get( 0 );
            }
        }
        
        public List<String> listPaths() {
            PathSet view = longestView();
            if (view == null) {
                return Collections.emptyList();
            } else {
                return longestView().listPaths();
            }
        }
        
        public String viewDescription(String path) {
            String description = "";
            for (PathSet ps : pathsets) {
                if (ps.hasPath(path)) {
                    if (!description.isEmpty()) {
                        description += ", ";
                    }
                    description += ps.viewname;
                }
            }
            return description;
        }
        
        public PropertySpec getEntry(String path) {
            return longestView().getEntry(path);
        }
    }
    
    /**
     * Represents a view with an ordered set of dotted-notation paths for it.
     */
    public static class PathSet implements Comparable<PathSet> {
        protected String viewname;
        protected ViewMap view;
        protected Set<String> paths = new HashSet<>();
        
        public PathSet(String viewname, ViewMap view) {
            this.viewname = viewname;
            this.view = view;
            for (ViewPath path : view.getAllPaths()) {
                if ( !path.isEmpty()) {
                    paths.add( path.asDotted() );
                }
            }
        }

        public List<String> listPaths() {
            List<String> orderedPaths = new ArrayList<>(paths);
            Collections.sort(orderedPaths);
            return orderedPaths;
        }
        
        public boolean hasPath(String path) {
            return paths.contains(path);
        }
        
        public PropertySpec getEntry(String path) {
            return view.findEntry(path);
        }
        
        @Override
        public int compareTo(PathSet other) {
            return - Integer.compare(paths.size(), other.paths.size());
        }
        
    }
}

