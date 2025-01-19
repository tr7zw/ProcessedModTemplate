package dev.tr7zw.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.VertexConsumer;

//#if MC >= 11903
import net.minecraft.core.registries.BuiltInRegistries;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.math.Axis;
//#else
//$$ import net.minecraft.core.Registry;
//$$ import com.mojang.math.Vector3f;
//$$ import com.mojang.math.Matrix4f;
//#endif
// Skins
//#if MC >= 12002
import net.minecraft.client.resources.PlayerSkin;
//#else
//$$ import com.mojang.authlib.minecraft.MinecraftProfileTexture;
//$$ import java.util.Map;
//#endif
//#if MC <= 12004
//$$ import net.minecraft.Util;
//$$ import org.apache.commons.lang3.StringUtils;
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import net.minecraft.nbt.NbtUtils;
//#else
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
//#endif

public class NMSHelper {

    private final static Minecraft MC = Minecraft.getInstance();
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2);
    public static final float TWO_PI = (float) (Math.PI * 2);
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

	//#if MC >= 11903
	public static Axis XN = f -> new Quaternionf().rotationX(-f);
	public static Axis XP = f -> new Quaternionf().rotationX(f);
	public static Axis YN = f -> new Quaternionf().rotationY(-f);
	public static Axis YP = f -> new Quaternionf().rotationY(f);
	public static Axis ZN = f -> new Quaternionf().rotationZ(-f);
	public static Axis ZP = f -> new Quaternionf().rotationZ(f);
	//#else
	//$$ public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
	//$$ public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
	//$$ public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
	//$$ public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
	//$$ public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
	//$$ public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
	//#endif

    public static ResourceLocation getResourceLocation(String namespace, String path) {
        //#if MC >= 12100
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //#else
        //$$ return new ResourceLocation(namespace, path);
        //#endif
    }

    public static ResourceLocation getResourceLocation(String key) {
        //#if MC >= 12100
        return ResourceLocation.parse(key);
        //#else
        //$$ return new ResourceLocation(key);
        //#endif
    }

    public static Item getItem(ResourceLocation key) {
        //#if MC >= 12102
        return BuiltInRegistries.ITEM.get(key).map(net.minecraft.core.Holder.Reference::value).orElse(Items.AIR);
    	//#elseif MC >= 11903
        //$$ return BuiltInRegistries.ITEM.get(key);
        //#else
        //$$ return Registry.ITEM.get(key);
        //#endif
    }


    public static float getXRot(Entity ent) {
    	//#if MC >= 11700
    	return ent.getXRot();
    	//#else
    	//$$ return ent.xRot;
    	//#endif
    }

    public static float getYRot(Entity ent) {
    	//#if MC >= 11700
    	return ent.getYRot();
    	//#else
    	//$$ return ent.yRot;
    	//#endif
    }

    public static void setXRot(Entity ent, float xRot) {
    	//#if MC >= 11700
    	ent.setXRot(xRot);
    	//#else
    	//$$ ent.xRot = xRot;
    	//#endif
    }

    public static void setYRot(Entity ent, float yRot) {
    	//#if MC >= 11700
    	ent.setYRot(yRot);
    	//#else
    	//$$ ent.yRot = yRot;
    	//#endif
    }

    public static ResourceLocation getPlayerSkin(AbstractClientPlayer player) {
        //#if MC >= 12002
        return player.getSkin().texture();
        //#else
        //$$ return player.getSkinTextureLocation();
        //#endif
    }

    public static ResourceLocation getPlayerSkin(GameProfile gameprofile) {
        //#if MC >= 12002
        PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(gameprofile);
        if (playerSkin.textureUrl() == null) {
            return null;
        }
        return playerSkin.texture();
        //#else
        //$$ Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Minecraft.getInstance().getSkinManager()
        //$$         .getInsecureSkinInformation(gameprofile);
        //$$ MinecraftProfileTexture texture = map.get(MinecraftProfileTexture.Type.SKIN);
        //$$  if (texture == null) {
        //$$      return null;
        //$$  }
        //$$  ResourceLocation resourceLocation = Minecraft.getInstance().getSkinManager().registerTexture(texture,
        //$$          MinecraftProfileTexture.Type.SKIN);
        //$$  return resourceLocation;
        //#endif
    }

    public static ResourceLocation getPlayerCape(AbstractClientPlayer player) {
        //#if MC >= 12002
        return player.getSkin().capeTexture();
        //#else
        //$$ return player.getCloakTextureLocation();
        //#endif
    }

    public static GameProfile getGameProfile(ItemStack itemStack) {
        //#if MC >= 12005
        if(itemStack.getComponents().has(DataComponents.CUSTOM_MODEL_DATA)) {
            return null;
        }
        if (itemStack.getComponents().has(DataComponents.PROFILE)) {
            ResolvableProfile resolvableProfile = (ResolvableProfile) itemStack.get(DataComponents.PROFILE);
            if (resolvableProfile != null && !resolvableProfile.isResolved()) {
                    itemStack.remove(DataComponents.PROFILE);
                    resolvableProfile.resolve().thenAcceptAsync(
                                    resolvableProfile2 -> itemStack.set(DataComponents.PROFILE, resolvableProfile2),
                                    Minecraft.getInstance());
                    resolvableProfile = null;
            }
            if(resolvableProfile != null) {
                return resolvableProfile.gameProfile();
            }
        }
        return null;
        //#else
        //$$ if (itemStack.hasTag()) {
        //$$     CompoundTag compoundTag = itemStack.getTag();
        //$$     if (compoundTag.contains("CustomModelData")) {
        //$$         return null; // do not try to 3d-fy custom head models
        //$$     }
        //$$     if (compoundTag.contains("SkullOwner", 10)) {
        //$$         return NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
        //$$     } else if (compoundTag.contains("SkullOwner", 8)
        //$$             && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
        //$$         return new GameProfile(Util.NIL_UUID, compoundTag.getString("SkullOwner"));
        //$$     }
        //$$ }
        //$$ return null;
        //#endif
    }

    public static void addVertex(VertexConsumer cons, Matrix4f matrix4f, float x, float y, float z, float u, float v, int lightmapUV) {
        addVertex(cons, matrix4f, x, y, z, u, v, lightmapUV & 65535, lightmapUV >> 16 & 65535);
    }

    public static void addVertex(VertexConsumer cons, Matrix4f matrix4f, float x, float y, float z, float u, float v, int u2, int v2) {
        //#if MC >= 12100
        cons.addVertex(matrix4f, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setUv2(u2, v2);
        //#else
        //$$ cons.vertex(matrix4f, x, y, z).color(1f, 1f, 1f, 1f).uv(u, v).uv2(u2, v2)
        //$$ .endVertex();
        //#endif
    }

    public static void addVertex(VertexConsumer cons, Matrix4f matrix4f, float x, float y, float z, float u, float v) {
        //#if MC >= 12100
        cons.addVertex(matrix4f, x, y, z).setColor(255, 255, 255, 255).setUv(u, v);
        //#else
        //$$ cons.vertex(matrix4f, x, y, z).color(1f, 1f, 1f, 1f).uv(u, v).endVertex();
        //#endif
    }
    
    public static void addVertex(VertexConsumer cons, Matrix4f matrix4f, float x, float y, float z, float u, float v, int overlay, int lightmapUV, float nx, float ny, float nz) {
        addVertex(cons, matrix4f, x, y, z, u, v, overlay, lightmapUV & 65535, lightmapUV >> 16 & 65535, nx, ny, nz);
    }
    
    public static void addVertex(VertexConsumer cons, Matrix4f matrix4f, float x, float y, float z, float u, float v, int overlay, int u2, int v2, float nx, float ny, float nz) {
        //#if MC >= 12100
        cons.addVertex(matrix4f, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setUv2(u2, v2).setOverlay(overlay).setNormal(nx, ny, nz);
        //#else
        //$$ cons.vertex(matrix4f, x, y, z).color(1f, 1f, 1f, 1f).uv(u, v).overlayCoords(overlay).uv2(u2, v2).normal(nx, ny, nz)
        //$$ .endVertex();
        //#endif
    }

    public static void sendChatMessage(Component message) {
        //#if MC < 11900
        //$$ if (MC.player != null)
        //$$ MC.player.sendMessage(message, null);
        //#elseif MC <= 12101
        //$$ if (MC.player != null)
        //$$ MC.player.sendSystemMessage(message);
        //#else
        MC.getChatListener().handleSystemMessage(message, false);
        //#endif
    }

    public static boolean isSame(ItemStack a, ItemStack b) {
        //#if MC < 11700
        //$$return ItemStack.isSame(a, b);
        //#elseif MC <= 12004
        //$$ return ItemStack.isSameItemSameTags(a, b);
        //#else
        return ItemStack.isSameItemSameComponents(a, b);
        //#endif
    }

    public static boolean hasCustomName(ItemStack stack) {
        //#if MC <= 12004
        //$$ return stack.hasCustomHoverName();
        //#else
        return stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        //#endif
    }

}