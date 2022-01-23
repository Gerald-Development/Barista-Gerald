package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

@Command
public class SuggestCommand extends AbstractCommand {

    @Autowired
    private ServerService serverService;

    @Autowired
    private SessionFactory sessionFactory;

    private final static String CHECK = "U+2705";
    private final static String CROSS = "U+274C";

    private SuggestionChannel getSuggestionChannel(long serverID) {
        try (Session session = sessionFactory.openSession()) {
            SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
            return suggestionChannel;
        }
    }

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
        SuggestionChannel config = getSuggestionChannel(server.getId());

        if (config != null) {
            if (config.voteRoleRequired()) {
            	Role role = context.getMember().getRoles().stream()
            			.filter(searchRole -> searchRole.getId().equals(config.getVoteRoleID()))
            			.findFirst()
            			.orElse(null);
            	if (role == null) {
            		context.reply("**You need the** `" + context.getGuild().getRoleById(config.getVoteRoleID()).getName() + "` **role to make suggestions!**");
            	} else {
            		postSuggestion(context, args, config);
            	}
            } else {
            	postSuggestion(context, args, config);
            }
            
        } else {
            context.reply("**This command has not been set up yet!**\n\n" + this.getUsage());
        }

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

    private boolean isGuildChannel(String channelID, CommandContext context) {
        return context.getGuild().getTextChannelById(channelID) != null;
    }
    
    private boolean isRole(String roleID, CommandContext context) {
    	return context.getGuild().getRoleById(roleID) != null;
    }

    private void addNewSuggestionChannel(long serverID, String channelID) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();

            SuggestionChannel suggestionChannel = new SuggestionChannel(serverID, channelID);
            session.saveOrUpdate(suggestionChannel);
            session.getTransaction().commit();
        }
    }
    
    private void saveRoleConfig(SuggestionChannel config) {
    	try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(config);
            session.getTransaction().commit();
        }
    }

    private void updateSuggestionChannel(long serverID, String channelID) {
        try (Session session = sessionFactory.openSession()) {
            SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();

            suggestionChannel.setSuggestionChannel(channelID);

            session.getTransaction().begin();
            session.saveOrUpdate(suggestionChannel);
            session.getTransaction().commit();
        }

    }

    private void validateInput(String channelID, Server server, CommandContext context) {
        if (ParsingUtils.isInteger(channelID)) {
            if (isGuildChannel(channelID, context)) {
                if (getSuggestionChannel(server.getId()) != null) {
                    updateSuggestionChannel(server.getId(), channelID);
                    context.reply("**The suggestion channel has been updated!!**");
                } else {
                    addNewSuggestionChannel(server.getId(), channelID);
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

    private void deleteSuggestionChannel(long guildID) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.createQuery("DELETE FROM SuggestionChannel WHERE ServerID = :guildID")
                    .setParameter("guildID", guildID)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }

    private void disableSuggestions(CommandContext context) {
        if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Server server = serverService.getServer(context.getGuild().getId());
            if (getSuggestionChannel(server.getId()) != null) {
                deleteSuggestionChannel(server.getId());
                context.reply("**The suggestion system has been disabled**");
            } else context.reply("**The suggestion system has not yet been set up!**");
        } else context.reply("**You do not have permission to do that!**");
    }
    
    private void validateRoleInput(CommandContext context, String roleID, Server server, SuggestionChannel config) {
    	if (ParsingUtils.isInteger(roleID)) {
            if (isRole(roleID, context)) {
                config.setVoteRole(roleID);
                saveRoleConfig(config);
                context.reply("**Suggestion role set to** `" + context.getGuild().getRoleById(roleID).getName() + "`");
            } else context.reply("**That is not a valid role!**");
        } else context.reply("**That is not a valid role!**");
	}
    
    private void setOrRemoveRole(CommandContext context, List<String> args) {
    	if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            Server server = serverService.getServer(context.getGuild().getId());
            SuggestionChannel config = getSuggestionChannel(server.getId());
            if (config != null) validateRoleInput(context, ParsingUtils.filterSnowflake(args.get(1)), server, config);
            else context.reply("**The suggestion system has not yet been set up!**");
        } else context.reply("**You do not have permission to do that!**");
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
            case "role":
            	setOrRemoveRole(context, args);
            	break;
            default:
                addSuggestion(context, args);
                break;
        }
    }

	@Override
    public String getDescription() {
        return "This command allows you to set up a suggestions box. "
        		+ "Simply set the suggestion box channel and your users can start sending suggestions! "
        		+ "If you only want certain users to make suggestions, use the role subcommand to set a suggestion role. "
        		+ "All users will require this role to make suggestions. This uncludes admins!";
    }

    @Override
    public String getUsage() {
        return "suggest channel [channel]\n"
                + "suggest disable\n"
                + "suggest role [role ID or role mention / clear]"
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
