package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.persistence.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServerService {

    @Autowired
    private ServerRepository repository;

    public synchronized Server getServer(String guildID) {
        Server server = repository.getServerByGuildID(guildID);
        if (server == null) {
            server = new Server(guildID);
            repository.save(server);
        }
        return server;
    }

    public synchronized void saveServer(Server server) {
        repository.save(server);
    }

    public synchronized void deleteServer(Server server) {
        repository.delete(server);
    }
}