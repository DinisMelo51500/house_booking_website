package pt.isel.ls.houses

import kotlin.math.absoluteValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinearRegressionTest {
    // ===============================
    // 1. Normalization tests
    // ===============================

    @Test
    fun `scale should normalize min to 0 and max to 1`() {
        val scale = Scale(listOf(10.0, 20.0, 30.0))
        assertEquals(0.0, scale.normalize(10.0), 1e-9)
        assertEquals(1.0, scale.normalize(30.0), 1e-9)
    }

    @Test
    fun `denormalize should invert normalize`() {
        val scale = Scale(listOf(5.0, 15.0))
        val value = 10.0
        val normalized = scale.normalize(value)
        assertEquals(0.5, normalized, 1e-9)
        val denormalized = scale.denormalize(normalized)
        assertEquals(value, denormalized, 1e-9)
    }

    @Test
    fun `normalize list of houses should produce values between 0 and 1`() {
        val normalized = houses.normalize()
        normalized.data.forEach {
            assertTrue(it.area in 0.0..1.0)
            assertTrue(it.price in 0.0..1.0)
        }
    }

    // ===============================
    // 2. Helper function tests
    // ===============================

    @Test
    fun `predict should compute linear function`() {
        val params = Params(2.0, 3.0)
        val result = predict(4.0, params)
        assertEquals(11.0, result, 1e-9)
    }

    @Test
    fun `error should compute difference between prediction and real`() {
        val err = error(10.0, 8.0)
        assertEquals(2.0, err, 1e-9)
    }

    @Test
    fun `gradients should compute correct partial derivatives`() {
        val grad =
            gradients(
                x = 2.0,
                error = 3.0,
                n = 4,
            )
        assertEquals((2.0 / 4) * 3.0 * 2.0, grad.w, 1e-9)
        assertEquals((2.0 / 4) * 3.0, grad.b, 1e-9)
    }

    @Test
    fun `updateParams should apply gradient descent step`() {
        val p = Params(1.0, 1.0)
        val delta = Params(0.5, 0.5)
        val lr = 0.1
        val (w, b) = updateParams(p, delta, lr)
        assertEquals(0.95, w, 1e-9)
        assertEquals(0.95, b, 1e-9)
    }

    // ===============================
    // 3. Training tests
    // ===============================

    @Test
    fun `train should reduce error on simple linear data`() {
        val simpleData =
            listOf(
                House(0.0, 0.0),
                House(0.5, 1.0),
                House(1.0, 2.0),
            )

        val params = train(simpleData, epochs = 5000, lr = 0.1)

        assertTrue((params.w - 2.0).absoluteValue < 0.1)
        assertTrue(params.b.absoluteValue < 0.1)
    }

    @Test
    fun `trained model should predict reasonably close to real data`() {
        val (_, _, data) = houses.normalize()
        val params = train(data, epochs = 4000, lr = 0.05)

        val house = data.first()
        val prediction = predict(house.area, params)
        val err = (prediction - house.price).absoluteValue

        assertTrue(err < 0.05)
    }

    // ===============================
    // 4. Params operator tests
    // ===============================

    @Test
    fun `params plus operator should sum weights and bias`() {
        val p1 = Params(1.0, 2.0)
        val p2 = Params(3.0, 4.0)

        val result = p1 + p2

        assertEquals(4.0, result.w, 1e-9)
        assertEquals(6.0, result.b, 1e-9)
    }

    // ===============================
    // 5. Normalization edge cases
    // ===============================

    @Test
    fun `normalize with single value should return NaN (Not a Number)`() {
        val scale = Scale(listOf(10.0))
        val normalized = scale.normalize(10.0)
        assertTrue(normalized.isNaN())
    }

    @Test
    fun `normalize should preserve list size`() {
        val normalized = houses.normalize()
        assertEquals(houses.size, normalized.data.size)
    }

    // ===============================
    // 6. Predict edge cases
    // ===============================

    @Test
    fun `predict should work with negative parameters`() {
        val params = Params(-2.0, -1.0)
        val result = predict(3.0, params)
        assertEquals(-7.0, result, 1e-9)
    }

    // ===============================
    // 7. Denormalization correctness
    // ===============================

    @Test
    fun `denormalize should return min when value is zero`() {
        val scale = Scale(listOf(10.0, 20.0))
        val denorm = scale.denormalize(0.0)
        assertEquals(10.0, denorm, 1e-9)
    }

    @Test
    fun `denormalize should return original max after normalization`() {
        val scale = Scale(listOf(10.0, 20.0))
        val normalizedMax = scale.normalize(20.0)
        val denorm = scale.denormalize(normalizedMax)
        assertEquals(20.0, denorm, 1e-9)
    }

    // ===============================
    // 8. Training robustness tests
    // ===============================

    @Test
    fun `train should learn linear relation with bias`() {
        val data =
            listOf(
                House(0.0, 1.0),
                House(1.0, 3.0),
                House(2.0, 5.0),
            )

        val params = train(data, epochs = 5000, lr = 0.05)

        assertTrue((params.w - 2.0).absoluteValue < 0.1)
        assertTrue((params.b - 1.0).absoluteValue < 0.1)
    }

    @Test
    fun `train should not diverge on small dataset`() {
        val data =
            listOf(
                House(0.0, 0.0),
                House(1.0, 1.0),
            )

        val params = train(data, epochs = 1000, lr = 0.05)

        assertFalse(params.w.isNaN())
        assertFalse(params.b.isNaN())
    }

    // ===============================
    // 9. Scale correctness tests
    // ===============================

    @Test
    fun `normalize midpoint correctly`() {
        val scale = Scale(listOf(0.0, 10.0))
        assertEquals(0.5, scale.normalize(5.0), 1e-9)
    }

    // ===============================
    // 10. Scale edge cases
    // ===============================

    @Test
    fun `normalize when all values are the same should return Nan(Not a Number)`() {
        val scale = Scale(listOf(10.0, 10.0))
        val normalized = scale.normalize(10.0)

        assertTrue(normalized.isNaN())
        assertFalse(normalized.isInfinite())
    }

    @Test
    fun `scale with empty list should throw exception`() {
        assertFailsWith<IllegalArgumentException> {
            Scale(emptyList())
        }
    }
}
