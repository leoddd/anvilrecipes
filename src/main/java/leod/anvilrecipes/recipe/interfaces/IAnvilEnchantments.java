package leod.anvilrecipes.recipe.interfaces;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public interface IAnvilEnchantments {
    boolean DEFAULT_KEEP_ENCHANTMENTS = true;

    /**
     * Copy enchantments from originStack to targetStack.
     */
    default void applyEnchantments(ItemStack originStack, ItemStack targetStack) {
        EnchantmentMap allEnchants = new EnchantmentMap();
        if (getKeepEnchantments()) {
            allEnchants.putAll(EnchantmentHelper.getEnchantments(originStack));
        }
        allEnchants.putAll(getNewEnchantments());

        removeAllEnchantments(targetStack);

        for (Map.Entry<Enchantment, Integer> enchEntry : allEnchants.entrySet()) {
            Enchantment enchantment = enchEntry.getKey();
            if (enchantment.canApply(targetStack)) {
                upgradeEnchantmentToLevel(enchantment, enchEntry.getValue(), targetStack);
            }
        }
    }

    static void removeAllEnchantments(ItemStack targetStack) {
        targetStack.removeChildTag("Enchantments");
    }

    static void upgradeEnchantmentToLevel(Enchantment enchantment, int targetLevel, ItemStack targetStack) {
        int currentLevel = EnchantmentHelper.getEnchantmentLevel(enchantment, targetStack);
        if (currentLevel < targetLevel) {
            if (currentLevel != 0) {
                removeEnchantments(targetStack, enchantment);
            }
            targetStack.addEnchantment(enchantment, targetLevel);
        }
    }

    static void removeEnchantments(ItemStack targetStack, Enchantment... enchantmentsToRemove) {
        ListNBT enchantmentNBT = targetStack.getEnchantmentTagList();

        Stack<Integer> indicesToRemove = new Stack<>();
        for (Enchantment enchantment : enchantmentsToRemove) {
            ResourceLocation enchId = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);

            for (int i = 0; i < enchantmentNBT.size(); ++i) {
                CompoundNBT enchData = enchantmentNBT.getCompound(i);
                if (enchData.getString("id").equals(enchId != null ? enchId.toString() : null)) {
                    indicesToRemove.push(i);
                }
            }
        }

        while (!indicesToRemove.empty()) {
            enchantmentNBT.remove((int) indicesToRemove.pop());
        }

        if (enchantmentNBT.isEmpty()) {
            targetStack.removeChildTag("Enchantments");
        } else {
            targetStack.setTagInfo("Enchantments", enchantmentNBT);
        }
    }

    EnchantmentMap getNewEnchantments();

    boolean getKeepEnchantments();


    class EnchantmentMap extends HashMap<Enchantment, Integer> {
        public static String END_SYMBOL = "%end";

        /**
         * Create a new EnchantmentMap from the JsonObject.
         */
        public static EnchantmentMap read(JsonObject enchantmentJson) {
            EnchantmentMap enchMap = new EnchantmentMap();
            if (!enchantmentJson.isJsonObject()) return enchMap;

            Set<Map.Entry<String, JsonElement>> enchantmentList = enchantmentJson.entrySet();
            try {
                for (Map.Entry<String, JsonElement> enchEntry : enchantmentList) {
                    enchMap.put(
                            ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchEntry.getKey())),
                            enchEntry.getValue().getAsInt()
                    );
                }
            }
            catch (ClassCastException e) {
                return new EnchantmentMap();
            }

            return enchMap;
        }

        /**
         * Create a new EnchantmentMap from the PacketBuffer.
         */
        public static EnchantmentMap read(PacketBuffer buf) {
            EnchantmentMap enchMap = new EnchantmentMap();

            while (true) {
                String newEnchantmentName = buf.readString();
                if (newEnchantmentName.equals(END_SYMBOL)) break;

                enchMap.put(
                        ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(newEnchantmentName)),
                        buf.readInt()
                );
            }

            return enchMap;
        }

        /**
         * Write the EnchantmentMap to the buffer.
         */
        public static void write(PacketBuffer buf, EnchantmentMap enchMap) {
            for (Entry<Enchantment, Integer> enchantmentEntry : enchMap.entrySet()) {
                buf.writeString(Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(enchantmentEntry.getKey())).toString());
                buf.writeInt(enchantmentEntry.getValue());
            }
            buf.writeString(END_SYMBOL);
        }
    }
}
