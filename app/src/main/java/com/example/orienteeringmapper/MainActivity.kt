package com.example.orienteeringmapper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.orienteeringmapper.ui.theme.OrienteeringMapperTheme
import java.util.*
import androidx.compose.ui.Alignment
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import java.io.File
import android.util.Log

object ImportState {
    var lastSvgPath: String? = null
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OrienteeringMapperTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ProjectPickerScreen { projectId ->
                        // Callback when user picks or creates a project
                        startActivity(
                            Intent(this, LeafletActivity::class.java)
                                .putExtra("EXTRA_PROJECT_ID", projectId)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectPickerScreen(
    onProjectSelected: (projectId: String) -> Unit
) {
    val context = LocalContext.current
    val existingProjects = remember { listOf("Demo project") }
    var dialogOpen by remember { mutableStateOf(false) }

    // Launcher for file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // You could parse file name/type here
            val projectId = importFileAndCreateProject(context, uri)
            onProjectSelected(projectId)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { dialogOpen = true }) {
            Text("Open Project…")
        }
        if (dialogOpen) {
            val items = existingProjects + "📂 Import File…"
            AlertDialog(
                onDismissRequest = { dialogOpen = false },
                title = { Text("Select or Create Project") },
                text = {
                    Column {
                        items.forEachIndexed { idx, label ->
                            TextButton(
                                onClick = {
                                    dialogOpen = false
                                    when {
                                        label.contains("Import") -> {
                                            filePickerLauncher.launch(arrayOf(
                                                "application/octet-stream",
                                                "image/svg+xml",
                                                "*/*" // fallback
                                            ))
                                        }
                                        else -> {
                                            onProjectSelected(existingProjects[idx])
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}
fun importFileAndCreateProject(context: Context, uri: Uri): String {
    val projectId = UUID.randomUUID().toString()

    val cursor = context.contentResolver.query(uri, null, null, null, null)
    var originalName = ""
    val mimeType = context.contentResolver.getType(uri) ?: ""
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) originalName = it.getString(nameIndex)
        }
    }
    val extension = originalName.substringAfterLast('.', mimeType.substringAfterLast('/'))
    val destFileName = "project_${projectId}.$extension"
    val dest = File(context.filesDir, destFileName)

    context.contentResolver.openInputStream(uri)?.use { input ->
        dest.outputStream().use { output -> input.copyTo(output) }
    }
    // If SVG, remember for Leaflet overlay
    if (extension.equals("svg", ignoreCase = true)) {
        ImportState.lastSvgPath = dest.absolutePath
    }
    // Log details
    Log.d("Import", "ProjectID: $projectId")
    Log.d("Import", "Source URI: $uri")
    Log.d("Import", "Original Name: $originalName, MIME: $mimeType")
    Log.d("Import", "Saved To: ${dest.absolutePath}")
    return projectId
}