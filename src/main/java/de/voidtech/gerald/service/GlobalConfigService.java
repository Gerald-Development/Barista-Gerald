package main.java.de.voidtech.gerald.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.GlobalConfig;

@Service
public class GlobalConfigService {
	
	@Autowired
	private SessionFactory sf;

	public GlobalConfig getGlobalConfig()
	{
		try(Session session = sf.openSession())
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
