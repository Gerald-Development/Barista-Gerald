package main.java.de.voidtech.gerald.service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.Commands;
import net.dv8tion.jda.api.entities.Message;

public class MessageHandler {

	private volatile static MessageHandler instance;
	// TODO: Prefix will be stored in the DB and Config later
	private static final String PREFIX = "$";
	private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

	// private for Singleton
	private MessageHandler() {
	}

	public static MessageHandler getInstance() {
		if (MessageHandler.instance == null) 
		{
			MessageHandler.instance = new MessageHandler();
		}

		return MessageHandler.instance;
	}

	public void handleMessage(Message message) {
		handleCommand(message);
	}

	private void handleCommand(Message message) {
		if (message.getContentRaw().startsWith(PREFIX)) 
		{
			String messageContent = message.getContentRaw().substring(PREFIX.length());
			List<String> messageArray = Arrays.asList(messageContent.split(" "));

			List<Commands> commandEnumEntries = Arrays.asList(Commands.values())//
					.stream()//
					.filter(commandsEntry -> commandsEntry.getName().equals(messageArray.get(0)))//
					.collect(Collectors.toList());

			if (commandEnumEntries.size() <= 0)
			{
				LOGGER.log(Level.INFO, "Command not found: " + messageArray.get(0));
				return;
			}

			AbstractCommand commandOpt = commandEnumEntries.get(0).getCommand();

			if (messageArray.size() <= 0) return;
			commandOpt.execute(message, messageArray.subList(0, messageArray.size()));
		}
	}
}
