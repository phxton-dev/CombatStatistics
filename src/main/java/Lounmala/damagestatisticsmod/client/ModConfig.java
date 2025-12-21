package Lounmala.damagestatisticsmod.client;

import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

public class ModConfig {
    // Settings
    public static int posX = 10;
    public static int posY = 100;
    public static float scale = 1.0f;
    public static boolean isVisible = true;

    // File Path: config/damagestatisticsmod.properties
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("damagestatisticsmod.properties");

    public static void save() {
        Properties props = new Properties();
        props.setProperty("posX", String.valueOf(posX));
        props.setProperty("posY", String.valueOf(posY));
        props.setProperty("scale", String.valueOf(scale));
        props.setProperty("isVisible", String.valueOf(isVisible));

        try (OutputStream out = new FileOutputStream(CONFIG_PATH.toFile())) {
            props.store(out, "Damage Statistics Mod Config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_PATH.toFile().exists()) return; // Keep defaults if no file

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(CONFIG_PATH.toFile())) {
            props.load(in);

            // Read values safely (fallback to default if missing)
            posX = Integer.parseInt(props.getProperty("posX", "10"));
            posY = Integer.parseInt(props.getProperty("posY", "100"));
            scale = Float.parseFloat(props.getProperty("scale", "1.0"));
            isVisible = Boolean.parseBoolean(props.getProperty("isVisible", "true"));

        } catch (Exception e) {
            e.printStackTrace();
            // If file is corrupted, defaults are used automatically
        }
    }
}