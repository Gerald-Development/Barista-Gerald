package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.exception.HandledGeraldException;
import main.java.de.voidtech.gerald.persistence.entity.MemeBlocklist;
import main.java.de.voidtech.gerald.persistence.repository.MemeBlocklistRepository;
import main.java.de.voidtech.gerald.service.ImageService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command
public class MemeCommand extends AbstractCommand {

    @Autowired
    private MemeBlocklistRepository repository;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ImageService imageService;

    private MemeBlocklist getBlocklist(long serverID) {
        return repository.getBlocklist(serverID);
    }

    private boolean blocklistExists(long serverID) {
        return getBlocklist(serverID) != null;
    }

    private MemeBlocklist getOrCreateBlocklist(long serverID) {
        if (!blocklistExists(serverID)) {
            repository.save(new MemeBlocklist(serverID, ""));
        }

        return getBlocklist(serverID);
    }

    private void updateBlocklist(String blocklistString, MemeBlocklist blocklistEntity) {
        blocklistEntity.setBlocklist(blocklistString);
        repository.save(blocklistEntity);
    }

    private List<String> getCaptions(List<String> args) {
        List<String> captions = new ArrayList<>(args);
        captions.remove(0);
        captions = captions.stream().map(s -> s.substring(1).trim()).toList();
        return captions;
    }

    private boolean templateIsBlocked(String template, long id, CommandContext context) {
        if (blocklistExists(id)) {
            MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(context.getGuild().getId()).getId());
            String blocklistString = blocklistEntity.getBlocklist();
            List<String> blocklist = Arrays.asList(blocklistString.split(","));
            return blocklist.contains(template);
        } else {
            return false;
        }
    }

    private void deliverMeme(CommandContext context, String template, List<String> text) {
        JSONObject apiResponse = imageService.getMeme(template, text);

        if (apiResponse == null) {
            context.reply("Couldn't find that template :(");
            return;
        }

        if (!apiResponse.getBoolean("success")) {
            context.reply("An error occurred whilst loading the template");
            throw new HandledGeraldException("Failed to load meme: " + apiResponse);
        }

        MessageEmbed memeImageEmbed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Image URL", apiResponse.getJSONObject("data").getString("url"))
                .setImage(apiResponse.getJSONObject("data").getString("url"))
                .setFooter("Requested By " + context.getAuthor().getEffectiveName(), context.getAuthor().getAvatarUrl())
                .build();
        context.reply(memeImageEmbed);
    }

    private void sendMeme(CommandContext context, List<String> args) {
        String messageText = String.join(" ", args);
        List<String> text = List.of();
        String template = args.get(0);

        if (messageText.contains("-")) {
            text = getCaptions(args);
        }

        if (context.getChannel().getType() == ChannelType.PRIVATE) {
            deliverMeme(context, template, text);
            return;
        }

        if (templateIsBlocked(template, serverService.getServer(context.getGuild().getId()).getId(), context)) {
            context.reply("**This template has been blocked**");
            return;
        }

        deliverMeme(context, template, text);
    }

    private void listBlockedMemes(CommandContext context) {
        if (blocklistExists(serverService.getServer(context.getGuild().getId()).getId())) {
            MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(context.getGuild().getId()).getId());
            String blocklistString = blocklistEntity.getBlocklist().replaceAll(",", "\n");

            context.reply("**Blocked Meme Templates:**\n" + blocklistString);
        } else {
            context.reply("**There is no blocklist yet!**");
        }
    }

    private void unblockMeme(CommandContext context, List<String> args) {
        if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            if (blocklistExists(serverService.getServer(context.getGuild().getId()).getId())) {
                MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(context.getGuild().getId()).getId());
                String blocklistString = blocklistEntity.getBlocklist();
                List<String> blocklist = new ArrayList<>(Arrays.asList(blocklistString.split(",")));

                List<String> modifiableArgs = args.subList(1, args.size());
                String templateToRemove = String.join(" ", modifiableArgs);

                if (templateToRemove.equals("")) {
                    context.reply("**You need to specify a template!**");
                } else {
                    if (blocklist.contains(templateToRemove)) {
                        blocklist.remove(templateToRemove);
                        blocklistString = String.join(",", blocklist);
                        updateBlocklist(blocklistString, blocklistEntity);
                        context.reply("'" + templateToRemove + "' **has been removed from the blocklist**");
                    } else {
                        context.reply("**This template is not blocked!**");
                    }
                }
            } else {
                context.reply("**There is no blocklist yet!**");
            }
        }
    }

    private void blockMeme(CommandContext context, List<String> args) {
        if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            MemeBlocklist blocklistEntity = getOrCreateBlocklist(serverService.getServer(context.getGuild().getId()).getId());
            String blocklistString = blocklistEntity.getBlocklist();
            List<String> blocklist = new ArrayList<>(Arrays.asList(blocklistString.split(",")));

            List<String> modifiableArgs = args.subList(1, args.size());
            String templateToAdd = String.join(" ", modifiableArgs);

            if (templateToAdd.equals("")) {
                context.reply("**You need to specify a template!**");
            } else {
                if (blocklist.contains(templateToAdd)) {
                    context.reply("**This template is already blocked!**");
                } else {
                    blocklist.add(templateToAdd);
                    blocklistString = String.join(",", blocklist);
                    updateBlocklist(blocklistString, blocklistEntity);
                    context.reply("'" + templateToAdd + "' **has been added to the blocklist**");
                }
            }
        }
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        switch (args.get(0)) {
            case "block":
                blockMeme(context, args);
                break;
            case "unblock":
                unblockMeme(context, args);
                break;
            case "blocklist":
                listBlockedMemes(context);
                break;
            default:
                sendMeme(context, args);
        }
    }

    @Override
    public String getDescription() {
        return "Allows you to request meme templates and add optional text to them. Note: caption text must be seperated by a '-'";
    }

    @Override
    public String getUsage() {
        return "meme [template name] [-text] [-text] .. [-text]\n"
                + "meme block [template name]\n"
                + "meme unblock [template name]\n"
                + "meme blocklist";
    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public boolean isDMCapable() {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"mememaker", "makememe"};
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