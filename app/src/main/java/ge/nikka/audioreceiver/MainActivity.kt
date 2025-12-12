package ge.nikka.audioreceiver

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import ge.nikka.audioreceiver.ui.theme.AudioReceiverTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationPermission(this).requestIfNeeded()
        enableEdgeToEdge()
        setContent {
            AudioReceiverTheme {
                ServiceControlScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ServiceControlScreen() {
        val ctx = LocalContext.current
        var running by remember {
            mutableStateOf(ServiceStateHelper.isRunning(ctx, AudioReceiverService::class.java))
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = { Text("Audio Receiver") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (running) "Status: Running" else "Status: Stopped",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val serviceIntent = Intent(ctx, AudioReceiverService::class.java)
                        if (running) {
                            ctx.stopService(serviceIntent)
                        } else {
                            ctx.startForegroundService(serviceIntent)
                        }
                        running = !running
                    },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(if (running) "Stop Service" else "Start Service")
                }
            }
        }
    }

    object ServiceStateHelper {
        fun isRunning(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            return manager.getRunningServices(Int.MAX_VALUE)
                .any { it.service.className == serviceClass.name }
        }
    }

    class NotificationPermission(private val activity: ComponentActivity) {
        private var request = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}
        fun requestIfNeeded() {
            if (Build.VERSION.SDK_INT >= 33) {
                request.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}