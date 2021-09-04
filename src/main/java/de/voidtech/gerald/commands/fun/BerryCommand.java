package main.java.de.voidtech.gerald.commands.fun;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.MRESameUserPredicate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Command
public class BerryCommand extends AbstractCommand{
	
	@Autowired
	private EventWaiter waiter;

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		BidiMap<String, String> berryMap = getBerryMap();
		List<String> keyList = berryMap.keySet().stream().collect(Collectors.toList());
		String currentBerry = keyList.get(new Random().nextInt(keyList.size()));
		
		MessageEmbed berryQuestEmbed = new EmbedBuilder()//
				.setTitle(String.format("%s guess that berry, you have 15 seconds!", context.getAuthor().getAsTag()))
				.setColor(Color.YELLOW)
				.setImage(berryMap.get(currentBerry))
				.build();
		
		context.getChannel().sendMessageEmbeds(berryQuestEmbed).queue();
		
		waiter.waitForEvent(MessageReceivedEvent.class,
				new MRESameUserPredicate(context.getAuthor()),
				event -> {
					boolean correctBerry = event.getMessage().getContentRaw().toLowerCase().equals(currentBerry);
					context.getChannel().sendMessage(String.format("%s! The Berry was **%s**",
							correctBerry ? "Correct" : "Incorrect", currentBerry)).queue();
				}, 15, TimeUnit.SECONDS, 
				() -> context.getChannel().sendMessage(String.format("Time is up! The Berry was **%s**", currentBerry)).queue());
	}
	
	private BidiMap<String, String> getBerryMap()
	{
		BidiMap<String, String> berryMap = new DualHashBidiMap<>();
		
		berryMap.put("cucumber", "https://cdn.mos.cms.futurecdn.net/EBEXFvqez44hySrWqNs3CZ.jpg");
		berryMap.put("watermelon", "https://specialtyproduce.com/sppics/11357.png");
		berryMap.put("grape", "https://pictures.attention-ngn.com/portal/185/191463/products/1499209833.7975_115_o.jpg");
		berryMap.put("blackberry", "https://www.specialfruit.com/en/thumbnail/productFull/product-1410443781/blackberries.jpg");
		berryMap.put("blueberry", "https://www.freshpoint.com/wp-content/uploads/commodity-blueberry.jpg");
		berryMap.put("strawberry", "https://www.thermofisher.com/blog/food/wp-content/uploads/sites/5/2015/08/single_strawberry__isolated_on_a_white_background.jpg");
		berryMap.put("raspberry", "https://cdn.shopify.com/s/files/1/1733/7409/products/Raspberries_f9263c6c-e4a7-41d9-b6a5-e795446631e4_x700.jpg?v=1539275589");
		berryMap.put("golden raspberry", "https://images.wisegeek.com/golden-raspberries.jpg");
		berryMap.put("black raspberry", "https://phasegenomics.com/wp-content/uploads/2018/03/Black-raspberry_2-e1522437849761.jpeg");
		berryMap.put("cranberry", "https://www.news-medical.net/image.axd?picture=2019%2F1%2Fshutterstock_739372951.jpg");
		berryMap.put("chokeberry", "https://cdn-a.william-reed.com/var/wrbm_gb_food_pharma/storage/images/3/0/6/8/2398603-1-eng-GB/Chokeberry-extracts-may-normalize-blood-clotting-Study_wrbm_large.jpg");
		berryMap.put("elderberry", "https://cdn-prod.medicalnewstoday.com/content/images/articles/323/323288/benefits-of-elderberry.jpg");
		berryMap.put("gooseberry", "https://static1.squarespace.com/static/545c9a01e4b043f3abfbb28f/545ce9e4e4b01d77329b8310/576d223e2e69cf237ffb09d0/1466784705255/IMG_1174.JPG?format=1500w");
		berryMap.put("lingonberry", "https://www.fona.com/wp-content/uploads/2016/02/lingonberry_0.jpg");
		berryMap.put("sloe", "https://www.collinsdictionary.com/images/full/sloe_60457573.jpg");
		berryMap.put("boysenberry", "https://www.fruitsinfo.com/images/hybrid-fruits/boysenberry.jpg");
		berryMap.put("redcurrant", "https://www.collinsdictionary.com/images/full/redcurrant_306080708.jpg");
		berryMap.put("blackcurrant", "https://cdn.ecommercedns.uk/files/1/231541/1/8021971/black-currant.jpg");
		berryMap.put("olallieberry", "https://specialtyproduce.com/sppics/7698.png");
		berryMap.put("mulberry", "https://www.nealsyardremedies.com/on/demandware.static/-/Sites-nyr-product-catalog/default/dw10a65858/images/3381/3381-mulberry-large.jpg");
		berryMap.put("acai", "https://www.gracefruit.com/uploads/images/products/large/gracefruit_gracefruit_acaiberryoil_1460546395dreamstimemaximum_43852716.jpg");
		berryMap.put("goji", "https://www.realfoodsource.com/wp-content/uploads/2016/11/C-GOJIBRY-e1480517101169.jpg");
		berryMap.put("physalis", "https://images.eatsmarter.de/sites/default/files/styles/576x432/public/images/thumb_physalis_576x432.jpg");
		berryMap.put("cloud berry", "https://d3h1lg3ksw6i6b.cloudfront.net/media/image/2019/07/29/a9f504c61b0840b9bb57ca21e851c752_shutterstock-cloudberries-HERO.jpg");
		berryMap.put("pine berry", "https://images-na.ssl-images-amazon.com/images/I/41LBbYMzbgL._AC_SY400_.jpg");
		berryMap.put("salmonberry", "https://www.kcaw.org/wp-content/uploads/2012/07/Salmonberries_lg.jpg");
		berryMap.put("avocado", "https://www.washingtonian.com/wp-content/uploads/2020/02/iStock-1027572462-scaled-2048x1695.jpg");
		berryMap.put("coffee berry", "https://avokadoskincare.com/wp-content/uploads/2019/06/coffee-cherry.png");
		berryMap.put("banana", "https://cdn.mos.cms.futurecdn.net/42E9as7NaTaAi4A6JcuFwG-1200-80.jpg");
		berryMap.put("strawberry tree", "https://upload.wikimedia.org/wikipedia/commons/c/cb/Arbouses.jpg");
		berryMap.put("agarita berry", "https://texasjellymaking.files.wordpress.com/2011/04/agarita.jpg");
		berryMap.put("barbados cherry", "https://draxe.com/wp-content/uploads/2020/01/acerola-cherry-facebook.jpg");
		berryMap.put("cherry", "https://upload.wikimedia.org/wikipedia/commons/b/bb/Cherry_Stella444.jpg");
		berryMap.put("plum", "https://www.treehugger.com/thmb/MUAkeEvYT8uHFuGpYMOf0EVvg3E=/1000x562/smart/filters:no_upscale()/__opt__aboutcom__coeus__resources__content_migration__mnn__images__2018__06__plums-65c0c343f7024361947cffe44bb6d2ba.jpg");
		
		return berryMap;
	}
	
	@Override
	public String getDescription() {
		return "guess the berry!";
	}

	@Override
	public String getUsage() {
		return "berry";
	}

	@Override
	public String getName() {
		return "berry";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"bguess", "berryguess"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
