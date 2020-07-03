package leod.extendedsmithing.recipe.additionsmithing;

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

public class AdditionSmithingSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AdditionSmithingRecipe> {
    public static final int DEFAULT_XP_COST = 0;
    public static final double DEFAULT_REPAIR_AMOUNT = 0d;
    public static final String DEFAULT_ACTION_REPAIR_COST = "clear";
    public static final boolean DEFAULT_KEEP_ENCHANTMENTS = true;

    public AdditionSmithingSerializer() {
        this.setRegistryName(ExtendedSmithing.MODID, "additionsmithing");
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public AdditionSmithingRecipe read(ResourceLocation recipeId, JsonObject json) {
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

        double repairAmount = JSONUtils.getFloat(outputJson, "repairAmount", (float) DEFAULT_REPAIR_AMOUNT);
        String actionRepairCost = JSONUtils.getString(outputJson, "actionRepairCost", DEFAULT_ACTION_REPAIR_COST);
        boolean keepEnchantments = JSONUtils.getBoolean(outputJson, "keepEnchantments", DEFAULT_KEEP_ENCHANTMENTS);

        return new AdditionSmithingRecipe(recipeId, left, right, out, leftAmount, rightAmount,
                xpCost, repairAmount, actionRepairCost, keepEnchantments);
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
        final double repairAmount = buffer.readDouble();
        final String actionRepairCost = buffer.readString();
        final boolean keepEnchantments = buffer.readBoolean();

        return new AdditionSmithingRecipe(recipeId, left, right, out, leftAmount, rightAmount,
                xpCost, repairAmount, actionRepairCost, keepEnchantments);
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
        buffer.writeDouble(recipe.getRepairAmount());
        buffer.writeString(recipe.getActionRepairCost());
        buffer.writeBoolean(recipe.getKeepEnchantments());
    }
}
