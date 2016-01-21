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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (paths == null) {
            Call call = result.getCall();
            EndpointSpec spec = call.getEndpoint();
            ViewMap viewmap = spec.getView( call.getRequest().getViewName() );
            paths = viewmap.getAllPaths();
            if (paths == null) {
                throw new EpiException("Can't render tree to CSV without a view specification");
            }
            for (Iterator<ViewPath> i = paths.iterator(); i.hasNext();) {
                ViewEntry entry = viewmap.findEntry(i.next());
                if (entry != null && entry.isSuppressId()) {
                    i.remove();
                }
            }
            writeHeaders( spec.getAPI().isFullPathsInCSVHeaders() );
        }
        
        StringBuffer buf = new StringBuffer();
        boolean started = false;
        for (ViewPath path : paths) {
            if (started) { buf.append(SEP); } else { started = true; }
            Collection<RDFNode> values = result.get(path);
            if (values == null || values.isEmpty()) {
                // Optional value
                buf.append( safeString("") );
            } else {
                boolean multi = false;
                for (RDFNode value : values) {
                    if (multi) buf.append(VALUE_SEP); else multi = true;
                    buf.append( serializeNode( (RDFNode)value ) );
                }
            }
        }
        buf.append(LINE_END);
        out.write( buf.toString().getBytes(ENC) );
    }
    
    protected String serializeNode(RDFNode node) {
        if (node.isAnon()) {
            return "[]";
        } else if (node.isURIResource()) {
            return node.asResource().getURI();
        } else {
            return safeString( node.asLiteral().getLexicalForm() );
        }
    }
    
    protected void writeHeaders(boolean showDotted) throws IOException {
        StringBuffer buf = new StringBuffer();
        boolean started = false;
        for (ViewPath path : paths) {
            if (started) { buf.append(SEP); } else { started = true; }
            if ( path.isEmpty() ) {
                buf.append( ID_COL );
            } else {
                buf.append( safeString( showDotted ? path.asDotted() : path.last() ) );
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
