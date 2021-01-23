package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class DeathmatchCommand extends AbstractCommand {

	private enum Turn {
		PLAYER_ONE,
		PLAYER_TWO
	}
	
	private ArrayList<User> userList = new ArrayList<>(2);

	private final List<String> attacksList = Arrays.asList("hits", "smacks", "punches", "runs over", "electrocutes",
			"atomic wedgies", "fish slaps", "clobbers", "pokes", "insults", "flicks");
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (message.getMentionedMembers().size() == 0) {
			message.getChannel().sendMessage("**You need to mention an opponent!**").queue();
		} else {
			userList.add(message.getMember().getUser());
			userList.add(message.getMentionedMembers().get(0).getUser());

			if (userList.get(0).equals(userList.get(1)))
				message.getChannel().sendMessage("**You cannot fight yourself!**").queue();
			else startGame(message);
		}
	}

	private void startGame(Message message) {
		MessageEmbed gameStartEmbed = new EmbedBuilder()
				.setTitle(userList.get(0).getName() + " VS " + userList.get(1).getName())
				.setColor(Color.RED).build();
		message.getChannel().sendMessage(gameStartEmbed).queue(sentMessage -> playRounds(sentMessage));
	}

	private void playRounds(Message message) {
		try {
			int[] playerHealth = {100, 100};
			Turn playerTurn = Turn.PLAYER_ONE;
		
			while (playerHealth[0] != 0 && playerHealth[1] != 0) {
				int damage = new Random().nextInt(40);

				playerHealth[1-playerTurn.ordinal()] -= damage;
				if (playerHealth[1-playerTurn.ordinal()] < 0)
					playerHealth[1-playerTurn.ordinal()] = 0;

				message.editMessage(craftEmbed(playerHealth[0], playerHealth[1], damage, playerTurn)).queue();

				playerTurn = Turn.values()[1-playerTurn.ordinal()];

				Thread.sleep(2000);
			}

			playerTurn = Turn.values()[1-playerTurn.ordinal()];

			sendWinnerMessage(playerTurn, message);
		} catch (InterruptedException e) {
			message.editMessage("**Something went wrong! The battle was a draw!**").queue();
		}
	}

	private String craftMessage(int damage, Turn playerTurn) {
						// if it was player ones turn write his name else player twos name etc...
		return "**" + (playerTurn == Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) +
				"** " + attacksList.get(new Random().nextInt(attacksList.size())) +
				" **" + (playerTurn != Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) +
				"** for **" + damage + "** damage";
	}

	private MessageEmbed craftEmbed(int playerOneHealth, int playerTwoHealth, int damage, Turn playerTurn) {
		return new EmbedBuilder()
				.setTitle(userList.get(0).getName() + " (" + playerOneHealth + " ❤) VS " + userList.get(1).getName() + " (" + playerTwoHealth + " ❤)")
				.setAuthor((playerTurn == Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) + "'s Attack!")
				.setThumbnail(playerTurn == Turn.PLAYER_ONE ? userList.get(0).getAvatarUrl() : userList.get(1).getAvatarUrl())
				.setDescription(craftMessage(damage, playerTurn))
				.setColor(playerTurn == Turn.PLAYER_ONE ? Color.ORANGE : Color.RED)
				.build();
	}

	private MessageEmbed craftWinnerEmbed(Turn playerTurn) {
		return new EmbedBuilder()
				.setThumbnail(playerTurn == Turn.PLAYER_ONE ? userList.get(0).getAvatarUrl() : userList.get(1).getAvatarUrl())
				.setTitle((playerTurn == Turn.PLAYER_ONE ? userList.get(0).getName() : userList.get(1).getName()) + " Has won the battle!")
				.setDescription("**" + (playerTurn == Turn.PLAYER_ONE ? userList.get(1).getName() : userList.get(0).getName()) + " Was defeated!**")
				.setColor(Color.GREEN)
				.build();
	}

	private void sendWinnerMessage(Turn playerTurn, Message message) {
		message.editMessage(craftWinnerEmbed(playerTurn)).queue();
	}

	@Override
	public String getDescription() {
		return "Allows you to have a deathmatch between yourself and another player";
	}

	@Override
	public String getUsage() {
		return "deathmatch [@another_user#0001]";
	}

}