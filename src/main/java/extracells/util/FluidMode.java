package extracells.util;

public enum FluidMode
{
	DROPS(20, 5F),
	QUART(250, 30F),
	BUCKETS(1000, 60F);

	private int amount;
	private float cost;

	FluidMode(int amount, float cost)
	{
		this.amount = amount;
		this.cost = cost;
	}

	public int getAmount()
	{
		return amount;
	}

	public float getCost()
	{
		return cost;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public void setCost(double _cost)
	{
		cost = (float) _cost;
	}
}
