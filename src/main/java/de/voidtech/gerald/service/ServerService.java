package main.java.de.voidtech.gerald.service;

import org.hibernate.Session;

import main.java.de.voidtech.gerald.entities.Server;

public class ServerService {
	
	private DatabaseService dbService;
	private static volatile ServerService instance;
	
	public static ServerService getInstance() {
		if (ServerService.instance == null) {
			ServerService.instance = new ServerService();
		}
		return ServerService.instance;
	}
	
	private ServerService()
	{
		this.dbService = DatabaseService.getInstance();
	}
	
	public Server getServer(String guildID)
	{
		Server server;
		try(Session session = dbService.getSessionFactory().openSession())
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
	
	public void saveServer(Server server)
	{
		try(Session session = dbService.getSessionFactory().openSession())
		{
			session.getTransaction().begin();
			session.saveOrUpdate(server);
			session.getTransaction().commit();
		}
	}
}
