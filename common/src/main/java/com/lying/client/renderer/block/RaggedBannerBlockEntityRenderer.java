package com.lying.client.renderer.block;

import com.lying.block.RaggedBannerBlock;
import com.lying.block.entity.RaggedBannerBlockEntity;

import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.model.BannerBlockModel;
import net.minecraft.client.render.block.entity.model.BannerFlagBlockModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;

public class RaggedBannerBlockEntityRenderer implements BlockEntityRenderer<RaggedBannerBlockEntity>
{
	private static final int ROTATIONS = 16;
	private static final float field_55282 = 0.6666667F;
	private final BannerBlockModel standingModel;
	private final BannerBlockModel wallModel;
	private final BannerFlagBlockModel standingFlagModel;
	private final BannerFlagBlockModel wallFlagModel;
	
	public RaggedBannerBlockEntityRenderer(BlockEntityRendererFactory.Context context)
	{
		this(context.getLoadedEntityModels());
	}
	
	public RaggedBannerBlockEntityRenderer(LoadedEntityModels models)
	{
		this.standingModel = new BannerBlockModel(models.getModelPart(EntityModelLayers.STANDING_BANNER));
		this.wallModel = new BannerBlockModel(models.getModelPart(EntityModelLayers.WALL_BANNER));
		this.standingFlagModel = new BannerFlagBlockModel(models.getModelPart(EntityModelLayers.STANDING_BANNER_FLAG));
		this.wallFlagModel = new BannerFlagBlockModel(models.getModelPart(EntityModelLayers.WALL_BANNER_FLAG));
	}
	
	public void render(RaggedBannerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay)
	{
		BlockState blockState = entity.getCachedState();
		BannerBlockModel bannerBlockModel;
		BannerFlagBlockModel bannerFlagBlockModel;
		float g;
		if (blockState.getBlock() instanceof RaggedBannerBlock)
		{
			g = -RotationPropertyHelper.toDegrees((Integer)blockState.get(BannerBlock.ROTATION));
			bannerBlockModel = this.standingModel;
			bannerFlagBlockModel = this.standingFlagModel;
		}
		else
		{
			g = -((Direction)blockState.get(WallBannerBlock.FACING)).getPositiveHorizontalDegrees();
			bannerBlockModel = this.wallModel;
			bannerFlagBlockModel = this.wallFlagModel;
		}
		
		long l = entity.getWorld().getTime();
		BlockPos blockPos = entity.getPos();
		float h = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l, 100L) + tickDelta) / 100.0F;
		render(
			matrices,
			vertexConsumers,
			light,
			overlay,
			g,
			bannerBlockModel,
			bannerFlagBlockModel,
			h,
			entity.getColorForState(),
			entity.getPatterns()
		);
	}
	
	private static void render(
			MatrixStack matrices,
			VertexConsumerProvider vertexConsumers,
			int light,
			int overlay,
			float rotation,
			BannerBlockModel model,
			BannerFlagBlockModel flagModel,
			float sway,
			DyeColor baseColor,
			BannerPatternsComponent patterns
		)
	{
		matrices.push();
		matrices.translate(0.5F, 0.0F, 0.5F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
		matrices.scale(field_55282, -field_55282, -field_55282);
		model.render(matrices, ModelBaker.BANNER_BASE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid), light, overlay);
		flagModel.sway(sway);
//		renderCanvas(matrices, vertexConsumers, light, overlay, flagModel.getRootPart(), ModelBaker.BANNER_BASE, true, baseColor, patterns);
		matrices.pop();
	}
}
