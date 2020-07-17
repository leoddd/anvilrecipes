package leod.anvilrecipes.recipe.additionsmithing;

import leod.anvilrecipes.recipe.AbstractAnvilRecipe;
import leod.anvilrecipes.recipe.interfaces.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;

import javax.annotation.Nonnull;

public class AdditionSmithingRecipe extends AbstractAnvilRecipe
        implements IAnvilRepairDurability, IAnvilRepairCost, IAnvilEnchantments, IAnvilNameable, IAnvilCanRefineAll, IAnvilCanOverrideContainers, IAnvilCanKeepMaterial {
    protected final boolean keepMaterial;
    protected final int materialDamageTicks;
    protected final ItemStack leftContainer;
    protected final ItemStack rightContainer;
    protected final float repairAmount;
    protected final boolean keepEnchantments;
    protected final EnchantmentMap newEnchantments;
    protected final RepairAction repairAction;
    protected final boolean refineAll;

    public AdditionSmithingRecipe(ResourceLocation id, Ingredient left, Ingredient right, ItemStack out,
                                  int leftAmount, int rightAmount, int xpCost, boolean keepMaterial,
                                  int materialDamageTicks, ItemStack leftContainer, ItemStack rightContainer,
                                  float repairAmount, RepairAction repairAction, boolean refineAll,
                                  boolean keepEnchantments, EnchantmentMap newEnchantments) {
        super(id, left, right, out, leftAmount, rightAmount, xpCost);

        this.keepMaterial = keepMaterial;
        this.materialDamageTicks = materialDamageTicks;
        this.leftContainer = leftContainer;
        this.rightContainer = rightContainer;
        this.repairAmount = repairAmount;
        this.repairAction = repairAction;
        this.keepEnchantments = keepEnchantments;
        this.newEnchantments = newEnchantments;
        this.refineAll = refineAll;
    }

    /**
     * Used to check if the recipe matches the given input items.
     */
    @Override
    public boolean matchesItemStacks(ItemStack left, ItemStack right) {
        return (this.left.test(left) && this.right.test(right)) && (left.getCount() >= getLeftAmount() && right.getCount() >= getRightAmount());
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
        applyRefineAll(leftStack, output);

        int finalXpCost = this.getXpCost() + leftStack.getRepairCost();
        if (renameStack(leftStack, output, event.getName())) ++finalXpCost;

        if (!output.equals(leftStack, false)) {
            event.setOutput(output);
            event.setCost(finalXpCost);
            event.setMaterialCost(getRightAmount());
        }
    }

    @Override
    public boolean getKeepMaterial() {
        return this.keepMaterial;
    }

    @Override
    public int getMaterialDamageTicks() {
        return this.materialDamageTicks;
    }

    @Override
    public ItemStack getLeftContainerOverride() {
        return this.leftContainer;
    }

    @Override
    public ItemStack getRightContainerOverride() {
        return this.rightContainer;
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

    @Override
    public boolean getRefineAll() {
        return this.refineAll;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return AdditionSmithing.SERIALIZER;
    }
}

