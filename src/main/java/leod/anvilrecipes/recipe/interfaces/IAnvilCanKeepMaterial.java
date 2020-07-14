package leod.anvilrecipes.recipe.interfaces;

public interface IAnvilCanKeepMaterial {
    boolean DEFAULT_KEEP_MATERIAL = false;
    int DEFAULT_DAMAGE_TICKS = 0;

    boolean getKeepMaterial();
    int getMaterialDamageTicks();
}