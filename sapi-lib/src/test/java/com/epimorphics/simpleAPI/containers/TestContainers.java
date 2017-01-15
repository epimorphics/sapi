/******************************************************************
 * File:        TestContainers.java
 * Created by:  Dave Reynolds
 * Created on:  15 Jan 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.containers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.data.impl.DatasetSparqlSource;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.query.impl.SparqlDataSource;

public class TestContainers {
    protected final String catNS = "http://vocab.epimorphics.com/def/catalog/";
    
    App app;
    API api;
    DatasetSparqlSource source;
    Container dsContainer;
    Container eltContainer;

    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/testCases/containerTests/app.conf"));
        api = app.getA(API.class);
        app.startup();
        SparqlDataSource ds = app.getA(SparqlDataSource.class);
        source = (DatasetSparqlSource) ds.getSource();
        dsContainer = app.getComponentAs("datasetContainer", Container.class);
        eltContainer = app.getComponentAs("elementContainer", Container.class);
    }

    @Test
    public void test() {
        dsContainer.add("dataset", new MockJsonldHeaders(), makePayload("ds1", "Dataset one", catNS + "Dataset"));
        checkState( "state1.trig" );
        eltContainer.add("dataset/ds1/element", new MockJsonldHeaders(), makePayload("e1", "Element one", catNS + "Element"));
        checkState( "state2.trig" );
        eltContainer.add("dataset/ds1/element", new MockJsonldHeaders(), makePayload("e2", "Element two", catNS + "Element"));
        checkState( "state3.trig" );
        eltContainer.delete("dataset/ds1/element/e2");
        checkState( "state2.trig" );
        dsContainer.replace("dataset/ds1", new MockJsonldHeaders(), makePayload("ds1", "Dataset one MODIFIED", catNS + "Dataset"));
        checkState( "state4.trig" );
        boolean exception = false;
        try {
            dsContainer.add("dataset", new MockJsonldHeaders(), makePayload("ds1", "Dataset one", catNS + "NotDataset"));
        } catch (WebApiException e) {
            exception = true;
        }
        assertTrue( exception );
//        printState();
    }
    
    protected void checkState(String expectedF) {
        Dataset current = source.getDataset();
        Dataset expected = RDFDataMgr.loadDataset("src/test/testCases/containerTests/expected/" + expectedF);
        for (Iterator<String> i = expected.listNames(); i.hasNext(); ) {
            String muri = i.next();
            Model currentModel = current.getNamedModel(muri);
            Model expectedModel = expected.getNamedModel(muri);
            assertTrue( current.containsNamedModel(muri) );
            assertTrue( expectedModel.isIsomorphicWith(currentModel) );
        }
    }
    
    protected void printState() {
        RDFDataMgr.write(System.out, source.getDataset(), Lang.TRIG);
    }
    
    protected InputStream makePayload(String uri, String title, String type) {
        String src = String.format("{ \"@id\": \"%s\", \"title\": \"%s\", \"type\": \"%s\"}", uri, title, type);
        return new ByteArrayInputStream( src.getBytes(StandardCharsets.UTF_8) );
    }

    class MockJsonldHeaders implements HttpHeaders {

        @Override
        public MediaType getMediaType() {
            return new MediaType("application", "ld+json");
        }
        
        @Override  public List<String> getRequestHeader(String name) { return null; }
        @Override  public String getHeaderString(String name) { return null; }
        @Override  public MultivaluedMap<String, String> getRequestHeaders()  { return null; }
        @Override  public List<MediaType> getAcceptableMediaTypes()  { return null; }
        @Override  public List<Locale> getAcceptableLanguages()  { return null; }
        @Override  public Locale getLanguage()  { return null; }
        @Override  public Map<String, Cookie> getCookies()  { return null; }
        @Override  public Date getDate()  { return null; }
        @Override  public int getLength()  { return -1; }
    }
}
