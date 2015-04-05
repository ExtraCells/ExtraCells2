package extracells.tileentity;

import appeng.api.networking.IGrid;

public interface IListenerTile {

	public void registerListener();

	public void removeListener();

	public void updateGrid(IGrid oldGrid, IGrid newGrid);

}
