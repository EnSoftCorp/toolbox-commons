package com.ensoftcorp.open.commons.analysis.subsystems.java;

import com.ensoftcorp.open.commons.analysis.subsystems.Subsystem;

public class IntrospectionSubsystem extends Subsystem {

	@Override
	public String getName() {
		return "Introspection Subsystem";
	}

	@Override
	public String getDescription() {
		return "Reflection and runtime libraries";
	}

	@Override
	public String getTag() {
		return "INTROSPECTION_SUBSYSTEM";
	}

	@Override
	public String[] getPackages() {
		// TODO Auto-generated method stub
		return null;
	}

}

