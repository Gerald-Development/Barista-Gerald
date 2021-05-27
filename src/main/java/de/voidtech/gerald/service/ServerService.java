package main.java.de.voidtech.gerald.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.Server;

@Service
public class ServerService {
	
	@Autowired
	private SessionFactory sf;
	
	private static final Logger LOGGER = Logger.getLogger(ServerService.class.getName());
	
	@SuppressWarnings("unchecked")
	public Server getServer(String guildID)
	{
		List<Server> serverList = new ArrayList<>();
		Server server = null;
		try(Session session = sf.openSession())
		{
			serverList = (List<Server>) session.createQuery("FROM Server WHERE guildID = :guildID")
					.setParameter("guildID", guildID)
					.list();
			
			if(serverList.isEmpty())
			{
				session.getTransaction().begin();
				server = new Server(guildID);
				session.save(server);
				session.getTransaction().commit();
			}
			else
			{
				server = serverList.get(0);
			}
			
			return server;
		} catch (ConstraintViolationException error) {
			LOGGER.log(Level.SEVERE, "ServerService has tried to create a duplicate server");
			error.printStackTrace();
			return server;
		}
	}
	
	public void saveServer(Server server)
	{
		try(Session session = sf.openSession())
		{
			session.getTransaction().begin();
			session.saveOrUpdate(server);
			session.getTransaction().commit();
		}
	}
	
	public void deleteServer(Server server)
	{
		try(Session session = sf.openSession())
		{
			session.getTransaction().begin();
			session.delete(server);
			session.getTransaction().commit();
		}
	}
}
