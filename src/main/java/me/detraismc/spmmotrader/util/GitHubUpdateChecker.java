package me.detraismc.spmmotrader.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class GitHubUpdateChecker {

    private final JavaPlugin plugin;
    private final String repo; // Format: "Username/Repository"

    public GitHubUpdateChecker(JavaPlugin plugin, String repo) {
        this.plugin = plugin;
        this.repo = repo;
    }

    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + repo + "/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", plugin.getName() + "-UpdateChecker");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    String latestVersion = jsonObject.get("tag_name").getAsString().replace("v", "");
                    String currentVersion = plugin.getDescription().getVersion().replace("v", "");

                    if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                        plugin.getLogger().log(Level.WARNING, "==================================================");
                        plugin.getLogger().log(Level.WARNING, "A new update for " + plugin.getName() + " is available!");
                        plugin.getLogger().log(Level.WARNING, "Current Version: " + currentVersion);
                        plugin.getLogger().log(Level.WARNING, "Latest Version: " + latestVersion);
                        plugin.getLogger().log(Level.WARNING, "Download it here: https://github.com/" + repo + "/releases");
                        plugin.getLogger().log(Level.WARNING, "==================================================");
                    } else {
                        plugin.getLogger().info("You are running the latest version of " + plugin.getName() + ".");
                    }
                } else if (responseCode == 403) {
                    plugin.getLogger().info("Update check skipped: GitHub API rate limit exceeded (Try again in an hour).");
                } else if (responseCode == 404) {
                    plugin.getLogger().warning("Update check failed: Repository '" + repo + "' not found or has no published releases.");
                } else {
                    plugin.getLogger().warning("Update check failed: GitHub API returned HTTP " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().info("Could not check for updates (Network error).");
            }
        });
    }
}
