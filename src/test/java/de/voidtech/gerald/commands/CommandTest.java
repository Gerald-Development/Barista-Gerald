package test.java.de.voidtech.gerald.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;

import junit.framework.TestCase;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.Commands;

public class CommandTest extends TestCase {

	@Test
	public void testAllCommands() throws Exception {
		Collection<AbstractCommand> commandList = Arrays.asList(Commands.values())//
				.stream()//
				.map(commands -> commands.getCommand())//
				.collect(Collectors.toList());
		
		commandList.forEach(command -> {
			assertNotNull(command.getDescription(), "The description of a command may not be null");
			assertNotNull(command.getUsage(), "The usage of a command may not be null");
		});
	}

}
