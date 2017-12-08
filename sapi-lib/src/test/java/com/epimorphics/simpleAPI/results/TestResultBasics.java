/******************************************************************
 * File:        TestResultBasics.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.ItemQuery;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.results.wappers.WJSONArray;
import com.epimorphics.simpleAPI.results.wappers.WJSONLangString;
import com.epimorphics.simpleAPI.results.wappers.WJSONObject;
import com.epimorphics.simpleAPI.results.wappers.WResult;
import com.epimorphics.simpleAPI.sapi2.Sapi2ListEndpointSpec;
import com.epimorphics.simpleAPI.util.JsonComparator;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.writers.CSVWriter;
import com.epimorphics.util.Asserts;
import com.epimorphics.webapi.test.MockUriInfo;

public class TestResultBasics {
    static final String EXPECTED = "src/test/testCases/baseResultTest/expected/";
    
    App app;
    API api;
    DataSource source;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/testCases/baseResultTest/app.conf"));
        api = app.getA(API.class);
//        app.startup();
        source = app.getA(DataSource.class);
    }

    @Test
    public void testSimpleResultStream() {
        Sapi2ListEndpointSpec spec = (Sapi2ListEndpointSpec) api.getSpec("listTest1");
        assertNotNull(spec);
        assertNotNull(spec.getQueryBuilder());
        assertNotNull(spec.getView());
        
        // Basic case, no nesting
        ListQueryBuilder builder = (ListQueryBuilder) spec.getQueryBuilder();
        ListQuery query = builder.sort("notation", false).build();
        ResultStream stream = source.query(query, new Call(spec, null));
        for (int i = 1; i <= 2; i++){
            assertTrue( stream.hasNext() );
            checkEntryRoot( (TreeResult)stream.next(), i);
        }
        assertFalse( stream.hasNext() );
        
        // Case with nesting
        spec = (Sapi2ListEndpointSpec) api.getSpec("listTest2");
        assertNotNull(spec);
        builder = (ListQueryBuilder) spec.getQueryBuilder();
        query = builder.sort("notation", false).build();
        stream = source.query(query, new Call(spec, null));
        for (int i = 1; i <= 2; i++){
            assertTrue( stream.hasNext() );
            TreeResult r = (TreeResult) stream.next();
            checkEntryRoot( r, i);
            String children = r.getSortedValues("narrower").stream().map(v -> v instanceof TreeResult ? ((TreeResult)v).getId().toString() : "Not nested").collect(Collectors.joining(","));
            assertEquals( "http://localhost/example/B%,http://localhost/example/C%".replace("%", Integer.toString(i)), children );
        }
        assertFalse( stream.hasNext() );
        
        // Nested case, check JSON render
        stream = source.query(query, new Call(spec, null));
        assertTrue( JsonComparator.equal(EXPECTED + "r1.json", stream.next().asJson()) );
        assertTrue( JsonComparator.equal(EXPECTED + "r2.json", stream.next().asJson()) );
        assertFalse( stream.hasNext() );
        stream = (ResultStream) api.getCall("listTest4", new MockUriInfo("test?_sort=@id"), null).getResults();
        assertTrue( JsonComparator.equal(EXPECTED + "r4.json", stream.next().asJson()) );
        
        // RDF rendering
        stream = (ResultStream) api.getCall("listTest1", new MockUriInfo("test?_sort=@id"), null).getResults();
        Resource resource = stream.next().asResource();
        assertEquals( "http://localhost/example/A1", resource.getURI() );
        assertTrue( resource.getModel().isIsomorphicWith( RDFDataMgr.loadModel(EXPECTED + "r1.ttl") ) );

        stream = (ResultStream) api.getCall("listTest2", new MockUriInfo("test?_sort=@id"), null).getResults();
        resource = stream.next().asResource();
        assertEquals( "http://localhost/example/A1", resource.getURI() );
        assertTrue( resource.getModel().isIsomorphicWith( RDFDataMgr.loadModel(EXPECTED + "r2.ttl") ) );
        
        // RDF rendering with nesting
        stream = (ResultStream) api.getCall("listTest5", new MockUriInfo("test?_sort=@id"), null).getResults();
        resource = stream.next().asResource();
        assertEquals( "http://localhost/example/F", resource.getURI() );
        assertTrue( resource.getModel().isIsomorphicWith( RDFDataMgr.loadModel(EXPECTED + "f.ttl") ) );
    }
    
    @Test
    public void testCSVRender() throws IOException {
        assertTrue( checkCSV( api.getCall("listTest3", new MockUriInfo("test?_sort=@id"), null).getResults(), "list3.csv", "list3-alt.csv") );
        api.setFullPathsInCSVHeaders(true);
        assertTrue( checkCSV( api.getCall("listTest3", new MockUriInfo("test?_sort=@id"), null).getResults(), "list3-dot.csv", "list3-alt-dot.csv") );
        assertTrue( checkCSV( api.getCall("listTest3ord", new MockUriInfo("test?_sort=@id"), null).getResults(), "list3ord.csv", "list3ord-alt.csv") );
        api.setFullPathsInCSVHeaders(false);
        
        assertTrue( checkCSV( api.getCall("listTestReg", new MockUriInfo("test?_sort=@id"), null).getResults(), "reglist.csv", "reglist-alt.csv") );
        
        assertTrue( checkCSV( api.getCall("listTest6", new MockUriInfo("test?_sort=@id"), null).getResults(), "csvMulti.csv", "csvMulti-alt.csv") );
    }
    
    @Test
    public void testWJSONwrapping() {
        WJSONObject actual = getFirstWrapped("listTest3");
        WJSONObject expected = makeWJSON("http://localhost/example/A", 
                "label", "A",
                "notation", "1",
                "child", makeWJSON("http://localhost/example/AC", "cnotation", "1C"));
        assertEquals(expected, actual);

        api.setShowLangTag(true);
        actual = getFirstWrapped("listTest4");
        expected = makeWJSON("http://localhost/example/test1", 
                "label", makeWJSONArray("a plain string", "another label"),
                "lang", new WJSONLangString("english", "en"),
                "int", 1,
                "decimal", new BigDecimal("1.5"),
                "boolean", true,
                "child", makeWJSON(null, "clabel", "child"),
                "offspring", makeWJSONArray( makeWJSON("http://localhost/example/o1", "olabel", "o1"), makeWJSON("http://localhost/example/o2", "olabel", "o2") )
                );
        assertEquals(expected, actual);
        api.setShowLangTag(false);
        
        Object labels = expected.getFromPath("offspring.olabel");
        assertTrue( labels instanceof Set<?> );
        @SuppressWarnings("unchecked")
        Set<Object> labelVals = (Set<Object>)labels;
        assertTrue( labelVals.contains( "o1") );
        assertTrue( labelVals.contains( "o2") );
        
        assertEquals("o1, o2", expected.getStringFromPath("offspring.olabel"));
    }
    
    @Test
    public void testNonIDEndpoint() {
        ResultStream stream = (ResultStream) api.getCall("countTest", new MockUriInfo("test"), null).getResults();
        TreeResult result = (TreeResult) stream.next();
        Object count = result.getValues("count").iterator().next();
        assertEquals(ResourceFactory.createTypedLiteral("6", XSDDatatype.XSDinteger), count);
    }
    
    protected WJSONObject getFirstWrapped(String spec) {
        ResultStream stream = (ResultStream) api.getCall(spec, new MockUriInfo("test?_sort=@id"), null).getResults();
        WJSONObject actual = new WResult( stream.next() ).asJson();
        return actual;
    }
    
    protected static WJSONObject makeWJSON(String id, Object...args) {
        WJSONObject obj = new WJSONObject();
        obj.put("@id", id);
        for (int i = 0; i < args.length;) {
            String key = args[i++].toString();
            Object value = args[i++];
            obj.put(key, value);
        }
        return obj;
    }
    
    protected static WJSONArray makeWJSONArray(Object...args) {
        WJSONArray array = new WJSONArray();
        for (Object arg : args) {
            array.add(arg);
        }
        return array;
    }
    
    protected String asCSV( ResultOrStream stream ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(bos);
        writer.write( (ResultStream) stream);
        return bos.toString();
    }
    
    protected boolean checkCSV(ResultOrStream stream, String...expectedFiles) throws IOException {
        String actual = asCSV(stream);
        for (String expectedFile : expectedFiles) {
            String expected = FileManager.get().readWholeFileAsUTF8(EXPECTED + expectedFile).replace("\n", "\r\n");
            if (actual.equals(expected)) {
                return true;
            }
        }
        System.out.println("Actual\n" + actual);
        return false;
    }
    
    @Test
    public void testDescribe() {
        EndpointSpec spec = api.getSpec("itemTest1");
        assertNotNull(spec);
        
        String URI = "http://localhost/example/A2";
        Request request = new Request(URI);
        ItemQuery query = (ItemQuery) spec.getQueryBuilder( request ).build();
        Asserts.assertContains(query.toString(), "eg:A2" );
        
        Result result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/itemTest1.json", result.asJson()) );
        
        spec = api.getSpec("itemTest2");
        assertNotNull(spec);
        query = (ItemQuery) spec.getQueryBuilder( request ).build();
        result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/itemTest2.json", result.asJson()) );
        
        spec = api.getSpec("itemTest3");
        assertNotNull(spec);
        query = (ItemQuery) spec.getQueryBuilder( request ).build();
        result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/itemTest2.json", result.asJson()) );
    }
    
    @Test
    public void testNestedDescribe() {
        Request request = new Request( "http://localhost/example/reg1" );
        EndpointSpec spec = api.getSpec("nestedDescribe");
        assertNotNull(spec);
        ItemQuery query = (ItemQuery) spec.getQueryBuilder( request ).build();
        Result result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/nestedDescribe.json", result.asJson()) );
    }
    
    // Check problems with json name mapping and use of "-" in local names
    @Test
    public void testNameGuards() {
        Call call = api.getCall("listVCardTest", new MockUriInfo("test"), null);
        ViewMap view = call.getView();
        PropertySpec entry = view.findEntry("address");
        assertNotNull(entry);
        assertEquals("address", entry.getJsonName());
        assertEquals( "http://www.w3.org/2006/vcard/ns#extended-address", entry.getProperty().getURI() );
        
        JsonObject jo = ((ResultStream)call.getResults()).next().asJson();
        jo = jo.get("site").getAsObject();
        jo = jo.get("siteAddress").getAsObject();
        assertEquals("3, Dovetons Drive, Williton, Taunton, Somerset, TA4 4ST", jo.get("address").getAsString().value());
        assertEquals("Some body", jo.get("organization_name").getAsString().value());
    }
    
    private void checkEntryRoot(TreeResult result, int index) {
        assertEquals( "http://localhost/example/A" + index, result.getId().asResource().getURI() );
        assertEquals( "" + index + 1, asLex( result.getValues("notation").iterator().next() ) );
        String labels = result.getSortedValues("label").stream().map(TestResultBasics::asLex).collect(Collectors.joining(","));
        assertEquals("A%,a%".replace("%", Integer.toString(index)), labels);
    }
    
    private static String asLex(Object value) {
        assertTrue(value instanceof Literal);
        return ((Literal) value).getLexicalForm();
    }
}
