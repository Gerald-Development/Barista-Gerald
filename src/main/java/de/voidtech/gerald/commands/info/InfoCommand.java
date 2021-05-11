package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class InfoCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;
	@Autowired
	private List<AbstractRoutine> routines;
	
	@Autowired
	private GeraldConfig geraldConfig;
	
	private static final String JENKINS_LATEST_BUILD_URL = "https://jenkins.voidtech.de/job/Barista%20Gerald/lastSuccessfulBuild/buildNumber";
	
	private String getLatestBuild() {
		Document doc = null;
		try {
			doc = Jsoup.connect(JENKINS_LATEST_BUILD_URL).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc.select("body").text();
	}
	
	private String getUptime() {
		Date date = new Date(ManagementFactory.getRuntimeMXBean().getUptime());
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return formatter.format(date);
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		long guildCount = message.getJDA().getGuildCache().size();
		long memberCount = message.getJDA().getGuildCache().stream().mapToInt(Guild::getMemberCount).sum();
		
		EmbedBuilder informationEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Barista Gerald - A Java Discord Bot", GlobalConstants.LINKTREE_URL)
				.addField("Gerald Owner", "```ElementalMP4#7458```", false)
				.addField("Barista Gerald Developers", "```\n"
						+ "ElementalMP4#7458\r\n"
						+ "Montori#4707\r\n"
						+ "0xffset#2267\r\n"
						+ "Scot_Survivor#8625```", false)
				.addField("Gerald Guild Count", "```" + String.valueOf(guildCount) + "```", true)
				.addField("Gerald Member Count", "```" + String.valueOf(memberCount) + "```", false)
				.addField("Latest Build Number", "```" + getLatestBuild() + "```", true)
				.addField("Latest Release", "```"+ GlobalConstants.VERSION +"```", true)
				.setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
				.setFooter("Command Count: " + commands.size() + "\nRoutine Count: " + routines.size(), message.getJDA().getSelfUser().getAvatarUrl());
		
		if (geraldConfig.getMasters().contains(message.getAuthor().getId())) {			
			informationEmbed.addField("Application Uptime", "```" + getUptime() + "```", false);
			informationEmbed.addField("Active Threads", "```" + Thread.activeCount() + "```", true);
		}
		
		MessageEmbed informationEmbedBuilt = informationEmbed.build();
		message.getChannel().sendMessage(informationEmbedBuilt).queue();
	}

	@Override
	public String getDescription() {
		return "Provides information about the Barista Gerald project and the developers who made it!";
	}

	@Override
	public String getUsage() {
		return "info";
	}

	@Override
	public String getName() {
		return "info";
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
		String[] aliases = {};
		return aliases;
	}

}
