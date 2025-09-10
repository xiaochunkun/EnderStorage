/**
 * 末影存储JEI插件类（已废弃）
 * 
 * 这是与JEI（Just Enough Items）模组集成的插件实现，目前已被注释掉。
 * 原本用于在JEI界面中显示末影存储的配方信息，特别是重新着色配方。
 * 
 * 原功能包括：
 * - 注册末影存储的自定义配方到JEI
 * - 提供合成网格助手用于配方显示
 * - 处理重新着色配方的JEI集成
 * 
 * TODO: 需要根据新版本的JEI API重新实现这个插件
 * 
 * @author covers1624
 * @since 8/07/2017
 * @deprecated 使用旧版JEI API，需要重构
 */
//package codechicken.enderstorage.plugin.jei;
//
//import codechicken.enderstorage.recipe.Factories;
//import codechicken.enderstorage.recipe.RecipeBase;
//import com.google.common.collect.Sets;
//import mezz.jei.api.*;
//import mezz.jei.api.gui.ICraftingGridHelper;
//import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * Created by covers1624 on 8/07/2017.
// */
//@JEIPlugin//TODO FIXME TODO, Recipe handler for recoloring.
//public class EnderStorageJEIPlugin implements IModPlugin {
//
//    public static ICraftingGridHelper gridHelper;
//
//    @Override
//    public void register(IModRegistry registry) {
//        IJeiHelpers helpers = registry.getJeiHelpers();
//        IGuiHelper guiHelpers = helpers.getGuiHelper();
//
//        gridHelper = guiHelpers.createCraftingGridHelper(1, 0);
//        registry.handleRecipes(Factories.CraftingRecipe.class, ESCraftingRecipeWrapper::new, VanillaRecipeCategoryUid.CRAFTING);
//    }
//
//}
