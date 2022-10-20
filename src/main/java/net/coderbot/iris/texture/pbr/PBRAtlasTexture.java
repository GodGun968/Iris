package net.coderbot.iris.texture.pbr;

import com.mojang.blaze3d.platform.TextureUtil;
import net.coderbot.iris.mixin.texture.AnimatedTextureAccessor;
import net.coderbot.iris.mixin.texture.FrameInfoAccessor;
import net.coderbot.iris.mixin.texture.SpriteContentsAccessor;
import net.coderbot.iris.mixin.texture.TickerAccessor;
import net.coderbot.iris.texture.util.TextureExporter;
import net.coderbot.iris.texture.util.TextureManipulationUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PBRAtlasTexture extends AbstractTexture {
	protected final TextureAtlas atlasTexture;
	protected final PBRType type;
	protected final ResourceLocation id;
	protected final Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
	protected final Map<TextureAtlasSprite, SpriteContents.Ticker> animationTickers = new HashMap<>();

	public PBRAtlasTexture(TextureAtlas atlasTexture, PBRType type) {
		this.atlasTexture = atlasTexture;
		this.type = type;
		id = type.appendToFileLocation(atlasTexture.location());
	}

	public PBRType getType() {
		return type;
	}

	public ResourceLocation getAtlasId() {
		return id;
	}

	public void addSprite(TextureAtlasSprite sprite) {
		sprites.put(sprite.contents().name(), sprite);
		SpriteContents.Ticker ticker = (SpriteContents.Ticker) sprite.contents().createTicker();
		if (ticker != null) {
			animationTickers.put(sprite, ticker);
		}
	}

	@Nullable
	public TextureAtlasSprite getSprite(ResourceLocation id) {
		return sprites.get(id);
	}

	public void clear() {
		sprites.clear();
		animationTickers.clear();
	}

	public void upload(int atlasWidth, int atlasHeight, int mipLevel) {
		int glId = getId();
		TextureUtil.prepareImage(glId, mipLevel, atlasWidth, atlasHeight);
		TextureManipulationUtil.fillWithColor(glId, mipLevel, type.getDefaultValue());

		for (TextureAtlasSprite sprite : sprites.values()) {
			try {
				uploadSprite(sprite);
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", id);
				crashReportCategory.setDetail("Sprite", sprite);
				throw new ReportedException(crashReport);
			}
		}

		if (!animationTickers.isEmpty()) {
			PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getOrCreatePBRHolder();
			switch (type) {
			case NORMAL:
				pbrHolder.setNormalAtlas(this);
				break;
			case SPECULAR:
				pbrHolder.setSpecularAtlas(this);
				break;
			}
		}

		if (PBRTextureManager.DEBUG) {
			TextureExporter.exportTextures("pbr_debug/atlas", id.getNamespace() + "_" + id.getPath().replaceAll("/", "_"), glId, mipLevel, atlasWidth, atlasHeight);
		}
	}

	public boolean tryUpload(int atlasWidth, int atlasHeight, int mipLevel) {
		try {
			upload(atlasWidth, atlasHeight, mipLevel);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	protected void uploadSprite(TextureAtlasSprite sprite) {
		SpriteContents.AnimatedTexture ticker = ((SpriteContentsAccessor) sprite.contents()).getAnimatedTexture();
		if (ticker instanceof AnimatedTextureAccessor) {
			AnimatedTextureAccessor accessor = (AnimatedTextureAccessor) ticker;

			accessor.invokeUploadFrame(sprite.getX(), sprite.getY(), ((FrameInfoAccessor) accessor.getFrames().get(getFrameFromSprite(sprite))).getIndex());
			return;
		}

		sprite.uploadFirstFrame();
	}

	public int getFrameFromSprite(TextureAtlasSprite sprite) {
		if (animationTickers.containsKey(sprite)) {
			return ((TickerAccessor) animationTickers.get(sprite)).getFrame();
		}
		return 0;
	}

	public int getSubFrameFromSprite(TextureAtlasSprite sprite) {
		if (animationTickers.containsKey(sprite)) {
			return ((TickerAccessor) animationTickers.get(sprite)).getSubFrame();
		}
		return 0;
	}

	public void setFrameOnSprite(TextureAtlasSprite sprite, int frame) {
		if (animationTickers.containsKey(sprite)) {
			((TickerAccessor) animationTickers.get(sprite)).setFrame(frame);
		}
	}

	public void setSubFrameOnSprite(TextureAtlasSprite sprite, int frame) {
		if (animationTickers.containsKey(sprite)) {
			((TickerAccessor) animationTickers.get(sprite)).setSubFrame(frame);
		}
	}

	public void cycleAnimationFrames() {
		bind();
		animationTickers.forEach((textureAtlasSprite, ticker) -> {
			ticker.tickAndUpload(textureAtlasSprite.getX(), textureAtlasSprite.getY());
		});
	}

	@Override
	public void close() {
		PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getPBRHolder();
		if (pbrHolder != null) {
			switch (type) {
			case NORMAL:
				pbrHolder.setNormalAtlas(null);
				break;
			case SPECULAR:
				pbrHolder.setSpecularAtlas(null);
				break;
			}
		}
	}

	@Override
	public void load(ResourceManager manager) {
	}
}
