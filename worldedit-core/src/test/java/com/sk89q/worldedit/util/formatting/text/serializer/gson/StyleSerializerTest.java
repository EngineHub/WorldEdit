package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StyleSerializerTest {

    @Test
    void hexColorIsDroppedInsteadOfFailing() {
        // Vanilla item names may carry hex colors ("#RRGGBB") since 1.16, text3 cannot represent them.
        Component hexOnly = GsonComponentSerializer.INSTANCE.deserialize(
                "{\"text\":\"Gold item\",\"color\":\"#FFD700\"}"
        );
        assertEquals("Gold item", ((TextComponent) hexOnly).content());
        assertNull(hexOnly.color());
    }

    @Test
    void namedColorStillParsed() {
        Component component = GsonComponentSerializer.INSTANCE.deserialize(
                "{\"text\":\"Gold item\",\"color\":\"gold\"}"
        );
        assertEquals(TextColor.GOLD, component.color());
    }
}
