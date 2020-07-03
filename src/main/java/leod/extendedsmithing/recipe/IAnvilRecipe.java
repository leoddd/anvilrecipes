package leod.extendedsmithing.recipe;

import leod.extendedsmithing.ExtendedSmithing;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;

import javax.annotation.Nonnull;

public interface IAnvilRecipe extends IRecipe<IInventory> {
    IRecipeType<IRecipe<?>> RECIPETYPE = new IRecipeType<IRecipe<?>>() {
        @Override
        public String toString() {
            return ExtendedSmithing.MODID + "smithing";
        }
    };

    boolean matchesItemStacks(ItemStack left, ItemStack right);

    void onMatch(AnvilUpdateEvent event);

    @Nonnull
    ItemStack getCraftingResult();

    Ingredient getLeft();

    Ingredient getRight();

    int getLeftAmount();

    int getRightAmount();

    int getXpCost();

    double getRepairAmount();

    @Nonnull
    ResourceLocation getId();

    @Nonnull
    IRecipeSerializer<?> getSerializer();

    @Nonnull
    default IRecipeType<?> getType() {
        return RECIPETYPE;
    }

    @Nonnull
    default String getGroup() {
        return "anvil";
    }

    @Nonnull
    default ItemStack getIcon() {
        return new ItemStack(Blocks.ANVIL);
    }

    @Nonnull
    @Override
    default ItemStack getCraftingResult(@Nonnull IInventory inv) {
        return getCraftingResult();
    }

    @Override
    default boolean matches(@Nonnull IInventory inv, @Nonnull World worldIn) {
        return false;
    }

    @Override
    default boolean canFit(int width, int height) {
        return false;
    }

    @Nonnull
    @Override
    default ItemStack getRecipeOutput() {
        return getCraftingResult();
    }
}
