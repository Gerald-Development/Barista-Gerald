package main.java.de.voidtech.gerald.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.Server;

@Service
public class ServerService {
	
	@Autowired
	private SessionFactory sf;
	
	public synchronized Server getServer(String guildID)
	{
		Server server;
		
		try(Session session = sf.openSession())
		{
			server = (Server) session.createQuery("FROM Server WHERE guildID = :guildID")
					.setParameter("guildID", guildID)
					.uniqueResult();
			
			if(server == null)
			{
				session.getTransaction().begin();
				server = new Server(guildID);
				session.save(server);
				session.getTransaction().commit();
			}
		}
		
		return server;
	}
	
	public synchronized void saveServer(Server server)
	{
		try(Session session = sf.openSession())
		{
			session.getTransaction().begin();
			session.saveOrUpdate(server);
			session.getTransaction().commit();
		}
	}
	
	public synchronized void deleteServer(Server server)
	{
		try(Session session = sf.openSession())
		{
			session.getTransaction().begin();
			session.delete(server);
			session.getTransaction().commit();
		}
	}
}