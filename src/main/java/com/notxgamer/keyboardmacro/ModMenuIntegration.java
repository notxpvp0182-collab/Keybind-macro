package com.notxgamer.keyboardmacro;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.notxgamer.keyboardmacro.gui.MacroMainScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MacroMainScreen::new;
    }
}
