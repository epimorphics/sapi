/******************************************************************
 * File:        Container.java
 * Created by:  Dave Reynolds
 * Created on:  13 Jan 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.containers;

import static com.epimorphics.webapi.marshalling.RDFXMLMarshaller.MIME_RDFXML;

import java.io.InputStream;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.core.PrefixService;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.jsonld.JsonLDUtil;
import com.epimorphics.simpleAPI.query.impl.SparqlDataSource;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.NameUtils;

/**
 * Support for simplified update of RDF resources.
 * 
 * <p> An instance of Container specifies the update pattern,
 * which might be applied at multiple URLs, not the individual container.</p>
 * 
 * <p>The core assumption is that each entry in a container (which may include other attached resources)
 * is stored in a separate graph whose URI matches that of the root resouce to be stored. The possible
 * links to and from the container resource are held in a (configurable) links graph.</p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Container extends ComponentBase {
    static final Logger log = LoggerFactory.getLogger( Container.class );
    
    public static final String MIME_JSONLD = "application/ld+json";
    public static final String MIME_TURTLE = "text/turtle";
    
    protected String membershipProp; 
    protected String invMembershipProp;
    protected String rootType;
    protected String linkGraph;
    protected Object jsonldContext;
    protected Container child;
    protected ModelTransform transform;
    protected boolean directParent = false;
    
    protected SparqlSource    source;
    protected Property membershipPropR; 
    protected Property invMembershipPropR;
    protected Resource rootTypeR;
    
    // -- Configuration ----
    
    public void setMembershipProp(String prop) {
        membershipProp = prop; 
    }
    
    public void setInvMembershipProp(String prop) {
        invMembershipProp = prop;
    }
    
    public void setRootType(String ty) {
        rootType = ty;
    }
    
    public void setLinkGraph(String linkGraph) {
        this.linkGraph = linkGraph;
    }
    
    public void setJsonldContext(String contextfile) {
        try {
            jsonldContext = JsonLDUtil.readContextFromFile( expandFileLocation(contextfile) );
        } catch (Exception e) {
            log.error("Failed to load jsonld context spec from " + contextfile, e);
        }
    }
    
    /**
     * Optional child container. If we add a item to this container then block
     * any children - they should be added via the child container
     */
    public void setChild(Container child) {
        this.child = child;
    }

    /**
     * If true then the target URI will be treated as the collection for adding children.
     * If false (default) then the collection is one level higher (strip off last segment)
     * Might want something more flexible here!
     */
    public void setDirectParent(boolean directParent) {
        this.directParent = directParent;
    }

    public Property getMembershipPropR() {
        return membershipPropR;
    }

    public void setTransform(ModelTransform transform) {
        this.transform = transform;
    }

    public Property getInvMembershipPropR() {
        return invMembershipPropR;
    }

    public Resource getRootTypeR() {
        return rootTypeR;
    }

    public String getLinkGraph() {
        return linkGraph;
    }
    
    public Object getJsonldContext() {
        return jsonldContext;
    }
    
    public Container getChild() {
        return child;
    }

    public ModelTransform getTransform() {
        return transform;
    }

    public boolean isDirectParent() {
        return directParent;
    }

    protected String expandURI(String uri) {
        PrefixService prefixes = getApp().getA(PrefixService.class);
        if (prefixes != null){
            return prefixes.getPrefixes().expandPrefix(uri);
        } else {
            return uri;
        }
    }
    
    public SparqlSource getSource() {
        if (source == null) {
            source = ((SparqlDataSource)getApp().getA(API.class).getSource()).getSource();
        }
        return source;
    }
    
    public DatasetAccessor getDataAccessor() {
        return getSource().getAccessor();
    }
        
    // -- actions ----
    
    /**
     * Replace the given resource by a new RDF payload
     */
    public void replace(String targetPath, HttpHeaders hh, InputStream body) {
        try {
            String baseURI = baseURI(targetPath);
            Model payload = getCleanModel(baseURI, hh, body);
            getDataAccessor().putModel(baseURI, payload);
        } catch (EpiException e) {
            throw new WebApiException(Status.BAD_REQUEST , "Could not parse replacement data: " + e);
        } catch (JenaException e) {
            throw new WebApiException(Status.INTERNAL_SERVER_ERROR, "Problem updating data source: " + e);
        }
    }
    
    /**
     * Delete the given resource
     */
    public void delete(String targetPath) {
        String baseURI = baseURI(targetPath);
        getDataAccessor().deleteModel(baseURI);
        if (linkGraph != null){
            // Also need to delete any links
            String updateStr = String.format( "DELETE WHERE { GRAPH <%s> {<%s> ?p ?v}}; DELETE WHERE { GRAPH <%s>  {?o ?q <%s>}};",
                    linkGraph, baseURI, linkGraph, baseURI );
            UpdateRequest update = UpdateFactory.create( updateStr );
            getSource().update(update);
        }
    }

    /**
     * Add a new resource to the payload, allocating a new local UUID if necessary
     * Return URI of new element
     */
    public String add(String targetPath, HttpHeaders hh, InputStream body) {
        try {
            // Allocate a default ID in case the payload has an empty relative URI
            String baseURI = baseURI(targetPath) + "/" + UUID.randomUUID();
            Model payload = getBodyModel(baseURI, hh, body);
            if (payload == null) {
                throw new WebApiException(Status.NOT_ACCEPTABLE, "Replacement data in unsupported format");
            }
            
            // Find the root resource which is being added
            ResIterator i = payload.listResourcesWithProperty(RDF.type, rootTypeR);
            if (!i.hasNext()) {
                throw new WebApiException(Status.BAD_REQUEST, "Could not find root resource with the right type");
            }
            Resource root = i.next();
            
            // Treat bNodes as a request for allocation
            if (root.isAnon()) {
                ResourceUtils.renameResource(root, baseURI);
                root = payload.getResource(baseURI);
            }
            cleanAndValidate(root.getURI(), payload);
            
            // Upload the main model its own graph
            getDataAccessor().putModel(root.getURI(), payload);
            
            // Establish the membership links if any
            Model links = ModelFactory.createDefaultModel();
            String parentURI = baseURI(targetPath);
            if ( ! isDirectParent() ) {
                parentURI = NameUtils.splitBeforeLast( parentURI, "/");
            }
            Resource parent = links.getResource( parentURI );
            if (membershipPropR != null) {
                links.add(parent, membershipPropR, root);
            }
            if (invMembershipPropR != null) {
                links.add(root, invMembershipPropR, parent);
            }
            if (!links.isEmpty()) {
                getDataAccessor().add(linkGraph, links);
            }
            
            return root.getURI();
            
        } catch (EpiException e) {
            throw new WebApiException(Status.BAD_REQUEST , "Could not parse replacement data: " + e);
        } catch (JenaException e) {
            throw new WebApiException(Status.INTERNAL_SERVER_ERROR, "Problem updating data source: " + e);
        }
    }
    
    protected Model getCleanModel(String baseURI, HttpHeaders hh, InputStream body) {
        Model payload = getBodyModel(baseURI, hh, body);
        if (payload == null) {
            throw new WebApiException(Status.NOT_ACCEPTABLE, "Replacement data in unsupported format");
        }
        if ( !cleanAndValidate(baseURI, payload) ) {
            throw new WebApiException(Status.BAD_REQUEST, "Supplied data incomplete, lacks required root type");
        }
        return payload;
    }
    
    /**
     * Convert an input payload an in memory model (streaming might be possible in the future) 
     * @return
     */
    protected Model getBodyModel(String baseURI, HttpHeaders hh, InputStream body) {
        MediaType mediaType = hh.getMediaType();
        if (mediaType == null) return null;
        String mime = mediaType.getType() + "/" + mediaType.getSubtype();   // ignore parameters

        if (mime.equals(MIME_JSONLD)) {
            return JsonLDUtil.readModel(baseURI, body, getJsonldContext());
            
        } else {
            String lang = null;
            if ( MIME_RDFXML.equals( mime ) ) {
                lang = FileUtils.langXML;
            } else if ( MIME_TURTLE.equals( mime ) ) {
                lang = FileUtils.langTurtle;
            } else {
                return null;
            }
            Model m = ModelFactory.createDefaultModel();
            m.read(body, baseURI, lang);
            return m;
        }
    }
    
    protected String baseURI(String targetPath) {
        return getApp().getA(API.class).getBaseURI() + targetPath;
    }
    
    protected boolean cleanAndValidate(String baseURI, Model model) {
        removeLinks(getMembershipPropR(), model);
        removeLinks(getInvMembershipPropR(), model);
        if (child != null) {
            removeLinks(child.getMembershipPropR(), model);
        }        
        Resource root = model.getResource(baseURI);
        if (transform != null) {
            transform.transform(root);
        }
        return rootTypeR == null || root.hasProperty(RDF.type, getRootTypeR());
    }
    
    protected void removeLinks(Property link, Model model) {
        if (link != null) {
            model.removeAll(null, link, (RDFNode)null);
        }
    }
    
    @Override
    public void startup(App app) {
        super.startup(app);
        if (membershipProp != null) {
            membershipPropR = ResourceFactory.createProperty( expandURI(membershipProp) );
        }
        if (invMembershipProp != null) {
            invMembershipPropR = ResourceFactory.createProperty( expandURI(invMembershipProp) );
        }
        if (rootType != null) { 
            rootTypeR = ResourceFactory.createProperty( expandURI(rootType) );            
        }
    }
    
}
