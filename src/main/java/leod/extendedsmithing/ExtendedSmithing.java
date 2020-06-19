package leod.extendedsmithing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mod(ExtendedSmithing.MODID)
public class ExtendedSmithing {
    public static final String MODID = "extendedsmithing";
    public final SmithingCache CACHE = new SmithingCache();

    public final IEventBus GAMEBUS = MinecraftForge.EVENT_BUS;
    public final IEventBus MODBUS = FMLJavaModLoadingContext.get().getModEventBus();

    public RecipeManager recipeManager = null;

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public static final boolean SHOW_DEBUG = true;

    public static void printDebug(String msg) {
        if (!SHOW_DEBUG) return;
        LogManager.getLogger(ExtendedSmithing.MODID).log(Level.WARN, msg);
    }

    public ExtendedSmithing() {
        printDebug("Registering Event Classes");
        MODBUS.register(new ModBusEvents());
        GAMEBUS.register(new GameBusEvents());
    }

    /**
     * Event subscriber classes
     */
    public static class ModBusEvents {
        @SubscribeEvent
        public void registerRecipeSerializer(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            printDebug("Registering Recipe Type and Serializer");
            // Also register the recipe type, since it has no own event.
            Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(AbstractAnvilRecipe.RECIPETYPE.toString()), AbstractAnvilRecipe.RECIPETYPE);
            event.getRegistry().register(RecipeSmithingRecipe.SERIALIZER);
        }
    }

    public class GameBusEvents {
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

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public void onAnvilScreenOpen(GuiOpenEvent event) {
            Screen originalGui = event.getGui();
            if (originalGui instanceof AnvilScreen && !(originalGui instanceof ExtendedSmithingScreen)) {
                AnvilScreen originalAnvilGui = (AnvilScreen) originalGui;

                assert Minecraft.getInstance().player != null;
                ExtendedSmithingScreen extendedGui = new ExtendedSmithingScreen(
                        originalAnvilGui.getContainer(),
                        Minecraft.getInstance().player.inventory,
                        TextComponent
                        );
                event.setGui(extendedGui);
            }
        }

        /**
         * When anvil contents are updated, check if it matches any registered smithing recipe.
         */
        @SubscribeEvent
        public void onAnvilUpdate(AnvilUpdateEvent event) {
            printDebug("Anvil Update Event");
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

}