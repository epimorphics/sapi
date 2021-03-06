/******************************************************************
 * File:        ConfigConstants.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

/**
 * Collection of constants used in configuration files
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ConfigConstants {
    public static final String TYPE = "type";
    public static final String TYPE_ITEM = "item";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_VIEW = "view";
    public static final String TYPE_MODEL = "model";
    public static final String NAME      = "name";
    public static final String ITEM_NAME = "itemName";   // Supports documentation
    
    public static final String TRANSFORM = "transform";
    public static final String GEOQUERY  = "geoquery";
    
    public static final String MAPPING   = "mapping";
    public static final String VIEW      = "view";
    public static final String VIEWS     = "views";
    public static final String CSVMAP    = "csvmap";
    public static final String GEOM_PROP = "geometryProp";
    
    public static final String TEMPLATE  = "template";
    
    public static final String PREFIXES   = "prefixes";
    public static final String BASE_QUERY = "baseQuery";
    public static final String QUERY      = "query";
    public static final String URL        = "url";
    
    public static final String BIND_VARS  = "bindVars";
    public static final String LIMIT      = "limit";
    public static final String SOFT_LIMIT = "softLimit";
    public static final String HAS_FORMAT = "hasFormat";
    public static final String FLATTEN_PATH = "flattenPath";
    public static final String NESTED_SELECT = "nestedSelect";
    public static final String NESTED_SELECT_VARS = "nestedSelectVars";
    public static final String DISTINCT    = "distinct"; 
    public static final String ALIAS       = "alias"; 
    public static final String PROCESSORS  = "processors"; 
    public static final String BINDINGS    = "bindings"; 
    
    public static final String PROPERTY    = "prop";
    public static final String OPTIONAL    = "optional";
    public static final String MULTIVALUED = "multi";
    public static final String NESTED      = "nested";
    public static final String COMMENT     = "comment";
    public static final String FILTERABLE  = "filterable";
    public static final String PROP_TYPE   = "type";
    public static final String RANGE       = "range";   // Alias for type
    public static final String VALUE_BASE  = "valueBase";
    public static final String SUPPRESSID  = "suppressID";
    public static final String HIDE        = "hide";
    public static final String EXCLUDE     = "excludeValue";
    
    public static final String CLASS       = "class";
    public static final String CLASSES     = "classes";
    public static final String URI         = "uri";
    public static final String PROPERTIES  = "properties";
    
    public static final String GEO           = "geoSearch";
    public static final String GEO_PARAMETER = "parameter";
    public static final String GEO_ALGORITHM = "algorithm";
    
    public static final String TEXT_SEARCH    = "textSearch";
    
    public static final String RESULT_ITEMS = "items";
    public static final String RESULT_ITEM  = "item";
    
    public static final String ROOT_VAR    = "id";
    
    public static final String DEFAULT_MODEL    = "defaultModel";
    public static final String MVIEW_PROJECTION = "projection";
    public static final String MVIEW_MODEL      = "model";
    public static final String MVIEW_CLASS      = "class";
}
