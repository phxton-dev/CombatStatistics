package Lounmala.damagestatisticsmod.client;

import Lounmala.damagestatisticsmod.CombatStats;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class PvPHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (!ModConfig.isVisible) return;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)ModConfig.posX, (float)ModConfig.posY);
        context.getMatrices().scale(ModConfig.scale, ModConfig.scale);

        int white = 0xFFFFFFFF;

        context.drawTextWithShadow(client.textRenderer, "§b§l» PVP STATS", 0, 0, 0xFF55FFFF);
        context.drawTextWithShadow(client.textRenderer, "Sprint: " + CombatStats.sprintHits, 0, 12, white);
        context.drawTextWithShadow(client.textRenderer, "Crits:  " + CombatStats.critHits, 0, 22, white);
        context.drawTextWithShadow(client.textRenderer, "Sweep:  " + CombatStats.sweepHits, 0, 32, white);
        context.drawTextWithShadow(client.textRenderer, String.format("Dmg:    %.1f", CombatStats.totalDmg), 0, 42, white);

        // Updated "Taken" Line
        context.drawTextWithShadow(client.textRenderer, String.format("Taken:  %.1f", CombatStats.incomingDamage), 0, 52, 0xFFFF5555);

        context.getMatrices().popMatrix();
    }
}