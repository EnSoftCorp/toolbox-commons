package com.ensoftcorp.open.commons.analysis.subsystems.java;

import com.ensoftcorp.open.commons.analysis.subsystems.Subsystem;

public class IOSubsystem extends Subsystem {

	@Override
	public String getName() {
		return "Input/Output Subystem";
	}

	@Override
	public String getDescription() {
		return "General input/output, serialization libraries";
	}

	@Override
	public String getTag() {
		return "IO_SUBSYSTEM";
	}

	@Override
	public String[] getPackages() {
		// TODO Auto-generated method stub
		return null;
	}

}
