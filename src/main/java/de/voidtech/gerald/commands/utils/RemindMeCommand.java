package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.RemindMeService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class RemindMeCommand extends AbstractCommand {
	
	@Autowired
	private RemindMeService remindMeService;

	private static HashMap<String, Integer> TimeMultipliers = new HashMap<String, Integer>();
	
	@EventListener(ApplicationReadyEvent.class)
	private void populateTimeMultipliers() {
		TimeMultipliers.put("w", Integer.valueOf(604800));
		TimeMultipliers.put("d", Integer.valueOf(86400));
		TimeMultipliers.put("h", Integer.valueOf(3600));
		TimeMultipliers.put("m", Integer.valueOf(60));
		TimeMultipliers.put("s", Integer.valueOf(1));
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		if (args.isEmpty()) showUserReminders(context);
		else {
			if (args.get(0).equals("delete")) deleteReminder(context, args);
			else createReminder(context, args);
		}
	}

	private void createReminder(CommandContext context, List<String> args) {
		if (args.size() == 1) {
			context.reply("**You need to specify a reminder message!**");
			return;
		}
		
		String timeString = args.get(0);
		String message = String.join(" ", args.subList(1, args.size()));
		String timeMultiplier = timeString.substring(timeString.length() - 1, timeString.length());
		
		if (!TimeMultipliers.containsKey(timeMultiplier)) {
			context.reply("**You need to enter a valid time multiplier!**");
			return;
		}
		
		String timeOnly = timeString.replaceAll(timeMultiplier, "");
		if (!ParsingUtils.isInteger(timeOnly)) {
			context.reply("**You need to enter a valid time quantity! (Must be a number)**");
			return;
		}
		
		long timeValue = Integer.parseInt(timeOnly);
		if (timeValue < 1) {
			context.reply("**You need to specify a higher time value! (Must be at least 1)**");
			return;
		}
		
		long multiplicationFactor = TimeMultipliers.get(timeMultiplier).longValue();
		long time = (timeValue * multiplicationFactor) + Instant.now().getEpochSecond();
		
		remindMeService.addReminder(context, message, time);
		context.reply("**Reminder added! I'll remind you on** <t:" + time + ":F>");
	}

	private void deleteReminder(CommandContext context, List<String> args) {
		if (args.size() == 1) {		
			context.reply("**You need to specify a reminder ID to delete! Use the** `reminders` **command to see your reminders!**");
			return;
		}
		String ID = args.get(1);
		if (!ParsingUtils.isInteger(ID)) {
			context.reply("**Reminder ID must be a number!**");
			return;
		}
		boolean taskDeleted = remindMeService.deleteReminder(context.getAuthor().getId(), Long.parseLong(ID));
		context.reply(taskDeleted ? "**Reminder deleted!**" : "**No task with ID** `" + ID + "` **was found!**");
	}

	private void showUserReminders(CommandContext context) {
		MessageEmbed remindersEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle(context.getAuthor().getName() + "'s Reminders")
				.setDescription(remindMeService.getRemindersList(context))
				.build();
		context.reply(remindersEmbed);
	}

	@Override
	public String getDescription() {
		return "Need a reminder to do something in a little while? Or maybe you want to remind yourself of an event in a few weeks?"
				+ " This command is for you! Simply enter the time delay and a reminder message and you're set!\n\n"
				+ "Use w, d, h and m (months, weeks, days, hours, minutes) to set your time delay. Examples: 12d, 13m, 4w\n"
				+ "Please note that there should be no spaces between the quantity and time multiplier.";
	}

	@Override
	public String getUsage() {
		return "remindme (to see your reminders)\n"
				+ "remindme [time delay] [reminder message]\n"
				+ "remindme delete [reminder ID]";
	}

	@Override
	public String getName() {
		return "remindme";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
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
		return new String[] {"reminder", "reminders", "remind"};
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
