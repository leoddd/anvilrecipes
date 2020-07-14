package leod.anvilrecipes.recipe.wildcardsmithing;

import leod.anvilrecipes.recipe.AbstractSmithingWrapper;

public class WildcardSmithing extends AbstractSmithingWrapper<WildcardSmithingSerializer> {
    public final static WildcardSmithingSerializer SERIALIZER = new WildcardSmithingSerializer();

    public WildcardSmithingSerializer getSerializer() {
        return SERIALIZER;
    }
}
