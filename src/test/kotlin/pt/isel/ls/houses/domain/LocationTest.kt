package pt.isel.ls.houses.domain

import org.junit.Assert.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocationTest {
    private val countryId = Id(1)
    private val regionId = Id(2)

    @Test
    fun `creates country without parent`() {
        val location =
            Location(
                id = countryId,
                name = Name("Portugal"),
                type = LocationType.COUNTRY,
                parentId = null,
            )

        assertEquals(LocationType.COUNTRY, location.type)
        assertEquals(null, location.parentId)
    }

    @Test
    fun `throws when country has parent`() {
        assertThrows(IllegalArgumentException::class.java) {
            Location(
                id = countryId,
                name = Name("Portugal"),
                type = LocationType.COUNTRY,
                parentId = Id(99),
            )
        }
    }

    @Test
    fun `throws when non country location has no parent`() {
        assertThrows(IllegalArgumentException::class.java) {
            Location(
                id = regionId,
                name = Name("Lisbon Region"),
                type = LocationType.REGION,
                parentId = null,
            )
        }
    }

    @Test
    fun `creates region with parent`() {
        val location =
            Location(
                id = regionId,
                name = Name("Lisbon Region"),
                type = LocationType.REGION,
                parentId = countryId,
            )

        assertEquals(LocationType.REGION, location.type)
        assertEquals(countryId, location.parentId)
    }
}

class LocationChildrenTest {
    private val portugal =
        Location(
            id = Id(1),
            name = Name("Portugal"),
            type = LocationType.COUNTRY,
        )

    private val lisbonRegion =
        Location(
            id = Id(2),
            name = Name("Lisbon Region"),
            type = LocationType.REGION,
            parentId = Id(1),
        )

    private val portoRegion =
        Location(
            id = Id(3),
            name = Name("Porto Region"),
            type = LocationType.REGION,
            parentId = Id(1),
        )

    private val municipality =
        Location(
            id = Id(4),
            name = Name("Lisbon Municipality"),
            type = LocationType.MUNICIPALITY,
            parentId = Id(2),
        )

    @Test
    fun `childrenOf returns locations with given parent`() {
        val locations = listOf(portugal, lisbonRegion, portoRegion, municipality)

        val children = locations.childrenOf(Id(1))

        assertEquals(2, children.size)
        assertTrue(children.contains(lisbonRegion))
        assertTrue(children.contains(portoRegion))
    }

    @Test
    fun `childrenOf returns empty list when no children exist`() {
        val locations = listOf(portugal, lisbonRegion, portoRegion, municipality)

        val children = locations.childrenOf(Id(999))

        assertTrue(children.isEmpty())
    }
}
