package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CitrusCommand extends AbstractCommand {
	
	@Override
	public void executeInternal(Message message, List<String> args) 
	{
		EventWaiter waiter = getEventWaiter();
		BidiMap<String, String> citrusMap = getCitrusMap();
		List<String> keyList = citrusMap.keySet().stream().collect(Collectors.toList());
		String currentCitrus = keyList.get(new Random().nextInt(keyList.size()));
		
		MessageEmbed citrusQuestEmbed = new EmbedBuilder()//
				.setTitle(String.format("%s guess that citrus, you have 15 seconds!", message.getAuthor().getAsTag()))
				.setColor(Color.YELLOW)
				.setImage(citrusMap.get(currentCitrus))
				.build();
		
		message.getChannel().sendMessage(citrusQuestEmbed).queue();;
		
		waiter.waitForEvent(MessageReceivedEvent.class,
				event -> ((MessageReceivedEvent) event).getAuthor().getId().equals(message.getAuthor().getId()),
				event -> {
					boolean correctCitrus = event.getMessage().getContentRaw().equals(currentCitrus);
					message.getChannel().sendMessage(String.format("%s! The Citrus was **%s**",
							correctCitrus ? "Correct" : "Incorrect", currentCitrus)).queue();;
				}, 15, TimeUnit.SECONDS, 
				() -> message.getChannel().sendMessage(String.format("Time is up! The Citrus was **%s**", currentCitrus)).queue());
	}
	
	private BidiMap<String, String> getCitrusMap()
	{
		BidiMap<String, String> citrusMap = new DualHashBidiMap<>();
		
		citrusMap.put("lemon", "https://share.upmc.com/wp-content/uploads/2014/10/lemon.png");
		citrusMap.put("grapefruit", "https://i.ndtvimg.com/mt/cooks/2014-11/grapefruit.jpg");
		citrusMap.put("orange", "https://www.quanta.org/orange/orange.jpg");
		citrusMap.put("lime", "https://www.allmychefs.com/images/968/1200-auto/fotolia_60158073_subscription_l-copy.jpg?poix=50&poiy=50");
		citrusMap.put("pomelo", "https://images-na.ssl-images-amazon.com/images/I/71kEAwiVH1L._AC_SL1500_.jpg");
		citrusMap.put("yuzu", "https://img1.mashed.com/img/gallery/the-perfect-yuzu-juice-substitute/intro-1564609031.jpg");
		citrusMap.put("orangelo", "https://i.pinimg.com/originals/e5/20/2b/e5202b706069b594f6ab828af0f9d038.jpg");
		citrusMap.put("citron", "https://specialtyproduce.com/sppics/8713.png");
		citrusMap.put("kumquat", "https://producemadesimple.ca/wp-content/uploads/2015/04/kumquat-2-ss.jpg");
		citrusMap.put("bergamot", "https://cdn11.bigcommerce.com/s-295z9o5zsa/images/stencil/1280x1280/products/719/1955/Bergamot_orange_iso_edited_square__66892.1552850001.jpg?c=2");
		citrusMap.put("finger lime", "https://www.nature-and-garden.com/wp-content/uploads/sites/4/2018/10/australian-finger-lime.jpg");
		citrusMap.put("cantaloupe", "https://seedworld.com/site/wp-content/uploads/2019/01/GettyImages-845261084.jpg");
		citrusMap.put("watermelon", "https://specialtyproduce.com/sppics/11357.png");
		citrusMap.put("pumpkin", "https://www.liveeatlearn.com/wp-content/uploads/2015/10/pumpkin-photo-1.jpg");
		citrusMap.put("honeydew melon", "https://groceries.morrisons.com/productImages/210/210305011_0_640x640.jpg?identifier=6ff605c91cd0384439fa1acbd7de32a1");
		
		return citrusMap;
	}
	
	@Override
	public String getDescription() {
		return "Guess the citrus!";
	}

	@Override
	public String getUsage() {
		return "citrus";
	}

}
