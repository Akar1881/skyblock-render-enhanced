package me.akar1881.sre.enchantment;

import me.akar1881.sre.enchantment.EnchantmentData.EnchantmentInfo;
import me.akar1881.sre.enchantment.EnchantmentData.ItemType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantmentHelper {
    
    private static final Set<String> SWORD_NAMES = new HashSet<>();
    private static final Set<String> BOW_NAMES = new HashSet<>();
    
    static {
        String[] commonSwords = {
            "Aspect Of The Jerry", "Fancy Sword", "Golden Sword", "Iron Sword", "Rogue Sword", 
            "Spider Sword", "Stone Sword", "Undead Sword", "Wooden Sword",
            "Cleaver", "Diamond Sword", "End Sword", "Flaming Sword", "Hunter Knife", 
            "Prismarine Blade", "Recluse Fang", "Silver Fang", "Squire Sword", 
            "Sword Of Bad Health", "Voidwalker Katana", "Wyld Sword",
            "Aspect Of The End", "Bingolibur", "Blade Of The Volcano", "Bonzo's Staff", 
            "Conjuring", "Dreadlord Sword", "Edible Mace", "Firedust Dagger", "Frozen Scythe", 
            "Golem Sword", "Ice Spray Wand", "Leech Sword", "Mercenary Axe", "Ragnarock", 
            "Raider Axe", "Revenant Falchion", "Self-Recursive Pickaxe", "Silent Death", 
            "Steak Stake", "Super Cleaver", "Tactician's Murder Weapon", "Tactician's Sword", 
            "Tarantula Fang", "Tribal Spear", "Twilight Dagger", "Voidedge Katana", 
            "Void Sword", "Zombie Soldier Cutlass", "Zombie Sword",
            "Adaptive Blade", "Arack", "Aspect Of The Void", "Earth Shard", "Ember Rod", 
            "Emerald Blade", "End Stone Sword", "Enrager", "Fel Sword", "Fire Freeze Staff", 
            "Fire Fury Staff", "Ghoul Buster", "Giant Cleaver", "Glacial Scythe", 
            "Hyper Cleaver", "Ink Wand", "Jerry-chine Gun", "Kindlebane Dagger", 
            "Leaping Sword", "Mawdredge Dagger", "Ornate Zombie Sword", "Reaper Falchion", 
            "Scorpion Foil", "Shaman Sword", "Silver-Laced Karambit", "Silver-Twist Karambit", 
            "Sinseeker Scythe", "Spirit Sword", "Sword Of Revelations", "Vorpal Katana",
            "Wither Cloak", "Zombie Commander Whip", "Zombie Knight Sword",
            "Aspect Of The Dragons", "Astraea", "Atomsplit Katana", "Aurora Staff", 
            "Halberd Of The Shredded", "Daedalus Blade", "Dark Claymore", "Deathripper Dagger", 
            "Florid Zombie Sword", "Flower Of Truth", "Giant's Sword", "Hyperion", 
            "Livid Dagger", "Midas Staff", "Midas' Sword", "Necromancer Sword", 
            "Necron's Blade", "Necron's Blade (Unrefined)", "Pigman Sword", "Pooch Sword", 
            "Pyrochaos Dagger", "Reaper Scythe", "Scylla", "Shadow Fury", "Silk-Edge Sword", 
            "Soul Whip", "Spirit Sceptre", "Sting", "Valkyrie", "Yeti Sword"
        };
        
        String[] commonBows = {
            "Bow",
            "Decent Bow", "Prismarine Bow", "Savanna Bow", "Wither Bow",
            "Artisanal Shortbow", "Ender Bow", "Machine Gun Shortbow", "Sniper Bow", 
            "Soulstealer Bow", "Undead Bow",
            "Death Bow", "Dragon Shortbow", "End Stone Bow", "Explosive Bow", 
            "Hurricane Bow", "Juju Shortbow", "Magma Bow", "Scorpion Bow", "Slime Bow", 
            "Souls Rebound", "Spider Queen's Stinger", "Sulphur Bow", "Super Undead Bow", 
            "Venom's Touch",
            "Bonemerang", "Last Breath", "Mosquito Shortbow", "Runaan's Bow", 
            "Spirit Shortbow", "Terminator"
        };
        
        for (String sword : commonSwords) {
            SWORD_NAMES.add(sword.toLowerCase());
        }
        for (String bow : commonBows) {
            BOW_NAMES.add(bow.toLowerCase());
        }
    }
    
    public static ItemType detectItemType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        
        String displayName = stack.getName().getString().toLowerCase();
        displayName = displayName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        for (String bowName : BOW_NAMES) {
            if (displayName.contains(bowName)) {
                return ItemType.BOW;
            }
        }
        
        for (String swordName : SWORD_NAMES) {
            if (displayName.contains(swordName)) {
                return ItemType.SWORD;
            }
        }
        
        String itemId = stack.getItem().toString().toLowerCase();
        if (itemId.contains("bow")) {
            return ItemType.BOW;
        }
        if (itemId.contains("sword")) {
            return ItemType.SWORD;
        }
        
        return null;
    }
    
    public static Map<String, Integer> parseEnchantmentsFromLore(ItemStack stack) {
        Map<String, Integer> enchantments = new HashMap<>();
        
        if (stack == null || stack.isEmpty()) {
            return enchantments;
        }
        
        List<String> loreLines = getLoreLines(stack);
        
        Pattern enchantPattern = Pattern.compile("([A-Za-z][A-Za-z\\s-]+)\\s+(I{1,3}V?|VI{0,4}|IX|X)(?:,|$)");
        
        for (String line : loreLines) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            
            Matcher matcher = enchantPattern.matcher(cleanLine);
            while (matcher.find()) {
                String enchantName = matcher.group(1).trim();
                String level = matcher.group(2);
                int levelNum = EnchantmentData.fromRoman(level);
                
                if (levelNum > 0) {
                    enchantments.put(enchantName, levelNum);
                }
            }
        }
        
        return enchantments;
    }
    
    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        
        if (stack == null || stack.isEmpty()) {
            return lines;
        }
        
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return lines;
        }
        
        List<Text> loreTexts = loreComponent.lines();
        for (Text text : loreTexts) {
            if (text != null) {
                lines.add(text.getString());
            }
        }
        
        return lines;
    }
    
    public static class MissingEnchantment {
        public final String name;
        public final int currentLevel;
        public final int maxLevel;
        public final List<Integer> missingLevels;
        
        public MissingEnchantment(String name, int currentLevel, int maxLevel) {
            this.name = name;
            this.currentLevel = currentLevel;
            this.maxLevel = maxLevel;
            this.missingLevels = new ArrayList<>();
            for (int i = currentLevel + 1; i <= maxLevel; i++) {
                missingLevels.add(i);
            }
        }
        
        public String getMissingLevelsString() {
            if (missingLevels.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < missingLevels.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(EnchantmentData.toRoman(missingLevels.get(i)));
            }
            return sb.toString();
        }
    }
    
    public static List<MissingEnchantment> getMissingEnchantments(ItemStack stack) {
        List<MissingEnchantment> missing = new ArrayList<>();
        
        ItemType type = detectItemType(stack);
        if (type == null) {
            return missing;
        }
        
        Map<String, Integer> currentEnchants = parseEnchantmentsFromLore(stack);
        Map<String, EnchantmentInfo> allEnchants = type == ItemType.BOW 
            ? EnchantmentData.getBowEnchantments() 
            : EnchantmentData.getSwordEnchantments();
        
        for (Map.Entry<String, EnchantmentInfo> entry : allEnchants.entrySet()) {
            String enchantName = entry.getKey();
            EnchantmentInfo info = entry.getValue();
            
            int currentLevel = 0;
            for (Map.Entry<String, Integer> current : currentEnchants.entrySet()) {
                if (current.getKey().equalsIgnoreCase(enchantName) || 
                    current.getKey().toLowerCase().contains(enchantName.toLowerCase()) ||
                    enchantName.toLowerCase().contains(current.getKey().toLowerCase())) {
                    currentLevel = current.getValue();
                    break;
                }
            }
            
            if (currentLevel < info.maxLevel) {
                missing.add(new MissingEnchantment(enchantName, currentLevel, info.maxLevel));
            }
        }
        
        return missing;
    }
    
    public static ItemStack getHeldItem() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return ItemStack.EMPTY;
        }
        return client.player.getMainHandStack();
    }
    
    public static class UltimateEnchantmentResult {
        public final boolean hasUltimate;
        public final String appliedName;
        public final int appliedLevel;
        public final int maxLevel;
        public final boolean isMaxLevel;
        public final Map<String, EnchantmentInfo> availableUltimates;
        
        public UltimateEnchantmentResult(boolean hasUltimate, String appliedName, int appliedLevel, 
                                          int maxLevel, Map<String, EnchantmentInfo> availableUltimates) {
            this.hasUltimate = hasUltimate;
            this.appliedName = appliedName;
            this.appliedLevel = appliedLevel;
            this.maxLevel = maxLevel;
            this.isMaxLevel = appliedLevel >= maxLevel;
            this.availableUltimates = availableUltimates;
        }
        
        public int getNextLevel() {
            return appliedLevel + 1;
        }
    }
    
    public static UltimateEnchantmentResult getUltimateEnchantmentStatus(ItemStack stack) {
        ItemType type = detectItemType(stack);
        if (type == null) {
            return null;
        }
        
        Map<String, Integer> currentEnchants = parseEnchantmentsFromLore(stack);
        Map<String, EnchantmentInfo> ultimateEnchants = EnchantmentData.getUltimateEnchantments(type);
        
        for (Map.Entry<String, Integer> current : currentEnchants.entrySet()) {
            String enchantName = current.getKey();
            
            for (Map.Entry<String, EnchantmentInfo> ultEntry : ultimateEnchants.entrySet()) {
                String ultName = ultEntry.getKey();
                EnchantmentInfo ultInfo = ultEntry.getValue();
                
                if (enchantName.equalsIgnoreCase(ultName) || 
                    enchantName.toLowerCase().contains(ultName.toLowerCase()) ||
                    ultName.toLowerCase().contains(enchantName.toLowerCase())) {
                    return new UltimateEnchantmentResult(true, ultName, current.getValue(), 
                                                         ultInfo.maxLevel, ultimateEnchants);
                }
            }
        }
        
        return new UltimateEnchantmentResult(false, null, 0, 0, ultimateEnchants);
    }
}
