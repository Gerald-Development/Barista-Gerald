package main.java.de.voidtech.gerald.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandContext {
    private final MessageChannel channel;
    private final Member member;
    private final List<String> args;
    private final Guild guild;
    private final List<Member> mentionedMembers;
    private final List<TextChannel> mentionedChannels;
    private final List<Role> mentionedRoles;
    private final boolean isSlash;
    private final Message message;
    private final SlashCommandEvent slashCommandEvent;

    private CommandContext(CommandContextBuilder builder) {
        this.channel = builder.channel;
        this.member = builder.member;
        this.guild = member.getGuild();
        this.args = builder.args;
        this.mentionedMembers = Collections.unmodifiableList(builder.mentionedMembers != null ? builder.mentionedMembers : new ArrayList<>());
        this.mentionedChannels = Collections.unmodifiableList(builder.mentionedChannels != null ? builder.mentionedChannels : new ArrayList<>());
        this.mentionedRoles = Collections.unmodifiableList(builder.mentionedRoles != null ? builder.mentionedRoles : new ArrayList<>());
        this.isSlash = builder.isSlash;
        this.message = builder.message;
        this.slashCommandEvent = builder.slashCommandEvent;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public Member getMember() {
        return member;
    }

    public List<String> getArgs() {
        return args;
    }

    public Guild getGuild() {
        return guild;
    }

    public List<Member> getMentionedMembers() {
        return mentionedMembers;
    }

    public List<TextChannel> getMentionedChannels() {
        return mentionedChannels;
    }

    public List<Role> getMentionedRoles() {
        return mentionedRoles;
    }

    public boolean isSlash() {
        return isSlash;
    }

    public JDA getJDA() {
        return member.getJDA();
    }
    
    public void replyWithFile(byte[] attachment, String attachmentName, MessageEmbed... embeds) {
        if (this.isSlash)
            slashCommandEvent.replyEmbeds(Arrays.asList(embeds)).addFile(attachment, attachmentName).queue();
        else
            message.replyEmbeds(Arrays.asList(embeds)).mentionRepliedUser(false).addFile(attachment, attachmentName).queue();
    }

    public void replyWithFile(byte[] attachment, String attachmentName, String text) {
        if (this.isSlash)
            slashCommandEvent.reply(text).addFile(attachment, attachmentName).queue();
        else
            message.reply(text).mentionRepliedUser(false).addFile(attachment, attachmentName).queue();
    }

    public void reply(MessageEmbed... embeds) {
        if (this.isSlash)
            slashCommandEvent.replyEmbeds(Arrays.asList(embeds)).queue();
        else
            message.replyEmbeds(Arrays.asList(embeds)).mentionRepliedUser(false).queue();
    }

    public void reply(String text) {
        if (this.isSlash)
            slashCommandEvent.reply(text).queue();
        else
            message.reply(text).mentionRepliedUser(false).queue();
    }

    public User getAuthor() {
        return this.member.getUser();
    }

    public Message getMessage() {
        return message;
    }

    public SlashCommandEvent getSlashCommandEvent() {
        return slashCommandEvent;
    }

    public static class CommandContextBuilder {
        private MessageChannel channel;
        private Member member;
        private List<String> args;
        private List<Member> mentionedMembers;
        private List<TextChannel> mentionedChannels;
        private List<Role> mentionedRoles;
        private final boolean isSlash;
        private Message message;
        private SlashCommandEvent slashCommandEvent;

        public CommandContextBuilder(boolean isSlashCommand) {
            this.isSlash = isSlashCommand;
        }

        public CommandContextBuilder channel(MessageChannel channel) {
            this.channel = channel;
            return this;
        }

        public CommandContextBuilder member(Member member) {
            this.member = member;
            return this;
        }

        public CommandContextBuilder args(List<String> args) {
            this.args = args;
            return this;
        }

        public CommandContextBuilder mentionedMembers(List<Member> mentionedMembers) {
            this.mentionedMembers = mentionedMembers;
            return this;
        }

        public CommandContextBuilder mentionedChannels(List<TextChannel> mentionedChannels) {
            this.mentionedChannels = mentionedChannels;
            return this;
        }

        public CommandContextBuilder mentionedRoles(List<Role> mentionedRoles) {
            this.mentionedRoles = mentionedRoles;
            return this;
        }

        public CommandContextBuilder message(Message message) {
            assert !isSlash : "Slash commands can not have a message to work on.";
            this.message = message;
            return this;
        }

        public CommandContextBuilder slashCommandEvent(SlashCommandEvent event) {
            assert isSlash : "Chat based commands can not have a SlashCommandEvent.";
            this.slashCommandEvent = event;
            return this;
        }

        public CommandContext build() {
            return new CommandContext(this);
        }
    }
}
