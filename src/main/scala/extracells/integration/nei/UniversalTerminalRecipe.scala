package extracells.integration.nei

import java.awt.Rectangle
import java.util

import codechicken.nei.api.{DefaultOverlayRenderer, IOverlayHandler, IRecipeOverlayRenderer, IStackPositioner}
import codechicken.nei.recipe.{RecipeInfo, TemplateRecipeHandler}
import codechicken.nei.{NEIClientUtils, PositionedStack}
import extracells.registries.ItemEnum
import extracells.util.UniversalTerminal
import net.minecraft.client.gui.inventory.{GuiContainer, GuiCrafting}
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack

class UniversalTerminalRecipe extends TemplateRecipeHandler {
  override def loadTransferRects {
    this.transferRects.add(new TemplateRecipeHandler.RecipeTransferRect(new Rectangle(84, 23, 24, 18), "crafting"))
  }

  override def loadCraftingRecipes(outputId: String, results: AnyRef*) {
    if ((outputId == "crafting")) {
      val cachedRecipe = new CachedShapelessRecipe(true)
      cachedRecipe.computeVisuals
      arecipes.add(cachedRecipe)
      val cachedRecipe2 = new CachedShapelessRecipe(false)
      cachedRecipe2.computeVisuals
      arecipes.add(cachedRecipe2)
    }else{
      super.loadCraftingRecipes(outputId, results:_*)
    }
  }

  override def loadCraftingRecipes(result: ItemStack) {
    if(result != null && result.getItem == ItemEnum.UNIVERSALTERMINAL.getItem){
      val cachedRecipe = new CachedShapelessRecipe(true)
      cachedRecipe.computeVisuals
      arecipes.add(cachedRecipe)
      val cachedRecipe2 = new CachedShapelessRecipe(false)
      cachedRecipe2.computeVisuals
      arecipes.add(cachedRecipe2)
    }
  }

  override def loadUsageRecipes(ingredient: ItemStack) {
    if(ingredient == null || ingredient.getItem == null)
      return
    if(UniversalTerminal.isTerminal(ingredient)){
      val cachedRecipe = new CachedShapelessRecipe(true)
      cachedRecipe.computeVisuals
      arecipes.add(cachedRecipe)
      val cachedRecipe2 = new CachedShapelessRecipe(false)
      cachedRecipe2.computeVisuals
      arecipes.add(cachedRecipe2)
    }else if(UniversalTerminal.isWirelessTerminal(ingredient)){
      val cachedRecipe = new CachedShapelessRecipe(false)
      cachedRecipe.computeVisuals
      arecipes.add(cachedRecipe)
    }else if(ingredient.getItem == ItemEnum.UNIVERSALTERMINAL.getItem){
      val cachedRecipe = new CachedShapelessRecipe(true)
      cachedRecipe.computeVisuals
      arecipes.add(cachedRecipe)
    }


  }

  def getGuiTexture: String = "textures/gui/container/crafting_table.png"


  override def getOverlayIdentifier: String = "crafting"


  override def getGuiClass: Class[_ <: GuiContainer] = classOf[GuiCrafting]


  override def hasOverlay(gui: GuiContainer, container: Container, recipe: Int): Boolean =
    (super.hasOverlay(gui, container, recipe)) || ((this.isRecipe2x2(recipe)) && (RecipeInfo.hasDefaultOverlay(gui, "crafting2x2")))


  override def getOverlayRenderer(gui: GuiContainer, recipe: Int): IRecipeOverlayRenderer = {
    val renderer: IRecipeOverlayRenderer = super.getOverlayRenderer(gui, recipe)
    if (renderer != null) {
      return renderer
    }
    val positioner: IStackPositioner = RecipeInfo.getStackPositioner(gui, "crafting2x2")
    if (positioner == null) {
      return null
    }
    new DefaultOverlayRenderer(this.getIngredientStacks(recipe), positioner)
  }

  override def getOverlayHandler(gui: GuiContainer, recipe: Int): IOverlayHandler = {
    val handler: IOverlayHandler = super.getOverlayHandler(gui, recipe)
    if (handler != null) {
      return handler
    }
    RecipeInfo.getOverlayHandler(gui, "crafting2x2")
  }

  private def isRecipe2x2(recipe: Int): Boolean = {
    import scala.collection.JavaConversions._
    for (stack <- this.getIngredientStacks(recipe)) {
      if ((stack.relx > 43) || (stack.rely > 24)) {
        return false
      }
    }
    true
  }

  def getRecipeName: String = NEIClientUtils.translate("recipe.shapeless")


  private class CachedShapelessRecipe (isUniversal: Boolean) extends CachedRecipe {
    private val ingredients = new util.ArrayList[PositionedStack]
    private val result: PositionedStack = new PositionedStack(ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0), 119, 24)
    setIngredients


    def getResult: PositionedStack = this.result

    override def getIngredients: util.List[PositionedStack] = {
      this.getCycledIngredients(UniversalTerminalRecipe.this.cycleticks / 20, this.ingredients)
    }

    def setIngredients() {

      val stack: PositionedStack = {
        if(isUniversal)
          new PositionedStack(ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0), 25, 6, false)
        else
          new PositionedStack(UniversalTerminal.wirelessTerminals, 25, 6, false)
      }
      stack.setMaxSize(1)
      this.ingredients.add(stack)

      val stack2: PositionedStack = new PositionedStack(UniversalTerminal.terminals, 43, 6, false)
      stack2.setMaxSize(1)
      this.ingredients.add(stack2)

    }

    def computeVisuals {
      import scala.collection.JavaConversions._
      for (p <- this.ingredients) {
        p.generatePermutations
      }
      this.result.generatePermutations
    }
  }

}