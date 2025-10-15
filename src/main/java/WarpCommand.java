import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final GeoWarp plugin;
    private final WarpStore store;

    public WarpCommand(GeoWarp plugin, WarpStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (a.length == 0) {
            usage(sender, label);
            return true;
        }

        // --- ADD (Anker immer an Spielerposition) ---
        if (a[0].equalsIgnoreCase("add")) {
            if (!sender.hasPermission("warp.admin")) {
                sender.sendMessage("§cKeine Berechtigung.");
                return true;
            }
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§cNur In-Game möglich.");
                return true;
            }

            switch (a.length) {
                case 2 -> { // Land
                    store.putCountryAnchor(a[1], p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage("§aLand-Anchor gespeichert: §b" + a[1]);
                }
                case 3 -> { // Land Stadt
                    store.putCityAnchor(a[1], a[2], p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage("§aStadt-Anchor gespeichert: §b" + a[1] + " / " + a[2]);
                }
                case 4 -> { // Land Stadt Straße
                    store.putStreetAnchor(a[1], a[2], a[3], p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage("§aStraßen-Anchor gespeichert: §b" + a[1] + " / " + a[2] + " / " + a[3]);
                }
                case 5 -> { // Land Stadt Straße Hausnr
                    store.putAddress(a[1], a[2], a[3], a[4], "", p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage("§aAdresse gespeichert: §b" + a[1] + " / " + a[2] + " / " + a[3] + " " + a[4]);
                }
                default -> sender.sendMessage("§e/warp add <Land> [Stadt] [Straße] [Hausnr]");
            }
            return true;
        }

        // --- TELEPORT ---
        if (!sender.hasPermission("warp.use")) {
            sender.sendMessage("§cKeine Berechtigung.");
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cNur In-Game nutzbar.");
            return true;
        }

        switch (a.length) {
            case 1 -> { // Land
                var loc = store.getCountryAnchor(a[0]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cLand nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0]);
            }
            case 2 -> { // Land Stadt
                var loc = store.getCityAnchor(a[0], a[1]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cStadt nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0] + " / " + a[1]);
            }
            case 3 -> { // Land Stadt Straße
                var loc = store.getStreetAnchor(a[0], a[1], a[2]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cStraße nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0] + " / " + a[1] + " / " + a[2]);
            }
            default -> { // >=4 → Adresse (nimmt die ersten 4)
                if (a.length < 4) {
                    usage(sender, label);
                    return true;
                }
                var loc = store.getAddress(a[0], a[1], a[2], a[3]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cAdresse nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0] + " / " + a[1] + " / " + a[2] + " " + a[3]);
            }
        }
        return true;
    }

    private void usage(CommandSender s, String label) {
        s.sendMessage("§e/" + label + " <Land> [Stadt] [Straße] [Hausnr]");
        s.sendMessage("§e/" + label + " add <Land> [Stadt] [Straße] [Hausnr]");
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String alias, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            out.add("add");
            out.addAll(store.lands());
        } else if (a.length == 2) {
            if (!a[0].equalsIgnoreCase("add"))
                out.addAll(store.cities(a[0]));
            else
                out.addAll(store.lands());
        } else if (a.length == 3) {
            if (!a[0].equalsIgnoreCase("add"))
                out.addAll(store.streets(a[0], a[1]));
            else
                out.addAll(store.cities(a[1]));
        } else if (a.length == 4) {
            if (!a[0].equalsIgnoreCase("add"))
                out.addAll(store.numbers(a[0], a[1], a[2]));
            else
                out.addAll(store.streets(a[1], a[2]));
        } else if (a.length == 5 && !a[0].equalsIgnoreCase("add")) {
            out.addAll(store.numbers(a[0], a[1], a[2]));
        }
        String last = a[a.length - 1].toLowerCase();
        return out.stream().filter(sug -> sug.toLowerCase().startsWith(last)).sorted().collect(Collectors.toList());
    }
}
