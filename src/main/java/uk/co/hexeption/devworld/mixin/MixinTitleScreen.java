package uk.co.hexeption.devworld.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.co.hexeption.devworld.Devworld;

/**
 * MixinTitleScreen
 *
 * @author Hexeption admin@hexeption.co.uk
 * @since 23/03/2020 - 05:25 pm
 */
@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

    @Shadow
    @Final
    private boolean doBackgroundFade;
    @Shadow
    private long backgroundFadeStart;
    private ButtonWidget buttonCreate;
    private ButtonWidget buttonLoad;
    private ButtonWidget buttonDelete;

    private int keyShiftCount = 0;

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {

        int buttonY = height / 4 + 48;
        int buttonX = width / 2 + 104;

        buttonCreate = new ButtonWidget(buttonX, buttonY, 84, 20, I18n.translate("menu.new.devworld", new Object[0]), button -> {
            Devworld.INSTANCE.createWorld();
        });

        buttonLoad = new ButtonWidget(buttonX, buttonY, 40, 20, I18n.translate("menu.load.devworld", new Object[0]), button -> {
            Devworld.INSTANCE.loadWorld();
        });

        buttonX += 44;

        buttonDelete = new ButtonWidget(buttonX, buttonY, 40, 20, I18n.translate("menu.delete.devworld", new Object[0]), button -> {
            Devworld.INSTANCE.deleteWorld();
            keyShiftCount = 0;
        });

        buttonCreate.visible = false;
        buttonLoad.visible = false;
        buttonDelete.visible = false;
        buttonDelete.active = false;

        keyShiftCount = 0;

        addButton(buttonCreate);
        addButton(buttonDelete);
        addButton(buttonLoad);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            keyShiftCount++;
        }
        if (keyShiftCount >= 2) {
            buttonDelete.active = true;
        }
        return false;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int textY = height / 4 + 20;
        int textX = width / 2 + 146;

        float f = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;
        if ((l & -67108864) != 0) {

            drawCenteredString(MinecraftClient.getInstance().textRenderer, "Dev World", textX, textY, 16777215 | l);
        }

        if (buttonDelete.isHovered() && !buttonDelete.active) {
            renderTooltip("Press [Left Shift] 2 times to enable delete", mouseX, mouseY);
        }

        if (!Devworld.INSTANCE.saveExist()) {
            buttonCreate.visible = true;
            buttonLoad.visible = false;
            buttonDelete.visible = false;
        } else {
            buttonCreate.visible = false;
            buttonLoad.visible = true;
            buttonDelete.visible = true;
        }

    }
}
