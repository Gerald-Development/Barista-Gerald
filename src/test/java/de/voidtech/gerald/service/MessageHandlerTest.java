package test.java.de.voidtech.gerald.service;

import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;

import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import main.java.de.voidtech.gerald.service.MessageHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class MessageHandlerTest extends TestCase
{
	private MessageHandler msgHandler;
	
//	@Before
//	public void setUp()
//	{
//		this.msgHandler = MessageHandler.getInstance();
//	}
//	
//	@Test
//	public void testOnlyAMessage() throws Exception
//	{
//		Message message = mock(Message.class);
//		
//		expect(message.getContentRaw()).andReturn("No Command just a message");
//		replay(message);
//		
//		msgHandler.handleMessage(message);
//	}
//	
//	@Test
//	public void testNotExistentCommand() throws Exception 
//	{
//		Message message = mock(Message.class);
//		
//		expect(message.getContentRaw()).andReturn("$keklmaobye").times(2);
//		replay(message);
//		
//		msgHandler.handleMessage(message);
//	}
//	
//	@Test
//	public void testCommand() throws Exception 
//	{
//		Message message = mock(Message.class);
//		User user = mock(User.class);
//		
//		expect(message.getContentRaw()).andReturn("$junittest").times(2);
//		expect(message.getAuthor()).andReturn(user).times(3);
//		expect(user.getAsTag()).andReturn("Barista#4711");
//		expect(user.getId()).andReturn("ACoolID");
//		replay(message, user);
//		
//		msgHandler.handleMessage(message);
//	}
}
