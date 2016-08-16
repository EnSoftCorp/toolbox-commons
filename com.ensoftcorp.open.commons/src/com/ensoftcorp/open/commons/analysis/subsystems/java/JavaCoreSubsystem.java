package com.ensoftcorp.open.commons.analysis.subsystems.java;

import com.ensoftcorp.open.commons.analysis.subsystems.Subsystem;

public class JavaCoreSubsystem extends Subsystem {

	@Override
	public String getName() {
		return "Java Core Subsystem";
	}

	@Override
	public String getDescription() {
		return "Java core language libraries that do not fall in other specific subsystems";
	}

	@Override
	public String getTag() {
		return "JAVACORE_SUBSYSTEM";
	}

	@Override
	public String[] getPackages() {
		// TODO Auto-generated method stub
		return null;
	}

}
