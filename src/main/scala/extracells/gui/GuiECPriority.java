package extracells.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerPriority;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IPriorityHost;
import extracells.network.packet.other.PacketGuiSwitch;
import extracells.part.PartFluidStorage;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

public class GuiECPriority extends AEBaseGui
{

	private GuiNumberBox priority;
	private GuiTabButton originalGuiBtn;

	private GuiButton plus1;
	private GuiButton plus10;
	private GuiButton plus100;
	private GuiButton plus1000;
	private GuiButton minus1;
	private GuiButton minus10;
	private GuiButton minus100;
	private GuiButton minus1000;

	private int OriginalGui = -1;
	private TileEntity te;

	public GuiECPriority(final InventoryPlayer inventoryPlayer, final IPriorityHost te )
	{
		super( new ContainerPriority( inventoryPlayer, te ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();

		final int a = AEConfig.instance.priorityByStacksAmounts( 0 );
		final int b = AEConfig.instance.priorityByStacksAmounts( 1 );
		final int c = AEConfig.instance.priorityByStacksAmounts( 2 );
		final int d = AEConfig.instance.priorityByStacksAmounts( 3 );

		this.buttonList.add( this.plus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 32, 22, 20, "+" + a ) );
		this.buttonList.add( this.plus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 32, 28, 20, "+" + b ) );
		this.buttonList.add( this.plus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 32, 32, 20, "+" + c ) );
		this.buttonList.add( this.plus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 32, 38, 20, "+" + d ) );

		this.buttonList.add( this.minus1 = new GuiButton( 0, this.guiLeft + 20, this.guiTop + 69, 22, 20, "-" + a ) );
		this.buttonList.add( this.minus10 = new GuiButton( 0, this.guiLeft + 48, this.guiTop + 69, 28, 20, "-" + b ) );
		this.buttonList.add( this.minus100 = new GuiButton( 0, this.guiLeft + 82, this.guiTop + 69, 32, 20, "-" + c ) );
		this.buttonList.add( this.minus1000 = new GuiButton( 0, this.guiLeft + 120, this.guiTop + 69, 38, 20, "-" + d ) );

		ItemStack myIcon = null;
		final Object target = ( (AEBaseContainer) this.inventorySlots ).getTarget();

		if( target instanceof PartFluidStorage)
		{
			te = ((PartFluidStorage) target).getHostTile();
			myIcon = ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDSTORAGE.ordinal());
			this.OriginalGui = ((PartFluidStorage) target).getSide().ordinal();
		}

		if( this.OriginalGui != -1 && myIcon != null )
		{
			this.buttonList.add( this.originalGuiBtn = new GuiTabButton( this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender ) );
		}

		this.priority = new GuiNumberBox( this.fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, this.fontRendererObj.FONT_HEIGHT, Long.class );
		this.priority.setEnableBackgroundDrawing( false );
		this.priority.setMaxStringLength( 16 );
		this.priority.setTextColor( 0xFFFFFF );
		this.priority.setVisible( true );
		this.priority.setFocused( true );
		( (ContainerPriority) this.inventorySlots ).setTextField( this.priority );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.fontRendererObj.drawString( GuiText.Priority.getLocal(), 8, 6, 4210752 );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );

		this.priority.drawTextBox();
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		if( btn == this.originalGuiBtn )
		{
			new PacketGuiSwitch(OriginalGui, te).sendPacketToServer();
		}

		final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
		final boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

		if( isPlus || isMinus )
		{
			this.addQty( this.getQty( btn ) );
		}
	}

	private void addQty( final int i )
	{
		try
		{
			String out = this.priority.getText();

			boolean fixed = false;
			while( out.startsWith( "0" ) && out.length() > 1 )
			{
				out = out.substring( 1 );
				fixed = true;
			}

			if( fixed )
			{
				this.priority.setText( out );
			}

			if( out.isEmpty() )
			{
				out = "0";
			}

			long result = Long.parseLong( out );
			result += i;

			this.priority.setText( out = Long.toString( result ) );

			NetworkHandler.instance.sendToServer( new PacketValueConfig( "PriorityHost.Priority", out ) );
		}
		catch( final NumberFormatException e )
		{
			// nope..
			this.priority.setText( "0" );
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}
	}

	@Override
	protected void keyTyped( final char character, final int key )
	{
		if( !this.checkHotbarKeys( key ) )
		{
			if( ( key == 211 || key == 205 || key == 203 || key == 14 || character == '-' || Character.isDigit( character ) ) && this.priority.textboxKeyTyped( character, key ) )
			{
				try
				{
					String out = this.priority.getText();

					boolean fixed = false;
					while( out.startsWith( "0" ) && out.length() > 1 )
					{
						out = out.substring( 1 );
						fixed = true;
					}

					if( fixed )
					{
						this.priority.setText( out );
					}

					if( out.isEmpty() )
					{
						out = "0";
					}

					NetworkHandler.instance.sendToServer( new PacketValueConfig( "PriorityHost.Priority", out ) );
				}
				catch( final IOException e )
				{
					AELog.debug( e );
				}
			}
			else
			{
				super.keyTyped( character, key );
			}
		}
	}

	protected String getBackground()
	{
		return "guis/priority.png";
	}
}
