package main.java.de.voidtech.gerald.service;

import org.hibernate.Session;

import main.java.de.voidtech.gerald.entities.GlobalConfig;

public class GlobalConfigService {
	private volatile static GlobalConfigService instance;
	private DatabaseService dbService;

	// private for Singleton
	private GlobalConfigService() {
		this.dbService = DatabaseService.getInstance();
	}

	public static GlobalConfigService getInstance() {
		if (GlobalConfigService.instance == null) {
			GlobalConfigService.instance = new GlobalConfigService();
		}

		return GlobalConfigService.instance;
	}
	
	public GlobalConfig getGlobalConfig()
	{
		try(Session session = dbService.getSessionFactory().openSession())
		{
			GlobalConfig globalConf = (GlobalConfig) session.createQuery("FROM GlobalConfig").uniqueResult();
			
			if(globalConf == null)
			{
				globalConf = new GlobalConfig();
				
				session.beginTransaction();
				session.persist(globalConf);
			}
			
			return globalConf;
		}
	}
}
