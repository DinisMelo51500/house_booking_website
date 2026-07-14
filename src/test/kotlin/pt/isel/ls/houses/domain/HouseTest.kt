package pt.isel.ls.houses.domain

import org.junit.Assert.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class TitleTest {
    @Test
    fun `creates title with valid value`() {
        val title = Name("Nice Beach House")
        assertEquals("Nice Beach House", title.value)
    }

    @Test
    fun `throws when title is blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            Name(" ")
        }
    }

    @Test
    fun `throws when title is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Name("")
        }
    }

    @Test
    fun `throws when title is too long`() {
        val longTitle = "a".repeat(101)

        assertThrows(IllegalArgumentException::class.java) {
            Name(longTitle)
        }
    }
}

class LocationIdTest {
    @Test
    fun `creates location id with valid value`() {
        val locationId = Id(1)
        assertEquals(1u, locationId)
    }
}

class AreaSqMtTest {
    @Test
    fun `creates area with valid value`() {
        val area = AreaSqMt(120)
        assertEquals(120u, area)
    }
}

class PricePerNightTest {
    @Test
    fun `creates price with valid value`() {
        val price = PricePerNight(100.0)
        assertEquals(100.0, price.value)
    }

    @Test
    fun `throws when price is zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            PricePerNight(0.0)
        }
    }

    @Test
    fun `throws when price is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            PricePerNight(-20.0)
        }
    }
}

class DescriptionTest {
    @Test
    fun `creates description with valid value`() {
        val description = Description("Beautiful house near the beach")
        assertEquals("Beautiful house near the beach", description.value)
    }

    @Test
    fun `throws when description is blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            Description(" ")
        }
    }

    @Test
    fun `throws when description is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Description("")
        }
    }

    @Test
    fun `throws when description is too long`() {
        val longDescription = "a".repeat(501)

        assertThrows(IllegalArgumentException::class.java) {
            Description(longDescription)
        }
    }
}

class HouseTest {
    private val id = Id(1)
    private val title = Name("Beach House")
    private val locationId = Id(10)
    private val area = AreaSqMt(150)
    private val price = PricePerNight(120.0)
    private val description = Description("Nice house with sea view")
    private val ownerId = Id(2)

    @Test
    fun `creates house with valid data`() {
        val house =
            House(
                id = id,
                title = title,
                location = locationId,
                areaSqMt = area,
                pricePerNight = price,
                description = description,
                ownerId = ownerId,
            )

        assertEquals(id, house.id)
        assertEquals(title, house.title)
        assertEquals(locationId, house.location)
        assertEquals(area, house.areaSqMt)
        assertEquals(price, house.pricePerNight)
        assertEquals(description, house.description)
        assertEquals(ownerId, house.ownerId)
    }
}
