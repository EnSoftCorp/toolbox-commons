package com.ensoftcorp.open.commons.analysis.subsystems.java;

import com.ensoftcorp.open.commons.analysis.subsystems.Subsystem;

public class NetworkSubsystem extends Subsystem {

	@Override
	public String getName() {
		return "Network Subsystem";
	}

	@Override
	public String getDescription() {
		return "Network IO libraries";
	}

	@Override
	public String getTag() {
		return "NETWORK_SUBSYSTEM";
	}

	@Override
	public String[] getPackages() {
		// TODO Auto-generated method stub
		return null;
	}

}