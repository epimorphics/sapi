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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.util.EpiException;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Support for writing ValueSets and streams out as CSV file streams.
 * Assumes all valuesets have the same key set as the first and are not mapped to a nested structure.
 */
public class CSVWriter {
    static final Logger log = LoggerFactory.getLogger( CSVWriter.class );
    
    protected static final String LINE_END = "\r\n" ;   // See https://tools.ietf.org/html/rfc4180
    protected static final String SEP = "," ;
    protected static final String VALUE_SEP = "|" ;
    protected static final String ID_COL = "@id" ;
    protected static final Charset ENC = StandardCharsets.UTF_8;
    
    protected List<String> headers;
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
    public void write(ValueStream stream) throws IOException {
        long count = 0;
        try {
            for (ValueSet vs : stream) {
                write(vs);
                count++;
            }
            log.info("Returned " + count + " coalesced rows");
        } finally {
            out.close();
        }
    }

    /**
     * Write a single valueset to the output.
     * If this is the first row it will generate a CSV header row as well.
     */
    public void write(ValueSet valueset) throws IOException {
        if (headers == null) {
            List<String> keys = valueset.listSortedKeys();
            if (includeID) {
                headers = new ArrayList<>( keys.size() + 1 );
                headers.add(ID_COL);
                headers.addAll(keys);
            } else {
                headers = keys;
            }
            writeHeaders();
        }
        StringBuffer buf = new StringBuffer();
        boolean started = false;
        for (String header : headers) {
            if (started) { buf.append(SEP); } else { started = true; }
            if (header.equals(ID_COL)) {
                buf.append( serializeNode(valueset.getId()) );
            } else {
                KeyValues kv = valueset.getKeyValues(header);
                if (kv == null) {
                    // Optional value
                    buf.append( safeString("") );
                } else {
                    List<Object> values = kv.getValues();
                    if (values.isEmpty()) {
                        buf.append( safeString("") );
                    } else {
                        boolean multi = false;
                        for (Object value : values) {
                            if (multi) buf.append(VALUE_SEP); else multi = true;
                            if (value instanceof RDFNode) {
                                buf.append( serializeNode( (RDFNode)value ) );
                            } else {
                                throw new EpiException("Internal inconsistency in value set, expecting RDF values");
                            }
                        }
                    }
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
    
    protected void writeHeaders() throws IOException {
        StringBuffer buf = new StringBuffer();
        boolean started = false;
        for (String header : headers) {
            if (started) { buf.append(SEP); } else { started = true; }
            buf.append( safeString(header) );
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
