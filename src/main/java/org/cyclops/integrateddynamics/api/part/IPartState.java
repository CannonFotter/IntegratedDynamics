package org.cyclops.integrateddynamics.api.part;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * A value holder for an {@link IPartType}.
 * This is what will be serialized from and to NBT.
 * This object is mutable and should not be recreated.
 * Note that you should be careful when passing around this state, because when the server sends an update to the
 * client, this state could be overwritten with a new version, so always try to use the part container to get the state.
 * @author rubensworks
 */
public interface IPartState<P extends IPartType> {

    public static final String GLOBALCOUNTER_KEY = "part";

    /**
     * Get the part state class.
     * This is used for doing dynamic construction of guis.
     * @return The actual class for this part state.
     */
    public Class<? extends IPartState> getPartStateClass();

    /**
     * Write a state to NBT.
     * @param tag The tag to write to.
     */
    public void writeToNBT(NBTTagCompound tag);

    /**
     * Read a state from NBT.
     * @param tag The tag to read from.
     */
    public void readFromNBT(NBTTagCompound tag);

    /**
     * Generate a server-wide unique ID for this part state.
     */
    public void generateId();

    /**
     * A server-wide unique ID for this part that is persisted when the part is broken and moved.
     * @return The unique ID
     */
    public int getId();

    /**
     * Set the update interval for this state.
     * @param updateInterval The tick interval to update this element.
     */
    public void setUpdateInterval(int updateInterval);

    /**
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval();

    /**
     * Check if dirty and reset the dirty state.
     * @return If this state has changed since the last time and needs to be persisted to NBT eventually.
     */
    public boolean isDirtyAndReset();

    /**
     * Check if this part state should update and reset the flag.
     * @return If this state has changed since the last time and needs to be updated to the client.
     */
    public boolean isUpdateAndReset();

    /**
     * Get the properties for the given aspect.
     * This will only retrieve the already saved properties, so this could be null if not set before.
     * It is better to call the {@link IAspect#getProperties(IPartType, PartTarget, IPartState)} method instead.
     * @param aspect The aspect to get the properties from.
     * @return The properties, this can be null if still the default.
     */
    public IAspectProperties getAspectProperties(IAspect aspect);

    /**
     * Set the properties for the given aspect.
     * @param aspect The aspect to get the properties from.
     * @param properties The properties, this can be null if still the default.
     */
    public void setAspectProperties(IAspect aspect, IAspectProperties properties);

    /**
     * Enable the part from working.
     * @param enabled If it should work.
     */
    public void setEnabled(boolean enabled);

    /**
     * @return If the part should work.
     */
    public boolean isEnabled();

    /**
     * Gathers the capabilities of this part state.
     * Don't call this unless you know what you're doing!
     * @param partType The part type this state is associated with.
     */
    public void gatherCapabilities(P partType);

    /**
     * If this part state has the given capability.
     * @param capability The capability to check.
     * @return If this has the given capability/
     */
    boolean hasCapability(Capability<?> capability);

    /**
     * Get the given capability.
     * @param capability The capability to get.
     * @param <T> The capability type.
     * @return The capability instance.
     */
    <T> T getCapability(Capability<T> capability);

    /**
     * Add a capability to this state that will not be automatically persisted to NBT.
     * @param capability The capability.
     * @param value The capability instance.
     * @param <T> The capability type.
     */
    public <T> void addVolatileCapability(Capability<T> capability, T value);

    /**
     * Remove a non-persisted capability.
     * @param capability The capability.
     */
    public void removeVolatileCapability(Capability<?> capability);

}
