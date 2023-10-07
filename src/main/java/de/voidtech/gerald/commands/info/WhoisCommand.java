package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Collectors;

@Command
public class WhoisCommand extends AbstractCommand {

    @Override
    public void executeInternal(CommandContext context, List<String> args) {

        Member member = ParsingUtils.getMember(context, args);
        List<String> memberRoles = member.getRoles().stream().map(Role::getAsMention).collect(Collectors.toList());
        MessageEmbed whoisEmbed = buildEmbed(member, memberRoles);
        context.reply(whoisEmbed);
    }

    private MessageEmbed buildEmbed(Member member, List<String> memberRoles) {
        return new EmbedBuilder()//
                .setTitle("Who is " + member.getUser().getEffectiveName() + "?")//
                .setThumbnail(member.getUser().getAvatarUrl())
                .setColor(member.getColor())
                .addField("Nickname:", member.getEffectiveName(), true)
                .addField("Account created on:", member.getUser().getTimeCreated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), false)
                .addField("Server joined on:", member.getTimeJoined().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), true)
                .addField(String.format("Roles [%d]:", memberRoles.size()), StringUtils.join(memberRoles.toArray()), false)
                .addField("ID:", member.getId(), false)
                .build();
    }

    @Override
    public String getDescription() {
        return "returns information about the specified user or yourself";
    }

    @Override
    public String getUsage() {
        return "whois\n"
                + "whois @BaristaBoi#4029";
    }

    @Override
    public String getName() {
        return "whois";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.INFO;
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
        return new String[]{"user", "userinfo", "ui"};
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
