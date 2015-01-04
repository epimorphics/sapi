/******************************************************************
 * File:        Api.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JsonUtil;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * AppBase component to manage the configuration of simple API elements.
 * Provides:
 * <ul>
 *   <li>Set of API specs which can be loaded once or dynamically monitored.</li>
 *   <li>Metadata which can be included in the json serialization (version, licence, documentation link etc).</li>
 *   <li>Base URI which can be used to convert incoming URI requests to RDF queries.</li>
 *   <li>An associated Sparql Source which will be queried.</li>
 *   <li>An optional set of shortname/URI mappings to use as a fall back when serializing an RDF instance.</li>
 * </ul>
 */
public class API extends ComponentBase {
    protected SparqlSource source;
    protected String baseURI = "http://localhost/";
    
    protected String documentation;
    protected String licence;
    protected String version = "0.1";
    protected String publisher;
    protected String contextURL;
    protected String comment;
    
    protected Map<String, String> shortnameToURI = new HashMap<String, String>();
    protected Map<String, String> uriToShortname = new HashMap<String, String>();
    
    /**
     * Return a global default API configuration called "api" if it exists.
     * Useful for simply deployments where a single configuration suffices.
     */
    public static API get() {
        return AppConfig.getApp().getComponentAs("api", API.class);
    }


    // TODO - APIConfig load and monitor
    
    // ---- Fallback shortname mapping support --------------------------------------------
    
    /**
     * The name of a json/yaml file 
     * @param mapfile
     */
    public void setShortnameMap(String mapfile) {
        String src = expandFileLocation(mapfile);
        try {
            JsonObject map = JsonUtil.readObject(src);
            for (Entry<String, JsonValue> entry : map.entrySet()) {
                if (entry.getValue().isString()) {
                    String uri = entry.getValue().getAsString().value();
                    shortnameToURI.put(entry.getKey(), uri);
                    uriToShortname.put(uri, entry.getKey());
                }
            }
        } catch (Exception e) {
            throw new EpiException("Failed to load shortname map file: " + mapfile, e);
        }
    }

    /**
     * Return the shortname for a URI
     */
    public String shortnameFor(String uri) {
        String shortname = uriToShortname.get(uri);
        if (shortname == null) {
            shortname = RDFUtil.getLocalname(uri);
        }
        return shortname;
    }

    /**
     * Return the shortname for a Resource
     */
    public String shortnameFor(Resource resource) {
        return shortnameFor( resource.getURI() );
    }
    
    
    // ---- Metadata support --------------------------------------------
    
    public void writeMetadata(JSFullWriter out) {
        condOut(out, "@context", contextURL);
        out.key("meta");
        out.startObject();
        condOut(out, "publisher", publisher);
        condOut(out, "licence",   licence);
        condOut(out, "documentation", documentation);
        condOut(out, "version", version);
        condOut(out, "comment", comment);
        
        out.finishObject();
    }

    // ---- Access to configurations ------------------------------------
    
    public APIConfig getDefaultConfig() {
        return new APIConfig(this);
    }

    // ---- Settings/getters --------------------------------------------
    
    /**
     * Set the sparql source which will be queried
     */
    public void setSource(SparqlSource source) {
        this.source = source;
    }
    
    /**
     * Set the baseURL which this instance is supposed to be serving from
     */
    public void setBaseURI(String baseURL) {
        this.baseURI = NameUtils.ensureLastSlash( baseURL );
    }


    public SparqlSource getSource() {
        return source;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String license) {
        this.licence = license;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String versionNumber) {
        this.version = versionNumber;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getContextURL() {
        return contextURL;
    }

    public void setContextURL(String contextURL) {
        this.contextURL = contextURL;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // ---- Internals -----------------------------------------------
    
    private void condOut(JSFullWriter out, String key, String value) {
        if (value != null) {
            out.pair(key, value);
        }
    }
}
