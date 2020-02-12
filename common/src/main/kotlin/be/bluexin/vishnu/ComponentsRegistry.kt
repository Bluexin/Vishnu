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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

@Suppress("ReplacePutWithAssignment")
object ComponentsRegistry { // TODO: get registry from master node
    private var currentId = 0
    private val registry : Object2IntMap<Class<out SerializedComponent>> = Object2IntOpenHashMap()
    private val reverseRegistry : Int2ObjectMap<Class<out SerializedComponent>> = Int2ObjectOpenHashMap()

    operator fun plusAssign(component: Class<out SerializedComponent>) {
        this[component]
    }

    operator fun get(component: Class<out SerializedComponent>): Int =
        if (component in this.registry) this.registry.getInt(component) else {
            this.registry.put(component, currentId)
            this.reverseRegistry.put(currentId, component)
            currentId++
        }

    operator fun get(id: Int): Class<out SerializedComponent>? = this.reverseRegistry.get(id)

    internal fun reset() {
        registry.clear()
        reverseRegistry.clear()
    }
}