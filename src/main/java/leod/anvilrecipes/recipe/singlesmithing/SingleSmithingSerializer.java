package leod.anvilrecipes.recipe.singlesmithing;

import com.google.gson.JsonObject;
import leod.anvilrecipes.AnvilRecipeImplementation;
import leod.anvilrecipes.recipe.IAnvilRecipeBase;
import leod.anvilrecipes.recipe.interfaces.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class SingleSmithingSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<SingleSmithingRecipe> {
    public SingleSmithingSerializer() {
        this.setRegistryName(AnvilRecipeImplementation.MODID, "singlesmithing");
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public SingleSmithingRecipe read(ResourceLocation recipeId, JsonObject json) {
        if (!json.has("input") || !json.has("output")) {
            throw new IllegalStateException(
                    "Not a valid [" + AnvilRecipeImplementation.MODID + "] JSON object."
            );
        }

        // Input parsing.
        JsonObject inputJson = JSONUtils.getJsonObject(json, "input");
        if (!inputJson.has("left")) {
            throw new IllegalStateException(
                    "[" + AnvilRecipeImplementation.MODID + "] JSON object is missing the 'left' item entry."
            );
        }

        int xpCost = JSONUtils.getInt(inputJson, "xp", IAnvilRecipeBase.DEFAULT_XP_COST);

        JsonObject leftObject = JSONUtils.getJsonObject(inputJson, "left");
        Ingredient left = Ingredient.deserialize(leftObject);

        int leftAmount = JSONUtils.getInt(leftObject, "count", IAnvilRecipeBase.DEFAULT_AMOUNT);
        ItemStack leftContainer = !inputJson.has("leftContainer") ? IAnvilCanOverrideContainers.DEFAULT_INGREDIENT_CONTAINER
                : ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(inputJson, "leftContainer"));

        // Output parsing.
        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");
        if (!outputJson.has("result")) {
            throw new IllegalStateException(
                    "[" + AnvilRecipeImplementation.MODID + "] JSON object's 'output' object is missing the 'item' item entry.."
            );
        }

        ItemStack out = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(outputJson, "result"));

        float repairAmount = JSONUtils.getFloat(outputJson, "repairAmount", (float) IAnvilRepairDurability.DEFAULT_REPAIR_AMOUNT);
        IAnvilRepairCost.RepairAction repairAction = IAnvilRepairCost.RepairAction.actionFromString(JSONUtils.getString(outputJson, "repairAction", IAnvilRepairCost.DEFAULT_REPAIR_ACTION));
        boolean keepEnchantments = JSONUtils.getBoolean(outputJson, "keepEnchantments", IAnvilEnchantments.DEFAULT_KEEP_ENCHANTMENTS);
        IAnvilEnchantments.EnchantmentMap newEnchantments = IAnvilEnchantments.EnchantmentMap.read(JSONUtils.getJsonObject(outputJson, "newEnchantments", new JsonObject()));

        return new SingleSmithingRecipe(recipeId, left, out, leftAmount, xpCost, leftContainer,
                repairAmount, repairAction, keepEnchantments, newEnchantments);
    }

    @ParametersAreNonnullByDefault
    @Override
    public SingleSmithingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        final Ingredient left = Ingredient.read(buffer);
        final ItemStack out = buffer.readItemStack();
        final int leftAmount = buffer.readInt();
        final int xpCost = buffer.readInt();
        final ItemStack leftContainer = buffer.readItemStack();
        final float repairAmount = buffer.readFloat();
        final IAnvilRepairCost.RepairAction repairAction = IAnvilRepairCost.RepairAction.actionFromString(buffer.readString());
        final boolean keepEnchantments = buffer.readBoolean();
        final IAnvilEnchantments.EnchantmentMap newEnchantments = IAnvilEnchantments.EnchantmentMap.read(buffer);

        return new SingleSmithingRecipe(recipeId, left, out, leftAmount, xpCost, leftContainer,
                repairAmount, repairAction, keepEnchantments, newEnchantments);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void write(PacketBuffer buffer, SingleSmithingRecipe recipe) {
        recipe.getLeft().write(buffer);
        buffer.writeItemStack(recipe.getRecipeOutput());
        buffer.writeInt(recipe.getLeftAmount());
        buffer.writeInt(recipe.getXpCost());
        buffer.writeItemStack(recipe.getLeftContainerOverride(), false);
        buffer.writeFloat(recipe.getRepairAmount());
        buffer.writeString(recipe.getRepairAction().toString());
        buffer.writeBoolean(recipe.getKeepEnchantments());
        IAnvilEnchantments.EnchantmentMap.write(buffer, recipe.getNewEnchantments());
    }
}
