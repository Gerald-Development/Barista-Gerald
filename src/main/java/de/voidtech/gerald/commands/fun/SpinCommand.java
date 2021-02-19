package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class SpinCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		Map<String, Color> colorMap = getSpinnerColors();

		if (args.size() > 0) {
			if (colorMap.containsKey(args.get(0).toLowerCase())) {
				int spinTime = new Random().nextInt(30);
				Color color = colorMap.get(args.get(0).toLowerCase());

				doTheSpinning(color, spinTime, message);
			} else if (args.get(0).equals("colors")) {
				String supportedColorsString = StringUtils.join(colorMap.keySet(), "\n");
				message.getChannel().sendMessage("**Spinner colors:**\n" + supportedColorsString).queue();
			} else {
				message.getChannel().sendMessage("That is not a valid color!").queue();
			}	
		} else {
			int spinTime = new Random().nextInt(30);
			
			Object[] values = colorMap.values().toArray();
			Color color = (Color) values[new Random().nextInt(values.length)];

			doTheSpinning(color, spinTime, message);
		}
	}

	@Override
	public String getDescription() {
		return "Using the most sophisticated digitally augmented rotation technology, you can spin a virtual spinner";
	}

	@Override
	public String getUsage() {
		return "spin [color name] OR spin colors (to see the colors you can use)";
	}

	private void doTheSpinning(Color color, int spinTime, Message message) {
		int spinDelay = spinTime * 1000;
		MessageEmbed spinnerStartEmbed = new EmbedBuilder()
				.setColor(color)
				.setTitle("Your spinner is spinning...")
				.build();
		message.getChannel().sendMessage(spinnerStartEmbed).queue(sentMessage -> {
			
			try {
				Thread.sleep(spinDelay);
				MessageEmbed spinnerEndEmbed = new EmbedBuilder()
						.setColor(color)
						.setTitle("Your spinner has stopped!")
						.setDescription("It lasted for **" + spinTime + "** seconds!")
						.build();
				sentMessage.editMessage(spinnerEndEmbed).queue();
			} catch (InterruptedException e) {
				message.getChannel().sendMessage("Your spinner broke!").queue();
			}
		});
	}

	private Map<String, Color> getSpinnerColors() {
		Map<String, Color> spinnerColors = new HashMap<String, Color>();
		spinnerColors.put("red", Color.RED);
		spinnerColors.put("orange", Color.ORANGE);
		spinnerColors.put("yellow", Color.YELLOW);
		spinnerColors.put("green", Color.GREEN);
		spinnerColors.put("blue", Color.BLUE);
		spinnerColors.put("cyan", Color.CYAN);
		spinnerColors.put("magenta", Color.MAGENTA);
		spinnerColors.put("pink", Color.PINK);
		return spinnerColors;
	}

	@Override
	public String getName() {
		return "spin";
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
		return false;
	}

}