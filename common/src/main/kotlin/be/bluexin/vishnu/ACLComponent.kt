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

import com.artemis.annotations.PooledWeaver
import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntMaps
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import java.io.DataInput
import java.io.DataOutput

@Noarg
@PooledWeaver
data class ACLComponent(var currentAcl: Int2IntMap = Int2IntMaps.EMPTY_MAP) : SerializedComponent() {

    override fun serializeTo(outputStream: DataOutput) {
        outputStream.writeInt(currentAcl.size)
        for (kv in currentAcl.int2IntEntrySet()) {
            outputStream.writeInt(kv.intKey)
            outputStream.writeInt(kv.intValue)
        }
    }

    override fun deserializeFrom(inputStream: DataInput) {
        val s = inputStream.readInt()
        currentAcl = Int2IntOpenHashMap(s)
        repeat(s) {
            @Suppress("ReplacePutWithAssignment") // would cause boxing
            currentAcl.put(inputStream.readInt(), inputStream.readInt())
        }
    }
}