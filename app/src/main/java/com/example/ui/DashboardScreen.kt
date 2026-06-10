package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ml.ThreatAlert
import com.example.ml.ThreatAnalyzer
import com.example.ml.ThreatLevel
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val analyzer = ThreatAnalyzer()
    
    private val _currentAlert = MutableStateFlow(ThreatAlert(ThreatLevel.SAFE, "System Ready", "Awaiting active call to monitor linguistic markers.", 100))
    val currentAlert: StateFlow<ThreatAlert> = _currentAlert.asStateFlow()

    private val _isMonitoring = MutableStateFlow(true)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    init {
        toggleMonitoring()
        toggleMonitoring()
    }

    fun toggleMonitoring() {
        if (_isMonitoring.value) {
            _isMonitoring.value = false
            _currentAlert.value = ThreatAlert(ThreatLevel.SAFE, "System Normal", "Monitoring offline.", 100)
        } else {
            _isMonitoring.value = true
            viewModelScope.launch {
                analyzer.observeThreats().collect { alert ->
                    if (_isMonitoring.value) {
                        _currentAlert.value = alert
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigateToVault: () -> Unit) {
    val viewModel: DashboardViewModel = viewModel()
    val currentAlert by viewModel.currentAlert.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("TrustPulse", fontWeight = FontWeight.ExtraBold, color = PoliHeaderDeep, fontSize = 24.sp, letterSpacing = (-0.5).sp)
                        Text(
                            if (isMonitoring) "ACTIVE SHIELDING ON" else "SHIELDING OFF", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.SemiBold, 
                            color = if (isMonitoring) PoliSafe else Color.Gray,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .background(PoliAvatarBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", color = PoliAvatarText, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = PoliNavBg,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, PoliLogItemBorder)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { viewModel.toggleMonitoring() },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Shield") },
                    label = { Text("Shield", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PoliHeaderDeep,
                        selectedTextColor = PoliHeaderDeep,
                        indicatorColor = PoliAvatarBg
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToVault,
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Vault") },
                    label = { Text("Vault", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_vault_button"),
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Threat Level Card
            val (bgColor, borderColor, textColor) = when (currentAlert.level) {
                ThreatLevel.SAFE -> Triple(PoliSafeBg, PoliSafeBorder, StatusSafe)
                ThreatLevel.WARNING -> Triple(Color(0xFFFFF8E1), Color(0xFFFFECB3), StatusWarning)
                ThreatLevel.DANGER -> Triple(Color(0xFFFFEBEE), Color(0xFFFFCDD2), StatusDanger)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(32.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(32.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "CURRENT THREAT LEVEL", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = PoliHeaderDeep, 
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val percent = when (currentAlert.level) {
                        ThreatLevel.SAFE -> "0%"
                        ThreatLevel.WARNING -> "65%"
                        ThreatLevel.DANGER -> "92%"
                    }
                    Text(
                        percent, 
                        fontSize = 72.sp, 
                        fontWeight = FontWeight.Black, 
                        color = textColor,
                        lineHeight = 72.sp
                    )
                    
                    val statusText = when (currentAlert.level) {
                        ThreatLevel.SAFE -> "Verified Secure"
                        ThreatLevel.WARNING -> "Suspicious Context"
                        ThreatLevel.DANGER -> "Highly Probable Scam"
                    }
                    Text(
                        statusText, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Medium, 
                        color = PoliHeaderDeep
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, textColor.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isMonitoring) "ON-DEVICE ML ACTIVE" else "MONITORING OFFLINE", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = textColor
                        )
                    }
                }
            }

            // Grid Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Audio Filter Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(1.dp, PoliCardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF0F0F3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(Icons.Default.Check, contentDescription = "Audio Filter", tint = PoliText)
                        }
                        Column {
                            Text("Audio Filter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoliText)
                            Text("Analyzing VoIP stream for deepfake...", fontSize = 11.sp, color = PoliLogItemSubtitle, lineHeight = 14.sp)
                        }
                    }
                }

                // Safe Word Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(1.dp, PoliCardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                        .clickable(onClick = onNavigateToVault)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF0F0F3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(Icons.Default.Lock, contentDescription = "Safe Word", tint = PoliText)
                        }
                        Column {
                            Text("Safe Word", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoliText)
                            Text("Encrypted Family Vault AES-256.", fontSize = 11.sp, color = PoliLogItemSubtitle, lineHeight = 14.sp)
                        }
                    }
                }
            }

            // Real-Time Logs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(PoliLogBg, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Real-Time Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PoliText)
                        Box(
                            modifier = Modifier
                                .background(PoliSafe, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (isMonitoring) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(1.dp, PoliLogItemBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.padding(top = 4.dp).size(8.dp).background(PoliSafe, CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(currentAlert.source, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PoliText)
                                    Text(currentAlert.message, fontSize = 10.sp, color = PoliLogItemSubtitle, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

