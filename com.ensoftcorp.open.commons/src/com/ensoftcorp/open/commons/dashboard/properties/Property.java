package com.ensoftcorp.open.commons.dashboard.properties;

import com.ensoftcorp.open.commons.dashboard.WorkItem;

public abstract class Property extends WorkItem {

	@Override
	public String getType() {
		return "Property";
	}

}