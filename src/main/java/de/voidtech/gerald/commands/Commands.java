package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.commands.info.*;

public enum Commands {
	
	PING("ping", new PingCommand()),
	SAY("say", new SayCommand()),
	SERVERINFO("serverinfo", new ServerInfoCommand()),
	@SuppressWarnings("deprecation")
	JUNIT_TEST("junittest", new TestCommand()),
	
	;
	
	private String name;
	private AbstractCommand command;
	
	private Commands(String name, AbstractCommand command) {
		this.name = name;
		this.command = command;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public AbstractCommand getCommand()
	{
		return this.command;
	}
}
