package leod.extendedsmithing.recipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public abstract class AbstractSmithingWrapper<T extends IRecipeSerializer<?>> {
    public abstract T getSerializer();

    @SubscribeEvent
    public void registerRecipeSerializer(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
        event.getRegistry().register(this.getSerializer());
    }
}
