package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.exception.UnhandledGeraldException;
import main.java.de.voidtech.gerald.persistence.entity.Experience;
import main.java.de.voidtech.gerald.persistence.entity.LevelUpRole;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.persistence.entity.ServerExperienceConfig;
import main.java.de.voidtech.gerald.persistence.repository.ExperienceRepository;
import main.java.de.voidtech.gerald.persistence.repository.LevelUpRoleRepository;
import main.java.de.voidtech.gerald.persistence.repository.ServerExperienceConfigRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExperienceService {

    private static final int EXPERIENCE_DELAY = 60; //Delay between incrementing XP in seconds

    private final ExperienceRepository experienceRepository;
    private final ServerExperienceConfigRepository configRepository;
    private final LevelUpRoleRepository levelRepository;
    private final ServerService serverService;
    private final ImageService imageService;

    @Autowired
    public ExperienceService(
            final ExperienceRepository experienceRepository,
            final ServerExperienceConfigRepository configRepository,
            final LevelUpRoleRepository levelRepository,
            final ServerService serverService,
            final ImageService imageService
    ) {
        this.experienceRepository = experienceRepository;
        this.configRepository = configRepository;
        this.levelRepository = levelRepository;
        this.serverService = serverService;
        this.imageService = imageService;
    }

    public byte[] getExperienceCard(
            String avatarURL,
            long xpAchieved,
            long xpNeeded,
            long level,
            long rank,
            String username
    ) {
        try {
            return imageService.createExperienceCard(avatarURL, xpAchieved, xpNeeded, level, rank, username);
        } catch (Exception e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public Experience getUserExperience(String userID, long serverID) {
        return experienceRepository.getUserExperience(userID, serverID);
    }

    public List<String> getNoExperienceChannelsForServer(long serverID, JDA jda) {
        List<String> channels;
        ServerExperienceConfig config = getServerExperienceConfig(serverID);
        config.getNoXPChannels()
                .stream()
                .filter(channel -> jda.getTextChannelById(channel) == null)
                .forEach(config::removeNoExperienceChannel);
        channels = new ArrayList<>(config.getNoXPChannels());
        return channels;
    }

    private ServerExperienceConfig getServerExperienceConfig(long serverID) {
        ServerExperienceConfig xpConf = configRepository.getServerExperienceConfig(serverID);
        if (xpConf == null) {
            xpConf = new ServerExperienceConfig(serverID);
            configRepository.save(xpConf);
        }
        return xpConf;
    }

    public List<Experience> getServerLeaderboard(long serverID) {
        return experienceRepository.getServerLeaderboard(serverID);
    }

    public List<Experience> getServerLeaderboardChunk(long serverID, int limit, int offset) {
        return experienceRepository.getLeaderboardChunk(serverID, limit, offset);
    }

    public int getUserLeaderboardPosition(long serverID, String userID) {
        List<Experience> leaderboard = getServerLeaderboard(serverID);
        int position = 0;

        for (Experience xp : leaderboard) {
            position++;
            if (xp.getUserID().equals(userID)) break;
        }

        return position;
    }

    private List<LevelUpRole> getRolesForLevelFromServer(long id, long level) {
        return levelRepository.getLevelUpRolesInServerForLevel(id, level);
    }

    public List<LevelUpRole> getAllLevelUpRolesForServer(long id) {
        return levelRepository.getAllLevelUpRolesForServer(id);
    }

    public boolean serverHasRoleForLevel(long id, long level) {
        return levelRepository.getLevelUpRoleForLevelInServer(id, level) != null;
    }

    public void saveUserExperience(Experience userXP) {
        experienceRepository.save(userXP);
    }

    private void saveServerExperienceConfig(ServerExperienceConfig config) {
        configRepository.save(config);
    }

    public void saveLevelUpRole(LevelUpRole role) {
        levelRepository.save(role);
    }


    public void removeLevelUpRole(long level, long serverID) {
        levelRepository.deleteRoleFromServer(serverID, level);
    }

    private void removeAllLevelUpRoles(long serverID) {
        levelRepository.deleteAllRolesFromServer(serverID);
    }

    private void removeAllUserExperience(long serverID) {
        experienceRepository.deleteAllExperienceForServer(serverID);
    }

    private void deleteServerExperienceConfig(long serverID) {
        configRepository.deleteServerExperienceConfig(serverID);
    }

    public void deleteNoXpChannel(String channelID, long serverID) {
        ServerExperienceConfig config = getServerExperienceConfig(serverID);
        config.removeNoExperienceChannel(channelID);
        saveServerExperienceConfig(config);
    }

    public void clearNoXpChannels(long serverID) {
        ServerExperienceConfig config = getServerExperienceConfig(serverID);
        config.clearNoExperienceChannels();
        saveServerExperienceConfig(config);
    }

    public void addNoXpChannel(String channelID, long serverID) {
        ServerExperienceConfig config = getServerExperienceConfig(serverID);
        config.addNoExperienceChannel(channelID);
        saveServerExperienceConfig(config);
    }

    public long totalXpNeededForLevel(long level) {
        return (long) Math.ceil(((double) 5 / (double) 6) * (level * ((2 * Math.pow(level, 2)) + (27 * level) + 91)));
    }

    public long xpNeededForLevelWithoutPreviousLevels(long level) {
        return level == 0 ? totalXpNeededForLevel(level) : totalXpNeededForLevel(level) - totalXpNeededForLevel(level - 1);
    }

    public long xpGainedToNextLevelWithoutPreviousLevels(long level, long currentXp) {
        long excess = level == 0 ? 0 : totalXpNeededForLevel(level - 1);
        return currentXp - excess;
    }

    private long xpToNextLevel(long nextLevel, long currentXP) {
        return totalXpNeededForLevel(nextLevel) - currentXP;
    }

    public void updateUserExperience(Member member, String guildID, String channelID) {
        Server server = serverService.getServer(guildID);
        ServerExperienceConfig config = getServerExperienceConfig(server.getId());
        Experience userXP = getUserExperience(member.getId(), server.getId());

        if (userXP == null) {
            userXP = new Experience(member.getId(), server.getId());
        }

        userXP.incrementMessageCount();

        if ((userXP.getLastMessageTime() + EXPERIENCE_DELAY) > Instant.now().getEpochSecond()) {
            saveUserExperience(userXP);
            return;
        }

        userXP.incrementExperience(config.getExperienceIncrement());
        long currentExperience = userXP.getTotalExperience();
        long xpToNextLevel = xpToNextLevel(userXP.getNextLevel(), currentExperience);
        if (xpToNextLevel <= 0) {
            userXP.setLevel(userXP.getNextLevel());
            performLevelUpActions(userXP, server, member, channelID);
        }

        userXP.setLastMessageTime(Instant.now().getEpochSecond());
        saveUserExperience(userXP);
    }

    private void performLevelUpActions(Experience userXP, Server server, Member member, String channelID) {
        ServerExperienceConfig config = getServerExperienceConfig(server.getId());

        List<LevelUpRole> roles = getRolesForLevelFromServer(server.getId(), userXP.getCurrentLevel());
        if (roles.isEmpty()) return;

        List<Role> memberRoles = member.getRoles();

        for (LevelUpRole role : roles) {
            Role roleToBeGiven = member.getGuild().getRoleById(role.getRoleID());
            if (roleToBeGiven == null) removeLevelUpRole(role.getLevel(), role.getServerID());
            else {
                if (!memberRoles.contains(roleToBeGiven)) {
                    member.getGuild().addRoleToMember(member, roleToBeGiven).complete();
                    if (config.levelUpMessagesEnabled()) sendLevelUpMessage(role, member, roleToBeGiven, channelID);
                }
            }
        }
    }

    public void addRolesOnServerJoin(Server server, Member member) {
        Experience userXP = getUserExperience(member.getId(), server.getId());
        if (userXP == null) return;

        List<LevelUpRole> roles = getRolesForLevelFromServer(server.getId(), userXP.getCurrentLevel());
        if (roles.isEmpty()) return;

        List<Role> memberRoles = member.getRoles();
        for (LevelUpRole role : roles) {
            Role roleToBeGiven = member.getGuild().getRoleById(role.getRoleID());
            if (roleToBeGiven == null) removeLevelUpRole(role.getLevel(), role.getServerID());
            else if (!memberRoles.contains(roleToBeGiven))
                member.getGuild().addRoleToMember(member, roleToBeGiven).complete();
        }
    }

    private void sendLevelUpMessage(LevelUpRole role, Member member, Role roleToBeGiven, String channelID) {
        MessageEmbed levelUpEmbed = new EmbedBuilder()
                .setColor(roleToBeGiven.getColor())
                .setTitle(member.getUser().getName() + " levelled up!")
                .setDescription(member.getAsMention() + " reached level `" + role.getLevel()
                        + "` and received the role " + roleToBeGiven.getAsMention())
                .build();
        member.getGuild().getTextChannelById(channelID).sendMessageEmbeds(levelUpEmbed).queue();
    }

    public boolean toggleLevelUpMessages(long id) {
        ServerExperienceConfig config = getServerExperienceConfig(id);
        boolean nowEnabled = !config.levelUpMessagesEnabled();
        config.setLevelUpMessagesEnabled(nowEnabled);
        saveServerExperienceConfig(config);
        return nowEnabled;
    }

    public void resetServer(long id) {
        removeAllLevelUpRoles(id);
        removeAllUserExperience(id);
        deleteServerExperienceConfig(id);
    }

    public String getServerExperienceRate(long id) {
        ServerExperienceConfig config = getServerExperienceConfig(id);
        return config.rateIsRandomised() ? "Random (1-15)" : String.valueOf(config.getRate());
    }

    public void setServerGainRate(int gainRate, long id) {
        ServerExperienceConfig config = getServerExperienceConfig(id);
        config.setRate(gainRate);
        saveServerExperienceConfig(config);
    }
}
