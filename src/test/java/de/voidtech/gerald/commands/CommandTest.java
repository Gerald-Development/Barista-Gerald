package test.java.de.voidtech.gerald.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.junit.Test;

import junit.framework.TestCase;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandRegistry;

public class CommandTest extends TestCase {

	@Test
	public void testAllCommands() {
		Collection<AbstractCommand> commandList = Arrays.asList(CommandRegistry.values())//
				.stream()//
				.map(commands -> {
					try {
						return commands.getCommand();
					} catch (Exception e) {
						// TODO refine this error handling Montori
						System.out.println("An Error has occurred while instantiating a Command: " + e.getMessage());
					}
					return null;
				})//
				.collect(Collectors.toList());
		
		commandList.forEach(command -> {
			assertNotNull(command.getDescription(), "The description of a command may not be null");
			assertNotNull(command.getUsage(), "The usage of a command may not be null");
		});
	}

}
