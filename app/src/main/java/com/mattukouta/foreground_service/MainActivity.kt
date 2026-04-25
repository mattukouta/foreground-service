package com.mattukouta.foreground_service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mattukouta.foreground_service.event.TakeEventFlow
import com.mattukouta.foreground_service.ui.theme.ForegroundserviceTheme
import com.mattukouta.foreground_service.vo.TakeEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForegroundserviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScreenshotControl(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ScreenshotControl(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(context, ScreenshotService::class.java).apply {
                putExtra(ScreenshotService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenshotService.EXTRA_DATA, result.data)
            }
            context.startForegroundService(serviceIntent)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            projectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }
    }

    LaunchedEffect(Unit) {
        TakeEventFlow.event.collect { event ->
            when (event) {
                TakeEvent.Success -> {
                    // 成功時の処理（トースト表示など）
                    Toast.makeText(context, "Screenshot saved!", Toast.LENGTH_SHORT).show()
                }

                TakeEvent.Failed -> {
                    // 失敗時の処理
                    Toast.makeText(context, "Failed to take screenshot", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = modifier) {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                projectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
            }
        }) {
            Text("Start Screenshot Service")
        }

        Button(onClick = {
            context.stopService(Intent(context, ScreenshotService::class.java))
        }) {
            Text("Stop Screenshot Service")
        }
    }
}
