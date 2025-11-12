package com.cmeworld.custompotions.states;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import com.cmeworld.custompotions.Actions.Action;
import com.cmeworld.custompotions.Actions.ExitSelectAction;
import com.cmeworld.custompotions.Actions.InvalidAction;
import com.cmeworld.custompotions.Actions.MenuNextAction;
import com.cmeworld.custompotions.Actions.MenuPreviousAction;
import com.cmeworld.custompotions.Actions.PageNextAction;
import com.cmeworld.custompotions.Actions.PagePreviousAction;
import com.cmeworld.custompotions.Colour;
import com.cmeworld.custompotions.Input;
import com.cmeworld.custompotions.Main;
import com.cmeworld.custompotions.Potion;
import com.cmeworld.custompotions.PotionEffectSerializable;
import com.cmeworld.custompotions.PotionRecipe;
import com.cmeworld.custompotions.inventorytypes.InventoryType;
import com.cmeworld.custompotions.utils.ItemStackUtil;
import com.cmeworld.custompotions.utils.MagicNumber;
import com.cmeworld.custompotions.utils.PotionUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing localized_name field within Minecraft ItemStacks.
 * Useful for interacting with the InventoryGUI by transmitting information about the current state.
 */
public abstract class State implements Cloneable, Serializable {
    // menu: mainMenu, potionType, potionColour, effectType, effectDuration, effectAmplifier, recipeIngredient, recipeBase, potionName, finalMenu

    // action: exit, pageNext, pagePrevious, pageInvalid, createPotion, selectPotion, selectType,
    //         selectColour, noEffects, addEffectType, selectEffectType, enterEffectDuration, enterEffectAmplifier,
    //         addRecipeIngredient, selectRecipeIngredient, addRecipeBase, removeRecipeBase, recipeBaseInvalid,
    //         enterName, finalInvalid, finalEdit, finalConfirm, skipL, skipR, give

    protected transient InventoryClickEvent event;
    protected Potion existingPotion;
    protected Action currentAction;
    protected InventoryType<?> inv;
    protected Input input;
    protected int pageNum;

    public State() {
    }

    /**
     * Constructs a state given the user's click event, the existing potion, the action taken to arrive at this state,
     * the inventory type of this state, and the input.
     */
    protected State(InventoryClickEvent event, Potion potion, Action action, InventoryType<?> inv, Input input) {
        this.event = event;
        this.existingPotion = potion;
        this.currentAction = action;
        this.inv = inv;
        this.input = input;
        this.pageNum = 0;
    }

    protected State(State state, InventoryType<?> inv, Input input) {
        this.event = state.event;
        this.existingPotion = state.existingPotion.clone();
        this.currentAction = state.currentAction;
        this.inv = inv;
        this.input = input.clone();
        this.pageNum = state.pageNum;
    }

    public State clone() {
        try {
            State s = (State) super.clone();
            s.existingPotion = this.existingPotion.clone();
            s.input = this.input.clone();
            return s;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    // Getters
    public InventoryClickEvent getEvent() {
        return event;
    }

    public Potion getPotion() {
        return this.existingPotion;
    } // TODO phase out chained uses

    public String getPotionId() {
        return this.existingPotion.getPotionId();
    }

    public String getPotionName() {
        return this.existingPotion.getName();
    }

    public ChatColor getPotionChatColor() {
        return this.existingPotion.getCorrespondingChatColor();
    }

    public List<PotionRecipe> getPotionRecipes() {
        return this.existingPotion.getRecipes();
    }

    public boolean isPotionLingering() {
        return this.existingPotion.isLingering();
    }

    public boolean hasPotionEffect(PotionEffectType effectType) {
        return this.existingPotion.hasEffect(effectType);
    }

    public ItemStack getPotionItemStack() {
        return this.existingPotion.toItemStack();
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public Action getAction() {
        return this.currentAction;
    }

    public Input getInput() {
        return this.input;
    } // TODO phase this out

    public String getInputEffectType() {
        return this.input.getEffectType();
    }

    public int getInputEffectDuration() {
        return this.input.getEffectDuration();
    }

    public String getInputRecipeIngredient() {
        return this.input.getIngredient();
    }

    // Setters and Updaters
    public State setEvent(InventoryClickEvent event) {
        this.event = event;
        return this;
    }

    public void setPotion(Potion potion) {
        this.existingPotion = potion;
    }

    public State setPotionColour(Colour colour) {
        this.existingPotion.setColour(colour);
        return this;
    }

    public State setPotionType(Material type) {
        this.existingPotion.setType(type);
        return this;
    }

    public State addPotionEffect(PotionEffectSerializable effect) {
        this.existingPotion.addEffect(effect);
        return this;
    }

    public State removePotionEffectByName(String name) {
        this.existingPotion.removeEffectByName(name);
        return this;
    }

    public State removePotionRecipesByIngredient(String name) {
        this.existingPotion.removeRecipesByIngredient(name);
        return this;
    }

    public State setPotionEffects(List<PotionEffectSerializable> effects) {
        this.existingPotion.setEffects(effects);
        return this;
    }

    public State setPotionName(String name) {
        this.existingPotion.setName(name);
        return this;
    }

    public State duplicatePotion() {
        this.existingPotion = this.existingPotion.duplicate();
        return this;
    }

    public State setPageNum(int pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public State decrementPageNum() {
        this.pageNum -= 1;
        return this;
    }

    public State incrementPageNum() {
        this.pageNum += 1;
        return this;
    }

    public State resetPageNum() {
        this.pageNum = 0;
        return this;
    }

    public State setAction(Action action) {
        this.currentAction = action;
        return this;
    }

    public State setInputEffectDuration(int durationTicks) {
        this.input.setEffectDuration(durationTicks);
        return this;
    }

    public State setInputEffectType(String effectType) {
        this.input.setEffectType(effectType);
        return this;
    }

    public State setInputIngredient(String ingredient) {
        this.input.setIngredient(ingredient);
        return this;
    }

    public State resetInput() {
        this.input = new Input();
        return this;
    }

    /**
     * Executes the current action on (this) current state and returns the new state.
     */
    public State nextState() {
        return this.currentAction.execute(this);
    }

    protected boolean isWithinBounds(int counter, int pageSize) {
        return (pageSize * this.pageNum <= counter) && (counter < (pageSize * (this.pageNum + 1)));
    }

    /**
     * Given the current page index (starting at 0), the size of each page, and the total number of items,
     * determine if there is another page following this one.
     */
    protected boolean determineIfNextPage(int pageSize, int totalItems) {
        return (totalItems - this.pageNum * pageSize) > pageSize;
    }

    protected boolean needsNextPage() {
        return false;
    }

    /**
     * Returns a button which brings the user to the previous page of the same menu.
     */
    protected ItemStack previousPageButton() {
        ItemStack button;
        State nextState = this.clone();

        if (this.pageNum > 0) {
            button = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            nextState.setAction(new PagePreviousAction());
            ItemStackUtil.setDisplayName(button, ChatColor.GOLD + "PREVIOUS PAGE");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Page " + this.pageNum);
            ItemStackUtil.addLore(button, lore);
        } else {
            button = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            nextState.setAction(new InvalidAction());
            ItemStackUtil.setDisplayName(button, ChatColor.RED + "NO PREVIOUS PAGE");
        }

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());

        return button;
    }

    /**
     * Returns a button which brings the user to the next page of the same menu.
     */
    protected ItemStack nextPageButton() {
        ItemStack button;
        State nextState = this.clone();

        if (this.needsNextPage()) {
            button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            nextState.setAction(new PageNextAction());
            ItemStackUtil.setDisplayName(button, ChatColor.GREEN + "NEXT PAGE");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "Page " + (this.pageNum + 2));
            ItemStackUtil.addLore(button, lore);
        } else {
            button = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            nextState.setAction(new InvalidAction());
            ItemStackUtil.setDisplayName(button, ChatColor.RED + "NO NEXT PAGE");
        }

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());

        return button;
    }

    protected List<String> previousMenuLore() {
        return new ArrayList<>();
    }

    protected List<String> nextMenuLore() {
        return new ArrayList<>();
    }

    /**
     * Returns a button which brings the user to the next menu.
     */
    protected ItemStack previousMenuButton(List<String> lore) {
        ItemStack button = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);

        State nextState = this.clone();
        nextState.setAction(new MenuPreviousAction());

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.GOLD + "PREVIOUS MENU");
        ItemStackUtil.addLore(button, lore);

        return button;
    }

    protected ItemStack nextMenuButton(List<String> lore) {
        ItemStack button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

        State nextState = this.clone();
        nextState.setAction(new MenuNextAction());

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        ItemStackUtil.setDisplayName(button, ChatColor.GREEN + "NEXT MENU");
        ItemStackUtil.addLore(button, lore);

        return button;
    }

    /**
     * Returns a button which exits the user from their menu, using the creator's existing potion.
     */
    protected ItemStack exitButton() {
        Potion existingPotion = this.getPotion();
        ItemStack button = existingPotion.toItemStack();

        State nextState = this.clone();
        nextState.setAction(new ExitSelectAction());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "This is your current potion.");
        lore.add(ChatColor.GREEN + "Left click to save your changes and exit.");
        lore.add(ChatColor.RED + "Right click to exit without saving.");

        ItemStackUtil.setLocalizedName(button, nextState.encodeToString());
        PotionUtil.addLoreRecipes(button, existingPotion);
        ItemStackUtil.addLore(button,lore);

        return button;
    }

    public Map<Integer, ItemStack> calculateButtons() {
        Map<Integer, ItemStack> buttons = new HashMap<>();
        buttons.put(MagicNumber.PREVIOUS_PAGE_SLOT, this.previousPageButton());
        buttons.put(MagicNumber.NEXT_PAGE_SLOT, this.nextPageButton());
        buttons.put(MagicNumber.PREVIOUS_MENU_SLOT, this.previousMenuButton(this.previousMenuLore()));
        buttons.put(MagicNumber.NEXT_MENU_SLOT, this.nextMenuButton(this.nextMenuLore()));
        buttons.put(MagicNumber.EXIT_SLOT, this.exitButton());
        return buttons;
    }

    /**
     * Fetches all potions to be displayed in the menu: should be equal to the menu's page size.
     */
    public abstract List<ItemStack> calculateInventoryItems();

    public void openInventory(Player player) {
        inv.openInventory(this, player);
    }

    /**
     * Encode state object as a string.
     */
    public String encodeToString() {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(this);
            objectOut.close();
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Main.logSevere("There was an error encoding the state to a string.");
            return null;
        }
    }

    /**
     * Constructs a state from a Bukkit ItemStack's localized name.
     */
    public static State decodeFromString(String localizedName) throws IOException, ClassNotFoundException {
        if (localizedName == null || localizedName.isEmpty()) {
            throw new IOException("LocalizedName is null or empty");
        }
        
        try {
            byte[] byteData = Base64.getDecoder().decode(localizedName);
            if (byteData == null || byteData.length == 0) {
                throw new IOException("Decoded byte array is null or empty");
            }
            
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteData);
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            State state = (State) objectIn.readObject();
            objectIn.close();
            return state;
        } catch (IllegalArgumentException e) {
            // Invalid Base64 string
            throw new IOException("Invalid Base64 string: " + e.getMessage(), e);
        } catch (java.io.EOFException e) {
            // Empty or corrupted data
            throw new IOException("Corrupted or empty state data: " + e.getMessage(), e);
        }
    }




}
