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

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

data class Update private constructor(
    val type: Type,
    val id: Int,
    val component: SerializedComponent?
) {
    enum class Type {
        COMPONENT,
        DELETE
    }

    companion object {
        fun component(id: Int, component: SerializedComponent) = Update(Type.COMPONENT, id, component)
        fun delete(id: Int) = Update(Type.DELETE, id, null)
    }
}

fun ReceiveChannel<Update>.mapAsBytes(): ReceiveChannel<ByteArray> = this.map {
    ByteArrayOutputStream().use { bos ->
        DataOutputStream(bos).use { dos ->
            dos.writeByte(it.type.ordinal)
            dos.writeInt(it.id)
            val component = it.component
            if (component != null) {
                dos.writeInt(ComponentsRegistry[component::class.java])
                component.serializeTo(dos)
            }
        }
        bos.toByteArray()
    }
}
