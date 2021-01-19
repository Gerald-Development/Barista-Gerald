package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class SpinCommand extends AbstractCommand{

	@Override
	public void executeInternal(Message message, List<String> args) {
		Map<String, Color> colorMap = getSpinnerColors();
		
		if (colorMap.containsKey(args.get(0).toLowerCase())) {
			Random random = new Random();
			int spinTime = random.nextInt(30);
			int spinDelay = spinTime * 1000;
			Color color = colorMap.get(args.get(0).toLowerCase());
			
			MessageEmbed spinnerStart = new EmbedBuilder()
					.setColor(color)
					.setTitle("Your spinner is spinning...")
					.build();			
			message.getChannel().sendMessage(spinnerStart).queue(sentMessage -> {
				try {
					Thread.sleep(spinDelay);
				} catch (InterruptedException e) {
					message.getChannel().sendMessage("Your spinner broke!").queue();
				}
				
				MessageEmbed spinnerEnd = new EmbedBuilder()
						.setColor(color)
						.setTitle("Your spinner has stopped!")
						.setDescription("It lasted for **" + spinTime + "** seconds!")
						.build();			
				sentMessage.editMessage(spinnerEnd).queue();
			});
			
		} else if (args.get(0).equals("colors")) { 
			String supportedColorsString = StringUtils.join(colorMap.keySet(), "\n");
			message.getChannel().sendMessage("**Spinner colors:**\n" + supportedColorsString).queue();
		} else {
			message.getChannel().sendMessage("That is not a valid color!").queue();
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

}