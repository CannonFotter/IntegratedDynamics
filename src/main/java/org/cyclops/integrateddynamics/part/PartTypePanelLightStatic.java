package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanel;

/**
 * A panel part that simply emits light.
 * @author rubensworks
 */
public class PartTypePanelLightStatic extends PartTypePanel<PartTypePanelLightStatic, PartStateEmpty<PartTypePanelLightStatic>> {

    public static final int LIGHT_LEVEL = 15;

    public PartTypePanelLightStatic(String name) {
        super(name);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlock(blockConfig);
    }

    @Override
    public Class<? super PartTypePanelLightStatic> getPartTypeClass() {
        return PartTypePanelLightStatic.class;
    }

    @Override
    public PartStateEmpty<PartTypePanelLightStatic> constructDefaultState() {
        return new PartStateEmpty<PartTypePanelLightStatic>();
    }

    @Override
    protected boolean hasGui() {
        return false;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return null;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return null;
    }

    @Override
    public void onNetworkAddition(IPartNetwork network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkAddition(network, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, LIGHT_LEVEL);
    }

    @Override
    public void onBlockNeighborChange(IPartNetwork network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state, IBlockAccess world, Block neighborBlock) {
        super.onBlockNeighborChange(network, target, state, world, neighborBlock);
        PartTypePanelLightDynamic.setLightLevel(target, LIGHT_LEVEL);
    }

    @Override
    public void onNetworkRemoval(IPartNetwork network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onNetworkRemoval(network, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, 0);
    }

    @Override
    public void onPostRemoved(IPartNetwork network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state) {
        super.onPostRemoved(network, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, 0);
    }

    @Override
    public void postUpdate(IPartNetwork network, PartTarget target, PartStateEmpty<PartTypePanelLightStatic> state, boolean updated) {
        boolean wasEnabled = isEnabled(state);
        super.postUpdate(network, target, state, updated);
        boolean isEnabled = isEnabled(state);
        if(wasEnabled != isEnabled) {
            PartTypePanelLightDynamic.setLightLevel(target, isEnabled ? LIGHT_LEVEL : 0);
        }
    }
}
