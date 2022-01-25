package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.SuggestionService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class SuggestCommand extends AbstractCommand {

    @Autowired
    private ServerService serverService;
    
    @Autowired
    private SuggestionService suggestionService;

    private final static String CHECK = "U+2705";
    private final static String CROSS = "U+274C";

    private void postSuggestion(CommandContext context, List<String> args, SuggestionChannel config) {
    	MessageEmbed newSuggestionEmbed = getMessageEmbed(context, args);
        context.getGuild().getTextChannelById(config.getSuggestionChannel()).sendMessageEmbeds(newSuggestionEmbed).queue(sentMessage -> {
            sentMessage.addReaction(CHECK).queue();
            sentMessage.addReaction(CROSS).queue();
            context.reply("**Your suggestion has been posted!**");
        });
    }
    
    private void addSuggestion(CommandContext context, List<String> args) {
        Server server = serverService.getServer(context.getGuild().getId());
        SuggestionChannel config = suggestionService.getSuggestionChannel(server.getId());

        if (config != null) {
            if (config.suggestRoleRequired()) {
            	boolean hasRole = suggestionService.memberHasRole(context.getMember(), config.getSuggestRoleID());
            	if (!hasRole) context.reply("**You need the** `"
            									+ context.getGuild().getRoleById(config.getSuggestRoleID()).getName()
            									+ "` **role to make suggestions!**");
            	else postSuggestion(context, args, config);
            } else postSuggestion(context, args, config);
        } else context.reply("**This command has not been set up yet!**\n\n" + this.getUsage());
    }

    @NotNull
    private MessageEmbed getMessageEmbed(CommandContext context, List<String> args) {
        MessageEmbed newSuggestionEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("New Suggestion!")
                .addField("Suggestion", String.join(" ", args), false)
                .setFooter("Suggested By " + context.getAuthor().getAsTag(), context.getAuthor().getAvatarUrl())
                .build();
        return newSuggestionEmbed;
    }

    private void validateInput(String channelID, Server server, CommandContext context) {
        if (ParsingUtils.isInteger(channelID)) {
            if (suggestionService.isGuildChannel(channelID, context)) {
            	SuggestionChannel config = suggestionService.getSuggestionChannel(server.getId());
                if (config != null) {
                    config.setSuggestionChannel(channelID);
                    suggestionService.saveSuggestionChannel(config);
                    context.reply("**The suggestion channel has been updated!!**");
                } else {
                	SuggestionChannel suggestionChannel = new SuggestionChannel(server.getId(), channelID);
                	suggestionService.saveSuggestionChannel(suggestionChannel);
                    context.reply("**The suggestion channel has been set up!**");
                }
            } else context.reply("**That is not a valid text channel!**");
        } else context.reply("**That is not a valid channel!**");
    }

    private void setChannel(CommandContext context, List<String> args) {
        if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            if (args.size() < 2) context.reply("**You need to specify a channel! Use a channel mention or its ID**");
            else {
                String channelID = ParsingUtils.filterSnowflake(args.get(1));
                Server server = serverService.getServer(context.getGuild().getId());

                validateInput(channelID, server, context);
            }
        } else context.reply("**You do not have permission to do that!**");
    }

    private void disableSuggestions(CommandContext context) {
        if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Server server = serverService.getServer(context.getGuild().getId());
            if (suggestionService.getSuggestionChannel(server.getId()) != null) {
            	suggestionService.deleteSuggestionChannel(server.getId());
                context.reply("**The suggestion system has been disabled**");
            } else context.reply("**The suggestion system has not yet been set up!**");
        } else context.reply("**You do not have permission to do that!**");
    }
    
    private void validateSuggestRoleInput(CommandContext context, String roleID, Server server, SuggestionChannel config) {
    	if (ParsingUtils.isInteger(roleID)) {
            if (suggestionService.isRole(roleID, context)) {
                config.setSuggestRole(roleID);
                suggestionService.saveSuggestionChannel(config);
                context.reply("**Suggestion role set to** `" + context.getGuild().getRoleById(roleID).getName() + "`");
            } else context.reply("**That is not a valid role!**");
        } else context.reply("**That is not a valid role!**");
	}
    
    private void validateVoteRoleInput(CommandContext context, String roleID, Server server, SuggestionChannel config) {
    	if (ParsingUtils.isInteger(roleID)) {
            if (suggestionService.isRole(roleID, context)) {
                config.setVoteRole(roleID);
                suggestionService.saveSuggestionChannel(config);
                context.reply("**Vote role set to** `" + context.getGuild().getRoleById(roleID).getName() + "`");
            } else context.reply("**That is not a valid role!**");
        } else context.reply("**That is not a valid role!**");
	}
    
    private void setOrRemoveSuggestionRole(CommandContext context, List<String> args) {
    	if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Server server = serverService.getServer(context.getGuild().getId());
            SuggestionChannel config = suggestionService.getSuggestionChannel(server.getId());
            if (config != null) {
            	if (args.get(1).equals("clear")) {
            		config.setSuggestRole(null);
            		suggestionService.saveSuggestionChannel(config);
            		context.reply("**Suggestion role removed!**");
            	} else validateSuggestRoleInput(context, ParsingUtils.filterSnowflake(args.get(1)), server, config);
            }
            else context.reply("**The suggestion system has not yet been set up!**");
        } else context.reply("**You do not have permission to do that!**");
    }
    
    private void setOrRemoveVoteRole(CommandContext context, List<String> args) {
    	if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Server server = serverService.getServer(context.getGuild().getId());
            SuggestionChannel config = suggestionService.getSuggestionChannel(server.getId());
            if (config != null) {
            	if (args.get(1).equals("clear")) {
            		config.setVoteRole(null);
            		suggestionService.saveSuggestionChannel(config);
            		context.reply("**Vote role removed!**");
            	} else validateVoteRoleInput(context, ParsingUtils.filterSnowflake(args.get(1)), server, config);
            }
            else context.reply("**The suggestion system has not yet been set up!**");
        } else context.reply("**You do not have permission to do that!**");
    }
    
	private void showSuggestionConfig(CommandContext context) {
		Server server = serverService.getServer(context.getGuild().getId());
		SuggestionChannel config = suggestionService.getSuggestionChannel(server.getId());
		if (config == null) context.getChannel().sendMessage("**There is no suggestion channel set up in this server!**").queue();
		else {
			String channel = "<#" + config.getSuggestionChannel() + ">";
			String voteRole = config.getVoteRoleID() == null ? "None set!" :
				context.getGuild().getRoleById(config.getVoteRoleID()).getName();
			String suggestRole = config.getSuggestRoleID() == null ? "None set!" :
				context.getGuild().getRoleById(config.getSuggestRoleID()).getName();
			context.getChannel().sendMessage("**Channel:** " + channel + "\n"
					+ "**Vote role:** `" + voteRole + "`\n"
					+ "**Suggestion Role:** `" + suggestRole + "`").queue();
		}
	}

	@Override
    public void executeInternal(CommandContext context, List<String> args) {
        switch (args.get(0)) {
            case "channel":
                setChannel(context, args);
                break;
            case "disable":
                disableSuggestions(context);
                break;
            case "suggestrole":
            	setOrRemoveSuggestionRole(context, args);
            	break;
            case "voterole":
            	setOrRemoveVoteRole(context, args);
            	break;
            case "config":
            	showSuggestionConfig(context);
            	break;
            default:
                addSuggestion(context, args);
                break;
        }
    }

	@Override
    public String getDescription() {
        return "This command allows you to set up a suggestions box. "
        		+ "Simply set the suggestion box channel and your users can start sending suggestions!\n"
        		+ "If you only want certain users to make suggestions, use the suggestrole subcommand to set a suggestion role.\n"
        		+ "If you only want certain users to vote, use the voterole subcommand to set a vote role.\n"
        		+ "Use the config subcommand to see your current configuration\n"
        		+ "All users will require these roles to make suggestions or vote. This includes admins!";
    }

    @Override
    public String getUsage() {
        return "suggest channel [channel]\n"
                + "suggest disable\n"
                + "suggest suggestrole [role ID / clear]\n"
                + "suggest voterole [role ID / clear]\n"
                + "suggest config"
                + "suggest [suggestion]";
    }

    @Override
    public String getName() {
        return "suggest";
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
        return new String[]{"idea", "suggestion"};
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
