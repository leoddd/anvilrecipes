package leod.anvilrecipes.recipe.additionsmithing;

import leod.anvilrecipes.recipe.AbstractSmithingWrapper;

public class AdditionSmithing extends AbstractSmithingWrapper<AdditionSmithingSerializer> {
    public final static AdditionSmithingSerializer SERIALIZER = new AdditionSmithingSerializer();

    public AdditionSmithingSerializer getSerializer() {
        return SERIALIZER;
    }
}
