package main.java.de.voidtech.gerald.commands;

import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.service.ThreadManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

public abstract class AbstractCommand{
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private ThreadManager threadManager;
	
	private void runCommandInThread(CommandContext context, List<String> args) {
        if (context.getChannel().getType() == ChannelType.PRIVATE && !this.isDMCapable()) {
			context.getChannel().sendMessage("**You can only use this command in guilds!**").queue();
        } else if (this.requiresArguments() && args.size() < 1) {
			context.getChannel().sendMessage("**This command needs arguments to work! See the help command for more details!**\n" + this.getUsage()).queue();
	    } else {
			Runnable commandThreadRunnable = () -> executeInternal(context, args);
			threadManager.getThreadByName("T-Command").execute(commandThreadRunnable);   
	    }
	}

	public void run(CommandContext context, List<String> args) {
		if (context.getChannel().getType() == ChannelType.PRIVATE) {
			runCommandInThread(context, args);
		} else {
			Server server = serverService.getServer(context.getGuild().getId());
			Set<String> channelWhitelist = server.getChannelWhitelist();
			Set<String> commandBlacklist = server.getCommandBlacklist();
			
			boolean channelWhitelisted = channelWhitelist.isEmpty() || (channelWhitelist.contains(context.getChannel().getId()));
			boolean commandOnBlacklist = commandBlacklist.contains(getName());
			
			if((channelWhitelisted && !commandOnBlacklist) || context.getMember().hasPermission(Permission.ADMINISTRATOR))
			{
				runCommandInThread(context, args);
		    }	
		}
	}
	
	public abstract void executeInternal(CommandContext context, List<String> args);

	public abstract String getDescription();

	public abstract String getUsage();
	    
	public abstract String getName();

	public abstract CommandCategory getCommandCategory();

	public abstract boolean isDMCapable();

	public abstract boolean requiresArguments();
	
	public abstract String[] getCommandAliases();

	public abstract boolean canBeDisabled();
	
	public abstract boolean isSlashCompatible();
	
}
