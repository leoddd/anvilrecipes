# Anvil Recipes
Allows mod and modpack developers to define anvil recipes using .json files, just like existing recipes.

There are three new recipe types:
- **additionsmithing**
	- Takes two specific ingredients and creates the specified output.
- **singlesmithing**
	- Only specifies one ingredient, for recipes where the right input slot should be empty.
- **wildcardsmithing**
	- Only specifies the right ingredient, the left ingredient can be anything.
	- Useful for universal repairing, repair cost clearing or enchanting materials.

To use this mod, you will have to place your recipe json files in the correct location:
- for a modpack. [Info](https://minecraft.gamepedia.com/Tutorials/Creating_a_data_pack).
- for a mod: [Info](https://mcforge.readthedocs.io/en/latest/utilities/recipes/).

## Options
The following attributes can be configured in every recipe:
- How much XP the recipe costs.
- Which and how many materials are necessary.
    - Tags can be used to allow multiple items.
- By how much to repair (or damage) the output item.
- What should happen to the hidden base repair cost of the output item.
    - It can be given the repair cost of the left ingredient, increase it or clear it entirely.
- Whether to keep the enchantments on the input item and whether to apply any additional enchantments.
- Whether to keep the right ingredient instead of consuming it.
    - If so, whether to reduce its durability.
- Whether to return an item for each item used up in either slot.
    - This overrides vanilla containers, meaning that a bucket will not return both an empty bucket and a custom container.
    It will only return the custom container.
- Whether, instead of using a specific amount of items from the left slot, all items will be used.
    - The amount of items the recipe outputs will be the same amount used up.

Recipes only display an output if it actually differs from the left input item.

## JSON Structure
Below are examples for each of the recipe types.
Except for the ingredient IDs and the output ID, all values are optional, with a sensible fallback.
If you're not sure about any of the defaults, it's better to simply define the behavior you want.

Note that these are all just examples. Options used in the wildcardsmithing example can also be used in other recipe types and vice versa, if applicable.

##### additionsmithing (example: golden apple peeling)
```json
{
  "type": "anvilrecipes:additionsmithing",
  "input": {
    "xp": 0,
    "refineAll": true,
    "left": {
      "item": "minecraft:golden_apple"
    },
    "leftContainer": {
      "item": "minecraft:apple"
    },
    "right": {
      "item": "minecraft:iron_axe",
      "keep": true,
      "damageTicks": 1
    }
  },
  "output": {
    "result": {
      "item": "minecraft:gold_nugget"
    }
  }
}
```
This recipe peels the gold off of golden apples. It expects a stack of golden apples on the left and an iron axe on the right. It will output as many golden nuggets as there are golden apples.

Due to the ``leftContainer`` item, each golden apple will turn into a regular apple on use.

``refineAll`` states that all apples should be used, instead of a specific amount.

The right ingredient will not be consumed due to the ``keep`` tag and will instead take 1 damage.

If we wanted this recipe to work with all axes, a tag should be used instead.

##### wildcardsmithing (example: very tough quartz)
```json
{
  "type": "anvilrecipes:wildcardsmithing",
  "input": {
    "xp": 0,
    "right": {
      "item": "minecraft:quartz",
      "count": 8
    }
  },
  "output": {
    "keepEnchantments": true,
    "newEnchantments": {
      "minecraft:unbreaking": 2
    },
    "repairAction": "increase",
    "repairAmount": 0.2
  }
}
```
``newEnchantments``, as the name implies, defines which enchantments to apply to the item, if possible.
In this case, using 8 quartz on any item that can be enchanted with *Unbreaking II* will apply the enchantment to it.
Thanks to ``keepEnchantments``, existing enchantments on the left ingredient are copied over.

``repairAction`` defines what happens to the base repair cost of the output item.
In this case, since we are successfully smithing onto a tool, we are increasing it as vanilla minecraft would.

``repairAmount`` defines by how much to repair the output. A value of 0.2, for example, would repair the output by a fifth of its maximum duration, using the duration of the left ingredient as a base.
Should you want the output to always be fully repaired, using 1.0 here works fine, as it will always be fully repaired.
Negative values damage the item instead. However, the result will never have less than 1 duration.

*(Keep in mind that the grindstone exists, so a recipe like this could be used for very roundabout XP farming.)*

##### singlesmithing (example: crushing blaze rods)
```json
{
  "type": "anvilrecipes:singlesmithing",
  "input": {
    "xp": 2,
    "left": {
      "item": "minecraft:blaze_rod",
      "count": 1
    }
  },
  "output": {
    "result": {
      "item": "minecraft:blaze_powder",
      "count": 5
    }
  }
}
```
This recipe crushes blaze rods into 5 blaze powder, instead of the usual 2. In exchange, it costs 2 whole levels of xp.

It's a lot simpler than the above examples, because not all recipes have to be a headache to understand.

# Todo
- JEI Integration