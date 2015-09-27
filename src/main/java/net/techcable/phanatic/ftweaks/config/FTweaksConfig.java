package net.techcable.phanatic.ftweaks.config;

import java.io.File;
import java.io.IOException;

import net.techcable.phanatic.ftweaks.FTweaks;

import lombok.*;
import org.bukkit.configuration.InvalidConfigurationException;

@Getter
public class FTweaksConfig extends AnnotationConfig {

    // PvP Settings

    @Setting("pvp.protectLoot")
    private boolean protectLoot;

    @Setting("pvp.protectLootTime")
    private int protectLootTime;

    //
    // Utilities
    //

    public void load(FTweaks plugin) throws IOException, InvalidConfigurationException {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        super.load(configFile, "config.yml");
    }

}
