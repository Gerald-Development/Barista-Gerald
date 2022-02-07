package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.WebhookManager;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.List;

@Command
public class ImpersonateCommand extends AbstractCommand{

	@Autowired
	private WebhookManager webhookManager;
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		EnumSet<Permission> perms = context.getGuild().getSelfMember().getPermissions((GuildChannel) context.getChannel());
		
		if (perms.contains(Permission.MESSAGE_MANAGE) && perms.contains(Permission.MANAGE_WEBHOOKS)) {
			if (context.getMentionedMembers().size() == 0) {
				context.reply("**You need to mention someone for that to work!**");
			} else {
				String memberSnowflake = ParsingUtils.filterSnowflake(args.get(0));
				Member memberToBeImpersonated = context.getGuild().retrieveMemberById(memberSnowflake).complete();
				if (memberToBeImpersonated == null) {
					context.reply("**That member could not be found!**");
				} else {
					sendWebhookMessage(context, args, memberToBeImpersonated);
				}
			}
		} else {
			context.reply("**I need Manage_Messages and Manage_Webhooks to do that!**");
		}
	}

	private void sendWebhookMessage(CommandContext context, List<String> args, Member memberToBeImpersonated) {
		StringBuilder messageToBeSent = new StringBuilder();
		for (int i = 1; i < args.size(); i++) {
			messageToBeSent.append(args.get(i)).append(" ");
		}
		Webhook impersonateHook = webhookManager.getOrCreateWebhook((TextChannel) context.getChannel(), "BGImpersonate", context.getJDA().getSelfUser().getId());
		webhookManager.postMessage(messageToBeSent.toString(), memberToBeImpersonated.getUser().getAvatarUrl(), memberToBeImpersonated.getUser().getName(), impersonateHook);

		//TODO (from: Franziska): Message needs to be deleted, message context does not have a message object. Should we add one? Do we do this somehow else? Should this command be available through slashes at all!?
		//context.delete().queue();
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
	
	@Override
	public String[] getCommandAliases() {
        return new String[]{"become", "pretend"};
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
