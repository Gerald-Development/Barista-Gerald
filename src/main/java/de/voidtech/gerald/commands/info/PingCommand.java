package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class PingCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {

		long time = System.currentTimeMillis();
		
		MessageEmbed beforePingHasBeenProcessedEmbed = new EmbedBuilder()
				.setAuthor("Ping?")
				.setColor(Color.RED)
				.build();
		message.getChannel().sendMessage(beforePingHasBeenProcessedEmbed).queue(response -> {
			MessageEmbed pingEmbed = new EmbedBuilder()//
					.setAuthor("Pong!")//
					.setColor(Color.GREEN)//
					.setDescription(String.format("Latency: %sms\nGateway Latency: %sms",
							(System.currentTimeMillis() - time), message.getJDA().getGatewayPing()))//
					.build();

			response.editMessage(pingEmbed).queue();
		});
	}

	@Override
	public String getDescription() {
		return "returns the ping of Gerald and the gateway";
	}

	@Override
	public String getUsage() {
		return "ping";
	}

	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFO;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"pong"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
