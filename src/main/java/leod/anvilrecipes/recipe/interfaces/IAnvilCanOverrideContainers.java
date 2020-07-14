package leod.anvilrecipes.recipe.interfaces;

import net.minecraft.item.ItemStack;

public interface IAnvilCanOverrideContainers {
   ItemStack DEFAULT_INGREDIENT_CONTAINER = null;

   default boolean hasLeftContainerOverride() {
      return this.getLeftContainerOverride() != null;
   }
   default boolean hasRightContainerOverride() {
      return this.getRightContainerOverride() != null;
   }

   ItemStack getLeftContainerOverride();
   ItemStack getRightContainerOverride();
}
