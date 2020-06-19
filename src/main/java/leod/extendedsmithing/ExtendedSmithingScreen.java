package leod.extendedsmithing;

import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.util.text.ITextComponent;

public class ExtendedSmithingScreen extends AnvilScreen {
    public ExtendedSmithingScreen(RepairContainer repairContainer, PlayerInventory playerInventory, ITextComponent titleIn) {
        super(repairContainer, playerInventory, titleIn);
    }
}
