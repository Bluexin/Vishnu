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

import com.artemis.annotations.Wire
import it.unimi.dsi.fastutil.ints.Int2IntFunction
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

/**
 * Class to [Wire] to get masterId and localId resolution.
 */
@Suppress("MemberVisibilityCanBePrivate")
class EntityMap {

    /**
     * Returns true if this map knows about [masterId], and false otherwise
     */
    operator fun contains(masterId: Int): Boolean = entityMap.containsKey(masterId)

    /**
     * Get the local ID for [masterId], or [UNKNOWN_ENTITY_ID] if [masterId] is not known
     */
    operator fun get(masterId: Int): Int = entityMap[masterId]

    /**
     * Stores the [localId] matching [masterId]
     */
    fun put(masterId: Int, localId: Int) {
        entityMap.put(masterId, localId)
    }

    /**
     * Removes the [masterId] from this map
     */
    fun remove(masterId: Int) {
        entityMap.remove(masterId)
    }

    /**
     * Operator alias for [put]
     */
    operator fun set(masterId: Int, localId: Int) = this.put(masterId, localId)

    /**
     * Operator alias for [remove]
     */
    operator fun minusAssign(masterId: Int) = this.remove(masterId)

    private val entityMap: Int2IntFunction = Int2IntOpenHashMap().apply {
        defaultReturnValue(UNKNOWN_ENTITY_ID)
    }

    companion object {
        /**
         * Special value to identify unknown IDs
         */
        const val UNKNOWN_ENTITY_ID = -1
    }
}