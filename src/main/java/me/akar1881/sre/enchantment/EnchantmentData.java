package me.akar1881.sre.enchantment;

import java.util.*;

public class EnchantmentData {
    
    public enum ItemType {
        SWORD,
        BOW
    }
    
    public static class EnchantmentInfo {
        public final String name;
        public final int maxLevel;
        public final String description;
        public final ItemType itemType;
        
        public EnchantmentInfo(String name, int maxLevel, String description, ItemType itemType) {
            this.name = name;
            this.maxLevel = maxLevel;
            this.description = description;
            this.itemType = itemType;
        }
        
        public String getDisplayName() {
            return name;
        }
        
        public String getLevelRange() {
            if (maxLevel == 1) {
                return "I";
            }
            return "I-" + toRoman(maxLevel);
        }
    }
    
    private static final Map<String, EnchantmentInfo> BOW_ENCHANTMENTS = new LinkedHashMap<>();
    private static final Map<String, EnchantmentInfo> SWORD_ENCHANTMENTS = new LinkedHashMap<>();
    private static final Map<String, EnchantmentInfo> BOW_ULTIMATE_ENCHANTMENTS = new LinkedHashMap<>();
    private static final Map<String, EnchantmentInfo> SWORD_ULTIMATE_ENCHANTMENTS = new LinkedHashMap<>();
    
    static {
        BOW_ENCHANTMENTS.put("Chance", new EnchantmentInfo("Chance", 5, "Increases the chance of a Monster dropping an item by 15-75%.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Cubism", new EnchantmentInfo("Cubism", 6, "Increases damage dealt to Magma Cubes, Slimes, and Creepers by 10-80%.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Dragon Tracer", new EnchantmentInfo("Dragon Tracer", 5, "Arrows home towards dragons if they are within 2-10 blocks.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Flame", new EnchantmentInfo("Flame", 1, "Arrows ignite your enemies for 3.5-4s, dealing 3-6% of your damage per second.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Gravity", new EnchantmentInfo("Gravity", 6, "Reduces arrow drop.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Impaling", new EnchantmentInfo("Impaling", 5, "Increases damage dealt to Sea Creatures by 25-75%.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Infinite Quiver", new EnchantmentInfo("Infinite Quiver", 10, "Saves arrows 3-30% of the time when you fire your bow. Disabled while sneaking.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Piercing", new EnchantmentInfo("Piercing", 1, "Arrows travel through enemies. The extra targets hit take 25% of the damage.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Overload", new EnchantmentInfo("Overload", 5, "Increases Crit Damage by 1-5% and Crit Chance by 1-5%. Having a Critical chance above 100% grants a chance to perform a Mega Critical Hit dealing 10-50% extra damage.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Power", new EnchantmentInfo("Power", 7, "Increases bow damage by 8-65%.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Punch", new EnchantmentInfo("Punch", 1, "Increases arrow knockback by 3-6 blocks.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Snipe", new EnchantmentInfo("Snipe", 4, "Arrows deal +1-4% damage for every 10 blocks traveled.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Tabasco", new EnchantmentInfo("Tabasco", 3, "Grants +2-3 weapon damage if you don't have a Dragon pet equipped.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Dragon Hunter", new EnchantmentInfo("Dragon Hunter", 5, "Increases damage dealt to Ender Dragons by 8-40%.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Divine Gift", new EnchantmentInfo("Divine Gift", 3, "Grants +2-6 Magic Find.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Toxophilite", new EnchantmentInfo("Toxophilite", 10, "Gain 3-10% extra Combat XP. Grants +3.7-10 Crit Chance.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Smoldering", new EnchantmentInfo("Smoldering", 5, "Increases damage dealt to Blazes by 3-15%.", ItemType.BOW));
        BOW_ENCHANTMENTS.put("Vicious", new EnchantmentInfo("Vicious", 5, "Grants +3-5 Ferocity.", ItemType.BOW));
        
        SWORD_ENCHANTMENTS.put("Bane Of Arthropods", new EnchantmentInfo("Bane Of Arthropods", 5, "Increases damage dealt to Spiders, Cave Spiders, and Silverfish by 10-100%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Champion", new EnchantmentInfo("Champion", 10, "Gain +3-10% extra Combat XP. The 2nd hit on a mob grants +1.4-5 Coins & +7-25 exp orbs.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Cleave", new EnchantmentInfo("Cleave", 6, "Deals 3-20% of your damage dealt to other monsters within 3.3-4.8 blocks of the target.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Critical", new EnchantmentInfo("Critical", 7, "Increases Crit Damage by 10-100%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Cubism", new EnchantmentInfo("Cubism", 6, "Increases damage dealt to Magma Cubes, Slimes, and Creepers by 10-80%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Divine Gift", new EnchantmentInfo("Divine Gift", 3, "Grants +2-6 Magic Find.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Dragon Hunter", new EnchantmentInfo("Dragon Hunter", 5, "Increases damage dealt to Ender Dragons by 8-40%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Ender Slayer", new EnchantmentInfo("Ender Slayer", 7, "Increases damage dealt to Endermen, Endermites, and Ender Dragons by 15-130%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Execute", new EnchantmentInfo("Execute", 6, "Increases damage dealt by 0.2-1.25% for each percent of health missing on your target.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Experience", new EnchantmentInfo("Experience", 5, "Grants a 12.5-62.5% chance for mobs and ores to drop double experience.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Fire Aspect", new EnchantmentInfo("Fire Aspect", 3, "Ignites your enemies for 3-4s, dealing 3-9% of your damage per second.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("First Strike", new EnchantmentInfo("First Strike", 5, "Increases melee damage dealt by 25-125% for the first hit on a mob.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Impaling", new EnchantmentInfo("Impaling", 3, "Increases damage dealt to Sea Creatures by 25-75%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Knockback", new EnchantmentInfo("Knockback", 2, "Increases knockback by 3-6 blocks.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Lethality", new EnchantmentInfo("Lethality", 6, "Reduces the Defense of your target by 1.2-9% for 4s each time you hit them with melee. Stacks up to 4 times.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Life Steal", new EnchantmentInfo("Life Steal", 5, "Heals for 0.5-2.5% of your max health each time you hit a mob.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Looting", new EnchantmentInfo("Looting", 5, "Increases the chance of a Monster dropping an item by 15-75%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Luck", new EnchantmentInfo("Luck", 7, "Increases the chance for Monsters to drop their armor by 5-35%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Mana Steal", new EnchantmentInfo("Mana Steal", 3, "Regain 0.25-0.75% of your mana on hit.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Prosecute", new EnchantmentInfo("Prosecute", 6, "Increases damage dealt by 0.1-1% for each percent of health your target has.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Scavenger", new EnchantmentInfo("Scavenger", 6, "Scavenge 0.3-1.8 Coins per monster level on kill.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Sharpness", new EnchantmentInfo("Sharpness", 7, "Increases melee damage dealt by 5-65%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Smite", new EnchantmentInfo("Smite", 7, "Increases damage dealt to Skeletons, Zombie Pigmen, Withers, and Zombies by 10-100%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Smoldering", new EnchantmentInfo("Smoldering", 5, "Increases damage dealt to Blazes by 3-15%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Drain", new EnchantmentInfo("Drain", 5, "Heals for 0.2-0.6% of your max health per 100 Crit Damage you deal per hit, up to 1,000 Crit Damage.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Tabasco", new EnchantmentInfo("Tabasco", 3, "Grants +2-3 weapon damage if you don't have a Dragon pet equipped.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Thunderbolt", new EnchantmentInfo("Thunderbolt", 6, "Strikes Monsters within 2 blocks with lightning every 3 consecutive hits, dealing 4-25% of your damage.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Thunderlord", new EnchantmentInfo("Thunderlord", 7, "Strikes a Monster with lightning every 3 consecutive hits, dealing 8-60% more damage.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Titan Killer", new EnchantmentInfo("Titan Killer", 7, "Increases damage dealt by 2-20% for every 100 defense your target has up to 6-80%.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Triple-Strike", new EnchantmentInfo("Triple-Strike", 5, "Increases melee damage dealt by 10-50% for the first three hits on a mob.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Vampirism", new EnchantmentInfo("Vampirism", 6, "Heals for 1-6% of your missing health whenever you kill an enemy.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Venomous", new EnchantmentInfo("Venomous", 6, "Reduces the target's walk speed by 5-30% and deals +0.3-1.8% of your damage per second per hit, stacking globally up to 40 hits. Lasts 5s.", ItemType.SWORD));
        SWORD_ENCHANTMENTS.put("Vicious", new EnchantmentInfo("Vicious", 5, "Grants +3-5 Ferocity.", ItemType.SWORD));
        
        BOW_ULTIMATE_ENCHANTMENTS.put("Ultimate Wise", new EnchantmentInfo("Ultimate Wise", 5, "Reduces the ability mana cost of this item by 10-50%.", ItemType.BOW));
        BOW_ULTIMATE_ENCHANTMENTS.put("Swarm", new EnchantmentInfo("Swarm", 5, "Increases your damage by 2-10% for each enemy within 10 blocks. Maximum of 10 enemies.", ItemType.BOW));
        BOW_ULTIMATE_ENCHANTMENTS.put("Soul Eater", new EnchantmentInfo("Soul Eater", 5, "Your weapon gains 2-10x the Damage of the latest monster killed and applies it on your next hit.", ItemType.BOW));
        BOW_ULTIMATE_ENCHANTMENTS.put("Rend", new EnchantmentInfo("Rend", 5, "Use Left Click ability to rip your arrows out of nearby enemies. Each arrow deals 5-25% of your last critical shot on the target, up to 5 arrows. 2s Cooldown.", ItemType.BOW));
        BOW_ULTIMATE_ENCHANTMENTS.put("Inferno", new EnchantmentInfo("Inferno", 5, "Every 10th hit on a mob traps it for 5s and deals 1.25-2.25x of that hit's damage over the trap duration.", ItemType.BOW));
        BOW_ULTIMATE_ENCHANTMENTS.put("Fatal Tempo", new EnchantmentInfo("Fatal Tempo", 5, "Attacking increases your Ferocity by 10-50% per hit, capped at 200% for 3 seconds after your last attack.", ItemType.BOW));
        BOW_ULTIMATE_ENCHANTMENTS.put("Duplex", new EnchantmentInfo("Duplex", 5, "Shoot a second arrow dealing 4-20% of the first arrow's damage. Targets hit take 1.1-1.5x fire damage for 60s.", ItemType.BOW));
        
        SWORD_ULTIMATE_ENCHANTMENTS.put("Ultimate Wise", new EnchantmentInfo("Ultimate Wise", 5, "Reduces the ability mana cost of this item by 10-50%.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Ultimate Jerry", new EnchantmentInfo("Ultimate Jerry", 5, "Increases the base damage of Aspect of the Jerry by 1,000-5,000%.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Swarm", new EnchantmentInfo("Swarm", 5, "Increases your damage by 2-10% for each enemy within 10 blocks. Maximum of 10 enemies.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Soul Eater", new EnchantmentInfo("Soul Eater", 5, "Your weapon gains 2-10x the Damage of the latest monster killed and applies it on your next hit.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("One For All", new EnchantmentInfo("One For All", 1, "Removes all other enchants but increases your damage by 500%.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Inferno", new EnchantmentInfo("Inferno", 5, "Every 10th hit on a mob traps it for 5s and deals 1.25-2.25x of that hit's damage over the trap duration.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Fatal Tempo", new EnchantmentInfo("Fatal Tempo", 5, "Attacking increases your Ferocity by 10-50% per hit, capped at 200% for 3 seconds after your last attack.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Combo", new EnchantmentInfo("Combo", 5, "Increases your damage by +1-5% per kill up to 2-10 kills within 2-10s.", ItemType.SWORD));
        SWORD_ULTIMATE_ENCHANTMENTS.put("Chimera", new EnchantmentInfo("Chimera", 5, "Copies +20-100% of your active pet's stats.", ItemType.SWORD));
    }
    
    public static Map<String, EnchantmentInfo> getBowEnchantments() {
        return Collections.unmodifiableMap(BOW_ENCHANTMENTS);
    }
    
    public static Map<String, EnchantmentInfo> getSwordEnchantments() {
        return Collections.unmodifiableMap(SWORD_ENCHANTMENTS);
    }
    
    public static Map<String, EnchantmentInfo> getBowUltimateEnchantments() {
        return Collections.unmodifiableMap(BOW_ULTIMATE_ENCHANTMENTS);
    }
    
    public static Map<String, EnchantmentInfo> getSwordUltimateEnchantments() {
        return Collections.unmodifiableMap(SWORD_ULTIMATE_ENCHANTMENTS);
    }
    
    public static Map<String, EnchantmentInfo> getUltimateEnchantments(ItemType type) {
        if (type == ItemType.BOW) {
            return Collections.unmodifiableMap(BOW_ULTIMATE_ENCHANTMENTS);
        } else {
            return Collections.unmodifiableMap(SWORD_ULTIMATE_ENCHANTMENTS);
        }
    }
    
    public static EnchantmentInfo getEnchantment(String name, ItemType type) {
        if (type == ItemType.BOW) {
            return BOW_ENCHANTMENTS.get(name);
        } else {
            return SWORD_ENCHANTMENTS.get(name);
        }
    }
    
    public static EnchantmentInfo getUltimateEnchantment(String name, ItemType type) {
        if (type == ItemType.BOW) {
            return BOW_ULTIMATE_ENCHANTMENTS.get(name);
        } else {
            return SWORD_ULTIMATE_ENCHANTMENTS.get(name);
        }
    }
    
    public static boolean isUltimateEnchantment(String name, ItemType type) {
        if (type == ItemType.BOW) {
            return BOW_ULTIMATE_ENCHANTMENTS.containsKey(name);
        } else {
            return SWORD_ULTIMATE_ENCHANTMENTS.containsKey(name);
        }
    }
    
    public static String toRoman(int number) {
        if (number <= 0 || number > 10) {
            return String.valueOf(number);
        }
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return romans[number - 1];
    }
    
    public static int fromRoman(String roman) {
        return switch (roman.toUpperCase()) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            default -> 0;
        };
    }
}
