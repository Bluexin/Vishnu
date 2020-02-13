/*
 * Copyright (C) 2019-2020 Arnaud 'Bluexin' Solé
 *
 * This file is part of Vishnu.
 *
 * Vishnu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vishnu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Vishnu.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.vishnu.sync

import be.bluexin.vishnu.ComponentsRegistry
import be.bluexin.vishnu.EntityMap
import be.bluexin.vishnu.read
import com.artemis.BaseSystem
import com.artemis.World
import com.artemis.annotations.Wire
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.ByteArrayInputStream
import java.io.DataInputStream

private const val UNKNOWN_ENTITY_ID = -1

/**
 * System to read updates from a given [inbound] [ReceiveChannel] and update the [World]
 */
class ChannelReadSystem(
    private val inbound: ReceiveChannel<ByteArray>
) : BaseSystem() {
    @Wire
    private lateinit var entityMap: EntityMap

    @Wire
    private lateinit var componentsRegistry: ComponentsRegistry

    @UseExperimental(ExperimentalCoroutinesApi::class)
    override fun processSystem() {
        check(!inbound.isClosedForReceive) { "Inbound channel was closed (${inbound.cancel()})" }
        var i = inbound.poll()
        while (i != null) {
            i.processUpdate()
            i = inbound.poll()
        }
    }

    private fun ByteArray.processUpdate() = ByteArrayInputStream(this).use { bis ->
        DataInputStream(bis).use { dis ->
            when (UpdateType.read(dis)) {
                UpdateType.COMPONENT -> {
                    val masterId = dis.readInt()
                    val componentClass = componentsRegistry[dis.readInt()] ?: return // = not interested
                    var id = entityMap[masterId]
                    if (id == UNKNOWN_ENTITY_ID) {
                        id = world.create()
                        entityMap[masterId] = id
                    }
                    world.edit(id).create(componentClass).read(dis)
                }
                UpdateType.DELETE -> {
                    val masterId = dis.readInt()
                    val id = entityMap[masterId]
                    if (id != UNKNOWN_ENTITY_ID) {
                        val componentId = dis.readInt()
                        if (componentId == UpdateType.NO_COMPONENT) {
                            // Entity delete
                            world.delete(id)
                            entityMap -= masterId
                        } else if (componentId in componentsRegistry) {
                            // Component delete
                            world.edit(id).remove(componentsRegistry[componentId])
                            entityMap -= masterId
                        }
                    }
                }
            }
            Unit
        }
    }
}