package org.cyclops.integrateddynamics.api.network;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Objects that can be an element of a {@link INetwork}.
 * Multiple instances for the same 'element' can be created, so the comparator implementation must
 * make sure that these instances are considered equal.
 * These instances are used as a simple way of referring to these elements.
 * @author rubensworks
 */
public interface INetworkElement<N extends INetwork> extends Comparable<INetworkElement<N>> {

    /**
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval();

    /**
     * @return If this element should be updated. This method is only called once during network initialization.
     */
    public boolean isUpdate();

    /**
     * Update at the tick interval specified.
     * @param network The network to update in.
     */
    public void update(N network);

    /**
     * Called right before the network is terminated or will be reset.
     * @param network The network to update in.
     */
    public void beforeNetworkKill(N network);

    /**
     * Called right after this network is initialized.
     * @param network The network to update in.
     */
    public void afterNetworkAlive(N network);

    /**
     * Called right after this network has come alive again,
     * for example after a network restart.
     * @param network The network to update in.
     */
    public void afterNetworkReAlive(N network);

    /**
     * Add the itemstacks to drop when this element is removed.
     * @param itemStacks The itemstack list to add to.
     * @param dropMainElement If the part itself should also be dropped.
     */
    public void addDrops(List<ItemStack> itemStacks, boolean dropMainElement);

    /**
     * Called when this element is added to the network.
     * @param network The network.
     * @return If the addition succeeded.
     */
    public boolean onNetworkAddition(N network);

    /**
     * Called when this element is removed from the network.
     * @param network The network.
     */
    public void onNetworkRemoval(N network);

    /**
     * Called when this element is about to be removed.
     * This is called before {@link INetwork#removeNetworkElementPre(INetworkElement)}.
     * @param network The network.
     */
    public void onPreRemoved(N network);

    /**
     * Called when this element has been removed.
     * This is called after {@link INetwork#removeNetworkElementPost(INetworkElement)}.
     * @param network The network.
     */
    public void onPostRemoved(N network);

    /**
     * Called when a neighbouring block is updated, more specifically when
     * {@link net.minecraft.block.Block#neighborChanged(IBlockState, World, BlockPos, Block)} is called.
     * @param network The network to update in.
     * @param world The world in which the neighbour was updated.
     * @param neighborBlock block type of the neighbour that was updated.
     */
    public void onNeighborBlockChange(N network, IBlockAccess world, Block neighborBlock);

}
