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
	
	User playerOne = null;
	User playerTwo = null;
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (message.getMentionedMembers().size() == 0) {
			message.getChannel().sendMessage("**You need to mention an opponent!**").queue();
		} else {
			playerTwo = message.getMentionedMembers().get(0).getUser();
			playerOne = message.getMember().getUser();

			if (playerTwo.equals(playerOne)) {
				message.getChannel().sendMessage("**You cannot fight yourself!**").queue();
			} else {
				startGame(message);
			}
		}
	}

	private void startGame(Message message) {
		MessageEmbed gameStartEmbed = new EmbedBuilder()
				.setTitle(playerOne.getName() + " VS " + playerTwo.getName())
				.setColor(Color.RED).build();
		message.getChannel().sendMessage(gameStartEmbed).queue(sentMessage -> {
			playRounds(sentMessage);
		});
	}

	private void playRounds(Message message) {
		try {
			List<String> attacks = getAttacks();
			int playerOneHealth = 100, playerTwoHealth = 100;
			int turn = 0;
			int damage = 0;
		
			while (playerOneHealth > 0 && playerTwoHealth > 0) {
				if (turn == 0) {
					damage = new Random().nextInt(40);
					playerTwoHealth = playerTwoHealth - damage;
					if (playerTwoHealth < 0) {
						playerTwoHealth = 0;
					}
					MessageEmbed playerOneAttack = new EmbedBuilder()
							.setTitle(playerOne.getName() + " (" + playerOneHealth + " ❤) VS " + playerTwo.getName() + " (" + playerTwoHealth + " ❤)")
							.setAuthor(playerOne.getName() + "'s Attack!")
							.setThumbnail(playerOne.getAvatarUrl())
							.setDescription("**" + playerOne.getName() + "** " + attacks.get(new Random().nextInt(attacks.size())) + " **" + playerTwo.getName() + "** for **" + damage + "** damage")
							.setColor(Color.ORANGE)
							.build();
					message.editMessage(playerOneAttack).queue();
					turn = 1;
				
				} else {
					if (playerOneHealth < 0) {
						playerOneHealth = 0;
					}
					damage = new Random().nextInt(20);
					playerOneHealth = playerOneHealth - damage;
					MessageEmbed playerTwoAttack = new EmbedBuilder()
							.setTitle(playerOne.getName() + " (" + playerOneHealth + " ❤) VS " + playerTwo.getName() + " (" + playerTwoHealth + " ❤)")
							.setAuthor(playerTwo.getName() + "'s Attack!")
							.setThumbnail(playerTwo.getAvatarUrl())
							.setDescription("**" + playerTwo.getName() + "** " + attacks.get(new Random().nextInt(attacks.size())) + " **" + playerOne.getName() + "** for **" + damage + "** damage")
							.setColor(Color.RED)
							.build();
					message.editMessage(playerTwoAttack).queue();
					turn = 0;
				}
				Thread.sleep(2000);
			}
			sendWinnerMessage(turn, message);
		} catch (InterruptedException e) {
			message.editMessage("**Something went wrong! The battle was a draw!**").queue();
		}
	}

	private void sendWinnerMessage(int winnerInteger, Message message) {
		if (winnerInteger == 1) {
			MessageEmbed playerOneVictory = new EmbedBuilder()
					.setThumbnail(playerOne.getAvatarUrl())
					.setTitle(playerOne.getName() + " Has won the battle!")
					.setDescription("**" + playerTwo.getName() + " Was defeated!**")
					.setColor(Color.GREEN)
					.build();					
			message.editMessage(playerOneVictory).queue();
		} else {
			MessageEmbed playerTwoVictory = new EmbedBuilder()
					.setThumbnail(playerTwo.getAvatarUrl())
					.setTitle(playerTwo.getName() + " Has won the battle!")
					.setDescription("**" + playerOne.getName() + " Was defeated!**")
					.setColor(Color.GREEN)
					.build();					
			message.editMessage(playerTwoVictory).queue();
		}
	}
	
	private List<String> getAttacks() {
		List<String> attacksList = Arrays.asList("hits", "smacks", "punches", "runs over", "electrocutes",
				"atomic wedgies", "fish slaps", "clobbers", "pokes", "insults", "flicks");
		return attacksList;
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