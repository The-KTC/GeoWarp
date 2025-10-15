import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
}
