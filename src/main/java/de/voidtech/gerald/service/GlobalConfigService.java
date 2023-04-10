package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.repository.GlobalConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.persistence.entity.GlobalConfig;

@Service
public class GlobalConfigService {
	
	@Autowired
	private GlobalConfigRepository repository;

	public GlobalConfig getGlobalConfig() {
		GlobalConfig config = repository.getGlobalConfig();
		if (config == null) {
			config = new GlobalConfig();
			repository.save(config);
		}
		return config;
	}
}
