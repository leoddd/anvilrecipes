package leod.anvilrecipes.recipe.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public interface IAnvilNameable {
    default boolean renameStack(ItemStack originStack, ItemStack targetStack,
                                String inputName) {
        if (inputName == null) return false;

        String leftDisplayName = originStack.hasDisplayName() ? originStack.getDisplayName().getString() : "";
        String outputDefaultName = targetStack.getDisplayName().getString();

        String currentName = !leftDisplayName.isEmpty() ? leftDisplayName : outputDefaultName;
        if (!inputName.isEmpty() && !inputName.equals(outputDefaultName))
            targetStack.setDisplayName(new StringTextComponent(inputName));

        return !inputName.equals(currentName) && !(leftDisplayName.isEmpty() && inputName.isEmpty());
    }
}
