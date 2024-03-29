package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Tunnel;
import main.java.de.voidtech.gerald.persistence.repository.TunnelRepository;
import main.java.de.voidtech.gerald.listeners.EventWaiter;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Command
public class TunnelCommand extends AbstractCommand {
    //TODO (from: Franziska): Needs some thinking and rewriting for SlashCommands. I will just not implement the context.reply just yet as it will confuse the refactoring later.

    private final BidiMap<String, String> pendingRequests = new DualHashBidiMap<>();
    @Autowired
    private TunnelRepository repository;
    @Autowired
    private EventWaiter waiter;

    private void fillTunnel(CommandContext context) {
        if (tunnelExists(context.getChannel().getId(), "")) {
            context.getChannel().sendMessage("Filling tunnel...").queue(sentMessage -> {
                Tunnel tunnel = getTunnel(context.getChannel().getId());

                String sourceChannel = tunnel.getSourceChannel();
                String destinationChannel = tunnel.getDestChannel();

                if (sourceChannel.equals(context.getChannel().getId())) {
                    context.getJDA().getTextChannelById(destinationChannel).sendMessage("**This tunnel has been filled.**").queue();
                } else {
                    context.getJDA().getTextChannelById(sourceChannel).sendMessage("**This tunnel has been filled.**").queue();
                }
                deleteTunnel(context.getChannel().getId());
                sentMessage.editMessage("**This tunnel has been filled.**").queue();
            });
        }
    }

    private void sendChannelVerificationRequest(TextChannel targetChannel, Message originChannelMessage, User tunnelInstantiator) {
        String targetChannelID = targetChannel.getId();
        String originChannelID = originChannelMessage.getChannel().getId();

        addToMap(targetChannel, originChannelMessage);

        targetChannel.getGuild().retrieveMember(tunnelInstantiator).queue(member -> {
            if (member == null)
                originChannelMessage.getChannel().sendMessage("**You need to be in that guild to dig a tunnel there!**").queue();
            else {
                if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
                    targetChannel.sendMessage("**Incoming tunnel request from " + originChannelMessage.getGuild().getName() + " > "
                            + originChannelMessage.getChannel().getName()
                            + "**\nSay 'accept' within 30 seconds to allow this tunnel to be dug!").queue();

                    waiter.waitForEvent(MessageReceivedEvent.class,
                            event -> tunnelAcceptedStatement(event, targetChannel), event -> {
                                boolean allowTunnel = event.getMessage().getContentRaw().equalsIgnoreCase("accept");
                                if (allowTunnel) digTunnel(targetChannel, originChannelMessage.getChannel());
                                else denyTunnel(originChannelMessage);
                                removeFromMap(targetChannelID, originChannelID);
                            }, 30, TimeUnit.SECONDS, () -> {
                                originChannelMessage.getChannel().sendMessage("**Request timed out**").queue();
                                removeFromMap(targetChannelID, originChannelID);
                            });
                } else
                    originChannelMessage.getChannel().sendMessage("**You do not have permission to do that there!**").queue();
            }
        });
    }

    private void removeFromMap(String targetChannelID, String originChannelID) {
        this.pendingRequests.remove(targetChannelID);
        this.pendingRequests.remove(originChannelID);
    }

    private void addToMap(TextChannel targetChannel, Message originChannelMessage) {
        this.pendingRequests.put(targetChannel.getId(), originChannelMessage.getChannel().getId());
    }

    private boolean tunnelAcceptedStatement(MessageReceivedEvent event, TextChannel targetChannel) {

        boolean messageEqualsAccept = event.getMessage().getContentRaw().equalsIgnoreCase("accept");
        boolean messageIsNotFromSelf = (!event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()));
        boolean messageIsFromTargetChannel = event.getChannel().getId().equals(targetChannel.getId());
        boolean memberHasPermissions = Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL);

        return messageEqualsAccept && messageIsNotFromSelf && messageIsFromTargetChannel && memberHasPermissions;
    }

    private void denyTunnel(Message originChannelMessage) {
        originChannelMessage.getChannel().sendMessage("**Tunnel request denied**").queue();
    }

    private void digTunnel(TextChannel targetChannel, MessageChannelUnion originChannel) {
        targetChannel.sendMessage("**Tunnel Dug**").queue();
        originChannel.sendMessage("**Tunnel Dug**").queue();

        writeTunnelPair(originChannel.getId(), targetChannel.getId());
    }

    private void writeTunnelPair(String sourceChannelID, String destChannelID) {
        repository.save(new Tunnel(sourceChannelID, destChannelID));
    }

    private boolean tunnelExists(String senderChannelID, String destChannelID) {
        if (destChannelID.equals("")) {
            return repository.getTunnelBySingleChannelId(senderChannelID) != null;
        } else {
            Tunnel tunnelFirst = repository.getTunnelBySingleChannelId(senderChannelID);
            Tunnel tunnelLast = repository.getTunnelBySingleChannelId(destChannelID);
            return tunnelFirst != null || tunnelLast != null;
        }
    }

    private Tunnel getTunnel(String senderChannelID) {
        return repository.getTunnelBySingleChannelId(senderChannelID);
    }

    private void deleteTunnel(String senderChannelID) {
        repository.deleteTunnel(senderChannelID);
    }

    private void doTheDigging(List<String> args, Message message) {
        if (args.size() < 2) {
            message.getChannel().sendMessage("**You need to supply a channel ID!**").queue();
        } else {
            String targetChannelID = ParsingUtils.filterSnowflake(args.get(1));

            if (targetChannelID.equals(""))
                message.getChannel().sendMessage("**That is not a valid channel.**").queue();
            else {
                TextChannel targetChannel = message.getJDA().getTextChannelCache().getElementById(targetChannelID);

                if (targetChannel == null)
                    message.getChannel().sendMessage("**That channel could not be found!**").queue();
                else if (targetChannel.getId().equals(message.getChannel().getId()))
                    message.getChannel().sendMessage("**You can't dig a tunnel here!**").queue();
                else if (pendingRequests.containsKey(message.getChannel().getId())
                        || pendingRequests.containsKey(targetChannelID))
                    message.getChannel().sendMessage("**There is already a pending tunnel request!**").queue();
                else {
                    if (tunnelExists(message.getChannel().getId(), targetChannel.getId()))
                        message.getChannel().sendMessage("**There is already a tunnel here!**").queue();
                        //TODO (from: Franziska): I don't understand (I also didn't try to) needs the creators attetion.
                    else sendChannelVerificationRequest(targetChannel, message, message.getAuthor());
                }
            }
        }
    }

    private void showTunnelInfo(CommandContext context) {
        Tunnel tunnel = getTunnel(context.getChannel().getId());
        if (tunnel == null)
            sendUsageError(context);
        else {
            JDA jda = context.getJDA();
            String sourceChannel = "**" + Objects.requireNonNull(jda.getTextChannelById(tunnel.getSourceChannel())).getGuild().getName() + " -> " + Objects.requireNonNull(jda.getTextChannelById(tunnel.getSourceChannel())).getName() + "**\n";
            String destChannel = "**" + Objects.requireNonNull(jda.getTextChannelById(tunnel.getDestChannel())).getGuild().getName() + " -> " + Objects.requireNonNull(jda.getTextChannelById(tunnel.getDestChannel())).getName() + "**";
            context.getChannel().sendMessage("**This tunnel is between:**\n" + sourceChannel + destChannel).queue();
        }
    }

    private void sendUsageError(CommandContext context) {
        context.getChannel().sendMessage("**Did you mean to use one of these?:**\n" + this.getUsage()).queue();
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (!context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) return;
        if (args.isEmpty()) showTunnelInfo(context);
        else {
            switch (args.get(0)) {
                case "fill":
                    fillTunnel(context);
                    break;
                case "dig":
                    doTheDigging(args, context.getMessage());
                    break;
                default:
                    sendUsageError(context);
                    break;
            }
        }
    }

    @Override
    public String getDescription() {
        return "Tunnels allow you to form a bridge between two servers/channels! Using this command, Gerald will forward all text messages between the chosen channels. Note: Users who wish to set up Tunnels must have Manage Channels permissions.";
    }

    @Override
    public String getUsage() {
        return "tunnel dig [channel ID/channel mention]\n"
                + "tunnel fill\n"
                + "tunnel (use this in a tunnel to see tunnel information)";
    }

    @Override
    public String getName() {
        return "tunnel";
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
        return new String[]{"spaceport", "t"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public boolean isSlashCompatible() {
        return false;
    }
}