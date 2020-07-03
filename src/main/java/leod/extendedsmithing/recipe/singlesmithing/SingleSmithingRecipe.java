package leod.extendedsmithing.recipe.singlesmithing;

import leod.extendedsmithing.recipe.AbstractAnvilRecipe;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AnvilUpdateEvent;

import javax.annotation.Nonnull;

public class SingleSmithingRecipe extends AbstractAnvilRecipe {
    protected final double repairAmount;
    protected final boolean keepEnchantments;
    protected final String actionRepairCost;

    public SingleSmithingRecipe(ResourceLocation id, Ingredient left, ItemStack out,
                                int leftAmount, int xpCost,
                                double repairAmount, String actionRepairCost,
                                boolean keepEnchantments) {
        super(id, left, Ingredient.fromItems(() -> SingleSmithing.SINGLE_DUMMY_ITEM), out, leftAmount, 1, xpCost);

        this.repairAmount = repairAmount;
        this.keepEnchantments = keepEnchantments;
        this.actionRepairCost = actionRepairCost;
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
        ItemStack output = getCraftingResult();

        // Calculate durability, should it be changed.
        if (leftStack.isDamageable() && output.isDamageable()) {
            double damageRatio = (1 / ((double) leftStack.getMaxDamage() / leftStack.getDamage())) - getRepairAmount();
            if (damageRatio > 0d) {
                int outMaxDamage = output.getMaxDamage();
                output.setDamage(Math.min((int) Math.ceil(outMaxDamage * damageRatio), outMaxDamage - 1));
            }
        }

        // Act on the repair cost. Either keep the same cost the left item has,
        // increase it by vanilla means or clear it entirely.
        switch (this.getActionRepairCost()) {
            case "keep":
                output.setRepairCost(leftStack.getRepairCost());
                break;
            case "increase":
                output.setRepairCost(RepairContainer.getNewRepairCost(leftStack.getRepairCost()));
                break;
            case "clear":
            default:
        }

        // Copy over enchantments.
        if (this.getKeepEnchantments()) {
            EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(leftStack), output);
        }

        // If a name is to be set, do set it and add to the cost.
        int finalXpCost = this.getXpCost();
        String inputName = event.getName();

        String leftDisplayName = leftStack.hasDisplayName() ? leftStack.getDisplayName().getString() : "";
        String outputDefaultName = output.getDisplayName().getString();

        String currentName = !leftDisplayName.isEmpty() ? leftDisplayName : outputDefaultName;
        if (!inputName.equals(currentName) && !(leftDisplayName.isEmpty() && inputName.isEmpty())) ++finalXpCost;

        if (!inputName.isEmpty() && !inputName.equals(outputDefaultName)) {
            output.setDisplayName(new StringTextComponent(inputName));
        }

        // Set all the output variables.
        event.setOutput(output);
        event.setCost(finalXpCost);

        event.setMaterialCost(1);
    }

    public double getRepairAmount() {
        return this.repairAmount;
    }

    public boolean getKeepEnchantments() {
        return this.keepEnchantments;
    }

    public String getActionRepairCost() {
        return this.actionRepairCost;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SingleSmithing.SERIALIZER;
    }
}

