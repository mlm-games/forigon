package app.forigon.settings

import android.content.Context
import io.github.mlmgames.settings.core.SettingsRepository
import io.github.mlmgames.settings.core.datastore.createSettingsDataStore
import io.github.mlmgames.settings.core.managers.MigrationManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LauncherSettingsManager(context: Context) {
    private val dataStore = createSettingsDataStore(context, name = "launcher_settings")
    val repo = SettingsRepository(dataStore, LauncherSettingsSchema)

    private val migrations = MigrationManager(
        dataStore = dataStore,
        currentVersion = schemaVersionOf<LauncherSettings>()
    )

    private val initMutex = Mutex()
    private var initialized = false

    suspend fun initOnce() {
        initMutex.withLock {
            if (initialized) return
            migrations.migrate()
            initialized = true
        }
    }
}