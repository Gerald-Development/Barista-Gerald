package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.commands.info.*;

import main.java.de.voidtech.gerald.commands.fun.*;

public enum CommandRegistry {

	PING("ping", PingCommand.class),
	SAY("say", SayCommand.class),
	SERVERINFO("serverinfo", ServerInfoCommand.class),
	FACT("fact", FactCommand.class),
	ASK("ask", AskCommand.class),
	WHOIS("whois", WhoisCommand.class),
	COMPILE("compile", CompileCommand.class),
	INSPIRO("inspiro", InspiroCommand.class),
	@SuppressWarnings("deprecation")
	JUNIT_TEST("junittest", TestCommand.class),

	;

	private String name;
	private Class<? extends AbstractCommand> commandClass;

	private CommandRegistry(String name, Class<? extends AbstractCommand> command) {
		this.name = name;
		this.commandClass = command;
	}

	public String getName() {
		return this.name;
	}

	public Class<? extends AbstractCommand> getCommandClass() {
		return this.commandClass;
	}
}
