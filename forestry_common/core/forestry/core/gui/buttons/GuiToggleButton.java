/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.core.gui.buttons;

public class GuiToggleButton extends GuiBetterButton {

    public boolean active;

    public GuiToggleButton(int id, int x, int y, String label, boolean active) {
        this(id, x, y, 200, StandardButtonTextureSets.LARGE_BUTTON, label, active);
    }

    public GuiToggleButton(int id, int x, int y, int width, String s, boolean active) {
        super(id, x, y, width, StandardButtonTextureSets.LARGE_BUTTON, s);
        this.active = active;
    }

    public GuiToggleButton(int id, int x, int y, int width, IButtonTextureSet texture, String s, boolean active) {
        super(id, x, y, width, texture, s);
        this.active = active;
    }

    public void toggle() {
        active = !active;
    }

    @Override
    public int getHoverState(boolean mouseOver) {
        int state = 1;
        if (!enabled) {
            state = 0;
        } else if (mouseOver) {
            state = 2;
        } else if (!active) {
            state = 3;
        }
        return state;
    }

    @Override
    public int getTextColor(boolean mouseOver) {
        if (!enabled) {
            return 0xffa0a0a0;
        } else if (mouseOver) {
            return 0xffffa0;
        } else if (!active) {
            return 0x777777;
        } else {
            return 0xe0e0e0;
        }
    }

}
