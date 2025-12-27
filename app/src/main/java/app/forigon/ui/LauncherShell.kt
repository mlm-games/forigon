package app.forigon.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import app.forigon.LauncherViewModel
import kotlinx.serialization.Serializable
import app.forigon.ui.screens.ControlCenter
import app.forigon.ui.screens.SettingsScreen
import app.forigon.ui.screens.WatchAppDrawer
import app.forigon.ui.screens.WatchFaceScreen

@Composable
fun LauncherShell(viewModel: LauncherViewModel) {
    val backStack = rememberNavBackStack(LauncherKey.Home)

    val view = LocalView.current

    var bezelActive by remember { mutableStateOf(false) }

    fun goHome() {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun setTop(key: LauncherKey) {
        goHome()
        if (key != LauncherKey.Home) backStack.add(key)
    }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = {
            val top = backStack.lastOrNull()
            when (top) {
                LauncherKey.Settings -> goHome() // required: settings -> home
                LauncherKey.AppDrawer -> goHome() // required: app drawer -> home
                LauncherKey.ControlCenter -> goHome()
                LauncherKey.Home, null -> Unit // do nothing: prevents exiting launcher
            }
        },
        entryProvider = entryProvider {
            entry<LauncherKey.Home> {
                WatchFaceScreen(
                    onAppDrawerClick = { setTop(LauncherKey.AppDrawer) },
                    onSettingsClick = {setTop(LauncherKey.ControlCenter)}
                )
            }

            entry<LauncherKey.ControlCenter> {
                ControlCenter(
                    onSettingsClick = { setTop(LauncherKey.Settings) }
                )
            }

            entry<LauncherKey.AppDrawer> {
                WatchAppDrawer(viewModel = viewModel)
            }

            entry<LauncherKey.Settings> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { goHome() } // required: settings -> home
                )
            }
        }
    )
}

@Serializable
sealed interface LauncherKey : NavKey {

    @Serializable
    data object Home : LauncherKey

    @Serializable
    data object ControlCenter : LauncherKey

    @Serializable
    data object AppDrawer : LauncherKey

    @Serializable
    data object Settings : LauncherKey
}