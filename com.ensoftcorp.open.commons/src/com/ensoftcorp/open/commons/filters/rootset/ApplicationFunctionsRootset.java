package com.ensoftcorp.open.commons.filters.rootset;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.SetDefinitions;

public class ApplicationFunctionsRootset extends FilterableRootset {

	@Override
	public String getName() {
		return "Application Functions";
	}

	@Override
	public String getDescription() {
		return "A set of all application functions";
	}

	@Override
	public Q getRootSet() {
		return SetDefinitions.app().nodesTaggedWithAny(XCSG.Function);
	}

}
