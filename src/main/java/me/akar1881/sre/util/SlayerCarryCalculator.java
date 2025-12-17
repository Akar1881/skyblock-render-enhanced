package me.akar1881.sre.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class SlayerCarryCalculator {
    
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.##");
    
    public static final Map<String, Map<Integer, Integer>> SLAYER_XP = new HashMap<>();
    
    static {
        Map<Integer, Integer> zombieXP = new HashMap<>();
        zombieXP.put(1, 5);
        zombieXP.put(2, 25);
        zombieXP.put(3, 100);
        zombieXP.put(4, 500);
        zombieXP.put(5, 1500);
        SLAYER_XP.put("revenant", zombieXP);
        SLAYER_XP.put("zombie", zombieXP);
        
        Map<Integer, Integer> spiderXP = new HashMap<>();
        spiderXP.put(1, 5);
        spiderXP.put(2, 25);
        spiderXP.put(3, 100);
        spiderXP.put(4, 500);
        SLAYER_XP.put("tarantula", spiderXP);
        SLAYER_XP.put("spider", spiderXP);
        
        Map<Integer, Integer> wolfXP = new HashMap<>();
        wolfXP.put(1, 5);
        wolfXP.put(2, 25);
        wolfXP.put(3, 100);
        wolfXP.put(4, 500);
        SLAYER_XP.put("sven", wolfXP);
        SLAYER_XP.put("wolf", wolfXP);
        
        Map<Integer, Integer> endermanXP = new HashMap<>();
        endermanXP.put(1, 5);
        endermanXP.put(2, 25);
        endermanXP.put(3, 100);
        endermanXP.put(4, 500);
        SLAYER_XP.put("voidgloom", endermanXP);
        SLAYER_XP.put("enderman", endermanXP);
        SLAYER_XP.put("seraph", endermanXP);
        
        Map<Integer, Integer> blazeXP = new HashMap<>();
        blazeXP.put(1, 10);
        blazeXP.put(2, 25);
        blazeXP.put(3, 100);
        blazeXP.put(4, 500);
        SLAYER_XP.put("inferno", blazeXP);
        SLAYER_XP.put("blaze", blazeXP);
        SLAYER_XP.put("demonlord", blazeXP);
        
        Map<Integer, Integer> vampireXP = new HashMap<>();
        vampireXP.put(1, 20);
        vampireXP.put(2, 75);
        vampireXP.put(3, 240);
        vampireXP.put(4, 840);
        vampireXP.put(5, 2400);
        SLAYER_XP.put("riftstalker", vampireXP);
        SLAYER_XP.put("bloodfiend", vampireXP);
        SLAYER_XP.put("vampire", vampireXP);
    }
    
    public static double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return 0;
        }
        
        priceStr = priceStr.toLowerCase().replace(",", "").replace(" ", "");
        
        double multiplier = 1;
        if (priceStr.endsWith("k")) {
            multiplier = 1_000;
            priceStr = priceStr.substring(0, priceStr.length() - 1);
        } else if (priceStr.endsWith("m")) {
            multiplier = 1_000_000;
            priceStr = priceStr.substring(0, priceStr.length() - 1);
        } else if (priceStr.endsWith("b")) {
            multiplier = 1_000_000_000;
            priceStr = priceStr.substring(0, priceStr.length() - 1);
        }
        
        try {
            return Double.parseDouble(priceStr) * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public static String formatPrice(double price) {
        if (price >= 1_000_000_000) {
            return PRICE_FORMAT.format(price / 1_000_000_000) + "B";
        } else if (price >= 1_000_000) {
            return PRICE_FORMAT.format(price / 1_000_000) + "M";
        } else if (price >= 1_000) {
            return PRICE_FORMAT.format(price / 1_000) + "K";
        } else {
            return PRICE_FORMAT.format(price);
        }
    }
    
    public static int parseTier(String tierStr) {
        if (tierStr == null || tierStr.isEmpty()) {
            return -1;
        }
        
        tierStr = tierStr.toLowerCase().replace("t", "").replace("tier", "");
        
        try {
            int tier = Integer.parseInt(tierStr);
            if (tier >= 1 && tier <= 5) {
                return tier;
            }
        } catch (NumberFormatException e) {
        }
        return -1;
    }
    
    public static String normalizeSlayerType(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        
        type = type.toLowerCase();
        
        if (type.contains("void") || type.contains("seraph") || type.contains("enderman")) {
            return "Voidgloom Seraph";
        } else if (type.contains("revenant") || type.contains("zombie") || type.contains("horror")) {
            return "Revenant Horror";
        } else if (type.contains("tarantula") || type.contains("spider") || type.contains("brood")) {
            return "Tarantula Broodfather";
        } else if (type.contains("sven") || type.contains("wolf") || type.contains("pack")) {
            return "Sven Packmaster";
        } else if (type.contains("inferno") || type.contains("blaze") || type.contains("demon")) {
            return "Inferno Demonlord";
        } else if (type.contains("rift") || type.contains("blood") || type.contains("vampire")) {
            return "Riftstalker Bloodfiend";
        }
        
        return null;
    }
    
    public static String getSlayerKey(String normalizedType) {
        if (normalizedType == null) return null;
        
        return switch (normalizedType) {
            case "Voidgloom Seraph" -> "voidgloom";
            case "Revenant Horror" -> "revenant";
            case "Tarantula Broodfather" -> "tarantula";
            case "Sven Packmaster" -> "sven";
            case "Inferno Demonlord" -> "inferno";
            case "Riftstalker Bloodfiend" -> "riftstalker";
            default -> null;
        };
    }
    
    public static int getXPPerBoss(String slayerKey, int tier) {
        Map<Integer, Integer> tierMap = SLAYER_XP.get(slayerKey.toLowerCase());
        if (tierMap == null) return 0;
        return tierMap.getOrDefault(tier, 0);
    }
    
    public static int calculateBossesNeeded(int currentXP, int targetXP, int xpPerBoss) {
        if (xpPerBoss <= 0) return -1;
        int needed = targetXP - currentXP;
        if (needed <= 0) return 0;
        return (int) Math.ceil((double) needed / xpPerBoss);
    }
    
    public static class SlayerCalcResult {
        public final String type;
        public final int tier;
        public final int amount;
        public final double priceEach;
        public final int discountPercent;
        public final double totalPrice;
        
        public SlayerCalcResult(String type, int tier, int amount, double priceEach, int discountPercent) {
            this.type = type;
            this.tier = tier;
            this.amount = amount;
            this.priceEach = priceEach;
            this.discountPercent = discountPercent;
            
            double baseTotal = priceEach * amount;
            if (discountPercent > 0) {
                this.totalPrice = baseTotal * (1 - discountPercent / 100.0);
            } else {
                this.totalPrice = baseTotal;
            }
        }
    }
    
    public static class XPCalcResult {
        public final String type;
        public final int tier;
        public final int currentXP;
        public final int targetXP;
        public final int xpPerBoss;
        public final int bossesNeeded;
        
        public XPCalcResult(String type, int tier, int currentXP, int targetXP, int xpPerBoss, int bossesNeeded) {
            this.type = type;
            this.tier = tier;
            this.currentXP = currentXP;
            this.targetXP = targetXP;
            this.xpPerBoss = xpPerBoss;
            this.bossesNeeded = bossesNeeded;
        }
    }
}
