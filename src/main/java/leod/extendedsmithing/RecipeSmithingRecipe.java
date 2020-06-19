package leod.extendedsmithing;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;

import javax.annotation.Nonnull;

public class RecipeSmithingRecipe extends AbstractAnvilRecipe {
    public static RecipeSmithingSerializer SERIALIZER = new RecipeSmithingSerializer(new ResourceLocation(ExtendedSmithing.MODID, "recipesmithing"));

    public RecipeSmithingRecipe(ResourceLocation id, Ingredient left, Ingredient right, ItemStack out,
                                int leftAmount, int rightAmount, int xpCost, boolean refreshDurability, double repairAmount) {
        super(id, left, right, out, leftAmount, rightAmount, xpCost, refreshDurability, repairAmount);
    }

    /**
     * Used to check if the recipe matches the given input items.
     */
    @Override
    public boolean matchesItemStacks(ItemStack left, ItemStack right) {
        return (this.left.test(left) && this.right.test(right)) && (left.getCount() >= this.getLeftAmount() && right.getCount() >= this.getRightAmount());
    }

    /**
     * Called when an event matches the recipe. Set outputs and costs here.
     *
     * @param event The event that matched this recipe.
     */
    public void onMatch(AnvilUpdateEvent event) {
        ItemStack leftItem = event.getLeft();
        ItemStack output = getCraftingResult();
        if (!getRefreshDurability() && leftItem.isDamageable() && output.isDamageable()) {
            //  Copy the damaging ratio from the left item to the right one if both can be damaged.
            int outDamage = (int) Math.round(output.getMaxDamage() * ((double) leftItem.getMaxDamage() / leftItem.getDamage()) + getRepairAmount());
            if (outDamage >= output.getMaxDamage()) --outDamage;
            output.setDamage(outDamage);
        }

        // Set all the output variables.
        event.setOutput(output);
        event.setCost(getXpCost());
        event.setMaterialCost(getRightAmount());
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

