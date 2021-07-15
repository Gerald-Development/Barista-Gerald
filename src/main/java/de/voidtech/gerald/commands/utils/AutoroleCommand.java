package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.AutoroleConfig;
import main.java.de.voidtech.gerald.service.AutoroleService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.MRESameUserPredicate;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Command
public class AutoroleCommand extends AbstractCommand {
	
	private static final String TRUE_EMOTE = "\u2705";
	private static final String FALSE_EMOTE = "\u274C";
	
	@Autowired
	private AutoroleService autoroleService;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private EventWaiter waiter;
	
	private void getAwaitedReply(Message message, String question, Consumer<String> result) {
        message.getChannel().sendMessage(question).queue();
        waiter.waitForEvent(MessageReceivedEvent.class,
                new MRESameUserPredicate(message.getAuthor()),
                event -> {
                    result.accept(event.getMessage().getContentRaw());
                }, 30, TimeUnit.SECONDS, 
                () -> message.getChannel().sendMessage(String.format("Request timed out.")).queue());
    }
	
	private void showAutoroles(Message message) {
		List<AutoroleConfig> configs = autoroleService.getAutoroleConfigs(serverService.getServer(message.getGuild().getId()).getId());
		if (configs.isEmpty()) message.getChannel().sendMessage("**No autoroles to show!**").queue();
		else {
			message.getChannel().sendMessageEmbeds(craftAutoroleEmbed(configs, message)).queue();			
		}
	}

	private MessageEmbed craftAutoroleEmbed(List<AutoroleConfig> configs, Message message) {
		EmbedBuilder autoroleEmbedBuilder = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Autoroles for " + message.getGuild().getName());
		String messageBody = "";
		for (AutoroleConfig config : configs) {
			messageBody += addAutoroleField(config);
		}
		autoroleEmbedBuilder.setDescription(messageBody);
		return autoroleEmbedBuilder.build();
	}

	private String addAutoroleField(AutoroleConfig config) {
		return String.format("**<@&%s>**\n```Applies to Bots:   %s\nApplies to Humans: %s```",
				config.getRoleID(),
				config.isAvailableForBots() ? TRUE_EMOTE : FALSE_EMOTE,
				config.isAvailableForHumans() ? TRUE_EMOTE : FALSE_EMOTE);
	}
	
	private void removeAutorole(Message message, List<String> args) {
		String roleID = ParsingUtils.filterSnowflake(args.get(0));
		if (message.getGuild().getRoleById(roleID) == null)
			message.getChannel().sendMessage("**You did not specify a valid role ID!**").queue();
		else if (autoroleService.getAutoroleConfigByRoleID(roleID) != null)
			message.getChannel().sendMessage("**This role is not set up for automation!**").queue();
		else
			removeAutoroleConfig(roleID, message);
	}

	private void removeAutoroleConfig(String roleID, Message message) {
		autoroleService.deleteAutoroleConfig(roleID);
		message.getChannel().sendMessage("**Autorole config deleted!**").queue();
	}

	private void addNewRole(Message message, List<String> args) {
		List<AutoroleConfig> configs = autoroleService.getAutoroleConfigs(serverService.getServer(message.getGuild().getId()).getId());
		if (configs.size() == 10)
			message.getChannel().sendMessage("**You have 10 roles set up already! You cannot add more!**").queue();
		else promptForRole(message);	
	}

	private void promptForRole(Message message) {
		getAwaitedReply(message, "**Please supply a role mention or ID:**", roleInput -> {
			String roleID = ParsingUtils.filterSnowflake(roleInput);
			if (message.getGuild().getRoleById(roleID) != null) promptForHumanAvailability(message, roleID);
			else message.getChannel().sendMessage("**You did not provide a valid role!**").queue();
		});
	}

	private void promptForHumanAvailability(Message message, String roleID) {
		getAwaitedReply(message, "**Should this role be applied to humans? (Yes/No)**", userInput -> {
			String input = userInput.toLowerCase();
			switch (input) {
			case "yes":
				promptForBotAvailability(message, roleID, true);
				break;
			case "y":
				promptForBotAvailability(message, roleID, true);
				break;
			case "no":
				promptForBotAvailability(message, roleID, false);
				break;
			case "n":
				promptForBotAvailability(message, roleID, false);
				break;
			default:
				message.getChannel().sendMessage("**You did not provide valid input!**").queue();
			}
		});
	}
	
	private void promptForBotAvailability(Message message, String roleID, boolean applyToHumans) {
		getAwaitedReply(message, "**Should this role be applied to bots? (Yes/No)**", userInput -> {
			String input = userInput.toLowerCase();
			switch (input) {
			case "yes":
				finishAddingAutorole(message, roleID, applyToHumans, true);
				break;
			case "y":
				finishAddingAutorole(message, roleID, applyToHumans, true);
				break;
			case "no":
				finishAddingAutorole(message, roleID, applyToHumans, false);
				break;
			case "n":
				finishAddingAutorole(message, roleID, applyToHumans, false);
				break;
			default:
				message.getChannel().sendMessage("**You did not provide valid input!**").queue();
			}
		});
	}
	
	private void finishAddingAutorole(Message message, String roleID, boolean applyToHumans, boolean applyToBots) {
		if (!applyToHumans && !applyToBots) {
			message.getChannel().sendMessage("**Autoroles must be added to at least either bots or humans!**").queue();
		}
		long serverID = serverService.getServer(message.getGuild().getId()).getId();
		AutoroleConfig config = new AutoroleConfig(serverID, roleID, applyToBots, applyToHumans);
		message.getChannel().sendMessage("**Autorole saved!**").queue();
		autoroleService.saveAutoroleConfig(config);
	}

	@Override
	public void executeInternal(Message message, List<String> args) {
		if (!message.getMember().hasPermission(Permission.MANAGE_ROLES))
			message.getChannel().sendMessage("**You need to have the** `Manage Roles` **permission to use this command!**").queue();			
		else {
			switch (args.get(0)) {
			case "add":
				addNewRole(message, args);
				break;
			case "remove":
				removeAutorole(message, args);
				break;
			case "list":
				showAutoroles(message);
				break;
			default:
				message.getChannel().sendMessage("**Did you mean one of these?**\n" + this.getUsage()).queue();
				break;
			}	
		}
	}

	@Override
	public String getDescription() {
		return "This command allows you to automatically assign roles to members and bots! You may choose if a role will be given to humans or bots. Maximum of 10 roles.";
	}

	@Override
	public String getUsage() {
		return "autorole add (then follow the instructions you are given)\n"
				+ "autorole remove [role mention/ID/'all']\n"
				+ "autorole list";
	}

	@Override
	public String getName() {
		return "autorole";
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
		return true;
	}

	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"ar"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}
}