/******************************************************************
 * File:        CSVStreamingWriter.java
 * Created by:  Dave Reynolds
 * Created on:  9 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.ResultSet;


/**
 *  a streaming writer which serializes the results of a select query to CSV format,
 * no mapping used, coalesces adjacent rows using a wrapping ValueStream.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class CSVStreamingWriter implements StreamingOutput {
    protected ValueStream valueStream;
    protected boolean includeID = true;
    
    public CSVStreamingWriter(ResultSet results) {
        valueStream = new ValueStream(results);
    }
    
    public CSVStreamingWriter(ResultSet results, boolean includeID) {
        valueStream = new ValueStream(results);
        this.includeID = includeID;
    }

    @Override
    public void write(OutputStream output) throws IOException,
            WebApplicationException {
        try {
            CSVWriter writer = new CSVWriter(output);
            writer.setIncludeID(includeID);
            writer.write(valueStream);
        } catch (EpiException e) {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
    }

}
