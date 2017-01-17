package com.ensoftcorp.open.commons.filters.rootset;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

public class UniverseRootset extends FilterableRootset {

	@Override
	public String getName() {
		return "Universe";
	}

	@Override
	public String getDescription() {
		return "Everything in the program graph";
	}

	@Override
	public Q getRootSet() {
		return Common.universe();
	}

}
