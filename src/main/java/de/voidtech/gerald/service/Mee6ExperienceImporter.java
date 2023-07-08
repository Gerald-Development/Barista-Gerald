package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Experience;
import main.java.de.voidtech.gerald.persistence.entity.LevelUpRole;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class Mee6ExperienceImporter {

    @Autowired
    private ExperienceService xpService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ThreadManager threadManager;

    private static final GeraldLogger LOGGER = LogService.GetLogger(Mee6ExperienceImporter.class.getSimpleName());
    private static final String API_BASE_URL = "https://mee6.xyz/api/plugins/levels/leaderboard/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";

    public boolean leaderboardExists(String guildId) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_BASE_URL + guildId).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.disconnect();
            return con.getResponseCode() == 200;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getLeaderboardPage(String guildID, int page) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_BASE_URL + guildID + "?page=" + page).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.disconnect();
            if (con.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    return new JSONObject(in.lines().collect(Collectors.joining()));
                }
            } else {
                LOGGER.logWithoutWebhook(Level.WARNING, "Unexpected status " + con.getResponseCode() + " - retrying after 5 seconds...");
                Thread.sleep(5000);
                return getLeaderboardPage(guildID, page);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getLevelRoles(Server server) {
        JSONArray roleRewards = getLeaderboardPage(server.getGuildID(), 0).getJSONArray("role_rewards");
        for (int i = 0; i < roleRewards.length(); i++) {
            JSONObject roleReward = roleRewards.getJSONObject(i);
            LevelUpRole role = new LevelUpRole(roleReward.getJSONObject("role").getString("id"),
                    server.getId(), roleReward.getLong("rank"));
            xpService.saveLevelUpRole(role);
        }
    }

    public void extractLeaderboardData(CommandContext context, String guildId) {
        Server server = serverService.getServer(guildId);
        xpService.resetServer(server.getId());
        getLevelRoles(server);
        ExecutorService mee6Executor = threadManager.getThreadByName("T-MEE6");
        mee6Executor.execute(() -> loadPages(server, 0, context));
    }

    private void loadPages(Server server, int page, CommandContext context) {
        LOGGER.logWithoutWebhook(Level.INFO, "Loading page " + page + " for server " + server.getGuildID());
        JSONArray result = getLeaderboardPage(server.getGuildID(), page).getJSONArray("players");
        for (int i = 0; i < result.length(); i++) {
            JSONObject xp = result.getJSONObject(i);
            Experience userXp = new Experience(xp.getString("id"), server.getId());

            long level = xp.getLong("level");
            userXp.setLevel(level);
            userXp.setTotalExperience(xpService.totalXpNeededForLevel(level - 1) + 1);

            xpService.saveUserExperience(userXp);
        }
        if (result.length() < 100) {
            context.reply("**All done!**");
            LOGGER.logWithoutWebhook(Level.INFO, "Finished importing leaderboard");
            return;
        }
        page++;
        LOGGER.logWithoutWebhook(Level.INFO, "Sleeping for a bit before continuing...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        loadPages(server, page, context);
    }
}
