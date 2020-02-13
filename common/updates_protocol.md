# Updates Protocol

*Note: All numbers are encoded with Big Endian*

Updates are sent over Aeron as bytes using the following format :

| bytes | value |
| ----: | :---- |
| 0     | [Update type](#update-type) |
| 1-4   | [Master ID](#master-id) |
| 5-8   | [Component ID](#component-id) |
| 9-*   | [Component Data](#component-data) |

## Update Type

Single byte representing the type of update for the packet.
Current possible values are :

| value | type |
| ----: | :--- |
| 0     | [Component](#component) |
| 1     | [Delete](#delete) |

### Component

The packet represents a component update.
The data should overrides any previous state for the component
[Component ID](#component-id) on the entity referred to by the provided
[Master ID](#master-id).

### Delete

The packet represents a deletion.

In case of a [Component ID](#component-id) with value `-1`, the entity
referred to by the provided [Master ID](#master-id) should be deleted.
Otherwise, any previous state for the component [Component ID](#component-id)
should be deleted on the entity referred to by the provided
[Master ID](#master-id).

## Master ID

4 bytes representing the numeric ID of the entity to act on.

## Component ID

4 bytes representing the numeric ID of the component to act on, or `-1`
in case of an entity deletion.

## Component Data

(only for [Components](#component))

Variable amount of bytes holding the data needed to deserialize the
component referred to by the provided [Component ID](#component-id).
