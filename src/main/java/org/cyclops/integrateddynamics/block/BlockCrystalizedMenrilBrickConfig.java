package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril block.
 * @author rubensworks
 *
 */
public class BlockCrystalizedMenrilBrickConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockCrystalizedMenrilBrickConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCrystalizedMenrilBrickConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalizedMenrilBrick",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        ConfigurableBlock block = (ConfigurableBlock) new ConfigurableBlock(this, Material.CLAY) {
            @Override
            public SoundType getSoundType() {
                return SoundType.SNOW;
            }
        }.setHardness(1.5F);
        block.setHarvestLevel("pickaxe", 0);
        return block;
    }
    
}
