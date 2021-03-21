package main.java.de.voidtech.gerald.routines.fun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class BottiusResponseRoutine extends AbstractRoutine{
	//Why? Because Bottius is awesome
	
	private List<String> getResponses()
	{
		List<String> responses = new ArrayList<>();
		
		responses.add("I agree ^");
		responses.add("Smh my head people be more active");
		responses.add("<a:bongobongo:806633789279305808>");
		responses.add("Howdy there Bottius");
		responses.add("Well if it isn't my main man Bottius <:fatmorning:766947919239708672>");
		responses.add("<:intensecat:804291788147130428>");
		
		return responses;
	}
	
	private boolean bottiusDetector(Message message) {
		return message.getContentRaw().equals("A lot of nothing in here... <:tumbleweed:764588927842910219>")
			&& message.getAuthor().getId().equals("639135241562488841");
	}
	
	@Override
	public void executeInternal(Message message) {
		if (bottiusDetector(message)) {
			Random random = new Random();
			List<String> responses = getResponses();
			message.getChannel().sendMessage(responses.get(random.nextInt(responses.size()))).queue();
		}
		
	}

	@Override
	public String getDescription() {
		return "Greets bottius mightily";
	}


}
