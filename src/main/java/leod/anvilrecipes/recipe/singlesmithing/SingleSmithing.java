package leod.anvilrecipes.recipe.singlesmithing;

import leod.anvilrecipes.recipe.AbstractSmithingWrapper;

public class SingleSmithing extends AbstractSmithingWrapper<SingleSmithingSerializer> {
    public final static SingleSmithingSerializer SERIALIZER = new SingleSmithingSerializer();

    public SingleSmithingSerializer getSerializer() {
        return SERIALIZER;
    }
}
