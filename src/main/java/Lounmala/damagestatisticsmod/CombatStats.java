package Lounmala.damagestatisticsmod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public class CombatStats {
    public static float totalDmg = 0;
    public static float incomingDamage = 0;
    public static int critHits = 0;
    public static int sprintHits = 0;
    public static int sweepHits = 0;

    private static int lastAttackerId = -1;

    public static void onAttack(PlayerEntity player, Entity target) {
        if (target instanceof LivingEntity livingTarget) {

            // 1. GATHER DATA
            boolean isSprint = player.isSprinting();
            boolean isGround = player.isOnGround();
            float fallDist = (float) player.fallDistance;
            float cooldown = player.getAttackCooldownProgress(0.5f);
            boolean isCrit = fallDist > 0.0f && !isGround && !player.isClimbing() && !player.isTouchingWater();
            boolean isSweep = cooldown > 0.9f && isGround && !isSprint && !isCrit;

            // 2. BASE DAMAGE
            float baseDamage = getManualWeaponDamage(player.getMainHandStack(), fallDist);
            float enchantDamage = 0.0f;
            try {
                RegistryWrapper.Impl<Enchantment> reg = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                RegistryEntry<Enchantment> sharpEntry = reg.getOrThrow(Enchantments.SHARPNESS);
                int sharpLevel = EnchantmentHelper.getLevel(sharpEntry, player.getMainHandStack());
                if (sharpLevel > 0) enchantDamage = 0.5f * sharpLevel + 0.5f;
            } catch (Exception ignored) {}

            // 3. MULTIPLIERS
            float cooldownMod = 0.2f + cooldown * cooldown * 0.8f;
            float rawTotal = (baseDamage * cooldownMod) + (enchantDamage * cooldownMod);
            if (isCrit) rawTotal = (baseDamage * cooldownMod * 1.5f) + (enchantDamage * cooldownMod);

            // 4. ARMOR
            double armorPoints = livingTarget.getAttributeValue(EntityAttributes.ARMOR);
            float armorReduction = (float) (armorPoints * 0.04);
            if (armorReduction > 0.8f) armorReduction = 0.8f;
            float damageAfterArmor = rawTotal * (1.0f - armorReduction);

            // 5. PROTECTION
            int totalProtLevel = 0;
            try {
                RegistryWrapper.Impl<Enchantment> reg = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                RegistryEntry<Enchantment> protEntry = reg.getOrThrow(Enchantments.PROTECTION);

                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                for (EquipmentSlot slot : slots) {
                    ItemStack armorStack = livingTarget.getEquippedStack(slot);
                    totalProtLevel += EnchantmentHelper.getLevel(protEntry, armorStack);
                }
            } catch (Exception ignored) {}

            if (totalProtLevel > 20) totalProtLevel = 20;
            float protReduction = totalProtLevel * 0.04f;

            float finalDamage = damageAfterArmor * (1.0f - protReduction);

            // 6. UPDATE
            if (isCrit) critHits++;
            else if (isSprint) sprintHits++;
            else if (isSweep) sweepHits++;

            totalDmg += finalDamage;
        }
    }

    public static void onIncomingDamage(float amount, Entity attacker) {
        // If attacker is null (e.g. fall damage), ID is -1.
        int currentAttackerId = (attacker != null) ? attacker.getId() : -1;

        // Reset if the ID changed (e.g. Player A -> Player B, or Player A -> Fall Damage)
        if (currentAttackerId != lastAttackerId) {
            incomingDamage = 0;
            lastAttackerId = currentAttackerId;
        }

        incomingDamage += amount;
    }

    public static void resetStats() {
        totalDmg = 0;
        incomingDamage = 0;
        critHits = 0;
        sprintHits = 0;
        sweepHits = 0;
        lastAttackerId = -1;
    }

    private static float getManualWeaponDamage(ItemStack stack, float fallDistance) {
        if (stack.isEmpty()) return 1.0f;
        Item item = stack.getItem();

        if (item == Items.NETHERITE_SWORD) return 8.0f;
        if (item == Items.DIAMOND_SWORD) return 7.0f;
        if (item == Items.IRON_SWORD) return 6.0f;
        if (item == Items.STONE_SWORD) return 5.0f;
        if (item == Items.GOLDEN_SWORD) return 4.0f;
        if (item == Items.WOODEN_SWORD) return 4.0f;

        if (item == Items.NETHERITE_AXE) return 10.0f;
        if (item == Items.DIAMOND_AXE) return 9.0f;
        if (item == Items.IRON_AXE) return 9.0f;
        if (item == Items.STONE_AXE) return 9.0f;
        if (item == Items.GOLDEN_AXE) return 7.0f;
        if (item == Items.WOODEN_AXE) return 7.0f;

        if (item instanceof MaceItem) {
            float maceDmg = 6.0f;
            if (fallDistance > 1.5f) maceDmg += (fallDistance - 1.5f) * 1.5f;
            return maceDmg;
        }

        if (item == Items.DIAMOND_PICKAXE) return 5.0f;
        if (item == Items.IRON_PICKAXE) return 4.0f;
        if (item == Items.DIAMOND_SHOVEL) return 5.5f;

        return 1.0f;
    }
}