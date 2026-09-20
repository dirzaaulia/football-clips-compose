---
name: compose-multiplatform-patterns
description: Patterns for building shared UI across Android, iOS, Desktop, and Web using Compose Multiplatform and Jetpack Compose. Covers state management, navigation, theming, and performance.
---

# Compose Multiplatform Patterns

Patterns for building shared UI across Android, iOS, Desktop, and Web using Compose Multiplatform and Jetpack Compose. Covers state management, navigation, theming, and performance.

## State Management

### ViewModel + Single State Object
Use a single data class for screen state. Expose it as `StateFlow` and collect in Compose:

```kotlin
data class ItemListState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class ItemListViewModel(
    private val getItems: GetItemsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ItemListState())
    val state: StateFlow<ItemListState> = _state.asStateFlow()

    fun onSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadItems(query)
    }

    private fun loadItems(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getItems(query).fold(
                onSuccess = { items ->
                    _state.update { it.copy(items = items, isLoading = false) }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            )
        }
    }
}
```

### Collecting State in Compose
```kotlin
@Composable
fun ItemListScreen(viewModel: ItemListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ItemListContent(
        state = state,
        onSearch = viewModel::onSearch
    )
}
```

### Event Sink Pattern
For complex screens, use a sealed interface for events:

```kotlin
sealed interface ItemListEvent {
    data class Search(val query: String) : ItemListEvent
    data class Delete(val itemId: String) : ItemListEvent
    data object Refresh : ItemListEvent
}

fun onEvent(event: ItemListEvent) {
    when (event) {
        is ItemListEvent.Search -> onSearch(event.query)
        is ItemListEvent.Delete -> deleteItem(event.itemId)
        is ItemListEvent.Refresh -> loadItems(_state.value.searchQuery)
    }
}
```

---

## Navigation

### Type-Safe Navigation (Compose Navigation)
Define routes as `@Serializable` objects:

```kotlin
@Serializable
data object HomeRoute

@Serializable
data class DetailRoute(val id: String)

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(onNavigateToDetail = { id -> navController.navigate(DetailRoute(id)) })
        }
        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            DetailScreen(id = route.id)
        }
    }
}
```

---

## KMP Platform-Specific UI

### expect/actual for Platform Composables

```kotlin
// commonMain
@Composable
expect fun PlatformStatusBar(darkIcons: Boolean)

// androidMain
@Composable
actual fun PlatformStatusBar(darkIcons: Boolean) {
    // Android implementation
}
```

---

## Performance

### Stable Types & Lazy Lists
Mark UI state models as `@Immutable` or `@Stable`.
Always provide stable keys in LazyColumn/LazyRow:

```kotlin
LazyColumn {
    items(
        items = items,
        key = { it.id }
    ) { item ->
        ItemRow(item = item)
    }
}
```

---

## Anti-Patterns to Avoid
- Using `mutableStateOf` in ViewModels when `MutableStateFlow` with `collectAsStateWithLifecycle` is safer for lifecycle.
- Passing `NavController` deep into composables — pass lambda callbacks instead.
- Heavy computation inside `@Composable` functions — move to ViewModel or `remember {}`.
- Creating new object instances in composable parameters.
