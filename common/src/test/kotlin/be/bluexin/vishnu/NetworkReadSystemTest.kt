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

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataOutput
import java.io.DataOutputStream

internal class NetworkReadSystemTest {
    @Noarg
    data class StringComponent(var message: String) : SerializedComponent() {
        override fun serializeTo(outputStream: DataOutput) {
            outputStream.writeUTF(message)
        }

        override fun deserializeFrom(inputStream: DataInput) {
            message = inputStream.readUTF()
        }
    }

    private fun SerializedComponent.toBytes() = ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            dos.writeByte(0)
            dos.writeInt(1)
            dos.writeInt(ComponentsRegistry[this::class.java])
            this.serializeTo(dos)
        }
        bos.toByteArray()
    }

    @BeforeEach
    fun beforeEach() {
        ComponentsRegistry.reset()
    }

    @Test
    fun `reading known components leads to entities being added to the World`() {
        ComponentsRegistry += StringComponent::class.java
        val channel = Channel<ByteArray>(1)
        val world = World(WorldConfigurationBuilder()
            .with(NetworkReadSystem(channel))
            .build())

        val component = StringComponent("Hello, World !")

        channel.offer(component.toBytes())

        world.process()

        val messageMapper = world.getMapper<StringComponent>()
        val result = messageMapper[0]

        assertEquals(component, result)
    }

    @Test
    fun `reading unknown components leads to no entities being added to the World`() {
        val channel = Channel<ByteArray>(1)
        val world = World(WorldConfigurationBuilder()
            .with(NetworkReadSystem(channel))
            .build())

        val component = StringComponent("Hello, World !")

        channel.offer(component.toBytes())
        ComponentsRegistry.reset()

        world.process()

        val messageMapper = world.getMapper<StringComponent>()
        val result = messageMapper[0]

        assertNull(result)
    }
}