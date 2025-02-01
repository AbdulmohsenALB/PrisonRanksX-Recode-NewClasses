package me.prisonranksx.bukkitutils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackParser {

    private static String c(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack parse(String stringValue) {
        ItemStack itemStack = new ItemStack(Material.STONE, 1);
        ItemMeta stackMeta = itemStack.getItemMeta();
        int amount = 1;
        int data = 0;
        String displayName = "";
        List<String> lore = new ArrayList<>();
        for (String stringMeta : stringValue.split(" ")) {
            // Three formats for items with data:
            // material=WOOL:1
            // material=WOOL#1
            // material=WOOL data=1
            if (stringMeta.startsWith("material=")) {
                String itemNameWithData = stringMeta.substring(9);
                if (itemNameWithData.contains("#")) {
                    String[] itemNameAndDataSplit = itemNameWithData.split("#");
                    String itemName = itemNameAndDataSplit[0];
                    short itemData = Short.parseShort(itemNameAndDataSplit[1]);
                    itemStack = new ItemStack(Material.matchMaterial(itemName), 1, itemData);
                } else {
                    String itemName = itemNameWithData;
                    itemStack = XMaterial.matchXMaterial(itemName).get().parseItem();
                }
                stackMeta = itemStack.getItemMeta();
            }
            if (stringMeta.startsWith("amount=")) {
                String stringAmount = stringMeta.substring(7);
                amount = isInt(stringAmount) ? Integer.parseInt(stringMeta) : 1;
                itemStack.setAmount(amount);
            }
            if (stringMeta.startsWith("data=")) {
                String stringData = stringMeta.substring(5);
                if (isInt(stringData)) data = Integer.parseInt(stringData);
                // Custom check for Enchanted Golden Apple due to common mistake.
                if (XMaterial.supports(13) && itemStack.getType() == XMaterial.GOLDEN_APPLE.parseMaterial()) {
                    if (data == 1) itemStack.setType(XMaterial.ENCHANTED_GOLDEN_APPLE.parseMaterial());
                } else {
                    itemStack.setDurability((short) data);
                }
            }
            if (stringMeta.startsWith("name=")) {
                displayName = c(stringMeta.substring(5));
                stackMeta.setDisplayName(displayName.replace("_", " ").replace("%us%", "_"));
            }
            if (stringMeta.startsWith("lore=")) {
                String fullLore = stringMeta.substring(5);
                if (fullLore.contains(",")) {
                    for (String loreLine : fullLore.split("\\,")) lore.add(c(loreLine).replace("_", " ").replace("%us%", "_"));
                } else {
                    lore.add(c(fullLore).replace("_", " ").replace("%us%", "_"));
                }
                stackMeta.setLore(lore);
            }
            if (stringMeta.startsWith("enchantments=")) {
                String fullEnchantmentWithLvl = stringMeta.substring(13);
                if (fullEnchantmentWithLvl.contains(",")) {
                    for (String singleEnchantmentWithLvl : fullEnchantmentWithLvl.split("\\,")) {
                        String[] enchantmentSplit = singleEnchantmentWithLvl.split("\\:");
                        String enchantment = enchantmentSplit[0];
                        int lvl = Integer.parseInt(enchantmentSplit[1]);
                        stackMeta.addEnchant(XEnchantment.matchXEnchantment(enchantment)
                                .orElse(XEnchantment.DURABILITY)
                                .getEnchant(), lvl, true);
                    }
                } else {
                    String[] enchantmentSplit = fullEnchantmentWithLvl.split("\\:");
                    String enchantment = enchantmentSplit[0];
                    int lvl = Integer.parseInt(enchantmentSplit[1]);
                    stackMeta.addEnchant(
                            XEnchantment.matchXEnchantment(enchantment).orElse(XEnchantment.DURABILITY).getEnchant(),
                            lvl, true);
                }
            }
            if (stringMeta.startsWith("flags=")) {
                String flagsList = stringMeta.substring(6);
                if (flagsList.contains(",")) {
                    for (String singleFlag : flagsList.split("\\,"))
                        stackMeta.addItemFlags(ItemFlag.valueOf(singleFlag.toUpperCase()));
                } else {
                    stackMeta.addItemFlags(ItemFlag.valueOf(flagsList.toUpperCase()));
                }
            }
            itemStack.setItemMeta(stackMeta);
        }
        return itemStack;
    }

    public static List<ItemStack> parse(List<String> stringList) {
        List<ItemStack> itemStacks = new ArrayList<>();
        stringList.forEach(string -> itemStacks.add(parse(string)));
        return itemStacks;
    }

}