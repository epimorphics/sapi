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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.sapi2.Sapi2BaseEndpointSpec;
import com.epimorphics.simpleAPI.views.ClassSpec;
import com.epimorphics.simpleAPI.views.ModelSpec;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.NameUtils;
import com.epimorphics.vocabs.SKOS;

/**
 * Experimental code to bootstrap an OWL ontology file from
 * a model spec.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class OntTool {
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
        
        OntTool doc = new OntTool( args[0], args[1] );
        doc.generateOntology();
    }
    
    public OntTool(String outputDir, String specfile) throws IOException {
        this.outputDir = new File(outputDir);
        app  = new App("test", new File(specfile));
        app.startup();
        api = app.getA(API.class);
        if (api == null) {
            System.err.println("No API defined, aborting");
        }
    }
    
    public void generateOntology() throws IOException {
        vocabulary = getVocabulary();
        for (ModelSpec modelspec : api.listConfigs(ModelSpec.class)) {
            System.out.println("Processing model " + modelspec.getName());
            try ( FileWriter out = new FileWriter( new File(outputDir, modelspec.getName() + ".ttl") ) ) {
                writePrefixes(out, api.getPrefixes());
                writeModel(out, modelspec);
            } catch (IOException e) {
                System.out.println("Cand create output file");
            }
        }
    }
    
    protected void writePrefixes(FileWriter out, PrefixMapping prefixes) throws IOException {
        for (Map.Entry<String,String> entry  : prefixes.getNsPrefixMap().entrySet()) {
            out.write( String.format("@prefix %s: <%s> .\n", entry.getKey(), entry.getValue() ) );
        }
        out.write("\n");
    }
    
    protected void writeModel(FileWriter out, ModelSpec model) throws IOException {
        for (ClassSpec cs : model.getClassSpecs()) {
            String cref = writeClassDef(out, cs);
            for (PropertySpec ps : cs.getChildren()) {
                writeProp(out, ps, cref);
            }
        }
    }
    
    protected void writeProp(FileWriter out, PropertySpec ps, String cref) throws IOException {
        String uri = ps.getProperty().getURI();
        
        String range = ps.getRange();
        if (range != null) {
            range = api.getPrefixes().shortForm(range);
        }
        String ptype = "owl:ObjectProperty";
        if (range == null || range.equals("rdf:PlainLiteral") || range.equals("rdf:langString") || range.equals("rdfs:Literal") ) {
            ptype = "owl:DatatypeProperty";
            range = null;
        } else if ( range.startsWith("xsd:") ) {
            ptype = "owl:DatatypeProperty";
        }

        out.write( String.format( "%s a %s;\n    rdfs:label '%s'@en ;\n", asURIReference(uri), ptype, ps.getJsonName() ) );
        String comment = ps.getComment();
        if (comment != null) {
            out.write(String.format("    rdfs:comment \"\"\"%s\"\"\"@en ;\n", comment));
        }
        out.write(String.format("    rdfs:domain %s ;\n", cref));
        if (range != null) {
            out.write(String.format("    rdfs:range %s ;\n", range));
        }
        out.write("    .\n\n");
    }
        
    protected String writeClassDef(FileWriter out, ClassSpec cs) throws IOException {
        String uri = cs.getUri().getURI();
        String localName = RDFUtil.getLocalname(uri);
        String ref = asURIReference(uri);
        
        out.write("# Definitions for class " + localName + "\n");
        out.write( String.format("%s a owl:Class;\n    rdfs:label '%s'@en .\n\n", ref, localName) );
        return ref;
    }
    
    protected String asURIReference(String uri) {
        String suri = api.getPrefixes().shortForm(uri);
        return uri.equals(suri) ? "<" + uri + ">" : suri;
    }
    
    public Model getVocabulary() throws IOException {
        Model vocabulary = ModelFactory.createDefaultModel();
        SparqlSource vocabSource = app.getComponentAs(VOCAB_SOURCE, SparqlSource.class);
        if (vocabSource == null) {
            System.out.println("Waring: No vocabularies defined");
        } else {
            vocabulary.add( vocabSource.getAccessor().getModel() );
            vocabulary.setNsPrefixes( api.getPrefixes() );
        }
        return vocabulary;
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
                type = "string (language specific)";
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

