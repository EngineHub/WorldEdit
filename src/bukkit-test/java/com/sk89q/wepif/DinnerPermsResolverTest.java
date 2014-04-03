package com.sk89q.wepif;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class DinnerPermsResolverTest {
    private DinnerPermsResolver resolver;

    @Before
    public void setUp() {
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        resolver = new DinnerPermsResolver(server);
    }
    
    @Test
    public void testBasicResolving() {
        final TestOfflinePermissible permissible = new TestOfflinePermissible();
        permissible.setPermission("testperm.test1", true);
        assertTrue(resolver.hasPermission(permissible, "testperm.test1"));
        assertFalse(resolver.hasPermission(permissible, "testperm.somethingelse"));
        assertFalse(resolver.hasPermission(permissible, "testperm.test1.anything"));
        assertFalse(resolver.hasPermission(permissible, "completely.unrelated"));
        permissible.clearPermissions();
    }
    
    @Test
    public void testBasicWildcardResolution() {
        final TestOfflinePermissible permissible = new TestOfflinePermissible();
        permissible.setPermission("commandbook.spawnmob.*", true);
        assertTrue(resolver.hasPermission(permissible, "commandbook.spawnmob.pig"));
        assertTrue(resolver.hasPermission(permissible, "commandbook.spawnmob.spider"));
        assertTrue(resolver.hasPermission(permissible, "commandbook.spawnmob.spider.skeleton"));
        permissible.clearPermissions();
    }
    
    @Test
    public void testNegatingNodes() {
        final TestOfflinePermissible permissible = new TestOfflinePermissible();
        permissible.setPermission("commandbook.*", true);
        permissible.setPermission("commandbook.cuteasianboys", false);
        permissible.setPermission("commandbook.warp.*", false);
        permissible.setPermission("commandbook.warp.create", true);
        
        assertTrue(resolver.hasPermission(permissible, "commandbook.motd"));
        assertFalse(resolver.hasPermission(permissible, "commandbook.cuteasianboys"));
        assertFalse(resolver.hasPermission(permissible, "commandbook.warp.remove"));
        assertTrue(resolver.hasPermission(permissible, "commandbook.warp.create"));
        
        permissible.clearPermissions();
    }
    
    
    @Test
    public void testInGroup() {
        final TestOfflinePermissible permissible = new TestOfflinePermissible();
        permissible.setPermission("group.a", true);
        permissible.setPermission("group.b", true);
        assertTrue(resolver.inGroup(permissible, "a"));
        assertTrue(resolver.inGroup(permissible, "b"));
        assertFalse(resolver.inGroup(permissible, "c"));
    }
}
