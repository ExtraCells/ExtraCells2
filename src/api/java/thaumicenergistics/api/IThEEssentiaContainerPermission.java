package thaumicenergistics.api;

public interface IThEEssentiaContainerPermission
{
	/**
	 * Can the container be partially filled?
	 */
	public boolean canHoldPartialAmount();

	/**
	 * The maximum amount this container can hold
	 */
	public int maximumCapacity();
}
