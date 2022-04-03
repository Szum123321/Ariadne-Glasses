package net.szum123321.ariadne_glasses.client;

import net.minecraft.client.option.KeyBinding;

/**
    Similar to {@link net.minecraft.client.option.StickyKeyBinding}, except it keeps cycling when key is kept pressed for more than the timeout ms
 */
public class ToggleStickyTimedKeybinding extends KeyBinding {
    private final int timeout;
    private long lastUpdate = 0;
    private boolean isSticky = true;

    public ToggleStickyTimedKeybinding(String translationKey, int code, String category, int timeout) {
        super(translationKey, code, category);
        this.timeout = timeout;
    }

    public void setSticky(boolean sticky) {
        isSticky = sticky;
    }

    @Override
    public void setPressed(boolean pressed) {
        if(!isSticky) {
            super.setPressed(pressed);
        } else {
            if (pressed) {
                long now = System.currentTimeMillis();
                if (now - lastUpdate >= (long) timeout) {
                    super.setPressed(!this.isPressed());
                    lastUpdate = now;
                }
            } else {
                lastUpdate = 0;
            }
        }
    }
}
