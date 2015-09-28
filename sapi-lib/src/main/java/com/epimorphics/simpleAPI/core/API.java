/******************************************************************
 * File:        API.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.core.Startup;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.impl.ConfigItem;
import com.epimorphics.util.NameUtils;

/**
 * This is the primary configuration component for sapi. It provides:
 * <ul>
 *   <li>Set of API specs which can be loaded once or dynamically monitored.</li>
 *   <li>Metadata which can be included in the json serialization (version, licence, documentation link etc).</li>
 *   <li>Base URI which can be used to convert incoming URI requests to RDF queries.</li>
 *   <li>An associated Sparql (or other) source which will be queried.</li>
 *   <li>An optional set of defauult shortname/URI mappings to use as a fall back when serializing an RDF instance.</li>
 * </ul>
 */
public class API extends ComponentBase implements Startup {
    protected SparqlSource source;    // TODO generalize to other sources
    
    protected String baseURI = "http://localhost/";
    
    protected String documentation;
    protected String licence;
    protected String version = "0.1";
    protected String publisher;
    protected String contextURL;
    protected String comment;
    protected int  maxAge = 60;
    protected boolean showLangTag = true;
    protected SpecMonitor monitor;
    
    /**
     * Return a global default API configuration called "api" if it exists.
     * Useful for simple deployments where a single configuration suffices.
     */
    public static API get() {
        return AppConfig.getApp().getComponentAs("api", API.class);
    }

    // ---- Metadata support --------------------------------------------
    
    public void startMetadata(JSFullWriter out) {
        condOut(out, "@context", contextURL);
        out.key("meta");
        out.startObject();
        condOut(out, "publisher", publisher);
        condOut(out, "licence",   licence);
        condOut(out, "documentation", documentation);
        condOut(out, "version", version);
        condOut(out, "comment", comment);
    }    

    public void writeMetadata(JSFullWriter out) {
        startMetadata(out);
        finishMetadata(out);
    }
    
    public void finishMetadata(JSFullWriter out) {
        out.finishObject();
    }
    
    /**
     * Inject a page description into a description model
     * @param thing The resource being described whose associated Model is where the description should be injected
     */
    public Resource addRDFMetadata(Resource thing) {
        Resource meta = thing.getModel().createResource()
            .addProperty(FOAF.primaryTopic, thing);
        condAddProperty(meta, DCTerms.publisher, publisher);
        condAddProperty(meta, DCTerms.license, licence);
        if (documentation != null) {
            meta.addProperty(RDFS.seeAlso, ResourceFactory.createResource(documentation));
        }
        condAddProperty(meta, OWL.versionInfo, version);
        condAddProperty(meta, RDFS.comment, comment);
        return meta;
    }
    
    private void condAddProperty(Resource meta, Property prop, String value) {
        if (value != null) {
            meta.addProperty(prop, value);
        }
    }
    
    // ---- Access to configurations ------------------------------------

    public EndpointSpec getSpec(String name) {
        if (monitor != null){
            ConfigItem item = monitor.get(name);
            if (item instanceof EndpointSpec) {
                return (EndpointSpec) item;
            }
        }
        return null;
    }

    /**
     * Return the default specification for how to render a given property.
     * May be null if there is no matching default
     */
    public ViewEntry getDefaultFor(String uri) {
        // TODO
        return null;
    }
    
    // ---- Monitor configurations ------------------------------------
    
    /**
     * Set a directory of endpoint specifications to load. 
     */
    public void setEndpointSpecDir(String dir) {
        monitor = new SpecMonitor(this);
        monitor.setDirectory(dir);
        monitor.setUseWatcher(true);
    }   
    
    @Override
    public void startup(App app) {
        super.startup(app);
        if (monitor != null) {
            monitor.startup(app);
        }
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

    
    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = (int)maxAge;
    }
    
    public boolean isShowLangTag() {
        return showLangTag;
    }

    public void setShowLangTag(boolean showLang) {
        this.showLangTag = showLang;
    }    

    // ---- Internals -----------------------------------------------


    private void condOut(JSFullWriter out, String key, String value) {
        if (value != null) {
            out.pair(key, value);
        }
    }
}
