package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.commands.info.*;

import main.java.de.voidtech.gerald.commands.fun.*;

import java.lang.reflect.InvocationTargetException;

public enum CommandRegistry {

	PING("ping", PingCommand.class),
	SAY("say", SayCommand.class),
	SERVERINFO("serverinfo", ServerInfoCommand.class),
	FACT("fact", FactCommand.class),
	ASK("ask", AskCommand.class),
	WHOIS("whois", WhoisCommand.class),
	COMPILE("compile", CompileCommand.class),
	INSPIRO("inspiro", InspiroCommand.class),
	APOD("apod", ApodCommand.class),
	CUDDLE("cuddle", ActionsCommand.class, "cuddle"),
	HUG("hug", ActionsCommand.class, "hug"),
	KISS("kiss", ActionsCommand.class, "kiss"),
	PAT("pat", ActionsCommand.class, "pat"),
	POKE("poke", ActionsCommand.class, "poke"),
	SLAP("slap", ActionsCommand.class, "slap"),

	@SuppressWarnings("deprecation")
	JUNIT_TEST("junittest", TestCommand.class, null),

	;

	private String name;
	private Class<? extends AbstractCommand> commandClass;
	private String arg;

	private CommandRegistry(String name, Class<? extends AbstractCommand> command, String arg) {
		this.name = name;
		this.commandClass = command;
		this.arg = arg;
	}
	
	private CommandRegistry(String name, Class<? extends AbstractCommand> command) {
		this(name, command, null);
	}



	public String getName() {
		return this.name;
	}

	public AbstractCommand getCommand() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (this.arg == null)
			return this.commandClass.newInstance();
		else
			return this.commandClass.getDeclaredConstructor(String.class).newInstance(arg);
	}
}
