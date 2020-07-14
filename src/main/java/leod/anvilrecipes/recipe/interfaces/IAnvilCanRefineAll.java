package leod.anvilrecipes.recipe.interfaces;

import net.minecraft.item.ItemStack;

public interface IAnvilCanRefineAll {
    boolean DEFAULT_REFINE_ALL = false;

    default int applyRefineAll(ItemStack originStack, ItemStack targetStack) {
        if (getRefineAll()) {
            targetStack.setCount(Math.min(originStack.getCount(), targetStack.getMaxStackSize()));
        }

        return targetStack.getCount();
    }

    boolean getRefineAll();
}
