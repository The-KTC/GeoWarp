import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class WarpStore {
    private final FileConfiguration cfg;

    public WarpStore(FileConfiguration cfg) {
        this.cfg = cfg;
        if (!cfg.isConfigurationSection("places"))
            cfg.createSection("places");
    }

    /* ---------- PUT ---------- */

    public void putCountryAnchor(String land, Location loc) {
        ensure("places." + land);
        setAnchor("places." + land, loc);
    }

    public void putCityAnchor(String land, String stadt, Location loc) {
        ensure("places." + land + "." + stadt);
        setAnchor("places." + land + "." + stadt, loc);
    }

    public void putStreetAnchor(String land, String stadt, String strasse, Location loc) {
        ensure("places." + land + "." + stadt + ".streets");
        ensure("places." + land + "." + stadt + ".streets." + strasse);
        setAnchor("places." + land + "." + stadt + ".streets." + strasse, loc);
    }

    public void putAddress(String land, String stadt, String strasse, String hausnr, String zusatz, Location loc) {
        String p = "places." + land + "." + stadt + ".streets." + strasse + ".numbers." + hausnr;
        ensure("places." + land + "." + stadt + ".streets." + strasse + ".numbers");
        cfg.set(p + ".world", loc.getWorld().getName());
        cfg.set(p + ".x", loc.getX());
        cfg.set(p + ".y", loc.getY());
        cfg.set(p + ".z", loc.getZ());
        cfg.set(p + ".yaw", loc.getYaw());
        cfg.set(p + ".pitch", loc.getPitch());
        if (zusatz != null && !zusatz.isEmpty())
            cfg.set(p + ".extra", zusatz);
    }

    /* ---------- GET ---------- */

    public Optional<Location> getCountryAnchor(String land) {
        return getAnchor("places." + land);
    }

    public Optional<Location> getCityAnchor(String land, String stadt) {
        return getAnchor("places." + land + "." + stadt);
    }

    public Optional<Location> getStreetAnchor(String land, String stadt, String strasse) {
        return getAnchor("places." + land + "." + stadt + ".streets." + strasse);
    }

    public Optional<Location> getAddress(String land, String stadt, String strasse, String hausnr) {
        String p = "places." + land + "." + stadt + ".streets." + strasse + ".numbers." + hausnr;
        String wName = cfg.getString(p + ".world");
        if (wName == null)
            return Optional.empty();
        World w = Bukkit.getWorld(wName);
        if (w == null)
            return Optional.empty();
        double x = cfg.getDouble(p + ".x"), y = cfg.getDouble(p + ".y"), z = cfg.getDouble(p + ".z");
        float yaw = (float) cfg.getDouble(p + ".yaw", 0), pitch = (float) cfg.getDouble(p + ".pitch", 0);
        return Optional.of(new Location(w, x, y, z, yaw, pitch));
    }

    /* ---------- LISTS FOR TAB ---------- */

    public Set<String> lands() {
        return keys("places");
    }

    public Set<String> cities(String land) {
        return keys("places." + land);
    }

    public Set<String> streets(String land, String city) {
        return keys("places." + land + "." + city + ".streets");
    }

    public Set<String> numbers(String land, String city, String street) {
        return keys("places." + land + "." + city + ".streets." + street + ".numbers");
    }

    /* ---------- helpers ---------- */

    private void ensure(String path) {
        if (cfg.getConfigurationSection(path) == null)
            cfg.createSection(path);
    }

    private void setAnchor(String base, Location loc) {
        cfg.set(base + "._anchor.world", loc.getWorld().getName());
        cfg.set(base + "._anchor.x", loc.getX());
        cfg.set(base + "._anchor.y", loc.getY());
        cfg.set(base + "._anchor.z", loc.getZ());
        cfg.set(base + "._anchor.yaw", loc.getYaw());
        cfg.set(base + "._anchor.pitch", loc.getPitch());
    }

    private Optional<Location> getAnchor(String base) {
        String wName = cfg.getString(base + "._anchor.world");
        if (wName == null)
            return Optional.empty();
        World w = Bukkit.getWorld(wName);
        if (w == null)
            return Optional.empty();
        double x = cfg.getDouble(base + "._anchor.x"),
                y = cfg.getDouble(base + "._anchor.y"),
                z = cfg.getDouble(base + "._anchor.z");
        float yaw = (float) cfg.getDouble(base + "._anchor.yaw", 0),
                pitch = (float) cfg.getDouble(base + "._anchor.pitch", 0);
        return Optional.of(new Location(w, x, y, z, yaw, pitch));
    }

    private Set<String> keys(String path) {
        ConfigurationSection s = cfg.getConfigurationSection(path);
        if (s == null)
            return Collections.emptySet();
        // Filter interne Keys (z.B. "_anchor", "streets", "numbers")
        Set<String> out = new HashSet<>(s.getKeys(false));
        out.remove("_anchor");
        return out;
    }
}
