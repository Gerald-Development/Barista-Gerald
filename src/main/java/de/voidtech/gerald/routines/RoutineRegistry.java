package main.java.de.voidtech.gerald.routines;

import main.java.de.voidtech.gerald.routines.utils.*;


public enum RoutineRegistry {
	NITROLITE("nitrolite", NitroliteRoutine.class),

	// TODO: add junit test
	;

	private String name;
	private Class<? extends AbstractRoutine> routineClass;

	private RoutineRegistry(String name, Class<? extends AbstractRoutine> routine) {
		this.name = name;
		this.routineClass = routine;
	}

	public String getName() {
		return this.name;
	}

	public AbstractRoutine getRoutine() throws IllegalAccessException, InstantiationException {
		return this.routineClass.newInstance();
	}
}
