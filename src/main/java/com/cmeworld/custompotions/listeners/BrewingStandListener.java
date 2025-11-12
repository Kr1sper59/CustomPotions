package com.cmeworld.custompotions.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cmeworld.custompotions.LivePotionRecipe;
import com.cmeworld.custompotions.Main;
import com.cmeworld.custompotions.Potion;
import com.cmeworld.custompotions.PotionRecipe;
import com.cmeworld.custompotions.utils.PotionUtil;
import java.util.ArrayList;
import java.util.List;

public class BrewingStandListener implements Listener {
    private final Main plugin;
    
    public BrewingStandListener(Main plugin) {
        this.plugin = plugin;
    }

    private void updateInventory(InventoryClickEvent event) {
        ((Player) event.getWhoClicked()).updateInventory();
    }

    private void clearCursor(InventoryClickEvent event) {
        event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
    }

    private void setCursor(InventoryClickEvent event, ItemStack item) {
        event.getWhoClicked().setItemOnCursor(item);
    }

    private void moveToOtherInventory(InventoryClickEvent event) {
        BrewerInventory brewerInventory = (BrewerInventory) event.getInventory();
        ItemStack ingredient = brewerInventory.getIngredient();
        ItemStack slotItem = event.getCurrentItem();

        int maxStackSize = slotItem.getMaxStackSize();
        int ingredientStackSize = ingredient.getAmount();
        int itemStackSize = slotItem.getAmount();
        int newIngredientStackSize = Math.min(maxStackSize, ingredientStackSize + itemStackSize);
        int newItemStackSize = Math.max(0, itemStackSize - (maxStackSize - ingredientStackSize));

        ingredient.setAmount(newIngredientStackSize);
        brewerInventory.setIngredient(ingredient);
        slotItem.setAmount(newItemStackSize);
        updateInventory(event);
    }

    private void placeAll(InventoryClickEvent event) {
        BrewerInventory brewerInventory = (BrewerInventory) event.getInventory();
        ItemStack ingredient = brewerInventory.getIngredient();
        ItemStack cursorItem = event.getCursor();

        int maxStackSize = cursorItem.getMaxStackSize();
        int ingredientStackSize = ingredient.getAmount();
        int itemStackSize = cursorItem.getAmount();
        int newIngredientStackSize = Math.min(maxStackSize, ingredientStackSize + itemStackSize);
        int newItemStackSize = Math.max(0, itemStackSize - (maxStackSize - ingredientStackSize));

        ingredient.setAmount(newIngredientStackSize);
        brewerInventory.setIngredient(ingredient);
        cursorItem.setAmount(newItemStackSize);
        setCursor(event, cursorItem);
    }

    private void placeOne(InventoryClickEvent event) {
        BrewerInventory brewerInventory = (BrewerInventory) event.getInventory();
        ItemStack ingredient = brewerInventory.getIngredient();
        ItemStack cursorItem = event.getCursor();

        int maxStackSize = cursorItem.getMaxStackSize();
        int ingredientStackSize = ingredient.getAmount();
        int itemStackSize = cursorItem.getAmount();
        int newIngredientStackSize = Math.min(maxStackSize, ingredientStackSize + 1);
        int newItemStackSize = Math.max(0, itemStackSize - (newIngredientStackSize - ingredientStackSize));

        ingredient.setAmount(newIngredientStackSize);
        brewerInventory.setIngredient(ingredient);
        cursorItem.setAmount(newItemStackSize);
        setCursor(event, cursorItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBrewingStandInteract(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = event.getInventory();
        if (clickedInventory == null || inventory.getType() != InventoryType.BREWING) return;
        BrewerInventory brewerInventory = (BrewerInventory) inventory;

        InventoryAction inventoryAction = event.getAction();
        ItemStack slotItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        Main.log.info(inventoryAction.toString());
        if (slotItem != null) Main.log.info("Slot item: " + slotItem.toString());
        if (cursorItem != null) Main.log.info("Cursor item: " + cursorItem.toString());

        // Player inventory was clicked
        if (clickedInventory.getType() == InventoryType.PLAYER) {
            // Shift click from player inventory
            if (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (slotItem == null || PotionUtil.isPotion(slotItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();

                // Inserting blaze powder
                if (slotItem.getType() == Material.BLAZE_POWDER) {
                    // Insert into fuel slot
                    if (ingredient == null || ingredient.getType() != Material.BLAZE_POWDER ||
                        ingredient.getAmount() == ingredient.getMaxStackSize()) {
                        return;
                    }
                    // Insert into ingredient slot
                    else {
                        moveToOtherInventory(event);
                        event.setCancelled(true);
                    }
                }

                // Empty ingredient slot, insert item
                if (ingredient == null) {
                    brewerInventory.setIngredient(slotItem);
                    clickedInventory.clear(event.getSlot());
                    updateInventory(event);
                    event.setCancelled(true); // prevents duplication of ingredients in hotbar / main
                }
                // Ingredient slot matches: insert item stack until ingredient slot full, or source empty
                else if (ingredient.getType() == slotItem.getType()) {
                    int ingredientStackSize = ingredient.getAmount();
                    int newIngredientStackSize = Math.min(slotItem.getMaxStackSize(), ingredientStackSize + slotItem.getAmount());
                    moveToOtherInventory(event);

                    // Prevents moving remaining ingredients between hotbar <-> main
                    if (ingredientStackSize != newIngredientStackSize) event.setCancelled(true);
                }
            }
        }
        // Brewing inventory was clicked
        else {
            // Not the ingredient slot of the brewing stand
            if (event.getSlotType() != InventoryType.SlotType.FUEL) return;

            // Placing stack in ingredient slot
            // Pickup all & left click -> X in ingredient slot, clicking on ingredient slot with X in cursor
            if (inventoryAction == InventoryAction.PLACE_ALL
                || inventoryAction == InventoryAction.PICKUP_ALL && event.isLeftClick()) {
                if (cursorItem == null || PotionUtil.isPotion(cursorItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();

                // Empty ingredient slot, insert item
                if (ingredient == null) {
                    brewerInventory.setIngredient(cursorItem);
                    clearCursor(event);
                    event.setCancelled(true);
                }
                // Ingredient slot matches: insert item stack until ingredient slot full, or source empty
                else if (ingredient.getType() == cursorItem.getType()) {
                    placeAll(event);
                    event.setCancelled(true);
                }
            }
            // Placing one of stack in ingredient slot
            // Pickup all & right click -> X in ingredient slot, clicking on ingredient slot with X in cursor
            else if (inventoryAction == InventoryAction.PLACE_ONE
                || event.isRightClick() && inventoryAction == InventoryAction.PICKUP_ALL) {
                if (cursorItem == null || PotionUtil.isPotion(cursorItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();

                // Empty ingredient slot, insert an item
                if (ingredient == null) {
                    ItemStack single = cursorItem.clone();
                    single.setAmount(1);
                    brewerInventory.setIngredient(single);
                    cursorItem.setAmount(cursorItem.getAmount() - 1);
                    setCursor(event, cursorItem);
                    event.setCancelled(true);
                }
                // Ingredient slot matches: insert single item
                else if (ingredient.getType() == cursorItem.getType()) {
                    placeOne(event);
                    event.setCancelled(true);
                }
            }
            // All cases where an item would be inserted into the ingredient slot but Minecraft does not register
            // it as a valid item, therefore returning a "NOTHING" action type.
            else if (inventoryAction == InventoryAction.NOTHING) {
                if (cursorItem == null || PotionUtil.isPotion(cursorItem)) return;
                ItemStack ingredient = brewerInventory.getIngredient();
                if (ingredient == null) return;

                // Ingredient slot matches
                if (ingredient.getType() == cursorItem.getType()) {
                    if (event.isLeftClick()) {
                        placeAll(event);
                        event.setCancelled(true);
                    } else if (event.isRightClick()) {
                        placeOne(event);
                        event.setCancelled(true);
                    }
                }
                // Swap cursor with existing ingredient.
                else {
                    brewerInventory.setIngredient(cursorItem);
                    setCursor(event, ingredient);
                }
            }
        }
        
        // Check for recipes and start brewing after ingredient is placed
        if (event.getAction() == InventoryAction.PLACE_ALL || 
            event.getAction() == InventoryAction.PLACE_ONE ||
            event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
            (event.getAction() == InventoryAction.NOTHING && brewerInventory.getIngredient() != null)) {
            // Schedule check after inventory updates
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                checkAndStartBrewing(brewerInventory);
            }, 1L);
        }
    }
    
    private void checkAndStartBrewing(BrewerInventory inventory) {
        if (inventory.getIngredient() == null) return;
        
        // Convert PotionRecipe to LivePotionRecipe
        List<LivePotionRecipe> liveRecipes = convertRecipesToLive();
        
        // Check if there's a matching recipe
        LivePotionRecipe recipe = LivePotionRecipe.getRecipe(inventory, liveRecipes, plugin);
        if (recipe != null && inventory.getHolder().getBrewingTime() == 0) {
            recipe.startBrewing(inventory, plugin, liveRecipes);
        }
    }
    
    private List<LivePotionRecipe> convertRecipesToLive() {
        List<LivePotionRecipe> liveRecipes = new ArrayList<>();
        List<Potion> customPotions = PotionUtil.getCustomPotions();
        
        plugin.getLogger().info("Converting recipes to live. Found " + customPotions.size() + " custom potions");
        
        int index = 0;
        for (Potion potion : customPotions) {
            ItemStack result = potion.toItemStack();
            plugin.getLogger().info("Processing potion: " + potion.getName() + " with " + potion.getRecipes().size() + " recipes");
            for (PotionRecipe recipe : potion.getRecipes()) {
                ItemStack base = PotionUtil.potionFromId(recipe.getBase());
                if (base != null) {
                    plugin.getLogger().info("Created live recipe: ingredient=" + recipe.getIngredient() + ", base=" + recipe.getBase() + ", result=" + potion.getPotionId());
                    liveRecipes.add(new LivePotionRecipe(result, base, recipe.getIngredient(), index++));
                } else {
                    plugin.getLogger().warning("Could not create base potion from ID: " + recipe.getBase());
                }
            }
        }
        
        plugin.getLogger().info("Total live recipes created: " + liveRecipes.size());
        return liveRecipes;
    }
}
