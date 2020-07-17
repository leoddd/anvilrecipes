package leod.anvilrecipes.recipe.singlesmithing;

import leod.anvilrecipes.recipe.AbstractAnvilRecipe;
import leod.anvilrecipes.recipe.interfaces.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;

import javax.annotation.Nonnull;

public class SingleSmithingRecipe extends AbstractAnvilRecipe
        implements IAnvilRepairDurability, IAnvilRepairCost, IAnvilEnchantments, IAnvilNameable, IAnvilCanOverrideContainers {
    protected final ItemStack leftContainer;
    protected final float repairAmount;
    protected final boolean keepEnchantments;
    protected final EnchantmentMap newEnchantments;
    protected final RepairAction repairAction;

    public SingleSmithingRecipe(ResourceLocation id, Ingredient left, ItemStack out,
                                int leftAmount, int xpCost, ItemStack leftContainer,
                                float repairAmount, RepairAction repairAction,
                                boolean keepEnchantments, EnchantmentMap newEnchantments) {
        super(id, left, Ingredient.fromItems(() -> Items.AIR), out, leftAmount, 1, xpCost);

        this.leftContainer = leftContainer;
        this.repairAmount = repairAmount;
        this.repairAction = repairAction;
        this.keepEnchantments = keepEnchantments;
        this.newEnchantments = newEnchantments;
    }

    /**
     * Used to check if the recipe matches the given input items.
     */
    @Override
    public boolean matchesItemStacks(ItemStack left, ItemStack right) {
        return this.left.test(left) && (right.isEmpty() || this.right.test(right)) && left.getCount() >= getLeftAmount();
    }

    /**
     * Called when an event matches the recipe. Set outputs and costs here.
     *
     * @param event The event that matched this recipe.
     */
    public void onMatch(@Nonnull AnvilUpdateEvent event) {
        ItemStack leftStack = event.getLeft();
        ItemStack output = this.getRecipeOutput();

        repairDurability(leftStack, output);
        applyRepairAction(leftStack, output);
        applyEnchantments(leftStack, output);

        int finalXpCost = this.getXpCost() + leftStack.getRepairCost();
        if (renameStack(leftStack, output, event.getName())) ++finalXpCost;

        if (!output.equals(leftStack, false)) {
            event.setOutput(output);
            event.setCost(finalXpCost);
            event.setMaterialCost(getRightAmount());
        }
    }

    @Override
    public ItemStack getLeftContainerOverride() {
        return this.leftContainer;
    }

    @Override
    public ItemStack getRightContainerOverride() {
        return DEFAULT_INGREDIENT_CONTAINER;
    }

    @Override
    public float getRepairAmount() {
        return this.repairAmount;
    }

    @Override
    public boolean getKeepEnchantments() {
        return this.keepEnchantments;
    }

    @Override
    public EnchantmentMap getNewEnchantments() {
        return this.newEnchantments;
    }

    @Override
    public RepairAction getRepairAction() {
        return this.repairAction;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SingleSmithing.SERIALIZER;
    }
}

