package com.cleanroommc.hadenoughids.core.mixins.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.RecipeMatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(ShapelessRecipes.class)
public class ShapelessRecipesMixin {

    @Shadow(remap = false) @Final private boolean isSimple;
    @Shadow @Final public NonNullList<Ingredient> recipeItems;

    /**
     * @author Rongmario
     * @reason Stop the use of overly-convoluted RecipeItemHelper, as it glitches out our Item ID/ItemStack meta expansion strategies.
     * @see Ingredient#getValidItemStacksPacked() <= nuisance
     */
    @Overwrite
    public boolean matches(InventoryCrafting inv, World world) {
        if (this.isSimple) {
            List<Ingredient> ingredients = null;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && !stack.isItemDamaged() && !stack.isItemEnchanted() && !stack.hasDisplayName()) {
                    if (ingredients == null) {
                        ingredients = new ArrayList<>(recipeItems);
                    }
                    for (int j = 0; j < recipeItems.size(); j++) {
                        Ingredient ingredient = recipeItems.get(j);
                        if (ingredient.test(stack)) {
                            if (ingredients.get(j) == null) {
                                return false;
                            }
                            ingredients.set(j, null);
                            break;
                        }
                    }
                }
            }
            if (ingredients != null) {
                for (Ingredient ingredient : ingredients) {
                    if (ingredient != null) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } else {
            List<ItemStack> inputs = new ArrayList<>();
            for (int i = 0; i < inv.getHeight(); i++) {
                for (int j = 0; j < inv.getWidth(); j++) {
                    ItemStack stack = inv.getStackInRowAndColumn(j, i);
                    if (!stack.isEmpty()) {
                        inputs.add(stack);
                    }
                }
            }
            return inputs.size() == this.recipeItems.size() && RecipeMatcher.findMatches(inputs, this.recipeItems) != null;
        }
    }

}
