package com.ensoftcorp.open.commons.analysis.subsystems.java;

import com.ensoftcorp.open.commons.analysis.subsystems.Subsystem;

public class TestingSubsystem extends Subsystem {

	@Override
	public String getName() {
		return "Testing Subsystem";
	}

	@Override
	public String getDescription() {
		return "Java mock interface and test libraries";
	}

	@Override
	public String getTag() {
		return "TESTING_SUBSYSTEM";
	}

	@Override
	public String[] getPackages() {
		// TODO Auto-generated method stub
		return null;
	}

}
