import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

public class GeoWarp extends JavaPlugin {
    private File dataFile;
    private FileConfiguration dataCfg;
    private WarpStore store;

    @Override
    public void onEnable() {
        dataFile = new File(getDataFolder(), "warps.yml");
        if (!dataFile.getParentFile().exists())
            dataFile.getParentFile().mkdirs();
        if (!dataFile.exists())
            saveResource("warps.yml", false); // optional: lege eine leere Datei im resources an
        dataCfg = YamlConfiguration.loadConfiguration(dataFile);
        store = new WarpStore(dataCfg);

        var cmd = new WarpCommand(this, store);
        getCommand("warp").setExecutor(cmd);
        getCommand("warp").setTabCompleter(cmd);

        getLogger().info("GeoWarp enabled");
    }

    public void saveWarps() {
        try {
            dataCfg.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save warps.yml: " + e.getMessage());
        }
    }

    public File getWarpsFile() {
        return this.dataFile;
    }

    /**
     * Speichert warps.yml und legt eine Kopie im Ordner plugins/GeoWarp/backups/
     * an.
     */
    public File backupWarps() throws IOException {
        // stelle sicher, dass der aktuelle Stand auf Platte ist
        saveWarps();

        File backupsDir = new File(getDataFolder(), "backups");
        if (!backupsDir.exists())
            backupsDir.mkdirs();

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File target = new File(backupsDir, "warps-" + ts + ".yml");

        Files.copy(getWarpsFile().toPath(), target.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES /* , StandardCopyOption.REPLACE_EXISTING */);

        return target;
    }

}
