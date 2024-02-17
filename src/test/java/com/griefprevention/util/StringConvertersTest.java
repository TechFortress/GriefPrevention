package com.griefprevention.util;

import com.griefprevention.test.ServerMocks;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@DisplayName("Feature: String converters should provide values for strings.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringConvertersTest {

    @BeforeAll
    void beforeAll() {
        Server server = ServerMocks.newServer();
        when(server.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("wall_signs"), Material.class))
                .thenReturn(new Tag<>() {
                    private final Set<Material> materials = Set.of(Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN);
                    @Override
                    public boolean isTagged(@NotNull Material item) {
                        return materials.contains(item);
                    }

                    @NotNull
                    @Override
                    public Set<Material> getValues() {
                        return materials;
                    }

                    @NotNull
                    @Override
                    public NamespacedKey getKey() {
                        return NamespacedKey.minecraft("wall_signs");
                    }
                });

        Bukkit.setServer(server);
    }

    @AfterAll
    void afterAll() {
        ServerMocks.unsetBukkitServer();
    }

    @Test
    void toEnumInvalid() {
        assertNull(StringConverters.toEnum(Tests.class, null), "Null yields null");
        assertNull(StringConverters.toEnum(Tests.class, "invalid"), "Invalid yields null");
    }

    @ParameterizedTest
    @EnumSource
    void toEnum(Tests test) {
        assertEquals(test, StringConverters.toEnum(Tests.class, test.name()));
        assertEquals(test, StringConverters.toEnum(Tests.class, test.name().toLowerCase()));
    }

    @Test
    void toKeyed() {
        Function<NamespacedKey, Keyed> func = key -> {
            if ("invalid_key".equals(key.getKey()))
            {
                return null;
            }
            return () -> key;
        };

        assertNull(StringConverters.toKeyed(func, null));

        assertNotNull(StringConverters.toKeyed(func, "good_key"));
        assertNotNull(StringConverters.toKeyed(func, "good_namespace:good_key"));
        assertNull(StringConverters.toKeyed(func, "bad namespace:good_key"));

        assertNull(StringConverters.toKeyed(func, "invalid_key"));
        assertNull(StringConverters.toKeyed(func, "good_namespace:invalid_key"));
        assertNull(StringConverters.toKeyed(func, "bad namespace:invalid_key"));

        assertNull(StringConverters.toKeyed(func, "bad key"));
        assertNull(StringConverters.toKeyed(func, "good_namespace:bad key"));
        assertNull(StringConverters.toKeyed(func, "bad namespace:bad key"));
    }

    @Test
    void toMaterialInvalid() {
        assertNull(StringConverters.toMaterial(null));
        assertNull(StringConverters.toMaterial("invalid"));
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.MATCH_NONE, names = { "LEGACY_.*" })
    void toMaterial(Material material) {
        assertEquals(
                material,
                StringConverters.toMaterial(material.getKey().toString()),
                "Value must be obtained from namespaced key.");
        assertEquals(
                material,
                StringConverters.toMaterial(material.getKey().getKey()),
                "Value must be obtained from un-namespaced key.");
        assertEquals(
                material,
                StringConverters.toMaterial(material.name()),
                "Value must be obtained from raw name.");
        assertEquals(
                material,
                StringConverters.toMaterial(material.name().replace('_', ' ')),
                "Value must be obtained from \"friendly\" name.");
    }

    @Test
    void toMaterialSet() {
        assertEquals(
                Set.of(Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN, Material.GOLD_ORE),
                StringConverters.toMaterialSet(List.of("#wall_signs", "#$invalid", "#fake:tag", "gold_ore", "$invalid")),
                "Values must be obtained from tags and keys"
        );
    }

    private enum Tests {
        ONE,
        TWO
    }

}
