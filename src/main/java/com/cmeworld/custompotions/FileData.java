package com.cmeworld.custompotions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import com.cmeworld.custompotions.utils.PotionUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileData {
    private Main pluginInstance;
    private String filePath;
    private List<Potion> potionCache;

    public FileData(Main pluginInstance) {
        this.pluginInstance = pluginInstance;
        this.filePath = this.pluginInstance.getDataFolder().getAbsolutePath() + "/potions.json";
        reloadData();
    }

    public void reloadData() {
        // Ensure data folder exists
        File dataFolder = this.pluginInstance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Save default file if it does not exist
        File file = new File(this.filePath);
        if (!file.exists()) {
            // Try to save default file from resources
            if (this.pluginInstance.getResource("potions.json") != null) {
                this.pluginInstance.saveResource("potions.json", false);
            } else {
                // If no default file exists, create empty array
                try {
                    Writer writer = new FileWriter(this.filePath);
                    writer.write("[]");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Refresh cache
        readData();
    }

    private void readData() {
        List<Potion> potions = new ArrayList<>();
        File file = new File(this.filePath);

        try {
            Reader reader = new FileReader(file);
            List<Potion> all = Arrays.asList(new Gson().fromJson(reader, Potion[].class));

            List<String> vanillaPotionIds = PotionUtil.getVanillaPotionIds();
            for (Potion potion : all) {
                String name = potion.getName();

                // Potion ID
                String id = potion.getPotionId();
                if (Objects.equals(id, "NoIdError")) {
                    Main.logWarning("A potion is missing its ID. Skipping the potion for now...");
                    continue;
                } else if (vanillaPotionIds.contains(id)) {
                    Main.logWarning(id + " is the potion ID of a Vanilla potion. Skipping the potion for now...");
                    continue;
                }

                // Type
                if (!PotionUtil.isPotion(potion.getType())) {
                    Main.logWarning(name + ChatColor.RESET + " does not have a valid type. " +
                        "Must be POTION, SPLASH_POTION or LINGERING_POTION. Skipping the potion for now...");
                    continue;
                }

                // Recipes
                List<PotionRecipe> validRecipes = new ArrayList<>();
                for (PotionRecipe potionRecipe: potion.getRecipes()) {
                    String base = potionRecipe.getBase();
                    if (all.stream().noneMatch(p -> Objects.equals(p.getPotionId(), base)) && !vanillaPotionIds.contains(base)) {
                        Main.logWarning(base + " is not a valid base potion for a recipe of " + name
                            + ChatColor.RESET + ". Skipping the recipe for now...");
                        continue;
                    }

                    if (Objects.equals(id, base)) {
                        Main.logWarning(name + ChatColor.RESET + " cannot use itself as a base in a recipe. "
                            + "Skipping the recipe for now...");
                        continue;
                    }

                    // TODO check that the recipe does not already exist for another potion. if it does, skip the recipe

                    validRecipes.add(potionRecipe);
                }
                // Prevent combining glowstone dust and redstone on the same potion
                List<PotionRecipe> filteredRecipes = new ArrayList<>();
                boolean hasGlowstoneRecipe = false;
                boolean hasRedstoneRecipe = false;
                for (PotionRecipe recipe : validRecipes) {
                    Material ingredient = recipe.getIngredient();
                    if (ingredient == Material.GLOWSTONE_DUST) {
                        if (hasRedstoneRecipe) {
                            Main.logWarning(name + ChatColor.RESET + " already has a Redstone recipe. Skipping the Glowstone recipe to prevent combining upgrades.");
                            continue;
                        }
                        hasGlowstoneRecipe = true;
                    } else if (ingredient == Material.REDSTONE) {
                        if (hasGlowstoneRecipe) {
                            Main.logWarning(name + ChatColor.RESET + " already has a Glowstone recipe. Skipping the Redstone recipe to prevent combining upgrades.");
                            continue;
                        }
                        hasRedstoneRecipe = true;
                    }
                    filteredRecipes.add(recipe);
                }

                potion.setRecipes(filteredRecipes);

                Main.logInfo("Successfully added " + name + ChatColor.RESET + " to the game.");

                // All valid
                potions.add(potion);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            Main.logSevere("A potion had an invalid number input. Please ensure all numbers are positive 32-bit integers. Skipping ALL potions for now...");
        }
        this.potionCache = potions;
    }

    public List<Potion> getCustomPotions() {
        return new ArrayList<>(this.potionCache);
    }

    public void writeData(Potion newPotion) {
        reloadData();
        try {
            List<Potion> potions = getCustomPotions();
            potions.removeIf(potion -> Objects.equals(potion.getPotionId(), newPotion.getPotionId()));
            potions.add(newPotion);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = new FileWriter(this.filePath, false);
            gson.toJson(potions, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadData();
    }

}
