package Lounmala.damagestatisticsmod;

import Lounmala.damagestatisticsmod.client.PvPHud;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Damagestatisticsmod implements ModInitializer {
    public static final String MOD_ID = "damagestatisticsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Damage Statistics Mod Initialized!");

        // This line registers your HUD so Minecraft knows to draw it!
        HudRenderCallback.EVENT.register(new PvPHud());
    }
}