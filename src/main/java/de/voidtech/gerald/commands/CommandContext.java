package main.java.de.voidtech.gerald.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandContext {
    private final MessageChannel channel;
    private final Member member;
    private final List<String> args;
    private final Guild guild;
    private final List<Member> mentionedMembers;
    private final List<TextChannel> mentionedChannels;

    public CommandContext(MessageChannel channel, Member member, List<String> args, List<Member> mentionedMembers, List<TextChannel> mentionedChannels) {
        this.member = member;
        this.args = args;
        this.channel = channel;
        this.guild = member.getGuild();
        this.mentionedMembers = Collections.unmodifiableList(mentionedMembers != null ? mentionedMembers : new ArrayList<>());
        this.mentionedChannels = Collections.unmodifiableList(mentionedChannels != null ? mentionedChannels : new ArrayList<>());
    }
    
    public CommandContext(MessageChannel channel, Member member, List<String> args, List<Member> mentionedMembers) {
        this(channel,member,args,mentionedMembers,null);
    }

    public CommandContext(MessageChannel channel, Member member, List<String> args) {
        this(channel, member, args, null);
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public User getAuthor() {
        return member.getUser();
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

    public JDA getJDA()
    {
        return this.member.getJDA();
    }

    public List<Member> getMentionedMembers() {
        return mentionedMembers != null ? mentionedMembers : Collections.unmodifiableList(new ArrayList<>());
    }

    public List<TextChannel> getMentionedChannels() {
        return mentionedChannels != null ? mentionedChannels : Collections.unmodifiableList(new ArrayList<>());
    }
}
