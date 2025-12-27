package app.forigon.settings

import io.github.mlmgames.settings.core.annotations.CategoryDefinition
import io.github.mlmgames.settings.core.annotations.SchemaVersion
import io.github.mlmgames.settings.core.annotations.Setting
import io.github.mlmgames.settings.core.types.Dropdown
import io.github.mlmgames.settings.core.types.Toggle
import kotlinx.serialization.Serializable

@CategoryDefinition(order = 0)
object General

@CategoryDefinition(order = 1)
object Apps

@CategoryDefinition(order = 2)
object Search

@Serializable
enum class ThemeMode { System, Light, Dark }

@Serializable
enum class SortOrder { AZ, ZA, Recent }

@Serializable
enum class SearchType { Contains, Fuzzy, StartsWith }

@Serializable
enum class AppDrawerStyle { List, Bubble }

@SchemaVersion(version = 2)
@Serializable
data class LauncherSettings(
    val theme: ThemeMode = ThemeMode.System,

    @Setting(
        title = "Show Icons",
        description = "Display icons in launcher",
        category = General::class,
        type = Toggle::class,
        key = "show_app_icons"
    )
    val showAppIcons: Boolean = true,

    val iconPack: String = "default",

    @Setting(
        title = "App Drawer Style",
        description = "List view or bubble cloud",
        category = Apps::class,
        type = Dropdown::class,
        key = "app_drawer_style",
        options = ["List", "Bubble"]
    )
    val appDrawerStyle: AppDrawerStyle = AppDrawerStyle.List,

    @Setting(
        title = "Sort Order",
        category = Apps::class,
        type = Dropdown::class,
        key = "sort_order",
        options = ["A-Z", "Z-A", "Recent"]
    )
    val sortOrder: SortOrder = SortOrder.AZ,

    @Setting(
        title = "Search Type",
        category = Search::class,
        type = Dropdown::class,
        key = "search_type",
        options = ["Contains", "Fuzzy", "Starts With"]
    )
    val searchType: SearchType = SearchType.Contains,

    @Setting(
        title = "Search Aliases",
        category = Search::class,
        type = Dropdown::class,
        key = "search_aliases_mode",
        options = ["Off", "Transliteration", "Keyboard Swap", "Both"]
    )
    val searchAliasesMode: Int = 0,

    @Setting(
        title = "Include Package Names in Search",
        category = Search::class,
        type = Toggle::class,
        key = "search_include_package_names"
    )
    val searchIncludePackageNames: Boolean = false,

    @Setting(
        title = "Show Hidden Apps in Search",
        category = Search::class,
        type = Toggle::class,
        key = "show_hidden_apps_on_search"
    )
    val showHiddenAppsOnSearch: Boolean = false,
)