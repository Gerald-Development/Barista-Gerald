package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.listeners.EventWaiter;
import main.java.de.voidtech.gerald.persistence.entity.NitroliteEmote;
import main.java.de.voidtech.gerald.service.EmoteService;
import main.java.de.voidtech.gerald.service.NitroliteService;
import main.java.de.voidtech.gerald.service.WebhookService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Command
public class NitroliteCommand extends AbstractCommand {
    @Autowired
    private NitroliteService nitroliteService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private EventWaiter waiter;

    private void sendFallbackMessage(CommandContext context, String content) {
        webhookService.postMessageWithFallback(
                context, content,
                context.getJDA().getSelfUser().getAvatarUrl(),
                context.getJDA().getSelfUser().getName(),
                "BGNitrolite");
    }

    private List<NitroliteEmote> getFirstFifteen(List<NitroliteEmote> list) {
        List<NitroliteEmote> firstFifteen = new ArrayList<>();
        if (list.size() > 15) {
            for (int i = 0; i < 15; i++) {
                firstFifteen.add(list.get(i));
            }
            return firstFifteen;
        } else {
            return list;
        }
    }

    private List<NitroliteEmote> listWithFirstFifteenRemoved(List<NitroliteEmote> list) {
        List<NitroliteEmote> tailOfList = new ArrayList<>();
        for (int i = 15; i < list.size(); i++) {
            tailOfList.add(list.get(i));
        }
        return tailOfList;
    }

    private void sendPages(CommandContext context, List<NitroliteEmote> result) {
        List<NitroliteEmote> firstFifteenResults = getFirstFifteen(result);
        String searchResult;
        boolean canSendMoreEmotes = false;

        StringBuilder searchResultBuilder = new StringBuilder();
        for (NitroliteEmote emote : firstFifteenResults) {
            searchResultBuilder.append(nitroliteService.constructEmoteString(emote)).append(" - ").append(emote.name()).append(" - ").append(emote.id()).append("\n");
        }
        searchResult = searchResultBuilder.toString();

        if (result.size() > 15) {
            searchResult += "\n**Send 'more' to see more results!**";
            canSendMoreEmotes = true;
        }

        sendFallbackMessage(context, searchResult);

        if (canSendMoreEmotes) {
            waiter.waitForEvent(MessageReceivedEvent.class,
                    event -> event.getAuthor().getId().equals(context.getAuthor().getId()),
                    event -> {
                        boolean moreRequested = event.getMessage().getContentRaw().equalsIgnoreCase("more");
                        if (moreRequested) {
                            sendPages(context, listWithFirstFifteenRemoved(result));
                        }
                    }, 60, TimeUnit.SECONDS,
                    () -> context.getChannel().sendMessage("**Search ended**").queue());
        }
    }

    private void searchEmoteDatabase(CommandContext context, List<String> args) {
        String search = args.get(1);

        if (search.length() < 3)
            context.getChannel().sendMessage("**Your search is too small! Please use at least 3 letters!**").queue();
        else {
            List<NitroliteEmote> result = emoteService.getEmotes(search, context.getJDA());

            StringBuilder searchResult = new StringBuilder("**Database searched for: **`" + search + "`\n");
            if (result.isEmpty()) {
                context.getChannel().sendMessage("**Nothing was found!**").queue();
            } else {
                if (result.size() > 15) {
                    sendPages(context, result);
                } else {
                    for (NitroliteEmote emote : result) {
                        searchResult.append(nitroliteService.constructEmoteString(emote)).append(" - ").append(emote.name()).append(" - ").append(emote.id()).append("\n");
                    }
                    sendFallbackMessage(context, searchResult.toString());
                }
            }
        }
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (args.get(0).equals("search")) {
            searchEmoteDatabase(context, args);
        } else {
            context.getChannel().sendMessage("**That's not a valid subcommand! Try something like this:**\n\n" + this.getUsage()).queue();
        }
    }

    @Override
    public String getDescription() {
        return """
                No Nitro? No problem!
                
                Nitrolite uses some magic code to allow you to use your favourite emotes anywhere with Gerald!
                To do so, simply write out your message, but add your emotes like this: [:a_cool_emote:] (note the square brackets, they are required)
                NOTE: this feature works best when Gerald can manage webhooks!""";
    }

    @Override
    public String getUsage() {
        return "To use nitrolite, type emotes like this - [:an_awesome_emote:] - You can use this command to search nitrlolite emotes";
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

    @Override
    public String[] getCommandAliases() {
        return new String[]{"nitro", "nl", "emotes", "emote"};
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
