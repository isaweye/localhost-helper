package uk.mqchinee.localhosthelper;

import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import uk.mqchinee.localhosthelper.utils.PluginUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class LocalhostHelper extends JavaPlugin implements CommandExecutor {
    private Map<String, File> map;
    private PluginUtils pluginUtils;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.pluginUtils = new PluginUtils();

        map = new HashMap<>();
        for (String string: getConfig().getStringList("reload")) {
            String[] s = string.split(" ");
            getLogger().info(Arrays.toString(s));
            File a = new File(s[1], s[2]);
            map.put(s[0], a);
        }

        getCommand("copy").setExecutor(this);

    }

    private void disableAll() {
        map.forEach(((pl, file) -> pluginUtils.disable(pl)));
    }

    private void reloadAll() {
        map.forEach(((pl, file) -> pluginUtils.reload(pl)));
    }

    private String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private void local(CommandSender sender) {
        long current = System.currentTimeMillis();
        disableAll();

        sender.sendMessage(colorize("&oCopying files..."));
        map.forEach(((pl, file) -> {

            File dst = new File(getConfig().getString("destination"), file.getName());

            try {
                Files.copy(file.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sender.sendMessage(colorize("&oSuccessfully : "+file.getName()));
            } catch (IOException e) {
                sender.sendMessage(colorize("&oError : "+file.getName()));
                e.printStackTrace();
            }

        }));


        reloadAll();
        sender.sendMessage(colorize(String.format("&oDone! (%s ms)", System.currentTimeMillis() - current)));
    }

    private void sync(Runnable r) {
        Bukkit.getScheduler().runTask(this, r);
    }

    private void ftp(CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                sync(() -> disableAll());

                sender.sendMessage(colorize("&oConnecting..."));
                FTPClient client;
                try {
                    client = new FTPClient();
                    client.connect(getConfig().getString("ftp.server"), getConfig().getInt("ftp.port"));
                    client.login(getConfig().getString("ftp.user"), getConfig().getString("ftp.password"));
                    client.enterLocalPassiveMode();

                    client.setFileType(FTPClient.BINARY_FILE_TYPE);
                    sender.sendMessage(colorize("&oSuccessfully connected to the FTP server."));
                } catch (IOException e) {
                    sender.sendMessage(colorize("&oUnable to connect to FTP server."));
                    e.printStackTrace();
                    return;
                }
                disableAll();

                sender.sendMessage(colorize("&oCopying files..."));
                map.forEach(((pl, file) -> {

                    File dst = new File(getConfig().getString("destination"), file.getName());
                    try {
                        client.deleteFile(dst.toURI().getPath());
                    } catch (Exception ignored) {}

                    try {
                        InputStream inputStream = Files.newInputStream(file.toPath());
                        if (client.storeFile(dst.getPath(), inputStream)) {
                            sender.sendMessage(colorize("&oSuccessfully : "+file.getName()));
                        }
                    } catch (IOException e) {
                        sender.sendMessage(colorize("&oError : "+file.getName()));
                        e.printStackTrace();
                    }
                }));

                try {
                    if (client.isConnected()) {
                        client.logout();
                        client.disconnect();
                        sender.sendMessage(colorize("&oSuccessfully disconnected..."));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sync(() -> reloadAll());
                sender.sendMessage(colorize(String.format("&oDone! (%s ms)", System.currentTimeMillis() - current)));
            }
        }.runTaskAsynchronously(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (!getConfig().getBoolean("ftp.client")) { local(sender); }
        else {
            ftp(sender);
        }
        return true;
    }


}
