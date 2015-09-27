package net.techcable.phanatic.ftweaks;

import java.io.IOException;
import java.util.logging.Level;

import net.techcable.phanatic.ftweaks.config.FTweaksConfig;
import net.techcable.phanatic.ftweaks.listeners.LootProtectionListener;

import lombok.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

public class FTweaks extends JavaPlugin {
    @Getter
    private FTweaksConfig settings;

    @Override
    public void onEnable() {
        try {
            settings = new FTweaksConfig();
            settings.load(this);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Could not load config", e);
            getLogger().severe("Shutting down");
            setEnabled(false);
            return;
        }
        getServer().getPluginManager().registerEvents(new LootProtectionListener(this), this);
    }

}
