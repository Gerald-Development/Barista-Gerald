package main.java.de.voidtech.gerald.routines.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.NitroliteService;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class NitroliteRoutine extends AbstractRoutine {
    
	@Autowired
	private NitroliteService nitroliteService;
	
	@Override
    public void executeInternal(Message message) {
       List<String> processedMessage = nitroliteService.processNitroliteMessage(message);
       if (processedMessage != null) {
    	   final String content = StringUtils.join(processedMessage, " ");
    	   nitroliteService.sendMessage(message, content);
       }
    }

	@Override
    public String getDescription() {
        return "Allows nitrolite messages to be detected and handled";
    }
    
	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

	@Override
	public String getName() {
		return "r-nitrolite";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

}
