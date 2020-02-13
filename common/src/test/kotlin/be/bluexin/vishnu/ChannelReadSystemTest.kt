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

package be.bluexin.vishnu

import be.bluexin.vishnu.sync.ChannelReadSystem
import be.bluexin.vishnu.sync.UpdateType
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.annotations.PooledWeaver
import com.github.javafaker.Faker
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataOutput
import java.io.DataOutputStream

private const val ENTITY_MASTER_ID = 42
private const val COMPONENT_ID = 8

internal class ChannelReadSystemTest {

    @BeforeEach
    fun beforeEach() = componentsRegistry.reset()

    @Test
    fun `reading a known component for an unknown Entity leads to an Entity being added to the World and recorded in the Entity ID map`() {
        this.registerComponent()
        val (channel, entityMap, world) = setup()
        val messageMapper = world.getMapper<StringComponent>()

        val component = StringComponent(strings())
        channel.offer(component.toBytes())

        world.process()

        assertTrue(ENTITY_MASTER_ID in entityMap)
        assertEquals(component, messageMapper[entityMap[ENTITY_MASTER_ID]])
    }

    @Test
    fun `reading a known component on an existing Entity leads to the component being added to the Entity`() {
        this.registerComponent()
        val (channel, entityMap, world) = setup()
        val messageMapper = world.getMapper<StringComponent>()

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        val component = StringComponent(strings())
        channel.offer(component.toBytes())

        world.process()

        assertEquals(component, messageMapper[id])
    }

    @Test
    fun `reading unknown components leads to no entities being added to the World and nothing to be inserted in the Entity ID map`() {
        val (channel, entityMap, world) = setup()
        val messageMapper = world.getMapper<StringComponent>()

        val component = StringComponent(strings())
        channel.offer(component.toBytes())

        world.process()

        assertNull(messageMapper[0])
        assertFalse(ENTITY_MASTER_ID in entityMap)
    }

    @Test
    fun `reading component delete causes it to be deleted from the Entity but without removing the Entity`() {
        val (channel, entityMap, world) = setup()
        val messageMapper = world.getMapper<StringComponent>()

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        channel.offer(getDeleteAsBytes(COMPONENT_ID))

        world.process()

        assertTrue(world.entityManager.isActive(id))
        assertNull(messageMapper[0])
    }

    @Test
    fun `reading component delete for an unknown entity causes nothing to happen`() {
        val (channel, entityMap, world) = setup()

        channel.offer(getDeleteAsBytes(COMPONENT_ID))

        world.process()

        assertFalse(ENTITY_MASTER_ID in entityMap)
    }

    @Test
    fun `reading Entity delete causes it to be deleted from the World and removed from the Entity ID map`() {
        val (channel, entityMap, world) = setup()

        val id = world.create()
        entityMap[ENTITY_MASTER_ID] = id
        channel.offer(getDeleteAsBytes(UpdateType.NO_COMPONENT))

        world.process()

        assertFalse(world.entityManager.isActive(id))
        assertFalse(ENTITY_MASTER_ID in entityMap)
    }

    @PooledWeaver
    @Noarg
    data class StringComponent(var message: String) : SerializedComponent() {
        override fun serializeTo(outputStream: DataOutput) = outputStream.writeUTF(message)

        override fun deserializeFrom(inputStream: DataInput) {
            message = inputStream.readUTF()
        }
    }

    /**
     * It is important to handle all serializing manually here, to make sure we respect the internal protocol
     */
    private fun StringComponent.toBytes() = ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            dos.writeByte(0)
            dos.writeInt(ENTITY_MASTER_ID)
            dos.writeInt(COMPONENT_ID)
            dos.writeUTF(this.message)
        }
        bos.toByteArray()
    }

    /**
     * It is important to handle all serializing manually here, to make sure we respect the internal protocol
     */
    private fun getDeleteAsBytes(componentId: Int) = ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            dos.writeByte(1)
            dos.writeInt(ENTITY_MASTER_ID)
            dos.writeInt(componentId)
        }

        bos.toByteArray()
    }

    private val componentsRegistry = TestComponentsRegistry()

    private fun registerComponent() {
        componentsRegistry[StringComponent::class.java] = COMPONENT_ID
    }

    private val strings = object : () -> String {
        private val beers = Faker().beer()
        override operator fun invoke() = beers.name()
    }

    private fun setup(): Triple<Channel<ByteArray>, EntityMap, World> {
        val channel = Channel<ByteArray>(1)
        val entityMap = EntityMap()
        return Triple(
            channel, entityMap, World(
                WorldConfigurationBuilder()
                    .with(ChannelReadSystem(channel))
                    .build()
                    .register(entityMap)
                    .register(ComponentsRegistry::class.java.canonicalName, componentsRegistry)
            )
        )
    }
}