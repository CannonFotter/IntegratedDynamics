package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammer;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

/**
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
class OperatorElementSubGuiRenderPattern extends SubGuiConfigRenderPattern<OperatorElement, GuiLogicProgrammer, ContainerLogicProgrammer> {

    public OperatorElementSubGuiRenderPattern(OperatorElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                              GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
        IConfigRenderPattern configRenderPattern = element.getRenderPattern();
        IOperator operator = element.getOperator();

        // Input type tooltips
        IValueType[] valueTypes = operator.getInputTypes();
        for (int i = 0; i < valueTypes.length; i++) {
            IValueType valueType = valueTypes[i];
            IInventory temporaryInputSlots = container.getTemporaryInputSlots();
            if (temporaryInputSlots.getStackInSlot(i) == null) {
                Pair<Integer, Integer> slotPosition = configRenderPattern.getSlotPositions()[i];
                if (gui.isPointInRegion(getX() + slotPosition.getLeft(), getY() + slotPosition.getRight(),
                        GuiLogicProgrammer.BOX_HEIGHT, GuiLogicProgrammer.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        // Output type tooltip
        IValueType outputType = operator.getOutputType();
        if (!container.hasWriteItemInSlot()) {
            if (gui.isPointInRegion(ContainerLogicProgrammer.OUTPUT_X, ContainerLogicProgrammer.OUTPUT_Y,
                    GuiLogicProgrammer.BOX_HEIGHT, GuiLogicProgrammer.BOX_HEIGHT, mouseX, mouseY)) {
                gui.drawTooltip(getValueTypeTooltip(outputType), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

}
