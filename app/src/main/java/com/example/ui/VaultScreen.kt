package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VaultEntry
import com.example.data.VaultRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {
    val entries: StateFlow<List<VaultEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEntry(title: String, secret: String) {
        viewModelScope.launch {
            // Simulated local encryption
            val simulatedEncrypted = "ENC[AES-256]:${secret.hashCode().toUInt()}"
            repository.insert(VaultEntry(title = title, secretData = simulatedEncrypted))
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}

class VaultViewModelFactory(private val repository: VaultRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VaultViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(repository: VaultRepository, onNavigateBack: () -> Unit) {
    val viewModel: VaultViewModel = viewModel(factory = VaultViewModelFactory(repository))
    val entries by viewModel.entries.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encrypted Vault", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = com.example.ui.theme.PoliHeaderDeep) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.PoliBg)
            )
        },
        containerColor = com.example.ui.theme.PoliBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, 
                modifier = Modifier.testTag("add_vault_entry"),
                containerColor = com.example.ui.theme.PoliSafe,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Safe Word")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Vault is empty. Add a family safe word or anchor.", style = MaterialTheme.typography.bodyLarge, color = com.example.ui.theme.PoliText)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entries) { entry ->
                        VaultItemCard(
                            entry = entry,
                            onDelete = { viewModel.deleteEntry(entry.id) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddVaultEntryDialog(
                onDismiss = { showDialog = false },
                onAdd = { title, secret ->
                    viewModel.addEntry(title, secret)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun VaultItemCard(entry: VaultEntry, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .border(1.dp, com.example.ui.theme.PoliCardBorder, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("vault_item_${entry.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(com.example.ui.theme.PoliLogBg, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Encrypted", tint = com.example.ui.theme.PoliSafe)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = entry.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = com.example.ui.theme.PoliText, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = entry.secretData, fontSize = 11.sp, color = com.example.ui.theme.PoliLogItemSubtitle)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Entry", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddVaultEntryDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Vault Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., Family Safe Word)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("vault_title_input")
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("Secret Phrase") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("vault_secret_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && secret.isNotBlank()) onAdd(title, secret) },
                modifier = Modifier.testTag("save_vault_button")
            ) {
                Text("Encrypt & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
