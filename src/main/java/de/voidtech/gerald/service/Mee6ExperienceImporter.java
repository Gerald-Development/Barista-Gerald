package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Experience;
import main.java.de.voidtech.gerald.entities.Server;
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

    public JSONArray getLeaderboardPage(String guildID, int page) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(API_BASE_URL + guildID + "?page=" + page).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.disconnect();
            if (con.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    return new JSONObject(in.lines().collect(Collectors.joining())).getJSONArray("players");
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

    public void extractLeaderboardData(CommandContext context) {
        int page = 0;
        Server server = serverService.getServer(context.getGuild().getId());
        ExecutorService mee6Executor = threadManager.getThreadByName("T-MEE6");
        mee6Executor.execute(() -> insertResults(server, page, context));
    }

    private void insertResults(Server server, int page, CommandContext context) {
        LOGGER.logWithoutWebhook(Level.INFO, "Loading page " + page + " for server " + server.getGuildID());
        JSONArray result = getLeaderboardPage(server.getGuildID(), page);
        for (int i = 0; i < result.length(); i++) {
            JSONObject xp = result.getJSONObject(i);
            Experience userXp = new Experience(xp.getString("id"), server.getId());
            userXp.setCurrentXP(xp.getLong("xp"));
            userXp.setLevel(xp.getLong("level"));
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
        insertResults(server, page, context);
    }
}
