package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.commands.info.*;

import main.java.de.voidtech.gerald.commands.fun.*;

import java.lang.reflect.InvocationTargetException;

public enum CommandRegistry {

	PING("ping", PingCommand.class, null),
	SAY("say", SayCommand.class, null),
	SERVERINFO("serverinfo", ServerInfoCommand.class, null),
	FACT("fact", FactCommand.class, null),
	ASK("ask", AskCommand.class, null),
	WHOIS("whois", WhoisCommand.class, null),
	COMPILE("compile", CompileCommand.class, null),
	INSPIRO("inspiro", InspiroCommand.class, null),
	APOD("apod", ApodCommand.class, null),
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
