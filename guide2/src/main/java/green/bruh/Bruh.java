package green.bruh;

import com.dndcraft.atlas.agnostic.Config;
import com.dndcraft.atlas.menu.icon.SimpleButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import com.dndcraft.atlas.menu.MenuBuilder;
import com.dndcraft.atlas.menu.icon.Icon;
import com.dndcraft.atlas.menu.icon.SimpleButton;
import com.dndcraft.atlas.util.ItemUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Bruh extends JavaPlugin implements Listener {

    private Map<Player, Inventory> playerInventories;
    private Map<Player, String> playerLastClicked;
    private Map<Player, String> playerLastClicked2;

    FileConfiguration config = getConfig();
    private File configFile;

    String lastClicked = "";

    Inventory guideMenu;
    @Override
    public void onEnable() {
        //REGISTERS LISTENER
        getServer().getPluginManager().registerEvents(this, this);

        playerInventories = new HashMap<>();
        playerLastClicked = new HashMap<>();
        playerLastClicked2 = new HashMap<>();


        configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
            // You can perform additional operations or modifications on the loaded configuration if needed.
        } else {
            // The file does not exist yet, so you can create a new configuration or load default values.
            config = new YamlConfiguration();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        save();
    }

    String nameOfCategory;
    String lastClicked2;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        Inventory clickedInventory = event.getClickedInventory(); //   <--- inventory getting clicked

        if (clickedInventory != null) {
            Inventory guideMenu = playerInventories.get(player);
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedInventory.equals(guideMenu)) { //<--- if the inventory that the player clicked on was the GuideMenu (AKA level 1)
                event.setCancelled(true);
                lastClicked = clickedItem.getItemMeta().getDisplayName(); //this sets LastClicked category book as the clicked item in the GuideMenu
                playerLastClicked.put(player, lastClicked);
                if (clickedItem.getType() != Material.AIR && clickedItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    Inventory sectionInv = Bukkit.createInventory(null, 9, lastClicked);
                    ConfigurationSection section = config.getConfigurationSection(clickedItem.getItemMeta().getDisplayName());
                    int counterSection = 0;
                    for(String key : section.getKeys(false)) {
                        if ( counterSection <= 8 && (!(key.equals("lore")))) {
                            String path = lastClicked + "." + key;
                            sectionInv.setItem(counterSection, createItemWithCustomName(Material.KNOWLEDGE_BOOK, key, config.getString(path + ".lore")));
                            counterSection++;
                        }
                    }
                    for (int o = 0 ; o < sectionInv.getSize(); o++) {
                        if (sectionInv.getItem(o) == null) {
                            sectionInv.setItem(o, createItemWithCustomName(Material.GRAY_STAINED_GLASS_PANE, " ", null));
                        }
                    }
                    player.openInventory(sectionInv);
                }
                else {
                    player.closeInventory();
                }
            }

            //    this is if the inventory is in the 2nd layer
            else if (playerLastClicked.get(player) != null && event.getView().getTitle().equals(playerLastClicked.get(player))) { //<-- if the player has clicked on the GuideMenu and the event title is the name of the item that the player last clicked on in the guide
                if (event.getCurrentItem().getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    event.setCancelled(true);
                    Inventory section2Inv = Bukkit.createInventory(null, 9, clickedItem.getItemMeta().getDisplayName());
                    lastClicked2 = clickedItem.getItemMeta().getDisplayName();
                    playerLastClicked2.put(player, lastClicked2);
                    ConfigurationSection section2 = config.getConfigurationSection(playerLastClicked.get(player) + "." + clickedItem.getItemMeta().getDisplayName());
                    int counterSection = 0;
                    for(String key : section2.getKeys(false)) {
                        if (counterSection <= 8 && (!(key.equals("lore")))) {
                            ConfigurationSection sect = config.getConfigurationSection(lastClicked + "." + lastClicked2 + "." + key);
                            section2Inv.setItem(counterSection, createItemWithCustomName(Material.BOOK, key, sect.getString("lore")));
                            counterSection++;
                        }
                    }
                    for (int o = 0 ; o < section2Inv.getSize(); o++) {
                        if (section2Inv.getItem(o) == null) {
                            section2Inv.setItem(o, createItemWithCustomName(Material.GRAY_STAINED_GLASS_PANE, " ", null));
                        }
                    }
                    player.openInventory(section2Inv);
                    //nameOfCategory = null;
                }
                else {
                    player.closeInventory();
                }
            }
            if (playerLastClicked2.get(player) != null && event.getView().getTitle().equals(playerLastClicked2.get(player))){
                event.setCancelled(true);
                String tempName = clickedItem.getItemMeta().getDisplayName(); // Woodcutting book for example
                player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.DARK_GREEN + "Your book is:");
                player.sendRawMessage(event.getCurrentItem().getItemMeta().getDisplayName());
                ConfigurationSection sect = config.getConfigurationSection(lastClicked + "." + lastClicked2 + "." + tempName);
                player.sendRawMessage((String) sect.getString("id"));
                lastClicked = null;
                playerLastClicked.put(player, null);
                lastClicked2 = null;
                playerLastClicked2.put(player, null);
                player.closeInventory();
            }

        }

    }

    public void save() {
        try {
            config.save(configFile);
            System.out.println("config.yml file saved successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while saving the config.yml file: " + e.getMessage());
        }
    }

    private void openGuideMenu(Player player) {
        lastClicked = null;
        Inventory guideMenu = Bukkit.createInventory(null, 9, "Guide");

        ConfigurationSection categoriesSection = config.getConfigurationSection("");
        if (categoriesSection != null) {
            int counter = 0;
            for (String category : categoriesSection.getKeys(false)) {
                if (counter <= 8) {
                    ConfigurationSection cat = config.getConfigurationSection(category);
                    guideMenu.setItem(counter, createItemWithCustomName(Material.BOOKSHELF, category, cat.getString("lore")));
                    counter++;
                }
            }
            for (int i = 0 ; i < guideMenu.getSize(); i++) {
                if (guideMenu.getItem(i) == null) {
                    guideMenu.setItem(i, createItemWithCustomName(Material.GRAY_STAINED_GLASS_PANE, " ", null));
                }
            }
        }
        else {
            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "There are no categories");
        }
        player.openInventory(guideMenu);
        playerInventories.put(player, guideMenu);
    }

    public ItemStack createItemWithCustomName(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore != null) {
            List<String> itemlore = new ArrayList<>();
            itemlore.add(lore);
            itemMeta.setLore(itemlore);
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (label.equals("guide")) {
                //    /guide
                if (args.length == 0) {
                    openGuideMenu(player);
                }

                //    /guide help
                if (args.length == 1 && args[0].equals("help")) {
                    player.sendRawMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Guide Help:");
                    player.sendRawMessage("/guide to open the menu");
                    player.sendRawMessage("/guide category add <CATEGORY> to create a new category, AKA the bookshelf menu in the first layer");
                    player.sendRawMessage("/guide category remove <CATEGORY> to remove a category");
                    player.sendRawMessage("/guide section <CATEGORY> add <SECTION> to add a section to an existing category");
                    player.sendRawMessage("/guide section <CATEGORY> remove <SECTION> to remove a section from a category");
                    player.sendRawMessage("/guide category <CATEGORY> section <SECTION> add <BOOK> <ID> to add a character to an existing section in an existing category");
                    player.sendRawMessage("/guide category <CATEGORY> section <SECTION> remove <BOOK> to remove a book from an existing section in an existing category");
                }


                //     /guide category add <CHARACTER> <lore>
                if (args.length >= 3) {
                    if (args[0].equals("category") && args[1].equals("add")) {
                        ConfigurationSection section = config.getConfigurationSection("");
                        if (section.getKeys(false).size() < 9) {

                            String lore = "";
                            for (int i = 3 ; i < args.length; i++) {
                                lore = lore + args[i] + " ";
                            }
                            config.createSection(args[2]);
                            config.createSection(args[2] + ".lore");
                            config.set(args[2] + ".lore", lore);

                            save();
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.GREEN + args[2] + " " + ChatColor.DARK_GREEN + "has been added");
                        }
                        else {
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "Guides are full");
                        }

                    }
                    //    /guide category remove <CHARACTER>
                    if (args[1].equals("remove")) {
                        ConfigurationSection categoryRemove = config.getConfigurationSection("");
                        if (categoryRemove.getKeys(false).size() > 0) {
                            categoryRemove.set(args[2], null);
                            save();
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.GREEN + args[2] + " " + ChatColor.DARK_GREEN + "has been removed");
                        }
                        else {
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "There are no guides to delete");
                        }
                    }

                }
                if (args.length >= 4) {
                    //     /guide section <CHARACTER> remove <KINGDOM>
                    if (args[2].equals("remove")) {
                        ConfigurationSection removeThis = config.getConfigurationSection(args[1]);
                        if (removeThis.getKeys(false).size() > 0) {
                            String pathRemove = args[1] + "." + args[3]; //<CHARACTER>.<KINGDOM>
                            config.set(pathRemove, null);
                            save();
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.GREEN + args[3] + " " + ChatColor.DARK_GREEN + "has been removed from " + ChatColor.YELLOW + args[1]);
                        }
                        else {
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "There are no sections to delete");
                        }
                    }

                    //     /guide section <CHARACTER> add <KINGDOMS> <lore>
                    if (args[2].equals("add")) {
                        ConfigurationSection addThis = config.getConfigurationSection(args[1]);
                        if (addThis.getKeys(false).size() < 9) {
                            String path = args[1] + "." + args[3];
                            config.createSection(path);
                            String lore = "";
                            for (int i = 4 ; i < args.length; i++) {
                                lore = lore + args[i] + " ";
                            }
                            config.createSection(path + ".lore");
                            config.set(path + ".lore", lore);
                            save();
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.GREEN + args[3] + " " + ChatColor.DARK_GREEN + "has been added to " + ChatColor.YELLOW + args[1]);
                        }
                        else {
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "This category is full");
                        }
                    }
                }

                if (args.length >= 6) {
                    //     /guide category <CHARACTER> section <KINGDOM> remove <SURGEON>
                    if (args[4].equals("remove")) {
                        String sectionRemove = args[1] + "." + args[3] + "." + args[5]; //Character.Kingdom.Surgeon
                        ConfigurationSection removeBook = config.getConfigurationSection(args[1] + "." + args[3]);
                        if (removeBook.getKeys(false).size() >= 1) {
                            config.set(sectionRemove, null);
                            save();
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.GREEN + args[5] + ChatColor.DARK_GREEN + " has been removed from " + ChatColor.YELLOW + args[3]);
                        }
                        else {
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "There are no books to delete");
                        }
                    }


                    //     /guide category <CHARACTER> section <KINGDOMS> add <SURGEON> <ID> <lore>
                    if (args[4].equals("add")) {
                        ConfigurationSection addThisBook = config.getConfigurationSection(args[1] + "." + args[3]);
                        if (addThisBook.getKeys(false).size() < 9) {
                            String path = args[1] + "." + args[3] + "." + args[5];  //Character.Kingdom.Surgeon
                            config.createSection(path);
                            config.createSection(path + ".id");
                            config.set(path + ".id", args[6]);
                            String lore = "";
                            for (int i = 7 ; i < args.length; i++) {
                                lore = lore + args[i] + " ";
                            }
                            config.createSection(path + ".lore");
                            config.set(path + ".lore", lore);
                            save();
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.GREEN + args[5] + ChatColor.DARK_GREEN + " has been added to " + ChatColor.YELLOW + args[3]);
                        }
                        else {
                            player.sendRawMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Guide] " + ChatColor.RED + "This section is full");
                        }
                    }
                }

            }
        }

        return false;
    }
}
