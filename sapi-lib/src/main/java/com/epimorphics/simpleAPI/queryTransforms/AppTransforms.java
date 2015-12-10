/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.simpleAPI.queryTransforms;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.core.Startup;

public class AppTransforms extends ComponentBase implements Startup {

	final List<String> transformNames = new ArrayList<String>();
	
	public AppTransforms() {		
	}	
	
	public void setInclude(String name) {
		transformNames.add(name);
		System.err.println(">> including " + transformNames + " in " + getName());
	}
	
	@Override public void startup(App app) {
		System.err.println(">> AppTramsforms.startup");
	}

}
