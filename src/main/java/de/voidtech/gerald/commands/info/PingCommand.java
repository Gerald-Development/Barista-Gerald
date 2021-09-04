package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;

@Command
public class PingCommand extends AbstractCommand {

	@Override
	public void executeInternal(CommandContext context, List<String> args) {

		long time = System.currentTimeMillis();
		
		MessageEmbed beforePingHasBeenProcessedEmbed = new EmbedBuilder()
				.setAuthor("Ping?")
				.setColor(Color.RED)
				.build();
		//TODO (from: Franziska): Queue again.
		context.getChannel().sendMessageEmbeds(beforePingHasBeenProcessedEmbed).queue(response -> {
			MessageEmbed pingEmbed = new EmbedBuilder()//
					.setAuthor("Pong!")//
					.setColor(Color.GREEN)//
					.setDescription(String.format("Latency: %sms\nGateway Latency: %sms",
							(System.currentTimeMillis() - time), context.getJDA().getGatewayPing()))//
					.build();

			response.editMessageEmbeds(pingEmbed).queue();
		});
	}

	@Override
	public String getDescription() {
		return null;
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
