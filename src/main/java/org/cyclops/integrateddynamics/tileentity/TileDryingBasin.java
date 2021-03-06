package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.SingleCache;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeRegistry;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.TankInventoryTileEntity;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;

/**
 * A tile entity for drying stuff.
 * @author rubensworks
 */
public class TileDryingBasin extends TankInventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    private static final int WOOD_IGNITION_TEMPERATURE = 573; // 300 degrees celcius

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private Float randomRotation = 0F;
    @NBTPersist
    private int progress = 0;
    @NBTPersist
    private int fire = 0;

    private SingleCache<Pair<ItemStack, FluidStack>,
            IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>> recipeCache;

    public TileDryingBasin() {
        super(1, "dryingBasingInventory", 1, Fluid.BUCKET_VOLUME, "dryingBasingTank");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.NORTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(0));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(0));

        // Efficient cache to retrieve the current craftable recipe.
        recipeCache = new SingleCache<>(
                new SingleCache.ICacheUpdater<Pair<ItemStack, FluidStack>,
                        IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties>>() {
                    @Override
                    public IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getNewValue(Pair<ItemStack, FluidStack> key) {
                        ItemAndFluidStackRecipeComponent recipeInput =
                                new ItemAndFluidStackRecipeComponent(key.getLeft(), key.getRight());
                        IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> maxRecipe = null;
                        for (IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe : getRegistry().findRecipesByInput(recipeInput)) {
                            if(key.getRight() == null) {
                                return recipe;
                            } else if(key.getRight().amount >= recipe.getInput().getFluidStack().amount
                                    && (maxRecipe == null
                                        || recipe.getInput().getFluidStack().amount > maxRecipe.getInput().getFluidStack().amount)) {
                                maxRecipe = recipe;
                            }
                        }
                        return maxRecipe;
                    }

                    @Override
                    public boolean isKeyEqual(Pair<ItemStack, FluidStack> cacheKey, Pair<ItemStack, FluidStack> newKey) {
                        return cacheKey == null || newKey == null ||
                                (ItemStack.areItemStacksEqual(cacheKey.getLeft(), newKey.getLeft()) &&
                                        FluidStack.areFluidStackTagsEqual(cacheKey.getRight(), newKey.getRight())) &&
                                        FluidHelpers.getAmount(cacheKey.getRight()) == FluidHelpers.getAmount(newKey.getRight());
                    }
                });
    }

    protected IRecipeRegistry<BlockDryingBasin, ItemAndFluidStackRecipeComponent,
            ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getRegistry() {
        return BlockDryingBasin.getInstance().getRecipeRegistry();
    }

    public IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> getCurrentRecipe() {
        return recipeCache.get(Pair.of(getStackInSlot(0), FluidHelpers.copy(getTank().getFluid())));
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if(!worldObj.isRemote) {
            if (!getTank().isEmpty() && getTank().getFluid().getFluid().getTemperature(getTank().getFluid()) >= WOOD_IGNITION_TEMPERATURE) {
                if (++fire >= 100) {
                    getWorld().setBlockState(getPos(), Blocks.FIRE.getDefaultState());
                } else if (getWorld().isAirBlock(getPos().offset(EnumFacing.UP)) && worldObj.rand.nextInt(10) == 0) {
                    getWorld().setBlockState(getPos().offset(EnumFacing.UP), Blocks.FIRE.getDefaultState());
                }

            } else if (getCurrentRecipe() != null) {
                IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = getCurrentRecipe();
                if (progress >= recipe.getProperties().getDuration()) {
                    ItemStack output = recipe.getOutput().getItemStack();
                    if (output != null) {
                        output = output.copy();
                    }
                    setInventorySlotContents(0, output);
                    int amount = FluidHelpers.getAmount(recipe.getInput().getFluidStack());
                    drain(amount, true);
                    if (recipe.getOutput().getFluidStack() != null) {
                        if (fill(recipe.getOutput().getFluidStack(), true) == 0) {
                            IntegratedDynamics.clog(Level.ERROR, "Encountered an invalid recipe: " + recipe.getNamedId());
                        }
                    }
                    progress = 0;
                } else {
                    progress++;
                    sendUpdate();
                }
                fire = 0;
            } else {
                progress = 0;
                fire = 0;
                sendUpdate();
            }
        } else if(progress > 0 && worldObj.rand.nextInt(5) == 0) {
            if(!getTank().isEmpty()) {
                Block block = getTank().getFluid().getFluid().getBlock();
                if(block != null) {
                    int blockStateId = Block.getStateId(block.getDefaultState());
                    getWorld().spawnParticle(EnumParticleTypes.BLOCK_DUST,
                            getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                            getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0, blockStateId);
                }
            }
            if(getStackInSlot(0) != null) {
                int itemId = Item.getIdFromItem(getStackInSlot(0).getItem());
                getWorld().spawnParticle(EnumParticleTypes.ITEM_CRACK,
                        getPos().getX() + Math.random() * 0.8D + 0.1D, getPos().getY() + Math.random() * 0.1D + 0.9D,
                        getPos().getZ() + Math.random() * 0.8D + 0.1D, 0, 0.1D, 0, itemId);
            }
        }
    }

    @Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
        super.setInventorySlotContents(slotId, itemstack);
        this.randomRotation = worldObj.rand.nextFloat() * 360;
        sendUpdate();
    }

    /**
     * Get the random rotation for displaying the item.
     * @return The random rotation.
     */
    public float getRandomRotation() {
        return randomRotation;
    }
}
