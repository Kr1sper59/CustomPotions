package com.cmeworld.custompotions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cmeworld.custompotions.Main;
import com.cmeworld.custompotions.gui.GUI;
import com.cmeworld.custompotions.states.State;
import com.cmeworld.custompotions.utils.ItemStackUtil;

import java.io.IOException;

public class InventoryGUIListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        ItemStack interaction = event.getCurrentItem();

        if (inv == null || interaction == null || interaction.getItemMeta() == null || event.getCurrentItem() == null) {
            return;
        }
        String localizedName = ItemStackUtil.getLocalizedName(interaction);
        if (localizedName == null || localizedName.isEmpty()) {
            // Not a custom potion GUI item
            return;
        }
        
        State state;
        try {
            state = State.decodeFromString(localizedName);
        } catch (IOException | ClassNotFoundException e) {
            // Not a State class or corrupted data - silently ignore
            // This is normal for non-GUI items
            return;
        }
        Main.log.info("Action: " + state.getAction() + "; Menu: " + state.getClass().getSimpleName());
        // TODO prevent placing in the custom inventories, including dragging event
        //  is this covered by interaction == null? (for the placing part)

        event.setCancelled(true);
        new GUI(state, (Player) event.getWhoClicked())
            .updateEvent(event)
            .nextState()
            .open();
    }
}