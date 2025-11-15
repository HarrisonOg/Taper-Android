# Taper Android - Unit Test Suite

This directory contains unit tests for the Taper Android application. The tests are organized by package and cover key components of the app.

## Test Structure

```
test/
├── com/harrisonog/taperAndroid/
│   ├── data/
│   │   └── db/
│   │       ├── HabitTest.kt           # Tests for Habit data class
│   │       └── HabitEventTest.kt      # Tests for HabitEvent data class
│   ├── logic/
│   │   └── ScheduleGeneratorTest.kt   # Tests for schedule generation logic
│   └── ui/
│       ├── FakeTaperRepository.kt     # Fake repository for testing
│       ├── HabitDetailViewModelTest.kt
│       ├── HabitEditorViewModelTest.kt
│       ├── HabitListViewModelTest.kt
│       └── MainDispatcherRule.kt      # Test rule for coroutines
```

## Test Coverage

### Data Layer Tests

#### `HabitTest.kt`
Tests the `Habit` data class including:
- Default values
- Copy functionality
- Equality/inequality
- Null handling for optional fields
- Good vs taper habit distinction

#### `HabitEventTest.kt`
Tests the `HabitEvent` data class including:
- Default values
- All response types (completed, denied, snoozed)
- Timestamp handling
- Snoozed event flags
- Equality comparisons

### Business Logic Tests

#### `ScheduleGeneratorTest.kt`
Tests the core scheduling algorithm:
- **Taper habits**: Verifies events decrease over time (5/day → 1/day)
- **Good habits**: Verifies events increase over time (1/day → 5/day)
- **Wake window**: Ensures all events fall within configured wake hours
- **Event distribution**: Validates events are evenly spread throughout the day
- **Multi-week habits**: Tests correct number of days (weeks × 7)
- **Edge cases**: Zero end-per-day, custom wake windows
- **Sorting**: Ensures events are chronologically ordered
- **Data integrity**: Verifies habit IDs and null response fields

### ViewModel Tests

#### `HabitEditorViewModelTest.kt`
Tests habit creation and editing:
- Creating new habits with all fields
- Updating existing habits
- Trimming whitespace from text fields
- Handling null descriptions
- Setting good vs taper habit flags
- Creating multiple habits

#### `HabitListViewModelTest.kt`
Tests the habit list screen:
- Deleting habits removes them from UI state
- Observable state updates

#### `HabitDetailViewModelTest.kt`
Tests the habit detail screen (existing)

### Test Utilities

#### `FakeTaperRepository.kt`
A test double that implements `TaperRepository`:
- In-memory storage using MutableStateFlow
- Supports all repository operations
- Allows setting test data via constructor
- Provides helper methods for test setup

#### `MainDispatcherRule.kt`
JUnit rule that:
- Replaces the main coroutine dispatcher with a test dispatcher
- Ensures coroutines run deterministically in tests
- Allows advancing time with `advanceUntilIdle()`

## Running Tests

### Command Line

Run all unit tests:
```bash
./gradlew test
```

Run tests for a specific module:
```bash
./gradlew app:test
```

Run tests with coverage:
```bash
./gradlew testDebugUnitTestCoverage
```

### Android Studio

1. Right-click on the `test` directory
2. Select "Run Tests"
3. Or run individual test files/methods by clicking the green arrow

## Writing New Tests

### Example Test Structure

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun testName_description() = runTest {
        // Arrange
        val repository = FakeTaperRepository()
        val viewModel = MyViewModel(repository)

        // Act
        viewModel.doSomething()
        advanceUntilIdle()

        // Assert
        assertEquals(expected, viewModel.state.value)
    }
}
```

### Best Practices

1. **Use descriptive names**: `testName_stateUnderTest_expectedBehavior()`
2. **Follow AAA pattern**: Arrange, Act, Assert
3. **Test one thing**: Each test should verify a single behavior
4. **Use fake dependencies**: Use `FakeTaperRepository` instead of real implementations
5. **Advance coroutines**: Call `advanceUntilIdle()` after async operations
6. **Clean state**: Tests should be independent and not share state

## Test Dependencies

The following testing libraries are used:

- **JUnit 4**: Test framework
- **kotlinx-coroutines-test**: Testing utilities for coroutines
- **Truth** (optional): More expressive assertions

## Coverage Goals

Current coverage areas:
- ✅ ViewModels (HabitEditor, HabitList, HabitDetail)
- ✅ Data classes (Habit, HabitEvent)
- ✅ Business logic (ScheduleGenerator)
- ⬜ Repository implementation (would require integration tests)
- ⬜ UI components (would require instrumented tests)
- ⬜ Database DAOs (would require instrumented tests)

## Continuous Integration

Tests run automatically on:
- Pull requests
- Commits to main branch
- Before release builds

## Troubleshooting

### Common Issues

**Tests fail with "Module with the Main dispatcher had failed to initialize"**
- Ensure `MainDispatcherRule` is applied: `@get:Rule val dispatcherRule = MainDispatcherRule()`

**Tests hang or timeout**
- Make sure to call `advanceUntilIdle()` after triggering async operations
- Check that background collection jobs are properly cancelled

**Flaky tests**
- Avoid using actual delays (`delay()`)
- Use `runTest` and virtual time instead
- Ensure tests don't depend on execution order

## Future Improvements

- Add integration tests for repository + database
- Add UI tests using Compose testing utilities
- Implement screenshot testing for UI components
- Add mutation testing to verify test quality
- Increase coverage to 80%+ for business logic
