package leod.anvilrecipes.recipe.interfaces;

import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;

public interface IAnvilRepairCost {
    enum RepairAction {
        KEEP, INCREASE, CLEAR;

        public String toString() {
            return super.toString().toLowerCase();
        }

        public static RepairAction actionFromString(String inputString) {
            inputString = inputString.toLowerCase();
            if (inputString.equals(RepairAction.KEEP.toString())) return RepairAction.KEEP;
            else if (inputString.equals(RepairAction.INCREASE.toString())) return RepairAction.INCREASE;
            else return RepairAction.CLEAR;
        }
    }

    String DEFAULT_REPAIR_ACTION = "clear";

    /**
     * Keep, increase or clear the additional repair cost of the targetStack, based on the originStack if it increases.
     */
    default void applyRepairAction(ItemStack originStack, ItemStack targetStack) {
        switch (getRepairAction()) {
            case KEEP:
                targetStack.setRepairCost(originStack.getRepairCost());
                break;
            case INCREASE:
                targetStack.setRepairCost(RepairContainer.getNewRepairCost(originStack.getRepairCost()));
                break;
            case CLEAR:
            default:
                targetStack.setRepairCost(0);
        }

        if (targetStack.getRepairCost() == 0) {
            targetStack.removeChildTag("RepairCost");
        }
    }

    RepairAction getRepairAction();
}
