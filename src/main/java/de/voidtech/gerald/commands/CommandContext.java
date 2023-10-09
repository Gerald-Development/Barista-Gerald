package main.java.de.voidtech.gerald.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

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
    private final List<GuildChannel> mentionedChannels;
    private final List<Role> mentionedRoles;
    private final boolean isSlash;
    private final Message message;
    private final SlashCommandInteractionEvent slashCommandEvent;
    private final boolean isPrivate;
    private final User user;
    private final GuildChannel guildChannel;
    private final boolean isMaster;

    private CommandContext(CommandContextBuilder builder) {
        this.channel = builder.channel;
        this.member = builder.member;
        this.guild = member == null ? null : member.getGuild();
        this.args = builder.args;
        this.mentionedMembers = Collections.unmodifiableList(builder.mentionedMembers != null ? builder.mentionedMembers : new ArrayList<>());
        this.mentionedChannels = Collections.unmodifiableList(builder.mentionedChannels != null ? builder.mentionedChannels : new ArrayList<>());
        this.mentionedRoles = Collections.unmodifiableList(builder.mentionedRoles != null ? builder.mentionedRoles : new ArrayList<>());
        this.isSlash = builder.isSlash;
        this.message = builder.message;
        this.slashCommandEvent = builder.slashCommandEvent;
        this.isPrivate = builder.isPrivateMessage;
        this.user = builder.user;
        this.guildChannel = builder.guildChannel;
        this.isMaster = builder.isMaster;
    }

    public boolean isMaster() {
        return this.isMaster;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public GuildChannel getGuildChannel() {
        return guildChannel;
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

    public List<GuildChannel> getMentionedChannels() {
        return mentionedChannels;
    }

    public List<Role> getMentionedRoles() {
        return mentionedRoles;
    }

    public boolean isSlash() {
        return isSlash;
    }

    public JDA getJDA() {
        return user.getJDA();
    }

    public void replyWithFile(byte[] attachment, String attachmentName, MessageEmbed... embeds) {
        FileUpload file = FileUpload.fromData(attachment, attachmentName);
        if (this.isSlash)
            slashCommandEvent.replyEmbeds(Arrays.asList(embeds)).addFiles(file).queue();
        else
            message.replyEmbeds(Arrays.asList(embeds)).mentionRepliedUser(false).addFiles(file).queue();
    }

    public void replyWithFile(byte[] attachment, String attachmentName, String text) {
        FileUpload file = FileUpload.fromData(attachment, attachmentName);
        if (this.isSlash)
            slashCommandEvent.reply(text).addFiles(file).queue();
        else
            message.reply(text).mentionRepliedUser(false).addFiles(file).queue();
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
        return this.user;
    }

    public Message getMessage() {
        return message;
    }

    public SlashCommandInteractionEvent getSlashCommandEvent() {
        return slashCommandEvent;
    }

    public static class CommandContextBuilder {
        private GuildChannel guildChannel;
        private MessageChannel channel;
        private Member member;
        private List<String> args;
        private List<Member> mentionedMembers;
        private List<GuildChannel> mentionedChannels;
        private List<Role> mentionedRoles;
        private final boolean isSlash;
        private Message message;
        private SlashCommandInteractionEvent slashCommandEvent;
        private boolean isPrivateMessage;
        private User user;
        private boolean isMaster;

        public CommandContextBuilder(boolean isSlashCommand) {
            this.isSlash = isSlashCommand;
        }

        public CommandContextBuilder guildChannel(GuildChannel channel) {
            this.guildChannel = channel;
            return this;
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

        public CommandContextBuilder mentionedChannels(List<GuildChannel> mentionedChannels) {
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

        public CommandContextBuilder slashCommandEvent(SlashCommandInteractionEvent event) {
            assert isSlash : "Chat based commands can not have a SlashCommandEvent.";
            this.slashCommandEvent = event;
            return this;
        }

        public CommandContextBuilder privateMessage(boolean isPrivateMessage) {
            this.isPrivateMessage = isPrivateMessage;
            return this;
        }

        public CommandContext build() {
            return new CommandContext(this);
        }

        public CommandContextBuilder user(User author) {
            this.user = author;
            return this;
        }

        public CommandContextBuilder master(boolean isMaster) {
            this.isMaster = isMaster;
            return this;
        }
    }
}
