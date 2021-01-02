package main.java.de.voidtech.gerald.commands;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PingCommand extends AbstractCommand {

	@Override
	public void execute(Message message, List<String> args) {

		long time = System.currentTimeMillis();

		message.getChannel().sendMessage("Pong!").queue(response -> {
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

}
