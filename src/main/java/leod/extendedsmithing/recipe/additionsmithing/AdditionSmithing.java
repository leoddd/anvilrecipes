package leod.extendedsmithing.recipe.additionsmithing;

import leod.extendedsmithing.recipe.AbstractSmithingWrapper;

public class AdditionSmithing extends AbstractSmithingWrapper<AdditionSmithingSerializer> {
    public final static AdditionSmithingSerializer SERIALIZER = new AdditionSmithingSerializer();

    public AdditionSmithingSerializer getSerializer() {
        return SERIALIZER;
    }
}
