package com.cmeworld.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import com.cmeworld.custompotions.Actions.StartupAction;
import com.cmeworld.custompotions.Input;
import com.cmeworld.custompotions.Potion;
import com.cmeworld.custompotions.inventorytypes.ChestInventory;

import java.util.List;

public class GiveMenu extends State {
    private final static int pageSize = 51;

    public GiveMenu() {
        super(null, new Potion(true), new StartupAction(),
            new ChestInventory(ChatColor.GOLD + "Select a Potion to Take"), new Input());
    }

    @Override
    protected boolean needsNextPage() {
        return false; // TODO
    }

    @Override
    public List<ItemStack> calculateInventoryItems() {
        return null; // TODO
    }

    /*public GiveMenu(State state) {
        super(state, new ChestInventory(ChatColor.GOLD + "Select a Potion to Take", 51), new Input());
    }// dont think we need because page next and previous is done by cloning*/
}
