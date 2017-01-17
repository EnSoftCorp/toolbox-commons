package com.ensoftcorp.open.commons.filters.rootset;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.analysis.SetDefinitions;

public class ApplicationRootset extends FilterableRootset {

	@Override
	public String getName() {
		return "Application";
	}

	@Override
	public String getDescription() {
		return "Everything in the program graph excluding libraries";
	}

	@Override
	public Q getRootSet() {
		return SetDefinitions.app();
	}

}
