package extracells.api;

public interface IHandlerFluidStorage {

	boolean isFormatted();

	int totalBytes();

	int totalTypes();

	int usedBytes();

	int usedTypes();

}
