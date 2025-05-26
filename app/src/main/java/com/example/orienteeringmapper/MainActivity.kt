package com.example.orienteeringmapper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.orienteeringmapper.ui.theme.OrienteeringMapperTheme
import java.io.File
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OrienteeringMapperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProjectPickerScreen { importedFilePath ->
                        // importedFilePath is the absolute path to the copied .ocd in filesDir
                        val intent = Intent(this, LeafletActivity::class.java)
                            .putExtra("EXTRA_OCD_FILE", importedFilePath)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectPickerScreen(
    onFileImported: (ocdAbsolutePath: String) -> Unit
) {
    val context = LocalContext.current
    var dialogOpen by remember { mutableStateOf(false) }

    // Launcher for picking *any* file (we'll filter client-side)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val destPath = importAndSaveFile(context, uri)
            if (destPath != null) {
                onFileImported(destPath)
            } else {
                Log.e("Import", "Failed to import file")
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { dialogOpen = true }) {
            Text("Open OCAD File…")
        }

        if (dialogOpen) {
            AlertDialog(
                onDismissRequest = { dialogOpen = false },
                title = { Text("Import OCAD File") },
                text = {
                    Text("Select an .ocd file to import into your project.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        dialogOpen = false
                        // Launch system picker, filtering for .ocd MIME
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }) {
                        Text("Choose File")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialogOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun importAndSaveFile(context: Context, uri: Uri): String? {
    return try {
        // Figure out original filename
        var originalName = "imported.ocd"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) originalName = cursor.getString(idx)
            }
        }

        // Build a safe destination name
        val projectId    = UUID.randomUUID().toString()
        val extension    = originalName.substringAfterLast('.', "ocd")
        val destFileName = "project_${projectId}.$extension"
        val destFile     = File(context.filesDir, destFileName)

        // Copy bytes
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Log.d("Import", "Saved OCAD to: ${destFile.absolutePath}")
        destFile.absolutePath
    } catch (e: Exception) {
        Log.e("Import", "Error importing OCAD", e)
        null
    }
}
