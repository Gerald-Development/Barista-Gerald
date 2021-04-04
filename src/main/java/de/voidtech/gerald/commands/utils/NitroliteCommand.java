package main.java.de.voidtech.gerald.commands.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.service.NitroliteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

@Command
public class NitroliteCommand extends AbstractCommand {
    
	@Autowired
	NitroliteService nitroliteService;
	
	@Override
    public void executeInternal(Message message, List<String> args) {

        List<Emote> emotes = message.getJDA()//
                .getEmoteCache()//
                .stream()//
                .filter(emote -> emote.getName().equals(args.get(0)))//
                .collect(Collectors.toList());

        if (!emotes.isEmpty()) {
            final String content = StringUtils.join(args.subList(1, args.size()), " ") +
                    " " + nitroliteService.constructEmoteString(emotes.get(0));

            nitroliteService.sendMessage(message, content);
        }
    }

    @Override
    public String getDescription() {
        return "Enables you to use emotes from servers Barista-Gerald is on everywhere";
    }

    @Override
    public String getUsage() {
        return "emote_name [text](optional)";
    }

	@Override
	public String getName() {
		return "nitrolite";
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
}
