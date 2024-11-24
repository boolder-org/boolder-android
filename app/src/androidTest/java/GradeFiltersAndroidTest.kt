import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boolder.boolder.R
import com.boolder.boolder.view.main.MainActivity
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class GradeFiltersAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testGradeFilters() = runTest {
        // Search for "Franchard Isatis" area
        onView(withId(R.id.search_container)).perform(click())
        onView(withText("Isatis")).perform(click())
        composeTestRule.waitForIdle()
        onView(withId(R.id.area_name)).apply {
            check(matches(withText("Franchard Isatis")))
            perform(click())
        }

        // Wait for the "Grades" button to be displayed
        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithText(composeTestRule.activity.getString(R.string.grades))
                .fetchSemanticsNodes().size == 1
        }

        // Open Grades filter bottom sheet
        composeTestRule.activity.getString(R.string.grades).let { gradesString ->
            composeTestRule.waitUntilAtLeastOneExists(hasText(gradesString))
            composeTestRule.onNodeWithText(gradesString).performClick()
        }

        // Select custom grade range
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.custom)).performClick()

        // Select a min grade that is greater than the current max grade
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.grade_min)).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1b").onParent().performScrollToNode(hasText("5b+"))
        composeTestRule.onNodeWithText("5b+").performClick()
        composeTestRule.waitForIdle()

        // Assert both min and max grades are 5b+
        composeTestRule.waitUntil(2_000L) {
            composeTestRule.onAllNodesWithText("5b+").fetchSemanticsNodes().size == 2
        }

        // Select a max grade that is lower than the current min grade
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.grade_max)).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1b").onParent().performScrollToNode(hasText("5a+"))
        composeTestRule.onNodeWithText("5a+").performClick()
        composeTestRule.waitForIdle()

        // Assert both min and max grades are 5a+
        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithText("5a+").fetchSemanticsNodes().size == 2
        }

        // Select min grade as 4b+
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.grade_min)).performClick()
        composeTestRule.onNodeWithText("5c").onParent().performScrollToNode(hasText("4b+"))
        composeTestRule.onNodeWithText("4b+").performClick()

        // Click on the "Reset" button
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.reset)).performClick()

        // Reopen Grades filter bottom sheet
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.grades)).performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.custom)).performClick()
        composeTestRule.waitForIdle()

        // Assert min and max grades are "4b+" and "5a+" (verify that previous custom settings are restored)
        assert(composeTestRule.onAllNodesWithText("4b+").fetchSemanticsNodes().size == 1)
        assert(composeTestRule.onAllNodesWithText("5a+").fetchSemanticsNodes().size == 1)
    }
}
