import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import missionchat.app.bycompose.desktop.generated.resources.Res
import missionchat.app.bycompose.desktop.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import ru.kyamshanov.missionChat.App
import ru.kyamshanov.missionChat.initKoin
import ru.kyamshanov.missionChat.presentation.components.RootComponent
import ru.kyamshanov.missionChat.presentation.factories.RootComponentFactory
import java.awt.Dimension


fun main() {
    Napier.base(DebugAntilog())
    val lifecycle = LifecycleRegistry()
    val root: RootComponent
    initKoin().also { koin ->
        root = runOnUiThread {
            koin.get<RootComponentFactory>()
                .create(DefaultComponentContext(lifecycle = lifecycle))
        }
    }


    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Maximized)

        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "missionChat",
            icon = painterResource(Res.drawable.app_icon)
        ) {
            window.minimumSize = Dimension(600, 800)
            App(root)
        }
    }
}
