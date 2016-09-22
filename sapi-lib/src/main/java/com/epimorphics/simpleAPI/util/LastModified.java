/******************************************************************
 * File:        LastModified.java
 * Created by:  Dave Reynolds
 * Created on:  22 Sep 2016
 * 
 * (c) Copyright 2016, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.DCTerms;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.simpleAPI.query.impl.SparqlDataSource;

/**
 * Utility to read and cache the time the
 * source data was last modified.
 * Assumes that the last modified time is available as the dct:modified property of source base resource.
 * Configure a default resource for getting the time stamp or can provide endpoint specific timestamp sources.
 */
public class LastModified {
    protected long retentionTime = 10 * 1000;
    protected String defaultTimestampResource;
    protected Map<String, LastModifiedRecord> timestamps = new HashMap<>();

    public void setDefaultTimestampResource(String timestampResource) {
        defaultTimestampResource = timestampResource;
    }
    
    public void setRetentionTime(long retentionTime) {
        this.retentionTime = retentionTime;
    }
    
    private synchronized LastModifiedRecord getRecord(String timestampResource) {
        LastModifiedRecord record = timestamps.get(timestampResource);
        if (record == null) {
            record = new LastModifiedRecord(timestampResource);
            timestamps.put(timestampResource, record);
        }
        return record;
    }
    
    /**
     * Return the most recent value of the timestamp of this resource, may be null
     * if no timestamp is available
     */
    public synchronized Long getTimestamp(String timestampResource, SparqlDataSource source) {
        return getRecord(timestampResource).getTimestamp(source);
    }
    
    /**
     * Return the timestamp of the default resource
     */
    public synchronized Long getTimestamp(SparqlDataSource source) {
        return getTimestamp(defaultTimestampResource, source);
    }
    
    /**
     * For testing
     */
    public synchronized Long lastFetched() {
        LastModifiedRecord record = getRecord(defaultTimestampResource);
        if (record != null) {
            return record.lastChecked;
        } else {
            return null;
        }
    }
    
    class LastModifiedRecord {
        protected String timestampResource;
        protected Long   timestamp;
        protected long   lastChecked;
        
        public LastModifiedRecord(String timestampResource) {
            this.timestampResource = timestampResource;
        }
        
        public Long getTimestamp(SparqlDataSource source) {
            long now = System.currentTimeMillis();
            if (timestamp == null || now - lastChecked > retentionTime) {
                System.out.println("Fetching " + timestampResource);
                ResultSet result = source.getSource().select( String.format("SELECT ?modified WHERE {<%s> <%s> ?modified}", timestampResource, DCTerms.modified.getURI()) );
                if (result.hasNext()) {
                    RDFNode modified = result.next().get("modified");
                    if (modified != null) {
                        timestamp = RDFUtil.asTimestamp(modified);
                        lastChecked = now;
                    }
                }
            }
            return timestamp;
        }
    }
    
}
