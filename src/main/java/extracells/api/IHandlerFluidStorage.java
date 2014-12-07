package extracells.api;

public interface IHandlerFluidStorage {

	boolean isPreformatted();

	int usedBytes();

	int totalBytes();

	int usedTypes();

	int totalTypes();

}
