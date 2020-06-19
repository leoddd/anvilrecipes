package leod.extendedsmithing;

import com.google.gson.JsonObject;
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

public class RecipeSmithingSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RecipeSmithingRecipe> {
    public static final int DEFAULT_XP_COST = 0;
    public static final boolean DEFAULT_REFRESH_DURABILITY = true;
    public static final double DEFAULT_REPAIR_AMOUNT = 0.0d;

    RecipeSmithingSerializer(ResourceLocation registryName) {
        this.setRegistryName(registryName);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public RecipeSmithingRecipe read(ResourceLocation recipeId, JsonObject json) {
        if (!json.has("input") || !json.has("output")) {
            throw new IllegalStateException(
                    "Not a valid [" + ExtendedSmithing.MODID + "] JSON object."
            );
        }

        // Input parsing.
        JsonObject inputJson = JSONUtils.getJsonObject(json, "input");
        if (!inputJson.has("left") || !inputJson.has("right")) {
            throw new IllegalStateException(
                    "[" + ExtendedSmithing.MODID + "] JSON object is missing the 'left' or 'right' entry."
            );
        }

        JsonObject leftObject = JSONUtils.getJsonObject(inputJson, "left");
        JsonObject rightObject = JSONUtils.getJsonObject(inputJson, "right");
        Ingredient left = Ingredient.deserialize(leftObject);
        Ingredient right = Ingredient.deserialize(rightObject);

        int xpCost = JSONUtils.getInt(inputJson, "xp", DEFAULT_XP_COST);
        int leftAmount = JSONUtils.getInt(leftObject, "count", 1);
        int rightAmount = JSONUtils.getInt(rightObject, "count", 1);

        // Output parsing.
        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");
        if (!outputJson.has("item")) {
            throw new IllegalStateException(
                    "[" + ExtendedSmithing.MODID + "] JSON object's 'output' object is missing the 'item' entry."
            );
        }

        ItemStack out = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(outputJson, "item"));

        boolean refreshDurability = DEFAULT_REFRESH_DURABILITY;
        if (JSONUtils.hasField(outputJson, "refreshDurability")) {
            refreshDurability = JSONUtils.getBoolean(outputJson, "refreshDurability");
        }
        double repairAmount = DEFAULT_REPAIR_AMOUNT;
        if (JSONUtils.hasField(outputJson, "repairAmount")) {
            repairAmount = JSONUtils.getFloat(outputJson, "repairAmount");
        }

        return new RecipeSmithingRecipe(recipeId, left, right, out, leftAmount, rightAmount, xpCost, refreshDurability, repairAmount);
    }

    @ParametersAreNonnullByDefault
    @Override
    public RecipeSmithingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        final Ingredient left = Ingredient.read(buffer);
        final Ingredient right = Ingredient.read(buffer);
        final ItemStack out = buffer.readItemStack();
        final int leftAmount = buffer.readInt();
        final int rightAmount = buffer.readInt();
        final int xpCost = buffer.readInt();
        final boolean refreshDurability = buffer.readBoolean();
        final double repairAmount = buffer.readDouble();

        return new RecipeSmithingRecipe(recipeId, left, right, out, leftAmount, rightAmount, xpCost, refreshDurability, repairAmount);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void write(PacketBuffer buffer, RecipeSmithingRecipe recipe) {
        recipe.getLeft().write(buffer);
        recipe.getRight().write(buffer);
        buffer.writeItemStack(recipe.getRecipeOutput());
        buffer.writeInt(recipe.getLeftAmount());
        buffer.writeInt(recipe.getRightAmount());
        buffer.writeInt(recipe.getXpCost());
        buffer.writeBoolean(recipe.getRefreshDurability());
        buffer.writeDouble(recipe.getRepairAmount());
    }
}
