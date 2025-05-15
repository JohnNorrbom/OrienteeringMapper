package com.example.orienteeringmapper

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
    // TODO: Replace this stub list with your real project-loading logic
    val existingProjects = remember { listOf("Trail-Map A", "Forest-Loop") }

    var dialogOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { dialogOpen = true }) {
            Text("Open Project…")
        }

        if (dialogOpen) {
            val items = existingProjects + "➕ New Project"
            AlertDialog(
                onDismissRequest = { dialogOpen = false },
                title = { Text("Select or Create Project") },
                text = {
                    Column {
                        items.forEachIndexed { idx, label ->
                            TextButton(
                                onClick = {
                                    dialogOpen = false
                                    val projectId = if (idx < existingProjects.size) {
                                        existingProjects[idx]
                                    } else {
                                        UUID.randomUUID().toString()
                                    }
                                    onProjectSelected(projectId)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
