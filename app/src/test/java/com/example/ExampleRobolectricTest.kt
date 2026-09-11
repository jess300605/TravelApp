package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Destination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Viajes Destinos", appName)
  }

  @Test
  fun `destination model toMap and fromMap verification`() {
    val destination = Destination(
      id = "test_1",
      name = "Cancún Paradise",
      country = "México",
      price = 450.0,
      description = "Un viaje increíble lleno de sol y arena blanca caribeña.",
      imageUri = "file:///dummy.jpg"
    )

    val map = destination.toMap()
    assertEquals("test_1", map["id"])
    assertEquals("Cancún Paradise", map["name"])
    assertEquals(450.0, map["price"])

    val restored = Destination.fromMap("test_1", map)
    assertEquals(destination.name, restored.name)
    assertEquals(destination.country, restored.country)
    assertEquals(destination.price, restored.price, 0.001)
    assertTrue(restored.description.length >= 20)
  }

  @Test
  fun `destination adapter item count and binding`() {
    val sampleList = listOf(
      Destination(
        id = "dest_1",
        name = "Cartagena Histórica",
        country = "Colombia",
        price = 350.0,
        description = "Ciudad amurallada caribeña con arquitectura colonial.",
        imageUri = ""
      )
    )
    val adapter = com.example.ui.adapter.DestinationAdapter(
      items = sampleList,
      onEditClick = {},
      onDeleteClick = {}
    )
    assertEquals(1, adapter.itemCount)
  }
}

