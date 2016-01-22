/******************************************************************
 * File:        GenericWriter.java
 * Created by:  Dave Reynolds
 * Created on:  22 Jan 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.marshalling;

import java.io.IOException;
import java.io.OutputStream;

import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.util.EpiException;

/**
 * Support for serializing a result or result stream.
 * Not used for Jersey (which finds the right marshaller for itself)
 * but useful in other contexts such as batch report generation.
 * Only guaranteed to support CSV and JSON output.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class GenericWriter {
    public static final String PLAIN_JSON_MT = "application/json";
    public static final String PLAIN_CSV_MT = "text/csv";
    
    public static void write(ResultOrStream results, OutputStream stream, String mediatype) throws IOException {
        if (mediatype.startsWith(PLAIN_JSON_MT)) {
            if (results instanceof Result) {
                new ResultJSON().writeTo((Result)results, null, null, null, null, null, stream);
                
            } else if (results instanceof ResultStream) {
                new ResultStreamJSON().writeTo((ResultStream)results, null, null, null, null, null, stream);
                
            }
        } else if (mediatype.startsWith(PLAIN_CSV_MT)) {
            if (results instanceof Result) {
                new ResultCSV().writeTo((Result)results, null, null, null, null, null, stream);
                
            } else if (results instanceof ResultStream) {
                new ResultStreamCSV().writeTo((ResultStream)results, null, null, null, null, null, stream);
                
            }
        } else {
            throw new EpiException("Generic writer does not support mediatype: " + mediatype);
        }
    }
}
