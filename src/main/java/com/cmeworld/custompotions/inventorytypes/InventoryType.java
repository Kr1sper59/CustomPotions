package com.cmeworld.custompotions.inventorytypes;

import org.bukkit.entity.Player;
import com.cmeworld.custompotions.states.State;

import java.io.Serializable;

public abstract class InventoryType<T> implements Serializable {
    // TODO think about making the subclasses static classes in here
    protected String title;

    protected InventoryType(String title) {
        this.title = title;
    }

    protected abstract T createInventory(State state);
    public abstract void openInventory(State state, Player player);
}
