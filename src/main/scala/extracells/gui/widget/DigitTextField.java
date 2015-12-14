package extracells.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class DigitTextField extends GuiTextField {

	public DigitTextField(FontRenderer fontRenderer, int x, int y, int length,
			int height) {
		super(fontRenderer, x, y, length, height);
	}

	private Boolean isWhiteListed(char key) {
		return "0123456789".contains(String.valueOf(key));
	}

	@Override
	public boolean textboxKeyTyped(char keyChar, int keyID) {
		if (isFocused()) {
			switch (keyChar) {
			case 1:
				this.setCursorPositionEnd();
				this.setSelectionPos(0);
				return true;
			case 3:
				GuiScreen.setClipboardString(getSelectedText());
				return true;
			case 22:
				this.writeText(GuiScreen.getClipboardString());
				return true;
			case 24:
				GuiScreen.setClipboardString(getSelectedText());
				this.writeText("");
				return true;
			default:
				switch (keyID) {
				case Keyboard.KEY_ESCAPE:
					this.setFocused(false);
					return true;
				case 14:
					if (GuiScreen.isCtrlKeyDown()) {
						this.deleteWords(-1);
					} else {
						this.deleteFromCursor(-1);
					}

					return true;
				case 199:
					if (GuiScreen.isShiftKeyDown()) {
						this.setSelectionPos(0);
					} else {
						this.setCursorPositionZero();
					}

					return true;
				case 203:
					if (GuiScreen.isShiftKeyDown()) {
						if (GuiScreen.isCtrlKeyDown()) {
							this.setSelectionPos(this.getNthWordFromPos(-1,
									this.getSelectionEnd()));
						} else {
							this.setSelectionPos(this.getSelectionEnd() - 1);
						}
					} else if (GuiScreen.isCtrlKeyDown()) {
						this.setCursorPosition(this.getNthWordFromCursor(-1));
					} else {
						this.moveCursorBy(-1);
					}

					return true;
				case 205:
					if (GuiScreen.isShiftKeyDown()) {
						if (GuiScreen.isCtrlKeyDown()) {
							this.setSelectionPos(this.getNthWordFromPos(1,
									this.getSelectionEnd()));
						} else {
							this.setSelectionPos(this.getSelectionEnd() + 1);
						}
					} else if (GuiScreen.isCtrlKeyDown()) {
						this.setCursorPosition(this.getNthWordFromCursor(1));
					} else {
						this.moveCursorBy(1);
					}

					return true;
				case 207:
					if (GuiScreen.isShiftKeyDown()) {
						this.setSelectionPos(getText().length());
					} else {
						this.setCursorPositionEnd();
					}

					return true;
				case 211:
					if (GuiScreen.isCtrlKeyDown()) {
						this.deleteWords(1);
					} else {
						this.deleteFromCursor(1);
					}

					return true;
				default:
					if (isWhiteListed(keyChar)) {
						this.writeText(Character.toString(keyChar));
						return true;
					} else if (keyChar == '-' && getText().isEmpty()) {
						writeText(Character.toString(keyChar));
						return true;
					} else {
						return false;
					}
				}
			}
		} else {
			return false;
		}
	}
}
