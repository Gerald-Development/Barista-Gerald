package main.java.de.voidtech.gerald.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;

@Deprecated //COMMAND FOR JUNIT TESTS ONLY!!
public class TestCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		message.getAuthor();
	}


	@Override
	public String getDescription() {
		return "not your business";
	}

	@Override
	public String getUsage() {
		return "";
	}


	@Override
	public String getName() {
		return "test";
	}

}
