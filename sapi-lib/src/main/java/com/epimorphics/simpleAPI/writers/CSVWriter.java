/******************************************************************
 * File:        CSVWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  8 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.EpiException;

/**
 * Support for writing Results and ResultStreams out as CSV file streams.
 * Only supports TreeResults
 */
public class CSVWriter {
    static final Logger log = LoggerFactory.getLogger( CSVWriter.class );
    
    protected static final String LINE_END = "\r\n" ;   // See https://tools.ietf.org/html/rfc4180
    protected static final String SEP = "," ;
    protected static final String VALUE_SEP = "|" ;
    protected static final String ID_COL = "@id" ;
    protected static final Charset ENC = StandardCharsets.UTF_8;

    protected List<ViewPath> paths;
    protected OutputStream out;
    protected boolean includeID = true;
    protected ViewPath flattenPath;
    protected boolean writtenHeaders = false;
    
    public CSVWriter(OutputStream out) {
        this.out = out;
    }
    
    /**
     * By default the URI (id field) of the value set is included in the output,
     * set this to false to suppress this.
     * @param includeID
     */
    public void setIncludeID(boolean includeID) {
        this.includeID = includeID;
    }
    
    /**
     * Override the set of paths to include in the CSV.
     * @param vpaths array of dotted path names to put in the CSV output
     */
    public void setViewPaths(String[] vpaths) {
        paths = new ArrayList<>();
        for (String path : vpaths) {
            paths.add( ViewPath.fromDotted(path) );
        }
    }
    
    /**
     * Write an entire value stream then close the output.
     */
    public void write(ResultStream stream) throws IOException {
        long count = 0;
        try {
            for (Result result : stream) {
                if (result instanceof TreeResult) {
                    write( (TreeResult) result);
                } else {
                    // TODO review how to handle this case
                    throw new EpiException("CSV output not support for RDF descriptions");
                }
                count++;
            }
            log.info("Returned " + count + " coalesced rows");
        } finally {
            out.close();
        }
    }

    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            throw new EpiException(e);
        }
    }
    
    /**
     * Write a single Result to the output.
     * If this is the first row it will generate a CSV header row as well.
     */
    public void write(TreeResult result) throws IOException {
        Call call = result.getCall();
        EndpointSpec spec = call.getEndpoint();

        if (paths == null) {
            ViewMap viewmap = spec.getView( call.getRequest().getViewName() );
            paths = viewmap.getAllPaths();
            if (paths == null) {
                throw new EpiException("Can't render tree to CSV without a view specification");
            }
            for (Iterator<ViewPath> i = paths.iterator(); i.hasNext();) {
                ViewEntry entry = viewmap.findEntry(i.next());
                if (entry != null && entry.isHide()) {
                    i.remove();
                }
            }
            String fp = spec.getFlattenPath();
            if (fp != null) {
                flattenPath = ViewPath.fromDotted( fp );
            }
        }
        
        if (!writtenHeaders) {
            writeHeaders( spec.getAPI().isFullPathsInCSVHeaders() );
            writtenHeaders = true;
        }
        
        StringBuffer buf = new StringBuffer();
        if ( flattenPath == null) {
            writeResult(result, buf);
        } else {
            for (TreeResult r : result.splitAt(flattenPath) ) {
                writeResult(r, buf);
            }
        }
        out.write( buf.toString().getBytes(ENC) );
    }
    
    /**
     * Write just the header line. Only useful if iterating over the results 
     * externally and want and empty result sets to include header row only. 
     */
    public void writeHeaders(API api) throws IOException {
        if (!writtenHeaders) {
            writeHeaders( api.isFullPathsInCSVHeaders() );
            writtenHeaders = true;
        }
    }
    
    protected void writeResult(TreeResult result, StringBuffer buf) {
        boolean started = false;
        for (ViewPath path : paths) {
            if (path.isEmpty() && !includeID) continue;
            if (started) { buf.append(SEP); } else { started = true; }
            Collection<RDFNode> values = result.get(path);
            if (values == null || values.isEmpty()) {
                // Optional value
                buf.append( safeString("") );
            } else {
                String v = "";
                boolean multi = false;
                for (RDFNode value : values) {
                    if (multi) v += VALUE_SEP; else multi = true;
                    v += serializeNode( (RDFNode)value );
                }
                buf.append( safeString(v) );
            }
        }
        buf.append(LINE_END);
    }
    
    protected String serializeNode(RDFNode node) {
        if (node.isAnon()) {
            return "[]";
        } else if (node.isURIResource()) {
            return node.asResource().getURI();
        } else {
            return node.asLiteral().getLexicalForm();
        }
    }
    
    protected void writeHeaders(boolean showDotted) throws IOException {
        StringBuffer buf = new StringBuffer();
        boolean started = false;
        for (ViewPath path : paths) {
            if (started) { buf.append(SEP); }
            if ( path.isEmpty() ) {
                if ( includeID ) {
                    buf.append( ID_COL );
                    started = true;
                }
            } else {
                buf.append( safeString( showDotted ? path.asDotted() : path.last() ) );
                started = true;
            }
        }
        buf.append(LINE_END);
        out.write( buf.toString().getBytes(ENC) );
    }
    
    protected String safeString(String str) {
        if (str.contains("\"") || str.contains(",") || str.contains("\r") || str.contains("\n") ) {
            str = "\"" + str.replaceAll("\"", "\"\"") + "\"";
        } else if ( str.isEmpty() ) {
            // Return the quoted empty string. 
            str = "\"\"" ;
        } 
        return str;
    }
}
