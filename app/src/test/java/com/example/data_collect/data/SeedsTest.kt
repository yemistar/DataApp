package com.example.data_collect.data

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedsTest {
    @Test
    fun defaultAppStateIncludesDiscoverableBroilerAndLayerFlows() {
        val state = Seeds.defaultAppState()
        val broiler = state.flocks.firstOrNull { it.type.equals("broilers", ignoreCase = true) }
        val layer = state.flocks.firstOrNull { it.type.equals("layers", ignoreCase = true) }

        assertNotNull(broiler)
        assertNotNull(layer)
        assertTrue(state.selectedFlockId == broiler?.id)
        assertTrue(state.logs.feed.any { it.flockId == broiler?.id })
        assertTrue(state.logs.feed.any { it.flockId == layer?.id })
        assertTrue(state.logs.eggs.any { it.flockId == layer?.id })
    }
}
