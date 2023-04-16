package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.persistence.repository.RiggedLengthRepository;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.RiggedLength;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class PPCommand extends AbstractCommand{

	@Autowired
	private RiggedLengthRepository repository;
	
	@Autowired
	private GeraldConfig config;
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		boolean shouldContinue = true;
		if (!args.isEmpty()) shouldContinue = tryRigUser(context);
		if (!shouldContinue) return;
		User user;
		if (context.getMentionedMembers().isEmpty()) user = context.getAuthor();
		else user = context.getMentionedMembers().get(0).getUser();
		int ppSizeNumber = determineLength(user.getId());
		String phrase = getPhrase(ppSizeNumber);
		Color color = getColor(ppSizeNumber);
		String ppSize = String.valueOf(ppSizeNumber);		
		
		//It's best if nobody questions this
		if (user.getId().equals("341300268660555778")) {
			ppSize = "YEEEEEEEEEEEEEEEEEEEEEEESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS";
			phrase = "G a r g a n t u a n.";
			color = Color.magenta;
		}
		//You saw nothing...
		
		MessageEmbed ppSizeEmbed = new EmbedBuilder()//
				.setTitle("How big is " + user.getName() + "'s PP?")
				.setColor(color)
				.setDescription("Your PP is **" + ppSize + (ppSizeNumber == 1 ? " inch.** " : " inches.** ") + phrase)
				.build();
		
		context.reply(ppSizeEmbed);
	}
	
	private boolean tryRigUser(CommandContext context) {
		if (!config.getMasters().contains(context.getAuthor().getId())) return true;
		if (context.getArgs().get(0).equals("rig")) {
			String userID = ParsingUtils.filterSnowflake(context.getArgs().get(1));
			int length = Integer.parseInt(context.getArgs().get(2));
			deleteRigged(userID);
			saveRigged(new RiggedLength(userID, length));
			context.reply("**Member** <@" + userID + "> **has been rigged at** `" + length + "` **inches**");
			return false;
		} else if (context.getArgs().get(0).equals("unrig")) {
			String userID = ParsingUtils.filterSnowflake(context.getArgs().get(1));
			deleteRigged(userID);
			context.reply("**Member** <@" + userID + "> **has been unrigged**");
			return false;
		}
		return true;
	}

	private Color getColor(int ppSize)
	{
		return ppSize > 6 
				? Color.GREEN 
				: ppSize > 4 
				? Color.ORANGE 
				: Color.RED;
	}
	
	private String getPhrase(int ppSize)
	{
		return ppSize > 6
				? "Thats pretty spankin' huge" 
				: ppSize > 4
				? "Meh could be bigger" 
				: "Does it even exist?";
	}
	
	private void deleteRigged(String userID) {
		repository.deleteByMemberID(userID);
	}
	
	private void saveRigged(RiggedLength rigged) {
		repository.save(rigged);
	}
	
	private int determineLength(String userID) {
		RiggedLength length = getRigged(userID);
		if (length == null) {
			long seed = Long.parseLong(userID);
			return new Random(seed).nextInt(12);	
		} else return length.getLength();
	}
	
	private RiggedLength getRigged(String userID) {
		return repository.getRiggedLengthByMemberID(userID);
	}
	
	@Override
	public String getDescription() {
		return "See how giant (or tiny) your pp is";
	}

	@Override
	public String getUsage() {
		return "pp";
	}

	@Override
	public String getName() {
		return "pp";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		return new String[]{"ppsize"};
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