package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.Destination
import com.example.ui.screens.DestinationCardItem
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun destination_card_screenshot() {
    val sampleDestination = Destination(
      id = "screenshot_test_1",
      name = "Cancún All-Inclusive Resort",
      country = "México",
      price = 599.00,
      description = "Disfruta de playas caribeñas de arena blanca, buffet internacional y deportes acuáticos en Riviera Maya.",
      imageUri = ""
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        DestinationCardItem(
          destination = sampleDestination,
          onEdit = {},
          onDelete = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

