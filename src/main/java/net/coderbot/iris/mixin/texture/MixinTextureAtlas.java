package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.pbr.PBRAtlasTexture;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(SpriteLoader.class)
public class MixinTextureAtlas {
	@ModifyArg(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteLoader;loadSpriteContents(Ljava/util/Map;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private Map<ResourceLocation, Resource> removeNormalSpecularSprites(Map<ResourceLocation, Resource> map) {
		Map<ResourceLocation, Resource> regions = new ConcurrentHashMap<>(map);
		regions.forEach((location, textureAtlasSprite) -> {
			if (location.getPath().contains("_n") || location.getPath().contains("_s")) {
				regions.remove(location);
			}
		});
		return regions;
	}
}
