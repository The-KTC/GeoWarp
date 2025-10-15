import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.ChatColor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

        // ---- Subcommands zuerst behandeln ----
        String sub = a[0].toLowerCase(Locale.ROOT);

        /* ---------- EXPORT (Backup) ---------- */
        if (sub.equals("export")) {
            if (!sender.hasPermission("warp.admin")) {
                sender.sendMessage(ChatColor.RED + "Keine Berechtigung.");
                return true;
            }
            try {
                var file = plugin.backupWarps(); // legt Kopie unter plugins/GeoWarp/backups/ an
                sender.sendMessage(ChatColor.GREEN + "Export erstellt: " + ChatColor.AQUA + "plugins/GeoWarp/backups/"
                        + file.getName());
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Export fehlgeschlagen: " + e.getMessage());
            }
            return true;
        }

        /* ---------- LIST ---------- */
        if (sub.equals("list")) {
            switch (a.length) {
                case 1 -> { // Länder
                    var items = store.lands().stream()
                            .sorted()
                            .map(l -> (store.hasCountryAnchor(l) ? "§e★ §f" : "§7• §f") + l)
                            .toList();
                    sendList(sender, "Länder", items);
                }
                case 2 -> { // Städte
                    var land = a[1];
                    var items = store.cities(land).stream()
                            .sorted()
                            .map(s -> (store.hasCityAnchor(land, s) ? "§e★ §f" : "§7• §f") + s)
                            .toList();
                    sendList(sender, "Städte in " + land, items);
                }
                case 3 -> { // Straßen
                    var land = a[1];
                    var stadt = a[2];
                    var items = store.streets(land, stadt).stream()
                            .sorted()
                            .map(st -> (store.hasStreetAnchor(land, stadt, st) ? "§e★ §f" : "§7• §f") + st)
                            .toList();
                    sendList(sender, "Straßen in " + land + " / " + stadt, items);
                }
                default -> { // Hausnummern
                    var land = a[1];
                    var stadt = a[2];
                    var strasse = a[3];
                    var items = store.numbers(land, stadt, strasse).stream()
                            .sorted()
                            .map(n -> "§7• §f" + n)
                            .toList();
                    sendList(sender, "Hausnummern in " + land + " / " + stadt + " / " + strasse, items);
                }
            }
            return true;
        }

        /* ---------- ADD (Anchor/Adresse = Spielerposition) ---------- */
        if (sub.equals("add")) {
            if (!sender.hasPermission("warp.admin")) {
                sender.sendMessage(ChatColor.RED + "Keine Berechtigung.");
                return true;
            }
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Nur In-Game möglich.");
                return true;
            }

            switch (a.length) {
                case 2 -> { // Land
                    String land = a[1];
                    store.putCountryAnchor(land, p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage(ChatColor.GREEN + "Land-Anchor gespeichert: " + ChatColor.AQUA + land);
                    suggestNext(sender, "/" + label + " add " + land + " <Stadt>");
                }
                case 3 -> { // Land Stadt
                    String land = a[1], stadt = a[2];
                    store.putCityAnchor(land, stadt, p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage(
                            ChatColor.GREEN + "Stadt-Anchor gespeichert: " + ChatColor.AQUA + land + " / " + stadt);
                    suggestNext(sender, "/" + label + " add " + land + " " + stadt + " <Straße>");
                }
                case 4 -> { // Land Stadt Straße
                    String land = a[1], stadt = a[2], strasse = a[3];
                    store.putStreetAnchor(land, stadt, strasse, p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage(ChatColor.GREEN + "Straßen-Anchor gespeichert: " + ChatColor.AQUA + land + " / "
                            + stadt + " / " + strasse);
                    suggestNext(sender, "/" + label + " add " + land + " " + stadt + " " + strasse + " <Hausnr>");
                }
                case 5 -> { // Land Stadt Straße Hausnr
                    String land = a[1], stadt = a[2], strasse = a[3], nr = a[4];
                    store.putAddress(land, stadt, strasse, nr, "", p.getLocation());
                    plugin.saveWarps();
                    sender.sendMessage(ChatColor.GREEN + "Adresse gespeichert: " + ChatColor.AQUA + land + " / " + stadt
                            + " / " + strasse + " " + nr);
                    suggestNext(sender, "/" + label + " list " + land + " " + stadt + " " + strasse);
                }
                default -> sender.sendMessage(ChatColor.YELLOW + "/" + label + " add <Land> [Stadt] [Straße] [Hausnr]");
            }
            return true;
        }

        /* ---------- REMOVE ---------- */
        if (sub.equals("remove")) {
            if (!sender.hasPermission("warp.admin")) {
                sender.sendMessage(ChatColor.RED + "Keine Berechtigung.");
                return true;
            }

            switch (a.length) {
                case 2 -> { // Land
                    String land = a[1];
                    boolean ok = store.removeCountryAnchor(land);
                    plugin.saveWarps();
                    sender.sendMessage(ok ? ChatColor.GREEN + "Land-Anchor entfernt: " + ChatColor.AQUA + land
                            : ChatColor.RED + "Land-Anchor nicht gefunden.");
                }
                case 3 -> { // Land Stadt
                    String land = a[1], stadt = a[2];
                    boolean ok = store.removeCityAnchor(land, stadt);
                    plugin.saveWarps();
                    sender.sendMessage(
                            ok ? ChatColor.GREEN + "Stadt-Anchor entfernt: " + ChatColor.AQUA + land + " / " + stadt
                                    : ChatColor.RED + "Stadt-Anchor nicht gefunden.");
                }
                case 4 -> { // Land Stadt Straße
                    String land = a[1], stadt = a[2], str = a[3];
                    boolean ok = store.removeStreetAnchor(land, stadt, str);
                    plugin.saveWarps();
                    sender.sendMessage(ok
                            ? ChatColor.GREEN + "Straßen-Anchor entfernt: " + ChatColor.AQUA + land + " / " + stadt
                                    + " / " + str
                            : ChatColor.RED + "Straßen-Anchor nicht gefunden.");
                }
                case 5 -> { // Land Stadt Straße Hausnr
                    String land = a[1], stadt = a[2], str = a[3], nr = a[4];
                    boolean ok = store.removeAddress(land, stadt, str, nr);
                    plugin.saveWarps();
                    sender.sendMessage(ok
                            ? ChatColor.GREEN + "Adresse entfernt: " + ChatColor.AQUA + land + " / " + stadt + " / "
                                    + str + " " + nr
                            : ChatColor.RED + "Adresse nicht gefunden.");
                }
                default ->
                    sender.sendMessage(ChatColor.YELLOW + "/" + label + " remove <Land> [Stadt] [Straße] [Hausnr]");
            }
            return true;
        }

        /* ---------- TELEPORT ---------- */
        if (!sender.hasPermission("warp.use")) {
            sender.sendMessage("§cKeine Berechtigung.");
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cNur In-Game nutzbar.");
            return true;
        }

        switch (a.length) {
            case 1 -> {
                var loc = store.getCountryAnchor(a[0]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cLand nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0]);
            }
            case 2 -> {
                var loc = store.getCityAnchor(a[0], a[1]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cStadt nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0] + " / " + a[1]);
            }
            case 3 -> {
                var loc = store.getStreetAnchor(a[0], a[1], a[2]);
                if (loc.isEmpty()) {
                    sender.sendMessage("§cStraße nicht gefunden.");
                    return true;
                }
                p.teleport(loc.get());
                sender.sendMessage("§aTeleportiert: §b" + a[0] + " / " + a[1] + " / " + a[2]);
            }
            default -> {
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

    private void sendList(CommandSender s, String title, List<String> items) {
        s.sendMessage("§6" + title + " §7(" + items.size() + "):");
        if (items.isEmpty()) {
            s.sendMessage("§7— leer —");
            return;
        }
        s.sendMessage(String.join("§7, §f", items));
    }

    private void usage(CommandSender s, String label) {
        s.sendMessage("§e/" + label + " <Land> [Stadt] [Straße] [Hausnr]");
        s.sendMessage("§e/" + label + " add <Land> [Stadt] [Straße] [Hausnr]");
        s.sendMessage("§e/" + label + " remove <Land> [Stadt] [Straße] [Hausnr]");
        s.sendMessage("§e/" + label + " list [Land] [Stadt] [Straße]");
        s.sendMessage("§e/" + label + " export");
    }

    private void suggestNext(CommandSender s, String suggest) {
        Component msg = Component.text("Weiter: ", NamedTextColor.GRAY)
                .append(Component.text(suggest, NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.suggestCommand(suggest)));
        s.sendMessage(msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String alias, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            out.add("add");
            out.add("list");
            out.add("remove");
            out.add("export");
            out.addAll(store.lands());
        } else if (a[0].equalsIgnoreCase("list")) {
            if (a.length == 2)
                out.addAll(store.lands());
            else if (a.length == 3)
                out.addAll(store.cities(a[1]));
            else if (a.length == 4)
                out.addAll(store.streets(a[1], a[2]));
            else if (a.length == 5)
                out.addAll(store.numbers(a[1], a[2], a[3]));
        } else if (a[0].equalsIgnoreCase("add") || a[0].equalsIgnoreCase("remove")) {
            if (a.length == 2)
                out.addAll(store.lands());
            else if (a.length == 3)
                out.addAll(store.cities(a[1]));
            else if (a.length == 4)
                out.addAll(store.streets(a[1], a[2]));
            else if (a.length == 5)
                out.addAll(store.numbers(a[1], a[2], a[3]));
        } else {
            // normaler TP
            if (a.length == 2)
                out.addAll(store.cities(a[0]));
            else if (a.length == 3)
                out.addAll(store.streets(a[0], a[1]));
            else if (a.length == 4)
                out.addAll(store.numbers(a[0], a[1], a[2]));
        }
        String last = a[a.length - 1].toLowerCase(Locale.ROOT);
        return out.stream().filter(sug -> sug.toLowerCase(Locale.ROOT).startsWith(last)).sorted()
                .collect(Collectors.toList());
    }
}
