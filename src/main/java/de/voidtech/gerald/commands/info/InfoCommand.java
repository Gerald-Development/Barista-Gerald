package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.HttpClientService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Command
public class InfoCommand extends AbstractCommand {

	@Autowired
	private List<AbstractCommand> commands;
	
	@Autowired
	private List<AbstractRoutine> routines;
	
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private HttpClientService httpClientService;
	
	private long getEmoteCount(JDA jda) {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("rawtypes")
			Query query = session.createQuery("select count(*) from NitroliteEmote");
			long count = ((long)query.uniqueResult()) + jda.getEmojiCache().size();
			session.close();
			return count;
		}
	}
	
	private static final String JENKINS_LATEST_BUILD_URL = "https://jenkins.voidtech.de/job/Barista%20Gerald/lastSuccessfulBuild/buildNumber";
	
	private String getLatestBuild() {
		try {
			return httpClientService.get(JENKINS_LATEST_BUILD_URL).body().string();
		} catch (IOException e) {
			e.printStackTrace();
			return "unknown";
		}
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		long guildCount = context.getJDA().getGuildCache().size();
		long memberCount = context.getJDA().getGuildCache().stream().mapToInt(Guild::getMemberCount).sum();
		long emoteCount = getEmoteCount(context.getJDA());
		
		MessageEmbed informationEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Barista Gerald - A Java Discord Bot", GlobalConstants.LINKTREE_URL)
				.addField("Gerald Owner", "```elementalmp4```", false)
				.addField("People who made this happen", "```\n"
						+ "elementalmp4\r\n"
						+ "montori\r\n"
						+ "scot_survivor\r\n"
						+ "pagwin\r\n"
						+ "foxi```", false)

				.addField("Gerald Guild Count", "```" + guildCount + "```", true)
				.addField("Gerald Member Count", "```" + memberCount + "```", true)
				.addField("Nitrolite Emote Count", "```" + emoteCount + "```", false)
				.addField("Latest Build Number", "```" + getLatestBuild() + "```", true)
				.addField("Active Threads", "```" + Thread.activeCount() + "```", true)
				.addField("Latest Release", "```"+ GlobalConstants.VERSION +"```", false)
				.setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
				.setFooter("Command Count: " + commands.size() + "\nRoutine Count: " + routines.size(), context.getJDA().getSelfUser().getAvatarUrl())
				.build();
		context.reply(informationEmbed);
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
		return new String[]{"botinfo", "botstats", "bi", "bs", "stats"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}

}