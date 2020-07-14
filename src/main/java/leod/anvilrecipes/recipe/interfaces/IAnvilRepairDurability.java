package leod.anvilrecipes.recipe.interfaces;

import net.minecraft.item.ItemStack;

public interface IAnvilRepairDurability {
    double DEFAULT_REPAIR_AMOUNT = 0d;

    /**
     * Copy durability from originStack to targetStack but multiply it by getRepairAmount().
     */
    default void repairDurability(ItemStack originStack, ItemStack targetStack) {
        if (originStack.isDamageable() && targetStack.isDamageable()) {
            applyDurationModifier(targetStack, getDamageRatio(originStack) - getRepairAmount());
        }
    }

    static float getDamageRatio(ItemStack stack) {
        return (1 / ((float) stack.getMaxDamage() / stack.getDamage()));
    }

    static void applyDurationModifier(ItemStack stack, float damageRatio) {
        int outMaxDamage = stack.getMaxDamage();
        stack.setDamage(Math.min((int) Math.ceil(outMaxDamage * damageRatio), outMaxDamage - 1));
    }

    float getRepairAmount();
}
