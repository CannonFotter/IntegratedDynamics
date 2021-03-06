package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.AttachCapabilitiesEventPart;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

import java.util.Map;

/**
 * A default implementation of the {@link IPartState} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public abstract class PartStateBase<P extends IPartType> implements IPartState<P>, INBTProvider, IDirtyMarkListener {

    private boolean dirty = false;
    private boolean update = false;
    @Delegate
    private INBTProvider nbtProviderComponent = new NBTProviderComponent(this);
    @NBTPersist
    private int updateInterval = GeneralConfig.defaultPartUpdateFreq;
    @NBTPersist
    private int id = -1;
    @NBTPersist
    private Map<String, IAspectProperties> aspectProperties = Maps.newHashMap();
    @NBTPersist
    private boolean enabled = true;
    private CapabilityDispatcher capabilities = null;
    private Map<Capability<?>, Object> volatileCapabilities = Maps.newHashMap();

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        writeGeneratedFieldsToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        readGeneratedFieldsFromNBT(tag);
    }

    @Override
    public void generateId() {
        this.id = IntegratedDynamics.globalCounters.getNext(IPartState.GLOBALCOUNTER_KEY);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean isDirtyAndReset() {
        boolean wasDirty = this.dirty;
        this.dirty = false;
        return wasDirty;
    }

    @Override
    public boolean isUpdateAndReset() {
        boolean wasUpdate = this.update;
        this.update = false;
        return wasUpdate;
    }

    @Override
    public void onDirty() {
        this.dirty = true;
    }

    /**
     * Enables a flag that tells the part container to send an NBT update to the client(s).
     */
    public void sendUpdate() {
        this.update = true;
    }

    @Override
    public IAspectProperties getAspectProperties(IAspect aspect) {
        return aspectProperties.get(aspect.getUnlocalizedName());
    }

    @Override
    public void setAspectProperties(IAspect aspect, IAspectProperties properties) {
        aspectProperties.put(aspect.getUnlocalizedName(), properties);
        sendUpdate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gathers the capabilities of this part state.
     * Don't call this unless you know what you're doing!
     */
    public void gatherCapabilities(P partType) {
        AttachCapabilitiesEventPart event = new AttachCapabilitiesEventPart(partType, this);
        MinecraftForge.EVENT_BUS.post(event);
        this.capabilities = event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities()) : null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability) {
        return volatileCapabilities.containsKey(capability)
                || (capabilities != null && capabilities.hasCapability(capability, null));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        Object o = volatileCapabilities.get(capability);
        if(o != null) {
            return (T) o;
        }
        return capabilities == null ? null : capabilities.getCapability(capability, null);
    }

    @Override
    public <T> void addVolatileCapability(Capability<T> capability, T value) {
        volatileCapabilities.put(capability, value);
    }

    @Override
    public void removeVolatileCapability(Capability<?> capability) {
        volatileCapabilities.remove(capability);
    }

}
