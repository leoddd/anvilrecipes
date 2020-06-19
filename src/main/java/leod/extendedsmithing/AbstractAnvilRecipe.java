package leod.extendedsmithing;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Implements a constructor, most getters and a Serializer base for IAnvilRecipes.
 */
public abstract class AbstractAnvilRecipe implements IAnvilRecipe {
    protected final ResourceLocation id;
    protected final Ingredient left;
    protected final Ingredient right;
    protected final ItemStack out;
    protected final int leftAmount;
    protected final int rightAmount;
    protected final int xpCost;
    protected final boolean refreshDurability;
    protected final double repairAmount;

    public AbstractAnvilRecipe(ResourceLocation id, Ingredient left, Ingredient right, ItemStack out,
                               int leftAmount, int rightAmount, int xpCost, boolean refreshDurability, double repairAmount) {
        this.id = id;

        this.left = left;
        this.right = right;
        this.out = out;
        this.leftAmount = leftAmount;
        this.rightAmount = rightAmount;
        this.xpCost = xpCost;
        this.refreshDurability = refreshDurability;
        this.repairAmount = repairAmount;
    }

    @Nonnull
    public ItemStack getCraftingResult() {
        return this.out.copy();
    }

    public Ingredient getLeft() {
        return this.left;
    }

    public Ingredient getRight() {
        return this.right;
    }

    public int getLeftAmount() {
        return this.leftAmount;
    }

    public int getRightAmount() {
        return this.rightAmount;
    }

    public int getXpCost() {
        return this.xpCost;
    }

    public boolean getRefreshDurability() {
        return this.refreshDurability;
    }

    public double getRepairAmount() {
        return this.repairAmount;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }


    @Override
    public String toString() {
        return getClass().getName() + "~" + this.out.getItem().getRegistryName() + "@" + Integer.toHexString(hashCode());
    }

    @Nonnull
    public abstract IRecipeSerializer<?> getSerializer();
}