package app.forigon.settings

import app.forigon.data.HomeLayout
import io.github.mlmgames.settings.core.annotations.Persisted
import io.github.mlmgames.settings.core.annotations.SchemaVersion
import io.github.mlmgames.settings.core.annotations.Serialized
import kotlinx.serialization.Serializable

@SchemaVersion(version = 1)
@Serializable
data class LauncherState(
    @Persisted(key = "hidden_apps")
    val hiddenApps: Set<String> = emptySet(),

    @Persisted(key = "renamed_apps")
    val renamedApps: Map<String, String> = emptyMap(),

    @Persisted(key = "recent_app_history")
    val recentAppHistory: Map<String, Long> = emptyMap(),

    @Persisted(key = "home_layout")
    @Serialized
    val homeLayout: HomeLayout = HomeLayout(),
)