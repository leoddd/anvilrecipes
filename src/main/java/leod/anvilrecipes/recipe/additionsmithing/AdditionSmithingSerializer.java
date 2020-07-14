package leod.anvilrecipes.recipe.additionsmithing;

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

public class AdditionSmithingSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AdditionSmithingRecipe> {
    public AdditionSmithingSerializer() {
        this.setRegistryName(AnvilRecipeImplementation.MODID, "additionsmithing");
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public AdditionSmithingRecipe read(ResourceLocation recipeId, JsonObject json) {
        if (!json.has("input") || !json.has("output")) {
            throw new IllegalStateException(
                    "Not a valid [" + AnvilRecipeImplementation.MODID + "] JSON object."
            );
        }

        // Input parsing.
        JsonObject inputJson = JSONUtils.getJsonObject(json, "input");
        if (!inputJson.has("left") || !inputJson.has("right")) {
            throw new IllegalStateException(
                    "[" + AnvilRecipeImplementation.MODID + "] JSON object is missing the 'left' or 'right' item entry."
            );
        }

        int xpCost = JSONUtils.getInt(inputJson, "xp", IAnvilRecipeBase.DEFAULT_XP_COST);
        boolean refineAll = JSONUtils.getBoolean(inputJson, "refineAll", IAnvilCanRefineAll.DEFAULT_REFINE_ALL);

        JsonObject leftObject = JSONUtils.getJsonObject(inputJson, "left");
        JsonObject rightObject = JSONUtils.getJsonObject(inputJson, "right");
        Ingredient left = Ingredient.deserialize(leftObject);
        Ingredient right = Ingredient.deserialize(rightObject);

        int leftAmount = JSONUtils.getInt(leftObject, "count", IAnvilRecipeBase.DEFAULT_AMOUNT);
        int rightAmount = JSONUtils.getInt(rightObject, "count", IAnvilRecipeBase.DEFAULT_AMOUNT);
        boolean keepMaterial = JSONUtils.getBoolean(rightObject, "keep", IAnvilCanKeepMaterial.DEFAULT_KEEP_MATERIAL);
        int materialDamageTicks = JSONUtils.getInt(rightObject, "damageTicks", IAnvilCanKeepMaterial.DEFAULT_DAMAGE_TICKS);
        ItemStack leftContainer = !inputJson.has("leftContainer") ? IAnvilCanOverrideContainers.DEFAULT_INGREDIENT_CONTAINER
                : ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(inputJson, "leftContainer"));
        ItemStack rightContainer = !inputJson.has("rightContainer") ? IAnvilCanOverrideContainers.DEFAULT_INGREDIENT_CONTAINER
                : ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(inputJson, "rightContainer"));

        // Output parsing.
        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");
        if (!outputJson.has("result")) {
            throw new IllegalStateException(
                    "[" + AnvilRecipeImplementation.MODID + "] JSON object's 'output' object is missing the 'result' item entry."
            );
        }

        ItemStack out = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(outputJson, "result"));

        float repairAmount = JSONUtils.getFloat(outputJson, "repairAmount", (float) IAnvilRepairDurability.DEFAULT_REPAIR_AMOUNT);
        IAnvilRepairCost.RepairAction repairAction = IAnvilRepairCost.RepairAction.actionFromString(JSONUtils.getString(outputJson, "repairAction", IAnvilRepairCost.DEFAULT_REPAIR_ACTION));
        boolean keepEnchantments = JSONUtils.getBoolean(outputJson, "keepEnchantments", IAnvilEnchantments.DEFAULT_KEEP_ENCHANTMENTS);
        IAnvilEnchantments.EnchantmentMap newEnchantments = IAnvilEnchantments.EnchantmentMap.read(JSONUtils.getJsonObject(outputJson, "newEnchantments", new JsonObject()));

        return new AdditionSmithingRecipe(recipeId, left, right, out, leftAmount, rightAmount, xpCost, keepMaterial, materialDamageTicks,
                leftContainer, rightContainer, repairAmount, repairAction, refineAll, keepEnchantments, newEnchantments);
    }

    @ParametersAreNonnullByDefault
    @Override
    public AdditionSmithingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        final Ingredient left = Ingredient.read(buffer);
        final Ingredient right = Ingredient.read(buffer);
        final ItemStack out = buffer.readItemStack();
        final int leftAmount = buffer.readInt();
        final int rightAmount = buffer.readInt();
        final int xpCost = buffer.readInt();
        final boolean keepMaterial = buffer.readBoolean();
        final int materialDamageTicks = buffer.readInt();
        final ItemStack leftContainer = buffer.readItemStack();
        final ItemStack rightContainer = buffer.readItemStack();
        final float repairAmount = buffer.readFloat();
        final IAnvilRepairCost.RepairAction repairAction = IAnvilRepairCost.RepairAction.actionFromString(buffer.readString());
        final boolean refineAll = buffer.readBoolean();
        final boolean keepEnchantments = buffer.readBoolean();
        final IAnvilEnchantments.EnchantmentMap newEnchantments = IAnvilEnchantments.EnchantmentMap.read(buffer);

        return new AdditionSmithingRecipe(recipeId, left, right, out, leftAmount, rightAmount, xpCost, keepMaterial, materialDamageTicks,
                leftContainer, rightContainer, repairAmount, repairAction, refineAll, keepEnchantments, newEnchantments);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void write(PacketBuffer buffer, AdditionSmithingRecipe recipe) {
        recipe.getLeft().write(buffer);
        recipe.getRight().write(buffer);
        buffer.writeItemStack(recipe.getRecipeOutput());
        buffer.writeInt(recipe.getLeftAmount());
        buffer.writeInt(recipe.getRightAmount());
        buffer.writeInt(recipe.getXpCost());
        buffer.writeBoolean(recipe.getKeepMaterial());
        buffer.writeInt(recipe.getMaterialDamageTicks());
        buffer.writeItemStack(recipe.getLeftContainerOverride(), false);
        buffer.writeItemStack(recipe.getRightContainerOverride(), false);
        buffer.writeFloat(recipe.getRepairAmount());
        buffer.writeString(recipe.getRepairAction().toString());
        buffer.writeBoolean(recipe.getRefineAll());
        buffer.writeBoolean(recipe.getKeepEnchantments());
        IAnvilEnchantments.EnchantmentMap.write(buffer, recipe.getNewEnchantments());
    }
}
