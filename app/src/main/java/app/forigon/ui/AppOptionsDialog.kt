package app.forigon.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forigon.data.AppModel
import app.forigon.helper.getUserHandleFromString
import app.forigon.helper.openAppInfo
import app.forigon.helper.uninstall

@Composable
fun AppOptionsDialog(
    context: Context,
    app: AppModel,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onToggleHidden: () -> Unit,
) {
    val user = getUserHandleFromString(context, app.userString)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.appLabel) },
        text = {
            Column {
                Text(app.appPackage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))

                Button(onClick = { onOpen(); onDismiss() }) { Text("Open") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    openAppInfo(context, user, app.appPackage)
                    onDismiss()
                }) { Text("App info") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    onToggleHidden()
                    onDismiss()
                }) { Text(if (isHidden) "Unhide" else "Hide") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        context.uninstall(app.appPackage)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Uninstall") }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
