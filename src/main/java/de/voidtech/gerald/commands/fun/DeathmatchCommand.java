package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.exception.HandledGeraldException;
import main.java.de.voidtech.gerald.exception.UnhandledGeraldException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Command
public class DeathmatchCommand extends AbstractCommand {

    private final static String HEART = ":heart:";
    private final List<String> attacksList = Arrays.asList("hits", "smacks", "punches", "runs over", "electrocutes",
            "atomic wedgies", "fish slaps", "clobbers", "pokes", "insults", "flicks");

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (context.getMentionedMembers().size() == 0) {
            context.reply("**You need to mention an opponent!**");
        } else {
            ArrayList<User> userList = new ArrayList<>(2);
            userList.add(context.getMember().getUser());
            userList.add(context.getMentionedMembers().get(0).getUser());

            if (userList.get(0).equals(userList.get(1)))
                context.reply("**You cannot fight yourself!**");
            else startGame(context, userList);
        }
    }

    private void startGame(CommandContext context, ArrayList<User> userList) {
        MessageEmbed gameStartEmbed = new EmbedBuilder()
                .setTitle(userList.get(0).getName() + " VS " + userList.get(1).getName())
                .setColor(Color.RED).build();
        //TODO (from: Franziska): relies on .queue() cannot use context.reply() I need to think about this. Maybe implement it with a consumer to specify what should happen after the reply has been queued
        context.getChannel().sendMessageEmbeds(gameStartEmbed).queue(sentMessage -> playRounds(sentMessage, userList));
    }

    private void playRounds(Message message, ArrayList<User> userList) {
        try {
            int[] playerHealth = {100, 100};
            Turn playerTurn = Turn.PLAYER_ONE;

            while (playerHealth[0] != 0 && playerHealth[1] != 0) {
                int damage = new Random().nextInt(40);

                playerHealth[1 - playerTurn.ordinal()] -= damage;
                if (playerHealth[1 - playerTurn.ordinal()] < 0)
                    playerHealth[1 - playerTurn.ordinal()] = 0;

                message.editMessageEmbeds(craftEmbed(playerHealth[0], playerHealth[1], damage, playerTurn, userList)).queue();
                playerTurn = Turn.values()[1 - playerTurn.ordinal()];
                Thread.sleep(2000);
            }

            playerTurn = Turn.values()[1 - playerTurn.ordinal()];

            sendWinnerMessage(playerTurn, message, userList);
        } catch (InterruptedException e) {
            message.editMessage("**Something went wrong! The battle was a draw!**").queue();
            throw new HandledGeraldException(e);
        }
    }

    private String craftMessage(int damage, Turn playerTurn, ArrayList<User> userList) {
        // if it was player one's turn write his name else player twos name etc...
        return "**" + (playerTurn == Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) +
                "** " + attacksList.get(new Random().nextInt(attacksList.size())) +
                " **" + (playerTurn != Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) +
                "** for **" + damage + "** damage";
    }

    private MessageEmbed craftEmbed(int playerOneHealth, int playerTwoHealth, int damage, Turn playerTurn, ArrayList<User> userList) {
        return new EmbedBuilder()
                .setTitle(userList.get(0).getName() + " (" + playerOneHealth + " " + HEART + ") VS " + userList.get(1).getName() + " (" + playerTwoHealth + " " + HEART + ")")
                .setAuthor((playerTurn == Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) + "'s Attack!")
                .setThumbnail(playerTurn == Turn.PLAYER_ONE ? userList.get(0).getAvatarUrl() : userList.get(1).getAvatarUrl())
                .setDescription(craftMessage(damage, playerTurn, userList))
                .setColor(playerTurn == Turn.PLAYER_ONE ? Color.ORANGE : Color.RED)
                .build();
    }

    private MessageEmbed craftWinnerEmbed(Turn playerTurn, ArrayList<User> userList) {
        return new EmbedBuilder()
                .setThumbnail(playerTurn == Turn.PLAYER_ONE ? userList.get(0).getAvatarUrl() : userList.get(1).getAvatarUrl())
                .setTitle((playerTurn == Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) + " has won the battle!")
                .setDescription("**" + (playerTurn == Turn.PLAYER_ONE ? userList.get(1).getName() : userList.get(0).getName()) + " was defeated!**")
                .setColor(Color.GREEN)
                .build();
    }

    private void sendWinnerMessage(Turn playerTurn, Message message, ArrayList<User> userList) {
        message.editMessageEmbeds(craftWinnerEmbed(playerTurn, userList)).queue();
    }

    @Override
    public String getDescription() {
        return "Allows you to have a deathmatch between yourself and another player";
    }

    @Override
    public String getUsage() {
        return "deathmatch [@another_user#0001]";
    }

    @Override
    public String getName() {
        return "deathmatch";
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
        return new String[]{"fight", "battle", "challenge"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public boolean isSlashCompatible() {
        return false;
    }

    private enum Turn {
        PLAYER_ONE,
        PLAYER_TWO
    }

}
