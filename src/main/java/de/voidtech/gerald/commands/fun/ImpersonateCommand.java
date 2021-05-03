package main.java.de.voidtech.gerald.commands.fun;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.service.WebhookManager;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

@Command
public class ImpersonateCommand extends AbstractCommand{

	@Autowired
	private WebhookManager webhookManager;
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		EnumSet<Permission> perms = message.getGuild().getSelfMember().getPermissions((GuildChannel) message.getChannel());
		
		if (perms.contains(Permission.MESSAGE_MANAGE) && perms.contains(Permission.MANAGE_WEBHOOKS)) {
			if (message.getMentionedUsers().size() == 0) {
				message.getChannel().sendMessage("**You need to mention someone for that to work!**").queue();
			} else {
				String memberSnowflake = ParsingUtils.filterSnowflake(args.get(0));
				Member memberToBeImpersonated = message.getGuild().retrieveMemberById(memberSnowflake).complete();
				if (memberToBeImpersonated == null) {
					message.getChannel().sendMessage("**That member could not be found!**").queue();
				} else {
					sendWebhookMessage(message, args, memberToBeImpersonated);
				}
			}
		} else {
			message.getChannel().sendMessage("**I need Manage_Messages and Manage_Webhooks to do that!**").queue();
		}
	}

	private void sendWebhookMessage(Message message, List<String> args, Member memberToBeImpersonated) {
		String messageToBeSent = "";
		for (int i = 1; i < args.size(); i++) {
			messageToBeSent += args.get(i) + " ";
		}
		Webhook impersonateHook = webhookManager.getOrCreateWebhook((TextChannel) message.getChannel(), "BGImpersonate");
		webhookManager.postMessage(messageToBeSent, memberToBeImpersonated.getUser().getAvatarUrl(), memberToBeImpersonated.getUser().getName(), impersonateHook);
		message.delete().queue();
	}

	@Override
	public String getDescription() {
		return "Allows you to pretend to be someone else... Use it responsibly!";
	}

	@Override
	public String getUsage() {
		return "impersonate @ElementalMP4#7458 oh wow im such a loser lolol";
	}

	@Override
	public String getName() {
		return "impersonate";
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
		return true;
	}

}
