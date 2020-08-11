package leod.anvilrecipes.integration.jei;

import leod.anvilrecipes.AnvilRecipeImplementation;
import leod.anvilrecipes.recipe.IAnvilRecipeBase;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class AnvilRecipeJEIPlugin implements IModPlugin {
    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(AnvilRecipeImplementation.MODID, "anvil");
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registry) {
        PlayerEntity player = Minecraft.getInstance().player;

        RepairContainer fakeRepairContainer = null;
        Slot fakeOutputSlot = null;
        if (player != null) {
            fakeRepairContainer = new RepairContainer(0, new PlayerInventory(player));
            fakeOutputSlot = fakeRepairContainer.getSlot(2);
        }

        List<Object> jeiRecipeList = new ArrayList<>();
        for (IAnvilRecipeBase recipe : AnvilRecipeImplementation.getAnvilRecipes().values()) {
            if (recipe.getLeft().hasNoMatchingItems() || recipe.getCraftingResult().isEmpty()) continue;

            List<ItemStack> matchingLeft = Arrays.asList(recipe.getLeft().getMatchingStacks());
            List<ItemStack> matchingRight = Arrays.asList(recipe.getRight().getMatchingStacks());

            // Start 1.15.2 Only, because JEI only pulled the necessary change into 1.16.
            if (matchingRight.isEmpty())
                continue;
            // End 1.15.2 Only

            List<ItemStack> leftStacks = new ArrayList<>();
            List<ItemStack> outputStacks = new ArrayList<>();

            // Calculate the output ItemStack.
            if (fakeRepairContainer != null) {
                for (ItemStack leftStack : matchingLeft) {
                    if (leftStack.isDamageable() && recipe.getRepairAmount() > 0.0 && recipe.getRepairAmount() < 1.0)
                        leftStack.setDamage((int) (leftStack.getMaxDamage() * recipe.getRepairAmount()));
                    else
                        leftStack.setCount(recipe.getLeftAmount());

                    ItemStack rightStack = matchingRight.isEmpty() ? ItemStack.EMPTY.copy() : matchingRight.get(0);
                    if (!rightStack.isEmpty())
                        rightStack.setCount(recipe.getRightAmount());

                    try {
                        fakeRepairContainer.getSlot(0).putStack(leftStack);
                        fakeRepairContainer.getSlot(1).putStack(rightStack);
                    } catch (RuntimeException e) {
                        System.out.println("JEI ran into an issue trying to parse an AnvilRecipe: " + e);
                        continue;
                    }

                    if (fakeOutputSlot.getHasStack()) {
                        ItemStack outputStack = fakeOutputSlot.getStack().copy();

                        if (!outputStack.equals(leftStack, false)) {
                            leftStacks.add(leftStack);
                            outputStacks.add(outputStack);
                        }
                    }
                }
            }

            // Don't even add this recipe if there is no valid input or output.
            if (matchingLeft.isEmpty() || outputStacks.isEmpty()) continue;

            jeiRecipeList.add(registry.getVanillaRecipeFactory().createAnvilRecipe(
                    leftStacks,
                    matchingRight,
                    outputStacks
            ));
        }

        registry.addRecipes(jeiRecipeList, VanillaRecipeCategoryUid.ANVIL);
    }
}
