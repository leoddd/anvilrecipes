package leod.extendedsmithing.recipe.singlesmithing;

import leod.extendedsmithing.ExtendedSmithing;
import leod.extendedsmithing.recipe.AbstractSmithingWrapper;
import net.minecraft.item.Item;

public class SingleSmithing extends AbstractSmithingWrapper<SingleSmithingSerializer> {
    public final static SingleSmithingSerializer SERIALIZER = new SingleSmithingSerializer();
    public final static Item SINGLE_DUMMY_ITEM = new Item(
            new Item.Properties()
    ).setRegistryName(ExtendedSmithing.MODID, "single_dummy");

    public SingleSmithingSerializer getSerializer() {
        return SERIALIZER;
    }

//    @SubscribeEvent
//    public void registerDummyItem(final RegistryEvent.Register<Item> event) {
//        event.getRegistry().register(SINGLE_DUMMY_ITEM);
//    }
}
