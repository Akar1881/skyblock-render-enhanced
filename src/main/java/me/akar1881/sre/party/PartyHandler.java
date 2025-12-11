package me.akar1881.sre.party;

import me.akar1881.sre.SkyblockRenderEnhanced;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v2.s2c.PartyInfoS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PartyHandler {
    private static final Set<UUID> partyMemberUuids = Collections.synchronizedSet(new HashSet<>());
    private static boolean inParty = false;
    private static long lastRequestTime = 0;
    private static final long REQUEST_COOLDOWN = 2000;
    
    public static void register() {
        HypixelPacketEvents.PARTY_INFO.register(PartyHandler::handlePartyInfo);
        SkyblockRenderEnhanced.LOGGER.info("Registered party info callback");
    }
    
    private static void handlePartyInfo(HypixelS2CPacket packet) {
        switch (packet) {
            case PartyInfoS2CPacket(var isInParty, var members) -> {
                inParty = isInParty;
                partyMemberUuids.clear();
                
                if (isInParty && members != null) {
                    partyMemberUuids.addAll(members.keySet());
                    SkyblockRenderEnhanced.LOGGER.info("Party updated with {} members", members.size());
                } else {
                    SkyblockRenderEnhanced.LOGGER.info("Not in a party");
                }
            }
            case ErrorS2CPacket(var id, var errorReason) -> {
                SkyblockRenderEnhanced.LOGGER.warn("Party info error: {}", errorReason);
            }
            default -> {}
        }
    }
    
    public static void requestPartyInfo() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < REQUEST_COOLDOWN) {
            return;
        }
        
        try {
            HypixelNetworking.sendPartyInfoC2SPacket(2);
            lastRequestTime = currentTime;
        } catch (UnsupportedOperationException e) {
            SkyblockRenderEnhanced.LOGGER.debug("Cannot request party info: not on Hypixel");
        }
    }
    
    public static boolean isInParty() {
        return inParty;
    }
    
    public static Set<UUID> getPartyMemberUuids() {
        return Collections.unmodifiableSet(new HashSet<>(partyMemberUuids));
    }
    
    public static boolean isPartyMember(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity localPlayer = client.player;
        if (localPlayer != null && localPlayer.getUuid().equals(uuid)) {
            return true;
        }
        
        return partyMemberUuids.contains(uuid);
    }
    
    public static int getPartySize() {
        return partyMemberUuids.size();
    }
    
    public static void clearPartyData() {
        partyMemberUuids.clear();
        inParty = false;
    }
}
