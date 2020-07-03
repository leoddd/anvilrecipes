package leod.extendedsmithing;

import leod.extendedsmithing.recipe.IAnvilRecipe;
import leod.extendedsmithing.recipe.additionsmithing.AdditionSmithing;
import leod.extendedsmithing.recipe.singlesmithing.SingleSmithing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

// TODO: Left slot consumes whole stacks still, regardless of intention.

@Mod(ExtendedSmithing.MODID)
public class ExtendedSmithing {
    public static final String MODID = "extendedsmithing";
    public final SmithingCache CACHE = new SmithingCache();

    public final IEventBus GAMEBUS = MinecraftForge.EVENT_BUS;
    public final IEventBus MODBUS = FMLJavaModLoadingContext.get().getModEventBus();

    public RecipeManager recipeManager = null;

    public static final boolean SHOW_DEBUG = true;

    public static void printDebug(String msg) {
        if (!SHOW_DEBUG) return;
        LogManager.getLogger(ExtendedSmithing.MODID).log(Level.WARN, msg);
    }

    public ExtendedSmithing() {
        // Registration and Setup.
        MODBUS.register(new SingleSmithing());
        MODBUS.register(new AdditionSmithing());

        //-------------------------------------

        // In-game event handling.
        GAMEBUS.register(new FindRecipeManager());
        GAMEBUS.register(new ModifyAnvilContainerLogic());
        GAMEBUS.register(new OnAnvilUpdate());
    }

    /**
     * Event subscriber classes
     */
    public class FindRecipeManager {
        @SubscribeEvent
        @OnlyIn(Dist.DEDICATED_SERVER)
        public void afterServerStart(FMLServerStartedEvent event) {
            printDebug("Memorizing Server RecipeManager");
            recipeManager = event.getServer().getRecipeManager();
            printDebug("Manager: " + recipeManager);
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public void onRecipesUpdated(RecipesUpdatedEvent event) {
            printDebug("Memorizing Client RecipeManager");
            recipeManager = event.getRecipeManager();
            printDebug("Manager: " + recipeManager);
        }
    }

    public class ModifyAnvilContainerLogic {

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public void modifyAnvilScreenOnOpen(final GuiScreenEvent.InitGuiEvent.Post event) {
            if (event.getGui() instanceof AnvilScreen) {
                AnvilScreen screen = (AnvilScreen) event.getGui();
                Minecraft minecraft = Minecraft.getInstance();
                RepairContainer repairContainer = ObfuscationReflectionHelper.getPrivateValue(ContainerScreen.class, screen, "field_147002_h");

                if (minecraft.player != null && repairContainer != null) {
                    // Transform the associated container to accept 0 cost recipes.
                    ExtendedSmithing.this.transformRepairContainer(repairContainer);

                    // Replace the nameField's listener to properly update the right item's name.
                    TextFieldWidget nameField = ObfuscationReflectionHelper.getPrivateValue(AnvilScreen.class, screen, "field_147091_w");
                    if (nameField != null) {
                        nameField.setResponder((newName) -> {
                            Slot leftSlot = repairContainer.getSlot(0);
                            if (leftSlot.getHasStack()) {
                                if (!StringUtils.isBlank(newName)) {
                                    Slot outputSlot = repairContainer.getSlot(2);

                                    ItemStack nameBaseStack = (outputSlot.getHasStack() && (outputSlot.getStack().getItem() != leftSlot.getStack().getItem()) ?
                                            outputSlot : leftSlot).getStack();

                                    if (newName.equals(I18n.format(nameBaseStack.getTranslationKey()))) {
                                        newName = "";
                                    }
                                } else {
                                    newName = "";
                                }

                                repairContainer.updateItemName(newName);
                                minecraft.player.connection.sendPacket(new CRenameItemPacket(newName));
                            }
                        });

                        // Replace the ContainerListener (the old one counteracts ours)
                        repairContainer.removeListener(screen);
                        repairContainer.addListener(new IContainerListener() {
                            /**
                             * Called whenever the content of any slot is changed.
                             */
                            @Override
                            @ParametersAreNonnullByDefault
                            public void sendSlotContents(Container containerToSend, int slotIdx, ItemStack updatedStack) {
                                if (slotIdx > 2) return;

                                if (slotIdx == 0) {
                                    nameField.setEnabled(!updatedStack.isEmpty());
                                }

                                String newName;
                                Slot middleSlot = repairContainer.getSlot(1);
                                if (!nameField.canWrite()) {
                                    newName = "";
                                } else if (slotIdx == 2 && (!updatedStack.isEmpty()) && (!middleSlot.getHasStack())) {
                                    newName = null;
                                } else {
                                    newName = this.getBaseName();
                                }

                                if (newName != null && !nameField.getText().equals(newName)) {
                                    nameField.setText(newName);
                                }

                                // TODO: Think up a way to get SingleSmithing to work.
//                                // aTODO: Make sure this update does not break the whole shebob.
//                                if (slotIdx != 2 && repairContainer.getSlot(0).getHasStack() && !insideEventRepost) {
//                                    printDebug("Posting placeholdered event");
//                                    insideEventRepost = true;
//                                    middleSlot.putStack(new ItemStack(() -> SingleSmithing.SINGLE_DUMMY_ITEM));
//                                    ForgeHooks.onAnvilChange(repairContainer, repairContainer.getSlot(0).getStack(), middleSlot.getStack(),
//                                            repairContainer.getSlot(2).inventory, newName, 0);
//                                    middleSlot.putStack(ItemStack.EMPTY);
//                                    insideEventRepost = false;
//                                }
                            }

                            private String getBaseName() {
                                ItemStack leftStack = repairContainer.getSlot(0).getStack();
                                ItemStack outputStack = repairContainer.getSlot(2).getStack();

                                if (leftStack.hasDisplayName()) {
                                    return leftStack.getDisplayName().getString();
                                } else {
                                    ItemStack baseStack = outputStack.isEmpty() ? leftStack : outputStack;
                                    return I18n.format(baseStack.getTranslationKey());
                                }
                            }


                            @Override
                            @ParametersAreNonnullByDefault
                            public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
                                screen.sendAllContents(containerToSend, itemsList);
                            }

                            @Override
                            @ParametersAreNonnullByDefault
                            public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
                                screen.sendWindowProperty(containerIn, varToUpdate, newValue);
                            }
                        });
                    }
                }
            }
        }

        @SubscribeEvent
        public void modifyRepairContainerOnOpen(final PlayerContainerEvent.Open event) {
            if (event.getContainer() instanceof RepairContainer) {
                RepairContainer repairContainer = (RepairContainer) event.getContainer();
                ExtendedSmithing.this.transformRepairContainer(repairContainer);
            }
        }
    }

    public class OnAnvilUpdate {
        /**
         * When anvil contents are updated, check if it matches any registered smithing recipe.
         */
        @SubscribeEvent
        public void onAnvilUpdate(AnvilUpdateEvent event) {
            ItemStack left = event.getLeft();
            ItemStack right = event.getRight();

            IAnvilRecipe matchingRecipe;

            // Check if a matching recipe has been cached.
            IAnvilRecipe cachedRecipe = CACHE.getRecipeByItems(left.getItem(), right.getItem());
            if (cachedRecipe != null && cachedRecipe.matchesItemStacks(left, right)) {
                matchingRecipe = cachedRecipe;
                printDebug("Found a cached matching recipe: " + matchingRecipe);
            }

            // If not, search for one in the entire recipe database.
            else {
                matchingRecipe = findMatchingRecipe(left, right);

                // Cache this recipe together with the used items if it matches anything.
                if (matchingRecipe != null) {
                    CACHE.cacheRecipe(left.getItem(), right.getItem(), matchingRecipe);
                    printDebug("Cached recipe: " + matchingRecipe);
                }
            }

            // If we have found a recipe that matches (cached or not), act on it.
            if (matchingRecipe != null) {
                matchingRecipe.onMatch(event);
            }
        }
    }

    /**
     * Helper methods.
     */
    public void transformRepairContainer(RepairContainer repairContainer) {
        ExtendedSlotWrapper.replaceSlot(repairContainer, 2, new ExtendedSlotWrapper(134, 47, repairContainer.getSlot(2)) {
            @Override
            @ParametersAreNonnullByDefault
            public boolean isItemValid(ItemStack stack) {
                return this.getWrappedSlot().isItemValid(stack);
            }

            @Override
            @ParametersAreNonnullByDefault
            public boolean canTakeStack(PlayerEntity playerIn) {
                return (playerIn.abilities.isCreativeMode || playerIn.experienceLevel >= repairContainer.getMaximumCost()) && this.getWrappedSlot().getHasStack();
            }

            @Override
            @Nonnull
            @ParametersAreNonnullByDefault
            public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
                // Implement ItemContainer returns.
                // This way smithing with a filled bucket will return an empty one.
                ItemStack[] slotContainers = new ItemStack[2];
                int[] amountsBefore = {0, 0};
                for (int i = 0; i <= 1; ++i) {
                    Slot curSlot = repairContainer.getSlot(i);
                    if (curSlot.getHasStack() && curSlot.getStack().hasContainerItem()) {
                        slotContainers[i] = curSlot.getStack().getContainerItem();
                        amountsBefore[i] = curSlot.getStack().getCount();
                    }
                }

                ItemStack stackToReturn = this.getWrappedSlot().onTake(playerIn, stack);

                for (int i = 0; i <= 1; ++i) {
                    if (slotContainers[i] != null) {
                        Slot curSlot = repairContainer.getSlot(i);

                        int diffInAmounts = amountsBefore[i] - (curSlot.getHasStack() ? curSlot.getStack().getCount() : 0);
                        if (slotContainers[i].isStackable()) {
                            slotContainers[i].setCount(diffInAmounts);
                        }

                        for (int j = (slotContainers[i].isStackable() ? 1 : diffInAmounts); j > 0; --j) {
                            if (!curSlot.getHasStack()) {
                                curSlot.putStack(slotContainers[i]);
                            } else {
                                playerIn.addItemStackToInventory(slotContainers[i]);
                            }
                            this.getWrappedSlot().onSlotChanged();
                        }
                    }
                }

                // Remove empty RepairCost tags, so unnamed items can be properly stacked.
                if (stackToReturn.getRepairCost() == 0) {
                    stackToReturn.removeChildTag("RepairCost");
                }

                return stackToReturn;
            }
        });
    }

    @Nullable
    public IAnvilRecipe findMatchingRecipe(ItemStack left, ItemStack right) {
        IAnvilRecipe matchingRecipe = null;
        Map<ResourceLocation, IRecipe<?>> anvilRecipes = getAnvilRecipes();
        if (anvilRecipes != null) {

            for (final IRecipe<?> genericRecipe : anvilRecipes.values()) {
                IAnvilRecipe anvilRecipe = (IAnvilRecipe) genericRecipe;

                if (anvilRecipe.matchesItemStacks(left, right)) {
                    matchingRecipe = anvilRecipe;
                    printDebug("[✔] findMatchingRecipe(" + Item.getIdFromItem(left.getItem()) + "," + Item.getIdFromItem(right.getItem()) + ") found a recipe: " + matchingRecipe);
                    break;
                }
            }
            printDebug("[x] findMatchingRecipe(" + Item.getIdFromItem(left.getItem()) + "," + Item.getIdFromItem(right.getItem()) + ") did not find a recipe.");
        }

        return matchingRecipe;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * Caches recipes and maps them by Items used, because you can't infer an Ingredient from matching Items.
     * This is mostly to accelerate repeated crafting and habitual recipe interpretations.
     * Each recipe is only saved once, because I think people will more often use the same materials than not.
     */
    private static class SmithingCache extends HashMap<Item, HashMap<Item, IAnvilRecipe>> {
        private final HashMap<IAnvilRecipe, CachedItems> cachedRecipesWithItemsUsed = new HashMap<>();

        public void cacheRecipe(@Nonnull Item left, @Nonnull Item right, IAnvilRecipe recipe) {
            if (isRecipeCached(recipe)) removeRecipeFromCache(recipe);

            HashMap<Item, IAnvilRecipe> leftMap = this.computeIfAbsent(left, k -> new HashMap<>());
            leftMap.put(right, recipe);

            cachedRecipesWithItemsUsed.put(recipe, new CachedItems(left, right));
        }

        public boolean isRecipeCached(IAnvilRecipe recipe) {
            return cachedRecipesWithItemsUsed.containsKey(recipe);
        }

        public void removeRecipeFromCache(IAnvilRecipe recipe) {
            if (!isRecipeCached(recipe)) return;

            CachedItems cachedRecipe = cachedRecipesWithItemsUsed.get(recipe);
            Item left = cachedRecipe.getLeft();

            HashMap<Item, IAnvilRecipe> leftMap = this.get(left);
            leftMap.remove(cachedRecipe.getRight());
            if (leftMap.isEmpty()) this.remove(left);

            cachedRecipesWithItemsUsed.remove(recipe);
        }

        public IAnvilRecipe getRecipeByItems(@Nonnull Item left, @Nonnull Item right) {
            HashMap<Item, IAnvilRecipe> leftMap = this.get(left);
            return leftMap != null ? leftMap.get(right) : null;
        }

        /**
         * Saves the items used for a recipe along with the recipe.
         */
        private static class CachedItems {
            private final Item LEFT;
            private final Item RIGHT;

            CachedItems(@Nonnull Item left, @Nonnull Item right) {
                this.LEFT = left;
                this.RIGHT = right;
            }

            public Item getLeft() {
                return LEFT;
            }

            public Item getRight() {
                return RIGHT;
            }
        }
    }

    /**
     * This method lets you get all recipes of the given type. The existing
     * methods for this require an IInventory, and this allows you to skip that overhead.
     * This method uses reflection to get the recipes map, but an access transformer would
     * also work.
     *
     * @return A map containing all anvil recipes. This map is immutable
     * and can not be modified.
     */
    private Map<ResourceLocation, IRecipe<?>> getAnvilRecipes() {
        final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipesMap = ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, getRecipeManager(), "field_199522_d");
        return recipesMap != null ? recipesMap.get(IAnvilRecipe.RECIPETYPE) : null;
    }

    /**
     * Utility class that wraps and replaces vanilla Slots,
     * so as to modify vanilla behavior non-invasively on anonymous classes.
     */
    public static class ExtendedSlotWrapper extends Slot {
        private final Slot wrappedSlot;

        public ExtendedSlotWrapper(int xPosition, int yPosition, Slot slotToWrap) {
            super(slotToWrap.inventory, slotToWrap.getSlotIndex(), xPosition, yPosition);
            this.wrappedSlot = slotToWrap;
        }

        public Slot getWrappedSlot() {
            return wrappedSlot;
        }

        public static void replaceSlot(Container container, int slotIdx, Slot slotToInsert) {
            if (container.inventorySlots.size() > slotIdx) {
                NonNullList<ItemStack> inventoryItemStacks = ObfuscationReflectionHelper.getPrivateValue(Container.class, container, "field_75153_a");
                assert inventoryItemStacks != null;

                inventoryItemStacks.remove(slotIdx);
                inventoryItemStacks.add(slotIdx, ItemStack.EMPTY);
                container.inventorySlots.remove(slotIdx);
                container.inventorySlots.add(slotIdx, slotToInsert);

                slotToInsert.slotNumber = slotIdx;
            }
        }
    }
}