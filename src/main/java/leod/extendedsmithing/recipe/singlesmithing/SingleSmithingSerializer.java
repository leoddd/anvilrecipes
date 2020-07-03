package leod.extendedsmithing.recipe.singlesmithing;

import com.google.gson.JsonObject;
import leod.extendedsmithing.ExtendedSmithing;
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
    public static final int DEFAULT_XP_COST = 0;
    public static final double DEFAULT_REPAIR_AMOUNT = 0d;
    public static final String DEFAULT_ACTION_REPAIR_COST = "clear";
    public static final boolean DEFAULT_KEEP_ENCHANTMENTS = true;

    public SingleSmithingSerializer() {
        this.setRegistryName(ExtendedSmithing.MODID, "singlesmithing");
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public SingleSmithingRecipe read(ResourceLocation recipeId, JsonObject json) {
        if (!json.has("input") || !json.has("output")) {
            throw new IllegalStateException(
                    "Not a valid [" + ExtendedSmithing.MODID + "] JSON object."
            );
        }

        // Input parsing.
        JsonObject inputJson = JSONUtils.getJsonObject(json, "input");
        if (!inputJson.has("left")) {
            throw new IllegalStateException(
                    "[" + ExtendedSmithing.MODID + "] JSON object is missing the 'left' entry."
            );
        }

        JsonObject leftObject = JSONUtils.getJsonObject(inputJson, "left");
        Ingredient left = Ingredient.deserialize(leftObject);

        int xpCost = JSONUtils.getInt(inputJson, "xp", DEFAULT_XP_COST);
        int leftAmount = JSONUtils.getInt(leftObject, "count", 1);

        // Output parsing.
        JsonObject outputJson = JSONUtils.getJsonObject(json, "output");
        if (!outputJson.has("item")) {
            throw new IllegalStateException(
                    "[" + ExtendedSmithing.MODID + "] JSON object's 'output' object is missing the 'item' entry."
            );
        }

        ItemStack out = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(outputJson, "item"));

        double repairAmount = JSONUtils.getFloat(outputJson, "repairAmount", (float) DEFAULT_REPAIR_AMOUNT);
        String actionRepairCost = JSONUtils.getString(outputJson, "actionRepairCost", DEFAULT_ACTION_REPAIR_COST);
        boolean keepEnchantments = JSONUtils.getBoolean(outputJson, "keepEnchantments", DEFAULT_KEEP_ENCHANTMENTS);

        return new SingleSmithingRecipe(recipeId, left, out, leftAmount,
                xpCost, repairAmount, actionRepairCost, keepEnchantments);
    }

    @ParametersAreNonnullByDefault
    @Override
    public SingleSmithingRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
        final Ingredient left = Ingredient.read(buffer);
        final ItemStack out = buffer.readItemStack();
        final int leftAmount = buffer.readInt();
        final int xpCost = buffer.readInt();
        final double repairAmount = buffer.readDouble();
        final String actionRepairCost = buffer.readString();
        final boolean keepEnchantments = buffer.readBoolean();

        return new SingleSmithingRecipe(recipeId, left, out, leftAmount,
                xpCost, repairAmount, actionRepairCost, keepEnchantments);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void write(PacketBuffer buffer, SingleSmithingRecipe recipe) {
        recipe.getLeft().write(buffer);
        buffer.writeItemStack(recipe.getRecipeOutput());
        buffer.writeInt(recipe.getLeftAmount());
        buffer.writeInt(recipe.getXpCost());
        buffer.writeDouble(recipe.getRepairAmount());
        buffer.writeString(recipe.getActionRepairCost());
        buffer.writeBoolean(recipe.getKeepEnchantments());
    }
}
