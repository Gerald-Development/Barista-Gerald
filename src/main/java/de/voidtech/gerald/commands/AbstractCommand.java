package main.java.de.voidtech.gerald.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand 
{
	public abstract void execute(Message message, List<String> args);
}
