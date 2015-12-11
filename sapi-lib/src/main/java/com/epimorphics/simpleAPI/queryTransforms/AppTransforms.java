/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.simpleAPI.queryTransforms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.core.Startup;

public class AppTransforms extends ComponentBase implements Startup {

	static final Logger log = LoggerFactory.getLogger( AppTransforms.class );

	public final List<String> transformNames = new ArrayList<String>();
	
	public AppTransforms() {		
	}	
	
	public void setInclude(String name) {
		log.debug("including transform " + name);
		transformNames.add(name);
	}
	
	@Override public void startup(App app) {
	}

}
