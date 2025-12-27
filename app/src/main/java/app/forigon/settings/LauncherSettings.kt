package app.forigon.settings

import io.github.mlmgames.settings.core.annotations.CategoryDefinition
import io.github.mlmgames.settings.core.annotations.SchemaVersion
import io.github.mlmgames.settings.core.annotations.Setting
import io.github.mlmgames.settings.core.types.Dropdown
import io.github.mlmgames.settings.core.types.Slider
import io.github.mlmgames.settings.core.types.Toggle
import kotlinx.serialization.Serializable

@CategoryDefinition(order = 0)
object Display

@CategoryDefinition(order = 1)
object Motion

@CategoryDefinition(order = 2)
object Apps

@CategoryDefinition(order = 3)
object Search

@CategoryDefinition(order = 4)
object Input

@CategoryDefinition(order = 5)
object VirtualBezel

@CategoryDefinition(order = 6)
object Sorting

@Serializable
enum class ThemeMode { System, Light, Dark }

@Serializable
enum class SortOrder { AZ, ZA, Recent }

@Serializable
enum class SearchType { Contains, Fuzzy, StartsWith }

@Serializable
enum class AppDrawerStyle { List, Bubble }

/** How to open the app options menu on touch devices */
@Serializable
enum class AppOptionsGesture { LongPress, DoubleTap }

/** Rotary/virtual bezel scroll mapping */
@Serializable
enum class BezelScrollMode { Items, Pixels }

/** Motion / animation mode for low-power devices */
@Serializable
enum class MotionMode { Full, Reduced, Off }

@SchemaVersion(version = 3)
@Serializable
data class LauncherSettings(
    @Setting(
        title = "Theme",
        description = "App color theme",
        category = Display::class,
        type = Dropdown::class,
        key = "theme",
        options = ["System", "Light", "Dark"]
    )
    val theme: ThemeMode = ThemeMode.System,

    @Setting(
        title = "UI Scale",
        description = "Global interface scaling",
        category = Display::class,
        type = Slider::class,
        key = "ui_scale"
    )
    val uiScale: Float = 1.0f,

    @Setting(
        title = "Touch Target Boost",
        description = "Increase touch target sizes for small screens",
        category = Display::class,
        type = Toggle::class,
        key = "touch_target_boost"
    )
    val touchTargetBoost: Boolean = false,

    @Setting(
        title = "Show Icons",
        description = "Display app icons in launcher",
        category = Apps::class,
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
        description = "How apps are sorted in the drawer",
        category = Sorting::class,
        type = Dropdown::class,
        key = "sort_order",
        options = ["A-Z", "Z-A", "Recent"]
    )
    val sortOrder: SortOrder = SortOrder.AZ,

    @Setting(
        title = "Search Type",
        description = "How search queries match app names",
        category = Search::class,
        type = Dropdown::class,
        key = "search_type",
        options = ["Contains", "Fuzzy", "Starts With"]
    )
    val searchType: SearchType = SearchType.Contains,

    @Setting(
        title = "Search Aliases",
        description = "Additional matching methods for search",
        category = Search::class,
        type = Dropdown::class,
        key = "search_aliases_mode",
        options = ["Off", "Transliteration", "Keyboard Swap", "Both"]
    )
    val searchAliasesMode: Int = 0,

    @Setting(
        title = "Include Package Names",
        description = "Also search app package names",
        category = Search::class,
        type = Toggle::class,
        key = "search_include_package_names"
    )
    val searchIncludePackageNames: Boolean = false,

    @Setting(
        title = "Show Hidden Apps",
        description = "Include hidden apps in search results",
        category = Search::class,
        type = Toggle::class,
        key = "show_hidden_apps_on_search"
    )
    val showHiddenAppsOnSearch: Boolean = false,

    @Setting(
        title = "Motion Mode",
        description = "Animation complexity level",
        category = Motion::class,
        type = Dropdown::class,
        key = "motion_mode",
        options = ["Full", "Reduced", "Off"]
    )
    val motionMode: MotionMode = MotionMode.Full,

    @Setting(
        title = "Animation Speed",
        description = "Speed multiplier for animations (Full mode only)",
        category = Motion::class,
        type = Slider::class,
        key = "animation_speed"
    )
    val animationSpeed: Float = 1.0f,

    @Setting(
        title = "App Options Gesture",
        description = "How to open app context menu",
        category = Input::class,
        type = Dropdown::class,
        key = "app_options_gesture",
        options = ["Long Press", "Double Tap"]
    )
    val appOptionsGesture: AppOptionsGesture = AppOptionsGesture.LongPress,

    @Setting(
        title = "Enable Virtual Bezel",
        description = "Rotary input via edge gestures",
        category = VirtualBezel::class,
        type = Toggle::class,
        key = "enable_virtual_bezel"
    )
    val enableVirtualBezel: Boolean = true,

    @Setting(
        title = "Edge Ring Thickness",
        description = "Active area as fraction of screen radius",
        category = VirtualBezel::class,
        type = Slider::class,
        key = "bezel_edge_threshold_fraction"
    )
    val bezelEdgeThresholdFraction: Float = 0.30f,

    @Setting(
        title = "Sticky Inner Radius",
        description = "How far finger can drift inward while captured",
        category = VirtualBezel::class,
        type = Slider::class,
        key = "bezel_sticky_inner_fraction"
    )
    val bezelStickyInnerFraction: Float = 0.60f,

    @Setting(
        title = "Detent Degrees",
        description = "Rotation angle per click/step",
        category = VirtualBezel::class,
        type = Slider::class,
        key = "bezel_detent_degrees"
    )
    val bezelDetentDegrees: Float = 15f,

    @Setting(
        title = "Scroll Mode",
        description = "What each detent scrolls by",
        category = VirtualBezel::class,
        type = Dropdown::class,
        key = "bezel_scroll_mode",
        options = ["Items", "Pixels"]
    )
    val bezelScrollMode: BezelScrollMode = BezelScrollMode.Items,

    @Setting(
        title = "Pixels per Detent",
        description = "Scroll distance when mode is Pixels",
        category = VirtualBezel::class,
        type = Slider::class,
        key = "bezel_scroll_pixels_per_detent"
    )
    val bezelScrollPixelsPerDetent: Float = 28f,

    @Setting(
        title = "Items per Detent",
        description = "Items to scroll when mode is Items",
        category = VirtualBezel::class,
        type = Slider::class,
        key = "bezel_scroll_items_per_detent"
    )
    val bezelScrollItemsPerDetent: Int = 1,

    @Setting(
        title = "Invert Direction",
        description = "Reverse bezel scroll direction",
        category = VirtualBezel::class,
        type = Toggle::class,
        key = "bezel_invert_direction"
    )
    val bezelInvertDirection: Boolean = false,

    @Setting(
        title = "Haptic Feedback",
        description = "Vibrate on each detent",
        category = VirtualBezel::class,
        type = Toggle::class,
        key = "bezel_haptics"
    )
    val bezelHaptics: Boolean = true,

    @Setting(
        title = "Click Sound",
        description = "Play sound on each detent",
        category = VirtualBezel::class,
        type = Toggle::class,
        key = "bezel_sound"
    )
    val bezelSound: Boolean = true
)