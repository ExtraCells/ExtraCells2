package extracells.api;


public interface IHandlerStorageBase {

    boolean isFormatted();

    int totalBytes();

    int totalTypes();

    int usedBytes();

    int usedTypes();

    long storedCount();

}
