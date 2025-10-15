import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GeoWarp extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("GeoWarp enabled");
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!c.getName().equalsIgnoreCase("warp"))
            return false;
        if (!(s instanceof Player)) {
            s.sendMessage("Nur ingame nutzbar.");
            return true;
        }
        s.sendMessage("§a/warp läuft (Test).");
        return true;
    }
}
