package leod.anvilrecipes.recipe.interfaces;

import net.minecraft.item.ItemStack;

public interface IAnvilCanOverrideContainers {
    ItemStack DEFAULT_INGREDIENT_CONTAINER = ItemStack.EMPTY;

    default boolean hasLeftContainerOverride() {
        return !this.getLeftContainerOverride().isEmpty();
    }

    default boolean hasRightContainerOverride() {
        return !this.getRightContainerOverride().isEmpty();
    }

    ItemStack getLeftContainerOverride();

    ItemStack getRightContainerOverride();
}
