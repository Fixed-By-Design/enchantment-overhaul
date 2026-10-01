package com.akitain.enchantmentoverhaul.enchant;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class HumanoidArmor {

    public static final List<EquipmentSlot> SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private HumanoidArmor() {}

    public static int count(LivingEntity entity, Predicate<ItemStack> predicate) {
        int count = 0;
        for (EquipmentSlot slot : SLOTS) {
            if (predicate.test(entity.getItemBySlot(slot))) count++;
        }
        return count;
    }

    public static int sum(LivingEntity entity, ToIntFunction<ItemStack> value) {
        int sum = 0;
        for (EquipmentSlot slot : SLOTS) {
            sum += value.applyAsInt(entity.getItemBySlot(slot));
        }
        return sum;
    }
}
