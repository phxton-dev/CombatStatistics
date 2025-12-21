package Lounmala.damagestatisticsmod.client;

import Lounmala.damagestatisticsmod.CombatStats;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class DamageStatisticsClient implements ClientModInitializer {

    private static KeyBinding configKey;
    private float lastHealth = 20.0f;
    private PlayerEntity lastPlayerInstance = null; // To detect entity swaps (Respawn)

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.damagestatisticsmod.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.damagestatisticsmod.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.setScreen(new HudConfigScreen());
            }

            if (client.player != null) {
                float currentHealth = client.player.getHealth() + client.player.getAbsorptionAmount();
                float maxHealth = client.player.getMaxHealth();

                // 1. DETECT ENTITY SWAP (Server Respawned You)
                if (lastPlayerInstance != client.player) {
                    CombatStats.resetStats();
                    lastPlayerInstance = client.player;
                    lastHealth = currentHealth;
                    return;
                }

                // 2. DETECT INSTANT RESPAWN (Low Health -> Full Health Spike)
                // If we were below 4 health (2 hearts) and suddenly are at Max Health
                if (lastHealth < 4.0f && currentHealth >= maxHealth) {
                    CombatStats.resetStats();
                    lastHealth = currentHealth;
                    return;
                }

                // 3. STANDARD DEATH (Health hits 0)
                if (currentHealth <= 0.0f || client.player.isDead()) {
                    CombatStats.resetStats();
                    lastHealth = maxHealth;
                    return;
                }

                // 4. DAMAGE CALCULATION
                // Only count damage if we didn't just heal/respawn
                if (currentHealth < lastHealth) {
                    float damageTaken = lastHealth - currentHealth;
                    Entity attacker = client.player.getAttacker();
                    CombatStats.onIncomingDamage(damageTaken, attacker);
                }

                // Update tracker
                lastHealth = currentHealth;
            }
        });

        HudRenderCallback.EVENT.register(new PvPHud());

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && !player.isSpectator()) {
                CombatStats.onAttack(player, entity);
            }
            return ActionResult.PASS;
        });
    }
}