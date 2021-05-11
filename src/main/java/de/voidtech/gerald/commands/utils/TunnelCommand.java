package main.java.de.voidtech.gerald.commands.utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Tunnel;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Command
public class TunnelCommand extends AbstractCommand {

	@Autowired
	private EventWaiter waiter;
	
	@Autowired
	private SessionFactory sessionFactory;

	private BidiMap<String, String> pendingRequests = new DualHashBidiMap<String, String>();

	private void fillTunnel(Message message) {
		if (tunnelExists(message.getChannel().getId(), "")) {
			message.getChannel().sendMessage("Filling tunnel...").queue(sentMessage -> {
				Tunnel tunnel = getTunnel(message.getChannel().getId());

				String sourceChannel = tunnel.getSourceChannel();
				String destinationChannel = tunnel.getDestChannel();

				if (sourceChannel.equals(message.getChannel().getId())) {
					message.getJDA().getTextChannelById(destinationChannel)
							.sendMessage("**This tunnel has been filled.**").queue();
				} else {
					message.getJDA().getTextChannelById(sourceChannel).sendMessage("**This tunnel has been filled.**")
							.queue();
				}
				deleteTunnel(message.getChannel().getId());
				sentMessage.editMessage("**This tunnel has been filled.**").queue();
			});
		}
	}

	private void sendChannelVerificationRequest(TextChannel targetChannel, Message originChannelMessage, User tunnelInstantiator) {
		String targetChannelID = targetChannel.getId();
		String originChannelID = originChannelMessage.getChannel().getId();
		//TODO: Redo this, it doesn't work
		//pendingRequests.put(targetChannel.getId(), originChannelMessage.getChannel().getLatestMessageId());
		
		targetChannel.getGuild().retrieveMember(tunnelInstantiator).queue(member -> {
			if (member == null) {
				originChannelMessage.getChannel().sendMessage("**You need to be in that guild to dig a tunnel there!**").queue();
			} else {
				if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
					targetChannel.sendMessage("**Incoming tunnel request from " + originChannelMessage.getGuild().getName() + " > "
							+ originChannelMessage.getChannel().getName()
							+ "**\nSay 'accept' within 15 seconds to allow this tunnel to be dug!").queue();
					
					waiter.waitForEvent(MessageReceivedEvent.class,
							event -> tunnelAcceptedStatement(((MessageReceivedEvent) event), targetChannel), event -> {
								boolean allowTunnel = event.getMessage().getContentRaw().toLowerCase().equals("accept");
								if (allowTunnel) {
									digTunnel(targetChannel, originChannelMessage.getChannel());
								} else {
									denyTunnel(originChannelMessage);
								} 	
								removeFromMap(targetChannelID, originChannelID);
							}, 30, TimeUnit.SECONDS, () -> {
								originChannelMessage.getChannel().sendMessage("**Request timed out**").queue();
								removeFromMap(targetChannelID, originChannelID);
							});
				} else {
					originChannelMessage.getChannel().sendMessage("**You do not have permission to do that there!**").queue();
				}	
			}
		});
	}

	private void removeFromMap(String targetChannelID, String originChannelID) {
		this.pendingRequests.remove(targetChannelID);
		this.pendingRequests.remove(originChannelID);
	}

	private boolean tunnelAcceptedStatement(MessageReceivedEvent event, TextChannel targetChannel) {
		
		boolean messageEqualsAccept = event.getMessage().getContentRaw().toLowerCase().equals("accept");
		boolean messageIsNotFromSelf = (event.getAuthor().getId() != event.getJDA().getSelfUser().getId());
		boolean messageIsFromTargetChannel = event.getChannel().getId().equals(targetChannel.getId());
		boolean memberHasPermissions = event.getMember().hasPermission(Permission.MANAGE_CHANNEL);
		
		return messageEqualsAccept && messageIsNotFromSelf && messageIsFromTargetChannel && memberHasPermissions;
	}

	private void denyTunnel(Message originChannelMessage) {
		originChannelMessage.getChannel().sendMessage("**Tunnel request denied**").queue();
	}

	private void digTunnel(TextChannel targetChannel, MessageChannel originChannel) {
		targetChannel.sendMessage("**Tunnel Dug**").queue();
		originChannel.sendMessage("**Tunnel Dug**").queue();

		writeTunnelPair(originChannel.getId(), targetChannel.getId());
	}

	private void writeTunnelPair(String sourceChannelID, String destChannelID) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			Tunnel tunnel = new Tunnel(sourceChannelID, destChannelID);

			tunnel.setSourceChannel(sourceChannelID);
			tunnel.setDestChannel(destChannelID);

			session.saveOrUpdate(tunnel);
			session.getTransaction().commit();
		}
	}

	private boolean tunnelExists(String senderChannelID, String destChannelID) {
		if (destChannelID.equals("")) {
			try (Session session = sessionFactory.openSession()) {
				Tunnel tunnel = (Tunnel) session
						.createQuery(
								"FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
						.setParameter("senderChannelID", senderChannelID).uniqueResult();
				return tunnel != null;
			}	
		} else {
			try (Session sessionFirst = sessionFactory.openSession()) {
				Tunnel tunnelFirst = (Tunnel) sessionFirst
						.createQuery(
								"FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
						.setParameter("senderChannelID", senderChannelID).uniqueResult();
				
				try (Session sessionLast = sessionFactory.openSession()) {
					Tunnel tunnelLast = (Tunnel) sessionLast
							.createQuery(
									"FROM Tunnel WHERE sourceChannelID = :destChannelID OR destChannelID = :destChannelID")
							.setParameter("destChannelID", destChannelID).uniqueResult();
					return tunnelFirst != null || tunnelLast != null;
				}	
			}	
		}
	}

	private Tunnel getTunnel(String senderChannelID) {

		try (Session session = sessionFactory.openSession()) {
			Tunnel tunnel = (Tunnel) session
					.createQuery(
							"FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
					.setParameter("senderChannelID", senderChannelID).uniqueResult();
			return tunnel;
		}
	}

	private void deleteTunnel(String senderChannelID) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();
			session.createQuery(
					"DELETE FROM Tunnel WHERE sourceChannelID = :senderChannelID OR destChannelID = :senderChannelID")
					.setParameter("senderChannelID", senderChannelID).executeUpdate();
			session.getTransaction().commit();
		}
	}

	private void doTheDigging(List<String> args, Message message) {
		if (args.size() < 2) {
			message.getChannel().sendMessage("**You need to supply a channel snowflake ID!**").queue();

		} else {
			String targetChannelID = ParsingUtils.filterSnowflake(args.get(1));
			
			if (targetChannelID == "") {
				message.getChannel().sendMessage("**That is not a valid channel.**").queue();
			} else {
				TextChannel targetChannel = message.getJDA().getTextChannelCache().getElementById(targetChannelID);

				if (targetChannel == null) {
					message.getChannel().sendMessage("**That channel could not be found!**").queue();

				} else if (targetChannel.getId().equals(message.getChannel().getId())) {
					message.getChannel().sendMessage("**You can't dig a tunnel here!**").queue();

				} else if (pendingRequests.containsKey(message.getChannel().getId())
						|| pendingRequests.containsKey(targetChannelID)) {
					message.getChannel().sendMessage("**There is already a pending tunnel request!**").queue();
				} else {
					if (tunnelExists(message.getChannel().getId(), targetChannel.getId())) {
						message.getChannel().sendMessage("**There is already a tunnel here!**").queue();
					} else {
						sendChannelVerificationRequest(targetChannel, message, message.getAuthor());
					}
				}	
			}
		}
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (!message.getMember().hasPermission(Permission.MANAGE_CHANNEL))
			return;

		if (args.get(0).equals("fill")) {
			fillTunnel(message);

		} else if (args.get(0).equals("dig")) {
			doTheDigging(args, message);
		} else {
			message.getChannel().sendMessage("**" + this.getUsage() + "**").queue();
		}
	}

	@Override
	public String getDescription() {
		return "Tunnels allow you to form a bridge between two servers/channels! Using this command, Gerald will forward all text messages between the chosen channels. Note: Users who wish to set up Tunnels must have Manage Channels permissions.";
	}

	@Override
	public String getUsage() {
		return "To create a tunnel: tunnel dig [channel ID/channel mention]\nTo destroy a tunnel: tunnel fill";
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
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {};
		return aliases;
	}

}
