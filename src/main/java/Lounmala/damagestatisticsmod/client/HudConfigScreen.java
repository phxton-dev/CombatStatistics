package Lounmala.damagestatisticsmod.client;

import Lounmala.damagestatisticsmod.CombatStats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HudConfigScreen extends Screen {

    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudConfigScreen() {
        super(Text.of("HUD Config"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 120;
        int buttonHeight = 20;
        int x = width / 2 - buttonWidth / 2;
        int y = height - 50;

        // 1. RESET STATS BUTTON
        ButtonWidget resetButton = ButtonWidget.builder(
                Text.of("§6Reset Stats"),
                button -> {
                    CombatStats.resetStats();
                    button.setMessage(Text.of("§aStats Cleared!"));
                }
        ).dimensions(x, y - 25, buttonWidth, buttonHeight).build();
        this.addDrawableChild(resetButton);

        // 2. TOGGLE VISIBILITY BUTTON
        ButtonWidget toggleButton = ButtonWidget.builder(
                Text.of(ModConfig.isVisible ? "§aHUD: ON" : "§cHUD: OFF"),
                button -> {
                    ModConfig.isVisible = !ModConfig.isVisible;
                    button.setMessage(Text.of(ModConfig.isVisible ? "§aHUD: ON" : "§cHUD: OFF"));
                }
        ).dimensions(x, y, buttonWidth, buttonHeight).build();
        this.addDrawableChild(toggleButton);
    }

    // SAVE ON CLOSE
    @Override
    public void close() {
        ModConfig.save(); // Save to file immediately
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x80000000);
        context.drawCenteredTextWithShadow(textRenderer, "§aEdit Mode", width / 2, 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "§7Drag to Move | Scroll to Scale", width / 2, 35, 0xFFFFFFFF);

        if (ModConfig.isVisible) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate((float)ModConfig.posX, (float)ModConfig.posY);
            context.getMatrices().scale(ModConfig.scale, ModConfig.scale);

            int white = 0xFFFFFFFF;
            context.drawTextWithShadow(textRenderer, "§b§l» PVP STATS", 0, 0, 0xFF55FFFF);
            context.drawTextWithShadow(textRenderer, "Sprint: " + CombatStats.sprintHits, 0, 12, white);
            context.drawTextWithShadow(textRenderer, "Crits:  " + CombatStats.critHits, 0, 22, white);
            context.drawTextWithShadow(textRenderer, "Sweep:  " + CombatStats.sweepHits, 0, 32, white);
            context.drawTextWithShadow(textRenderer, String.format("Dmg:    %.1f", CombatStats.totalDmg), 0, 42, white);
            context.drawTextWithShadow(textRenderer, String.format("Taken:  %.1f", CombatStats.incomingDamage), 0, 52, 0xFFFF5555);

            context.getMatrices().popMatrix();
        } else {
            context.drawCenteredTextWithShadow(textRenderer, "§c(HUD Hidden)", width / 2, height / 2, 0xFFFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0) {
            isDragging = true;
            dragOffsetX = (int)mouseX - ModConfig.posX;
            dragOffsetY = (int)mouseY - ModConfig.posY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            ModConfig.posX = (int)mouseX - dragOffsetX;
            ModConfig.posY = (int)mouseY - dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) ModConfig.scale += 0.1f;
        else ModConfig.scale -= 0.1f;
        if (ModConfig.scale < 0.5f) ModConfig.scale = 0.5f;
        if (ModConfig.scale > 3.0f) ModConfig.scale = 3.0f;
        return true;
    }
}