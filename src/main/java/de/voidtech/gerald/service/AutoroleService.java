package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.entity.AutoroleConfig;
import main.java.de.voidtech.gerald.persistence.repository.AutoroleConfigRepository;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
public class AutoroleService {

    @Autowired
    private AutoroleConfigRepository repository;

    public List<AutoroleConfig> getAutoroleConfigs(long serverID) {
        return repository.getAutoroleConfigsByServerId(serverID);
    }

    public AutoroleConfig getAutoroleConfigByRoleID(String roleID) {
        return getAutoroleConfigByRoleID(roleID);
    }

    public void deleteAutoroleConfig(String roleID) {
        repository.deleteAutoroleConfigByRoleId(roleID);
    }

    public void saveAutoroleConfig(AutoroleConfig config) {
        repository.save(config);
    }

    public void addRolesToMember(GuildMemberJoinEvent event, List<AutoroleConfig> configs) {
        EnumSet<Permission> perms = event.getGuild().getSelfMember().getPermissions();
        List<AutoroleConfig> discardedConfigs = new ArrayList<>();

        if (perms.contains(Permission.MANAGE_ROLES)) {
            for (AutoroleConfig config : configs) {
                Role role = event.getJDA().getRoleById(config.getRoleID());

                if (role != null) {
                    if (config.isAvailableForBots() && event.getUser().isBot())
                        event.getGuild().addRoleToMember(event.getMember(), role).queue();
                    if (config.isAvailableForHumans() && !event.getUser().isBot())
                        event.getGuild().addRoleToMember(event.getMember(), role).queue();
                } else discardedConfigs.add(config);
            }
            if (!discardedConfigs.isEmpty()) {
                for (AutoroleConfig config : discardedConfigs) {
                    deleteAutoroleConfig(config.getRoleID());
                }
            }
        }
    }

    public void removeAllGuildConfigs(long serverID) {
        repository.deleteAutoroleConfigsByServerId(serverID);
    }
}