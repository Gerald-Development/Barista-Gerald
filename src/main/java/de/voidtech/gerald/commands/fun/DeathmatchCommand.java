package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class DeathmatchCommand extends AbstractCommand {

	private enum Turn { // enum as indicator whichs turn it is
		PLAYER_ONE,
		PLAYER_TWO
	}
	
	private User playerOne;
	private User playerTwo;

	private final List<String> attacksList = Arrays.asList("hits", "smacks", "punches", "runs over", "electrocutes",
			"atomic wedgies", "fish slaps", "clobbers", "pokes", "insults", "flicks"); // class field!
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (message.getMentionedMembers().size() == 0) {
			message.getChannel().sendMessage("**You need to mention an opponent!**").queue();
		} else {
			playerTwo = message.getMentionedMembers().get(0).getUser();
			playerOne = message.getMember().getUser();

			// removed the curly brackets around one line ifs
			if (playerTwo.equals(playerOne))
				message.getChannel().sendMessage("**You cannot fight yourself!**").queue();
			else
				startGame(message);
		}
	}

	private void startGame(Message message) {
		MessageEmbed gameStartEmbed = new EmbedBuilder()
				.setTitle(playerOne.getName() + " VS " + playerTwo.getName())
				.setColor(Color.RED).build();
		message.getChannel().sendMessage(gameStartEmbed).queue(sentMessage -> playRounds(sentMessage));
	}

	private void playRounds(Message message) {
		try {
			int playerOneHealth = 100, playerTwoHealth = 100;
			Turn playerTurn = Turn.PLAYER_ONE; // replaced with enum
			int damage;
		
			while (playerOneHealth > 0 && playerTwoHealth > 0) {
				damage = new Random().nextInt(40); // put damage outside ifs because its the same for each player

				// using enum values instead of ints for readability
				if (playerTurn == Turn.PLAYER_ONE) {
					playerTwoHealth = playerTwoHealth - damage;
					if (playerTwoHealth < 0) // removed the curly brackets around one line ifs
						playerTwoHealth = 0;

					message.editMessage(craftEmbed(playerOneHealth, playerTwoHealth, damage, Turn.PLAYER_ONE)).queue();
					playerTurn = Turn.PLAYER_TWO;
				} else {
					playerOneHealth = playerOneHealth - damage;
					if (playerOneHealth < 0)
						playerOneHealth = 0;

					message.editMessage(craftEmbed(playerOneHealth, playerTwoHealth, damage, Turn.PLAYER_TWO)).queue();
					playerTurn = Turn.PLAYER_ONE;
				}
				Thread.sleep(2000);
			}

			// reverse playerTurn to show who won
			if (playerTurn == Turn.PLAYER_ONE)
				playerTurn = Turn.PLAYER_TWO;
			else
				playerTurn = Turn.PLAYER_ONE;

			sendWinnerMessage(playerTurn, message);
		} catch (InterruptedException e) {
			message.editMessage("**Something went wrong! The battle was a draw!**").queue();
		}
	}

	// use our enum to display the correct message with some logic
	// if condition ? then this : else this
	private String craftMessage(int damage, Turn playerTurn) {
						// if it was player ones turn write his name else player twos name etc...
		return "**" + (playerTurn == Turn.PLAYER_ONE ? playerOne.getName() : playerTwo.getName()) +
				"** " + attacksList.get(new Random().nextInt(attacksList.size())) +
				" **" + (playerTurn != Turn.PLAYER_ONE ? playerOne.getName() : playerTwo.getName()) +
				"** for **" + damage + "** damage";
	}

	// use our enum to display the correct message with some logic
	// if condition ? then this : else this
	private MessageEmbed craftEmbed(int playerOneHealth, int playerTwoHealth, int damage, Turn playerTurn) {
		return new EmbedBuilder()
				.setTitle(playerOne.getName() + " (" + playerOneHealth + " ❤) VS " + playerTwo.getName() + " (" + playerTwoHealth + " ❤)")
				.setAuthor((playerTurn == Turn.PLAYER_ONE ? playerOne.getName() : playerTwo.getName()) + "'s Attack!")
				.setThumbnail(playerTurn == Turn.PLAYER_ONE ? playerOne.getAvatarUrl() : playerTwo.getAvatarUrl())
				.setDescription(craftMessage(damage, playerTurn))
				.setColor(playerTurn == Turn.PLAYER_ONE ? Color.ORANGE : Color.RED)
				.build();
	}

	// use our enum to display the correct message with some logic
	// if condition ? then this : else this
	private MessageEmbed craftWinnerEmbed(Turn playerTurn) {
		return new EmbedBuilder()
				.setThumbnail(playerTurn == Turn.PLAYER_ONE ? playerOne.getAvatarUrl() : playerTwo.getAvatarUrl())
				.setTitle((playerTurn == Turn.PLAYER_ONE ? playerOne.getName() : playerTwo.getName()) + " Has won the battle!")
				.setDescription("**" + (playerTurn == Turn.PLAYER_ONE ? playerTwo.getName() : playerOne.getName()) + " Was defeated!**")
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