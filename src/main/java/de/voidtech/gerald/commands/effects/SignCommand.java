package main.java.de.voidtech.gerald.commands.effects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class SignCommand extends AbstractCommand {
	
	private static final String BUNNY = "(\\__/) ││\n(•ㅅ•) ││\n/ 　 づ";

	@Override
	public void executeInternal(Message message, List<String> args) {
		String rawText = StringUtils.join(args, " ");
		List<String> signMessage = groupWords(args);
		
		int signLength = signMessage.stream().mapToInt(line -> line.length()).max().getAsInt();
		if(rawText.length() > 60) message.getChannel().sendMessage("Your message is too long for a protest sign, keep it short.").queue();
		else if(signLength > 14) message.getChannel().sendMessage("One of the words is too long for a protest sign, keep it short.").queue();
		else {
			String signBunny = generateSign(signMessage, signLength);
			signBunny += BUNNY;
			
			message.getChannel().sendMessage("```" + signBunny +"```").queue();
		}
	}
	
	private String generateSign(List<String> signMessage, int maxLength)
	{
		String padding = StringUtils.repeat(" ", (int) Math.floor((14-maxLength)/2));	
		String result = String.format("%s┌%s┐\n", padding, StringUtils.repeat("─", maxLength));
		
		for(String message : signMessage) {
			 result += String.format("%s│%s%s│\n", padding, message, StringUtils.repeat(" ", maxLength-message.length()));
		}
		
		result += String.format("%s└%s┘\n", padding, StringUtils.repeat("─", maxLength));
		
		return result;
	}
	
	private List<String> groupWords(final List<String> args)
	{
		List<String> words = new ArrayList<String>(args);
		List<String> result = new ArrayList<>();
		
		while(words.size() > 0)
		{
			String line = words.get(0);
			words.remove(0);
			while(words.size() > 1 && (line + words.get(0)).length() < 10)
			{
				line += words.get(0);
				words.remove(0);
			}
			
			result.add(line);
		}
		
		return result;
	}
	

	@Override
	public String getDescription() {
		return "will send a very vocal bunny with a protest sign into the chat";
	}

	@Override
	public String getUsage() {
		return "sign [message]";
	}

	@Override
	public String getName() {
		return "sign";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.EFFECTS;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"bunnysign", "protest"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}
}
