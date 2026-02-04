// Copyright (c) 2024-2026 The INTcoin Core developers
// Distributed under the MIT software license

package org.intcoin.wallet.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.intcoin.sdk.*
import org.intcoin.wallet.ui.theme.*
import java.util.*

/**
 * INTcoin Mobile Wallet - Post-Quantum Secure
 * Android app with INTcoin dark theme branding
 */
class INTcoinWalletActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            INTcoinTheme {
                INTcoinWalletApp()
            }
        }
    }
}

@Composable
fun INTcoinWalletApp(
    viewModel: WalletViewModel = viewModel()
) {
    val navController = rememberNavController()
    val isWalletOpen by viewModel.isWalletOpen.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isWalletOpen) "main" else "setup"
    ) {
        composable("setup") {
            WalletSetupScreen(
                viewModel = viewModel,
                onWalletCreated = { navController.navigate("main") { popUpTo("setup") { inclusive = true } } }
            )
        }
        composable("main") {
            MainScreen(viewModel = viewModel)
        }
    }
}

// MARK: - Wallet Setup Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSetupScreen(
    viewModel: WalletViewModel,
    onWalletCreated: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isCreatingWallet by remember { mutableStateOf(true) }
    var showMnemonicDialog by remember { mutableStateOf(false) }
    var mnemonic by remember { mutableStateOf("") }
    val errorMessage by viewModel.errorMessage.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = INTcoinColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(INTcoinDesign.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            INTcoinLogo(size = 100.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "INTcoin Wallet",
                style = MaterialTheme.typography.headlineLarge,
                color = INTcoinColors.TextPrimary
            )

            Text(
                text = "Post-Quantum Secure",
                style = MaterialTheme.typography.bodyMedium,
                color = INTcoinColors.Accent
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Tab selector
            TabRow(
                selectedTabIndex = if (isCreatingWallet) 0 else 1,
                containerColor = INTcoinColors.BackgroundSecondary,
                contentColor = INTcoinColors.Primary,
                modifier = Modifier.clip(RoundedCornerShape(INTcoinDesign.CornerRadiusSmall))
            ) {
                Tab(
                    selected = isCreatingWallet,
                    onClick = { isCreatingWallet = true },
                    text = { Text("Create Wallet") }
                )
                Tab(
                    selected = !isCreatingWallet,
                    onClick = { isCreatingWallet = false },
                    text = { Text("Open Wallet") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = INTcoinColors.Primary,
                    unfocusedBorderColor = INTcoinColors.Border,
                    focusedLabelColor = INTcoinColors.Primary,
                    unfocusedLabelColor = INTcoinColors.TextSecondary
                )
            )

            if (isCreatingWallet) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = INTcoinColors.Primary,
                        unfocusedBorderColor = INTcoinColors.Border,
                        focusedLabelColor = INTcoinColors.Primary,
                        unfocusedLabelColor = INTcoinColors.TextSecondary,
                        errorBorderColor = INTcoinColors.Error
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action button
            Button(
                onClick = {
                    if (isCreatingWallet) {
                        viewModel.createWallet(password)?.let {
                            mnemonic = it
                            showMnemonicDialog = true
                        }
                    } else {
                        if (viewModel.openWallet(password)) {
                            onWalletCreated()
                        }
                    }
                },
                enabled = password.isNotEmpty() && (!isCreatingWallet || password == confirmPassword),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(INTcoinDesign.ButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = INTcoinColors.Primary,
                    contentColor = INTcoinColors.OnPrimary,
                    disabledContainerColor = INTcoinColors.BackgroundTertiary,
                    disabledContentColor = INTcoinColors.TextTertiary
                )
            ) {
                Text(
                    text = if (isCreatingWallet) "Create Wallet" else "Open Wallet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Error message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = INTcoinColors.Error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = INTcoinColors.Error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = INTcoinColors.Error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Security info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = INTcoinColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dilithium5 Signatures",
                    style = MaterialTheme.typography.bodySmall,
                    color = INTcoinColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = INTcoinColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NIST Level 5 Security",
                    style = MaterialTheme.typography.bodySmall,
                    color = INTcoinColors.TextSecondary
                )
            }
        }
    }

    // Mnemonic backup dialog
    if (showMnemonicDialog) {
        MnemonicBackupDialog(
            mnemonic = mnemonic,
            onDismiss = {
                showMnemonicDialog = false
                onWalletCreated()
            }
        )
    }
}

@Composable
fun MnemonicBackupDialog(
    mnemonic: String,
    onDismiss: () -> Unit
) {
    var confirmed by remember { mutableStateOf(false) }
    val words = mnemonic.split(" ")

    AlertDialog(
        onDismissRequest = { },
        containerColor = INTcoinColors.BackgroundSecondary,
        title = {
            Text(
                "Backup Recovery Phrase",
                color = INTcoinColors.TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    "Write down these 24 words in order. They are the only way to recover your wallet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = INTcoinColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(words) { index, word ->
                        Row(
                            modifier = Modifier
                                .background(
                                    INTcoinColors.BackgroundTertiary,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}.",
                                style = MaterialTheme.typography.labelSmall,
                                color = INTcoinColors.TextTertiary,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                word,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = INTcoinColors.TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = INTcoinColors.Warning.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = INTcoinColors.Warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Never share your recovery phrase. Anyone with these words can steal your funds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = INTcoinColors.Warning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { confirmed = !confirmed }
                ) {
                    Checkbox(
                        checked = confirmed,
                        onCheckedChange = { confirmed = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = INTcoinColors.Primary,
                            uncheckedColor = INTcoinColors.Border
                        )
                    )
                    Text(
                        "I have written down my recovery phrase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = INTcoinColors.TextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                enabled = confirmed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = INTcoinColors.Primary,
                    contentColor = INTcoinColors.OnPrimary
                )
            ) {
                Text("Continue")
            }
        }
    )
}

// MARK: - Main Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WalletViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = INTcoinColors.Background,
        bottomBar = {
            NavigationBar(
                containerColor = INTcoinColors.BackgroundSecondary,
                contentColor = INTcoinColors.TextPrimary
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text("Wallet") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = INTcoinColors.Primary,
                        selectedTextColor = INTcoinColors.Primary,
                        unselectedIconColor = INTcoinColors.TextSecondary,
                        unselectedTextColor = INTcoinColors.TextSecondary,
                        indicatorColor = INTcoinColors.Primary.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = "Activity") },
                    label = { Text("Activity") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = INTcoinColors.Primary,
                        selectedTextColor = INTcoinColors.Primary,
                        unselectedIconColor = INTcoinColors.TextSecondary,
                        unselectedTextColor = INTcoinColors.TextSecondary,
                        indicatorColor = INTcoinColors.Primary.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = INTcoinColors.Primary,
                        selectedTextColor = INTcoinColors.Primary,
                        unselectedIconColor = INTcoinColors.TextSecondary,
                        unselectedTextColor = INTcoinColors.TextSecondary,
                        indicatorColor = INTcoinColors.Primary.copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> WalletScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
            1 -> TransactionsScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
            2 -> SettingsScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
        }
    }
}

// MARK: - Wallet Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.balance.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var showReceiveSheet by remember { mutableStateOf(false) }
    var showSendSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    INTcoinLogo(size = 32.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("INTcoin", style = MaterialTheme.typography.titleLarge)
                }
            },
            actions = {
                IconButton(onClick = {
                    if (isSyncing) viewModel.stopSync() else viewModel.startSync()
                }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = if (isSyncing) "Stop Sync" else "Sync",
                        tint = INTcoinColors.Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = INTcoinColors.Background,
                titleContentColor = INTcoinColors.TextPrimary
            )
        )

        // Balance Card
        BalanceCard(balance = balance)

        Spacer(modifier = Modifier.height(16.dp))

        // Sync Status
        if (isSyncing) {
            SyncStatusCard(progress = syncProgress)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = INTcoinDesign.PaddingMedium),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionButton(
                title = "Receive",
                icon = Icons.Default.ArrowDownward,
                color = INTcoinColors.Success,
                modifier = Modifier.weight(1f),
                onClick = { showReceiveSheet = true }
            )
            ActionButton(
                title = "Send",
                icon = Icons.Default.ArrowUpward,
                color = INTcoinColors.Primary,
                modifier = Modifier.weight(1f),
                onClick = { showSendSheet = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats
        QuickStatsRow(viewModel = viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions
        RecentTransactionsSection(transactions = transactions)
    }

    // Receive Bottom Sheet
    if (showReceiveSheet) {
        ReceiveSheet(
            viewModel = viewModel,
            onDismiss = { showReceiveSheet = false }
        )
    }

    // Send Bottom Sheet
    if (showSendSheet) {
        SendSheet(
            viewModel = viewModel,
            onDismiss = { showSendSheet = false }
        )
    }
}

@Composable
fun BalanceCard(balance: Balance?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = INTcoinDesign.PaddingMedium),
        colors = CardDefaults.cardColors(
            containerColor = INTcoinColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(INTcoinDesign.CornerRadiusLarge),
        border = BorderStroke(1.dp, INTcoinColors.Border)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            INTcoinColors.BackgroundSecondary,
                            INTcoinColors.Primary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(INTcoinDesign.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = INTcoinColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (balance != null) {
                    Text(
                        text = balance.totalFormatted,
                        style = MaterialTheme.typography.displaySmall,
                        color = INTcoinColors.Success,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = INTcoinColors.Success,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Confirmed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = INTcoinColors.TextSecondary
                                )
                            }
                            Text(
                                balance.confirmedFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = INTcoinColors.TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = INTcoinColors.Warning,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = INTcoinColors.TextSecondary
                                )
                            }
                            Text(
                                balance.unconfirmedFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = INTcoinColors.Warning
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator(
                        color = INTcoinColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusCard(progress: Double) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = INTcoinDesign.PaddingMedium),
        colors = CardDefaults.cardColors(
            containerColor = INTcoinColors.BackgroundSecondary
        ),
        border = BorderStroke(1.dp, INTcoinColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(INTcoinDesign.PaddingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = INTcoinColors.Primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Syncing blockchain...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = INTcoinColors.TextSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = INTcoinColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = INTcoinColors.Primary,
                trackColor = INTcoinColors.BackgroundTertiary
            )
        }
    }
}

@Composable
fun ActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(INTcoinDesign.ButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(INTcoinDesign.CornerRadiusMedium)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun QuickStatsRow(viewModel: WalletViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    val receivedCount = transactions.count { it.type == TransactionEvent.Type.RECEIVED }
    val sentCount = transactions.count { it.type == TransactionEvent.Type.SENT }
    val pendingCount = transactions.count { it.confirmations < 6 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = INTcoinDesign.PaddingMedium),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Default.ArrowDownward,
            title = "Received",
            value = receivedCount.toString(),
            color = INTcoinColors.Success,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.ArrowUpward,
            title = "Sent",
            value = sentCount.toString(),
            color = INTcoinColors.Primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Schedule,
            title = "Pending",
            value = pendingCount.toString(),
            color = INTcoinColors.Warning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = INTcoinColors.BackgroundSecondary
        ),
        border = BorderStroke(1.dp, INTcoinColors.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(INTcoinDesign.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = INTcoinColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = INTcoinColors.TextSecondary
            )
        }
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<TransactionEvent>) {
    Column(
        modifier = Modifier.padding(horizontal = INTcoinDesign.PaddingMedium)
    ) {
        Text(
            "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            color = INTcoinColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = INTcoinColors.BackgroundSecondary
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = INTcoinColors.TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No transactions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = INTcoinColors.TextSecondary
                    )
                    Text(
                        "Send or receive INT to get started",
                        style = MaterialTheme.typography.bodySmall,
                        color = INTcoinColors.TextTertiary
                    )
                }
            }
        } else {
            transactions.take(5).forEach { tx ->
                TransactionRow(transaction = tx)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: TransactionEvent) {
    val isReceived = transaction.type == TransactionEvent.Type.RECEIVED

    Card(
        colors = CardDefaults.cardColors(
            containerColor = INTcoinColors.BackgroundSecondary
        ),
        border = BorderStroke(1.dp, INTcoinColors.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(INTcoinDesign.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isReceived) INTcoinColors.Success.copy(alpha = 0.2f)
                        else INTcoinColors.Primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isReceived) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isReceived) INTcoinColors.Success else INTcoinColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isReceived) "Received" else "Sent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = INTcoinColors.TextPrimary
                )
                Text(
                    transaction.address.take(16) + "...",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = INTcoinColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (isReceived) "+" else "-") + transaction.amountFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isReceived) INTcoinColors.Success else INTcoinColors.TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when {
                            transaction.confirmations >= 6 -> Icons.Default.CheckCircle
                            transaction.confirmations > 0 -> Icons.Default.Schedule
                            else -> Icons.Default.HourglassEmpty
                        },
                        contentDescription = null,
                        tint = when {
                            transaction.confirmations >= 6 -> INTcoinColors.Success
                            transaction.confirmations > 0 -> INTcoinColors.Warning
                            else -> INTcoinColors.TextTertiary
                        },
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${transaction.confirmations} conf",
                        style = MaterialTheme.typography.labelSmall,
                        color = INTcoinColors.TextSecondary
                    )
                }
            }
        }
    }
}

// MARK: - Transactions Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Activity") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = INTcoinColors.Background,
                titleContentColor = INTcoinColors.TextPrimary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(INTcoinDesign.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions) { tx ->
                TransactionRow(transaction = tx)
            }
        }
    }
}

// MARK: - Settings Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = INTcoinColors.Background,
                titleContentColor = INTcoinColors.TextPrimary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(INTcoinDesign.PaddingMedium)
        ) {
            // Wallet Section
            item {
                SettingsSectionHeader("Wallet")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Key,
                    iconColor = INTcoinColors.Warning,
                    title = "Backup Recovery Phrase",
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconColor = INTcoinColors.Primary,
                    title = "Change Password",
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    iconColor = INTcoinColors.Primary,
                    title = "Rescan Blockchain",
                    onClick = { viewModel.startSync() }
                )
            }

            // Network Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionHeader("Network")
            }

            item {
                SettingsInfoItem(
                    icon = Icons.Default.Cloud,
                    iconColor = INTcoinColors.Primary,
                    title = "Network",
                    value = "Mainnet"
                )
            }

            item {
                SettingsInfoItem(
                    icon = Icons.Default.Dns,
                    iconColor = INTcoinColors.Success,
                    title = "Node Status",
                    value = "Connected",
                    valueColor = INTcoinColors.Success
                )
            }

            // Security Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionHeader("Security")
            }

            item {
                SettingsInfoItem(
                    icon = Icons.Outlined.Shield,
                    iconColor = INTcoinColors.Success,
                    title = "Signature Algorithm",
                    value = "Dilithium5"
                )
            }

            item {
                SettingsInfoItem(
                    icon = Icons.Default.Lock,
                    iconColor = INTcoinColors.Success,
                    title = "Security Level",
                    value = "NIST Level 5"
                )
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSectionHeader("About")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconColor = INTcoinColors.Primary,
                    title = "About INTcoin",
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                SettingsInfoItem(
                    icon = Icons.Default.Apps,
                    iconColor = INTcoinColors.TextSecondary,
                    title = "Version",
                    value = "1.2.0"
                )
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = INTcoinColors.TextSecondary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = INTcoinColors.BackgroundSecondary
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(INTcoinDesign.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = INTcoinColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = INTcoinColors.TextTertiary
            )
        }
    }
}

@Composable
fun SettingsInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    valueColor: Color = INTcoinColors.TextSecondary
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = INTcoinColors.BackgroundSecondary
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(INTcoinDesign.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = INTcoinColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor
            )
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = INTcoinColors.BackgroundSecondary,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                INTcoinLogo(size = 60.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("INTcoin", color = INTcoinColors.TextPrimary)
                Text(
                    "Post-Quantum Cryptocurrency",
                    style = MaterialTheme.typography.bodySmall,
                    color = INTcoinColors.Accent
                )
            }
        },
        text = {
            Column {
                AboutInfoRow("Signature Algorithm", "Dilithium5")
                AboutInfoRow("Hash Algorithm", "SHA3-256")
                AboutInfoRow("Mining Algorithm", "RandomX")
                AboutInfoRow("Security Level", "NIST Level 5")
                AboutInfoRow("Block Time", "60 seconds")
                AboutInfoRow("Max Supply", "21,000,000 INT")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Copyright 2025-2026 INTcoin Team\nMIT License",
                    style = MaterialTheme.typography.bodySmall,
                    color = INTcoinColors.TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = INTcoinColors.Primary)
            }
        }
    )
}

@Composable
fun AboutInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = INTcoinColors.TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = INTcoinColors.TextPrimary
        )
    }
}

// MARK: - Receive Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveSheet(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val currentAddress by viewModel.currentAddress.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = INTcoinColors.BackgroundSecondary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(INTcoinDesign.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Receive INT",
                style = MaterialTheme.typography.titleLarge,
                color = INTcoinColors.TextPrimary
            )

            Text(
                "Scan or share your address",
                style = MaterialTheme.typography.bodyMedium,
                color = INTcoinColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code
            currentAddress?.let { address ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(220.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        QRCodeImage(content = address, size = 188)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Address
                Text(
                    address,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = INTcoinColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            INTcoinColors.BackgroundTertiary,
                            RoundedCornerShape(INTcoinDesign.CornerRadiusSmall)
                        )
                        .padding(INTcoinDesign.PaddingMedium)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Copy Button
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(address))
                        copied = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = INTcoinColors.Primary
                    ),
                    border = BorderStroke(2.dp, INTcoinColors.Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (copied) "Copied!" else "Copy Address")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // New Address Button
                TextButton(
                    onClick = { viewModel.generateNewAddress() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = INTcoinColors.Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate New Address", color = INTcoinColors.Primary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }
}

@Composable
fun QRCodeImage(content: String, size: Int) {
    val bitmap = remember(content) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(size.dp)
        )
    }
}

// MARK: - Send Sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSheet(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    val isValidInput = INTcoinSDK.validateAddress(recipientAddress) && amount.isNotEmpty()

    if (showConfirmation) {
        SendConfirmationSheet(
            address = recipientAddress,
            amount = amount,
            onConfirm = {
                viewModel.sendTransaction(recipientAddress, amount)
                onDismiss()
            },
            onDismiss = { showConfirmation = false }
        )
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = INTcoinColors.BackgroundSecondary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(INTcoinDesign.PaddingLarge)
            ) {
                Text(
                    "Send INT",
                    style = MaterialTheme.typography.titleLarge,
                    color = INTcoinColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Recipient
                Text(
                    "Recipient",
                    style = MaterialTheme.typography.titleSmall,
                    color = INTcoinColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recipientAddress,
                    onValueChange = { recipientAddress = it },
                    placeholder = { Text("INT address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = INTcoinColors.Primary,
                        unfocusedBorderColor = INTcoinColors.Border
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount
                Text(
                    "Amount",
                    style = MaterialTheme.typography.titleSmall,
                    color = INTcoinColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("0.000000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Text("INT", color = INTcoinColors.TextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = INTcoinColors.Primary,
                        unfocusedBorderColor = INTcoinColors.Border
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Available: ${balance?.totalFormatted ?: "0.000000 INT"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = INTcoinColors.Success
                    )
                    TextButton(
                        onClick = {
                            balance?.let {
                                amount = String.format(Locale.US, "%.6f", it.total / 1_000_000.0)
                            }
                        }
                    ) {
                        Text("MAX", color = INTcoinColors.Primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Send Button
                Button(
                    onClick = { showConfirmation = true },
                    enabled = isValidInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(INTcoinDesign.ButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = INTcoinColors.Primary,
                        contentColor = INTcoinColors.OnPrimary,
                        disabledContainerColor = INTcoinColors.BackgroundTertiary,
                        disabledContentColor = INTcoinColors.TextTertiary
                    )
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Review Transaction", fontWeight = FontWeight.SemiBold)
                }

                // Security note
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = INTcoinColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Protected by Dilithium5 post-quantum signatures",
                        style = MaterialTheme.typography.bodySmall,
                        color = INTcoinColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendConfirmationSheet(
    address: String,
    amount: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = INTcoinColors.BackgroundSecondary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(INTcoinDesign.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Confirm Transaction",
                style = MaterialTheme.typography.titleLarge,
                color = INTcoinColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = INTcoinColors.BackgroundTertiary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(INTcoinDesign.PaddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = INTcoinColors.TextSecondary
                    )
                    Text(
                        "$amount INT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = INTcoinColors.TextPrimary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = INTcoinColors.Border
                    )

                    Text(
                        "To",
                        style = MaterialTheme.typography.labelMedium,
                        color = INTcoinColors.TextSecondary
                    )
                    Text(
                        address,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = INTcoinColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = INTcoinColors.Border
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Network Fee",
                            style = MaterialTheme.typography.bodyMedium,
                            color = INTcoinColors.TextSecondary
                        )
                        Text(
                            "~0.000100 INT",
                            style = MaterialTheme.typography.bodyMedium,
                            color = INTcoinColors.TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(INTcoinDesign.ButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = INTcoinColors.Primary,
                    contentColor = INTcoinColors.OnPrimary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm & Send", fontWeight = FontWeight.SemiBold)
            }

            TextButton(onClick = onDismiss) {
                Text("Cancel", color = INTcoinColors.TextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// MARK: - INTcoin Logo

@Composable
fun INTcoinLogo(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(size)
                .border(
                    width = size * 0.08f,
                    brush = Brush.linearGradient(
                        colors = listOf(INTcoinColors.Primary, INTcoinColors.Accent)
                    ),
                    shape = CircleShape
                )
        )

        // Inner background
        Box(
            modifier = Modifier
                .size(size * 0.85f)
                .background(INTcoinColors.BackgroundSecondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "INT",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (size.value * 0.28f).sp
                ),
                fontWeight = FontWeight.Bold,
                color = INTcoinColors.Primary
            )
        }
    }
}

// MARK: - ViewModel

class WalletViewModel : ViewModel() {
    private val _isWalletOpen = MutableStateFlow(false)
    val isWalletOpen: StateFlow<Boolean> = _isWalletOpen.asStateFlow()

    private val _balance = MutableStateFlow<Balance?>(null)
    val balance: StateFlow<Balance?> = _balance.asStateFlow()

    private val _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress: StateFlow<String?> = _currentAddress.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEvent>>(emptyList())
    val transactions: StateFlow<List<TransactionEvent>> = _transactions.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0.0)
    val syncProgress: StateFlow<Double> = _syncProgress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun createWallet(password: String): String? {
        // Mock implementation - would use SDK in production
        return "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
    }

    fun openWallet(password: String): Boolean {
        // Mock implementation
        _isWalletOpen.value = true
        loadWalletData()
        return true
    }

    private fun loadWalletData() {
        _balance.value = Balance(
            confirmed = 15_000_000,
            unconfirmed = 500_000
        )
        _currentAddress.value = "int1q8h4k9j2m5n7p3r6s0t2v4w6x8y0z1a2b3c4d5e6f7g8"
        loadMockTransactions()
    }

    fun generateNewAddress() {
        _currentAddress.value = "int1m9n0p1q2r3s4t5u6v7w8x9y0z1a2b3c4d5e6f7g8h9"
    }

    fun startSync() {
        _isSyncing.value = true
        // Mock sync progress
    }

    fun stopSync() {
        _isSyncing.value = false
    }

    fun sendTransaction(toAddress: String, amount: String) {
        // Mock implementation
    }

    private fun loadMockTransactions() {
        _transactions.value = listOf(
            TransactionEvent(
                type = TransactionEvent.Type.RECEIVED,
                txHash = ByteArray(32),
                address = "int1q8h4k9j2m5n7p3r6s0t2v4w6x8y0z1a2b3c4d5e",
                amountINTS = 1_500_000,
                confirmations = 156,
                timestamp = System.currentTimeMillis() - 3600000
            ),
            TransactionEvent(
                type = TransactionEvent.Type.SENT,
                txHash = ByteArray(32),
                address = "int1m9n0p1q2r3s4t5u6v7w8x9y0z1a2b3c4d5e6f7",
                amountINTS = 250_000,
                confirmations = 89,
                timestamp = System.currentTimeMillis() - 7200000
            ),
            TransactionEvent(
                type = TransactionEvent.Type.RECEIVED,
                txHash = ByteArray(32),
                address = "int1a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9",
                amountINTS = 5_000_000,
                confirmations = 3,
                timestamp = System.currentTimeMillis() - 1800000
            )
        )
    }
}
