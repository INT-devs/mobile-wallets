// Copyright (c) 2024-2026 The INTcoin Core developers
// Distributed under the MIT software license

import SwiftUI
import CoreImage.CIFilterBuiltins

/// INTcoin Mobile Wallet - Post-Quantum Secure
/// iOS app with INTcoin dark theme branding
@main
struct INTcoinWalletApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(.dark)
        }
    }
}

struct ContentView: View {
    @StateObject private var walletViewModel = WalletViewModel()

    var body: some View {
        NavigationView {
            if walletViewModel.isWalletOpen {
                MainTabView(viewModel: walletViewModel)
            } else {
                WalletSetupView(viewModel: walletViewModel)
            }
        }
        .accentColor(INTcoinColors.primary)
    }
}

// MARK: - Main Tab View

struct MainTabView: View {
    @ObservedObject var viewModel: WalletViewModel
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            WalletView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "wallet.pass.fill")
                    Text("Wallet")
                }
                .tag(0)

            TransactionsListView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "arrow.left.arrow.right")
                    Text("Activity")
                }
                .tag(1)

            SettingsView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "gearshape.fill")
                    Text("Settings")
                }
                .tag(2)
        }
        .background(INTcoinColors.background)
    }
}

// MARK: - Wallet Setup View

struct WalletSetupView: View {
    @ObservedObject var viewModel: WalletViewModel
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isCreatingWallet = true
    @State private var showMnemonic = false
    @State private var mnemonic: String = ""

    var body: some View {
        ZStack {
            INTcoinColors.background.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 32) {
                    Spacer().frame(height: 40)

                    // Logo
                    INTcoinLogo(size: 100)

                    Text("INTcoin Wallet")
                        .font(INTcoinTypography.largeTitle)
                        .foregroundColor(INTcoinColors.textPrimary)

                    Text("Post-Quantum Secure")
                        .font(INTcoinTypography.subheadline)
                        .foregroundColor(INTcoinColors.accent)

                    // Action Picker
                    Picker("Action", selection: $isCreatingWallet) {
                        Text("Create Wallet").tag(true)
                        Text("Open Wallet").tag(false)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, INTcoinDesign.paddingLarge)

                    // Password Fields
                    VStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Password")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)

                            SecureField("Enter password", text: $password)
                                .intcoinTextField()
                        }

                        if isCreatingWallet {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Confirm Password")
                                    .font(INTcoinTypography.caption)
                                    .foregroundColor(INTcoinColors.textSecondary)

                                SecureField("Confirm password", text: $confirmPassword)
                                    .intcoinTextField()
                            }
                        }
                    }
                    .padding(.horizontal, INTcoinDesign.paddingLarge)

                    // Action Button
                    Button(action: {
                        if isCreatingWallet {
                            if let newMnemonic = viewModel.createWallet(password: password) {
                                mnemonic = newMnemonic
                                showMnemonic = true
                            }
                        } else {
                            viewModel.openWallet(password: password)
                        }
                    }) {
                        Text(isCreatingWallet ? "Create Wallet" : "Open Wallet")
                    }
                    .buttonStyle(INTcoinPrimaryButtonStyle(
                        isEnabled: !password.isEmpty && (!isCreatingWallet || password == confirmPassword)
                    ))
                    .disabled(password.isEmpty || (isCreatingWallet && password != confirmPassword))
                    .padding(.horizontal, INTcoinDesign.paddingLarge)

                    // Error Message
                    if let error = viewModel.errorMessage {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(INTcoinColors.error)
                            Text(error)
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.error)
                        }
                        .padding()
                        .background(INTcoinColors.error.opacity(0.1))
                        .cornerRadius(INTcoinDesign.cornerRadiusSmall)
                        .padding(.horizontal, INTcoinDesign.paddingLarge)
                    }

                    // Security Info
                    VStack(spacing: 8) {
                        HStack(spacing: 8) {
                            Image(systemName: "shield.checkered")
                                .foregroundColor(INTcoinColors.success)
                            Text("Dilithium5 Signatures")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)
                        }

                        HStack(spacing: 8) {
                            Image(systemName: "lock.shield.fill")
                                .foregroundColor(INTcoinColors.success)
                            Text("NIST Level 5 Security")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)
                        }
                    }
                    .padding(.top, 20)

                    Spacer()
                }
            }
        }
        .sheet(isPresented: $showMnemonic) {
            MnemonicBackupView(mnemonic: mnemonic) {
                showMnemonic = false
                viewModel.isWalletOpen = true
            }
        }
    }
}

// MARK: - Mnemonic Backup View

struct MnemonicBackupView: View {
    let mnemonic: String
    let onComplete: () -> Void
    @State private var confirmed = false

    var mnemonicWords: [String] {
        mnemonic.split(separator: " ").map(String.init)
    }

    var body: some View {
        ZStack {
            INTcoinColors.background.ignoresSafeArea()

            VStack(spacing: 24) {
                Text("Backup Recovery Phrase")
                    .font(INTcoinTypography.title2)
                    .foregroundColor(INTcoinColors.textPrimary)

                Text("Write down these 24 words in order. They are the only way to recover your wallet.")
                    .font(INTcoinTypography.subheadline)
                    .foregroundColor(INTcoinColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                // Mnemonic Grid
                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: 12) {
                    ForEach(Array(mnemonicWords.enumerated()), id: \.offset) { index, word in
                        HStack {
                            Text("\(index + 1).")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textTertiary)
                                .frame(width: 24, alignment: .trailing)
                            Text(word)
                                .font(INTcoinTypography.monospace)
                                .foregroundColor(INTcoinColors.textPrimary)
                            Spacer()
                        }
                        .padding(8)
                        .background(INTcoinColors.backgroundSecondary)
                        .cornerRadius(6)
                    }
                }
                .padding()
                .background(INTcoinColors.backgroundTertiary)
                .cornerRadius(INTcoinDesign.cornerRadiusMedium)
                .padding(.horizontal)

                // Warning
                HStack {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundColor(INTcoinColors.warning)
                    Text("Never share your recovery phrase. Anyone with these words can steal your funds.")
                        .font(INTcoinTypography.caption)
                        .foregroundColor(INTcoinColors.warning)
                }
                .padding()
                .background(INTcoinColors.warning.opacity(0.1))
                .cornerRadius(INTcoinDesign.cornerRadiusSmall)
                .padding(.horizontal)

                // Confirmation Toggle
                Toggle(isOn: $confirmed) {
                    Text("I have written down my recovery phrase")
                        .font(INTcoinTypography.subheadline)
                        .foregroundColor(INTcoinColors.textPrimary)
                }
                .toggleStyle(SwitchToggleStyle(tint: INTcoinColors.primary))
                .padding(.horizontal)

                Button(action: onComplete) {
                    Text("Continue")
                }
                .buttonStyle(INTcoinPrimaryButtonStyle(isEnabled: confirmed))
                .disabled(!confirmed)
                .padding(.horizontal)

                Spacer()
            }
            .padding(.top, 40)
        }
    }
}

// MARK: - Main Wallet View

struct WalletView: View {
    @ObservedObject var viewModel: WalletViewModel
    @State private var showingReceive = false
    @State private var showingSend = false

    var body: some View {
        ZStack {
            INTcoinColors.background.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    // Balance Card
                    BalanceCard(balance: viewModel.balance)

                    // Sync Status
                    if viewModel.isSyncing {
                        SyncStatusView(progress: viewModel.syncProgress)
                    }

                    // Action Buttons
                    HStack(spacing: 16) {
                        ActionButton(
                            title: "Receive",
                            icon: "arrow.down.circle.fill",
                            color: INTcoinColors.success
                        ) {
                            showingReceive = true
                        }

                        ActionButton(
                            title: "Send",
                            icon: "arrow.up.circle.fill",
                            color: INTcoinColors.primary
                        ) {
                            showingSend = true
                        }
                    }
                    .padding(.horizontal)

                    // Quick Stats
                    QuickStatsView(viewModel: viewModel)

                    // Recent Transactions
                    RecentTransactionsView(viewModel: viewModel)
                }
                .padding(.vertical)
            }
        }
        .navigationTitle("INTcoin")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.isSyncing ? viewModel.stopSync() : viewModel.startSync()
                }) {
                    Image(systemName: viewModel.isSyncing ? "stop.circle.fill" : "arrow.triangle.2.circlepath")
                        .foregroundColor(INTcoinColors.primary)
                }
            }
        }
        .sheet(isPresented: $showingReceive) {
            ReceiveView(viewModel: viewModel)
        }
        .sheet(isPresented: $showingSend) {
            SendView(viewModel: viewModel)
        }
    }
}

// MARK: - Balance Card

struct BalanceCard: View {
    let balance: Balance?

    var body: some View {
        VStack(spacing: 16) {
            Text("Total Balance")
                .font(INTcoinTypography.caption)
                .foregroundColor(INTcoinColors.textSecondary)

            if let balance = balance {
                Text(balance.totalFormatted)
                    .font(INTcoinTypography.balance)
                    .foregroundColor(INTcoinColors.success)

                HStack(spacing: 32) {
                    VStack(spacing: 4) {
                        HStack(spacing: 4) {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 12))
                                .foregroundColor(INTcoinColors.success)
                            Text("Confirmed")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)
                        }
                        Text(balance.confirmedFormatted)
                            .font(INTcoinTypography.subheadline)
                            .foregroundColor(INTcoinColors.textPrimary)
                    }

                    VStack(spacing: 4) {
                        HStack(spacing: 4) {
                            Image(systemName: "clock.fill")
                                .font(.system(size: 12))
                                .foregroundColor(INTcoinColors.warning)
                            Text("Pending")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)
                        }
                        Text(balance.unconfirmedFormatted)
                            .font(INTcoinTypography.subheadline)
                            .foregroundColor(INTcoinColors.warning)
                    }
                }
            } else {
                ProgressView()
                    .tint(INTcoinColors.primary)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(INTcoinDesign.paddingLarge)
        .background(
            LinearGradient(
                colors: [
                    INTcoinColors.backgroundSecondary,
                    INTcoinColors.primary.opacity(0.1)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(INTcoinDesign.cornerRadiusLarge)
        .overlay(
            RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusLarge)
                .stroke(INTcoinColors.border, lineWidth: 1)
        )
        .padding(.horizontal)
    }
}

// MARK: - Sync Status View

struct SyncStatusView: View {
    let progress: Double

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Image(systemName: "arrow.triangle.2.circlepath")
                    .foregroundColor(INTcoinColors.primary)
                    .rotationEffect(.degrees(progress * 360))
                    .animation(.linear(duration: 1).repeatForever(autoreverses: false), value: progress)

                Text("Syncing blockchain...")
                    .font(INTcoinTypography.subheadline)
                    .foregroundColor(INTcoinColors.textSecondary)

                Spacer()

                Text("\(Int(progress * 100))%")
                    .font(INTcoinTypography.subheadline)
                    .foregroundColor(INTcoinColors.primary)
            }

            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Rectangle()
                        .fill(INTcoinColors.backgroundTertiary)
                        .frame(height: 6)
                        .cornerRadius(3)

                    Rectangle()
                        .fill(INTcoinColors.gradientPrimary)
                        .frame(width: geometry.size.width * progress, height: 6)
                        .cornerRadius(3)
                }
            }
            .frame(height: 6)
        }
        .padding()
        .background(INTcoinColors.backgroundSecondary)
        .cornerRadius(INTcoinDesign.cornerRadiusMedium)
        .overlay(
            RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusMedium)
                .stroke(INTcoinColors.border, lineWidth: 1)
        )
        .padding(.horizontal)
    }
}

// MARK: - Action Button

struct ActionButton: View {
    let title: String
    let icon: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: INTcoinDesign.iconSizeLarge))
                Text(title)
                    .font(INTcoinTypography.subheadline)
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, INTcoinDesign.paddingMedium)
            .background(color)
            .cornerRadius(INTcoinDesign.cornerRadiusMedium)
        }
    }
}

// MARK: - Quick Stats View

struct QuickStatsView: View {
    @ObservedObject var viewModel: WalletViewModel

    var body: some View {
        HStack(spacing: 12) {
            StatCard(
                icon: "arrow.down",
                title: "Received",
                value: "\(viewModel.receivedCount)",
                color: INTcoinColors.success
            )

            StatCard(
                icon: "arrow.up",
                title: "Sent",
                value: "\(viewModel.sentCount)",
                color: INTcoinColors.primary
            )

            StatCard(
                icon: "clock",
                title: "Pending",
                value: "\(viewModel.pendingCount)",
                color: INTcoinColors.warning
            )
        }
        .padding(.horizontal)
    }
}

struct StatCard: View {
    let icon: String
    let title: String
    let value: String
    let color: Color

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)

            Text(value)
                .font(INTcoinTypography.title2)
                .foregroundColor(INTcoinColors.textPrimary)

            Text(title)
                .font(INTcoinTypography.caption)
                .foregroundColor(INTcoinColors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, INTcoinDesign.paddingMedium)
        .background(INTcoinColors.backgroundSecondary)
        .cornerRadius(INTcoinDesign.cornerRadiusMedium)
        .overlay(
            RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusMedium)
                .stroke(INTcoinColors.border, lineWidth: 1)
        )
    }
}

// MARK: - Recent Transactions View

struct RecentTransactionsView: View {
    @ObservedObject var viewModel: WalletViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("Recent Activity")
                    .font(INTcoinTypography.headline)
                    .foregroundColor(INTcoinColors.textPrimary)

                Spacer()

                if !viewModel.transactions.isEmpty {
                    NavigationLink(destination: TransactionsListView(viewModel: viewModel)) {
                        Text("See All")
                            .font(INTcoinTypography.subheadline)
                            .foregroundColor(INTcoinColors.primary)
                    }
                }
            }
            .padding(.horizontal)

            if viewModel.transactions.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "clock.arrow.circlepath")
                        .font(.system(size: 48))
                        .foregroundColor(INTcoinColors.textTertiary)

                    Text("No transactions yet")
                        .font(INTcoinTypography.subheadline)
                        .foregroundColor(INTcoinColors.textSecondary)

                    Text("Send or receive INT to get started")
                        .font(INTcoinTypography.caption)
                        .foregroundColor(INTcoinColors.textTertiary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 40)
            } else {
                VStack(spacing: 8) {
                    ForEach(viewModel.transactions.prefix(5)) { tx in
                        TransactionRow(transaction: tx)
                    }
                }
            }
        }
    }
}

// MARK: - Transaction Row

struct TransactionRow: View {
    let transaction: TransactionItem

    var body: some View {
        HStack(spacing: 12) {
            // Icon
            ZStack {
                Circle()
                    .fill(transaction.isReceived ?
                          INTcoinColors.success.opacity(0.2) :
                          INTcoinColors.primary.opacity(0.2))
                    .frame(width: 44, height: 44)

                Image(systemName: transaction.isReceived ? "arrow.down" : "arrow.up")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(transaction.isReceived ? INTcoinColors.success : INTcoinColors.primary)
            }

            // Details
            VStack(alignment: .leading, spacing: 4) {
                Text(transaction.isReceived ? "Received" : "Sent")
                    .font(INTcoinTypography.subheadline)
                    .foregroundColor(INTcoinColors.textPrimary)

                Text(String(transaction.address.prefix(16)) + "...")
                    .font(INTcoinTypography.monospace)
                    .foregroundColor(INTcoinColors.textSecondary)
                    .lineLimit(1)
            }

            Spacer()

            // Amount & Status
            VStack(alignment: .trailing, spacing: 4) {
                Text(transaction.amountFormatted)
                    .font(INTcoinTypography.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(transaction.isReceived ? INTcoinColors.success : INTcoinColors.textPrimary)

                HStack(spacing: 4) {
                    if transaction.confirmations >= 6 {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 12))
                            .foregroundColor(INTcoinColors.success)
                    } else if transaction.confirmations > 0 {
                        Image(systemName: "clock.fill")
                            .font(.system(size: 12))
                            .foregroundColor(INTcoinColors.warning)
                    } else {
                        Image(systemName: "hourglass")
                            .font(.system(size: 12))
                            .foregroundColor(INTcoinColors.textTertiary)
                    }
                    Text("\(transaction.confirmations) conf")
                        .font(INTcoinTypography.caption)
                        .foregroundColor(INTcoinColors.textSecondary)
                }
            }
        }
        .padding()
        .background(INTcoinColors.backgroundSecondary)
        .cornerRadius(INTcoinDesign.cornerRadiusMedium)
        .padding(.horizontal)
    }
}

// MARK: - Transactions List View

struct TransactionsListView: View {
    @ObservedObject var viewModel: WalletViewModel

    var body: some View {
        ZStack {
            INTcoinColors.background.ignoresSafeArea()

            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(viewModel.transactions) { tx in
                        TransactionRow(transaction: tx)
                    }
                }
                .padding(.vertical)
            }
        }
        .navigationTitle("Activity")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Receive View

struct ReceiveView: View {
    @ObservedObject var viewModel: WalletViewModel
    @Environment(\.dismiss) var dismiss
    @State private var copied = false

    var body: some View {
        NavigationView {
            ZStack {
                INTcoinColors.background.ignoresSafeArea()

                VStack(spacing: 24) {
                    Text("Receive INT")
                        .font(INTcoinTypography.title2)
                        .foregroundColor(INTcoinColors.textPrimary)

                    Text("Scan or share your address")
                        .font(INTcoinTypography.subheadline)
                        .foregroundColor(INTcoinColors.textSecondary)

                    // QR Code
                    if let address = viewModel.currentAddress {
                        QRCodeView(string: address)
                            .frame(width: 220, height: 220)
                            .padding()
                            .background(Color.white)
                            .cornerRadius(INTcoinDesign.cornerRadiusMedium)

                        // Address
                        VStack(spacing: 12) {
                            Text(address)
                                .font(INTcoinTypography.monospace)
                                .foregroundColor(INTcoinColors.textPrimary)
                                .multilineTextAlignment(.center)
                                .padding()
                                .background(INTcoinColors.backgroundSecondary)
                                .cornerRadius(INTcoinDesign.cornerRadiusSmall)
                                .overlay(
                                    RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusSmall)
                                        .stroke(INTcoinColors.border, lineWidth: 1)
                                )

                            Button(action: {
                                UIPasteboard.general.string = address
                                copied = true
                                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                                    copied = false
                                }
                            }) {
                                HStack {
                                    Image(systemName: copied ? "checkmark" : "doc.on.doc")
                                    Text(copied ? "Copied!" : "Copy Address")
                                }
                            }
                            .buttonStyle(INTcoinSecondaryButtonStyle())
                        }
                        .padding(.horizontal)
                    }

                    // New Address Button
                    Button(action: {
                        viewModel.generateNewAddress()
                    }) {
                        HStack {
                            Image(systemName: "plus.circle")
                            Text("Generate New Address")
                        }
                        .foregroundColor(INTcoinColors.primary)
                    }

                    Spacer()
                }
                .padding(.top, 40)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundColor(INTcoinColors.primary)
                }
            }
        }
    }
}

// MARK: - QR Code View

struct QRCodeView: View {
    let string: String

    var body: some View {
        if let image = generateQRCode(from: string) {
            Image(uiImage: image)
                .interpolation(.none)
                .resizable()
                .scaledToFit()
        } else {
            Image(systemName: "qrcode")
                .font(.system(size: 100))
                .foregroundColor(INTcoinColors.textTertiary)
        }
    }

    func generateQRCode(from string: String) -> UIImage? {
        let context = CIContext()
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(string.utf8)

        if let outputImage = filter.outputImage {
            let transform = CGAffineTransform(scaleX: 10, y: 10)
            let scaledImage = outputImage.transformed(by: transform)

            if let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) {
                return UIImage(cgImage: cgImage)
            }
        }
        return nil
    }
}

// MARK: - Send View

struct SendView: View {
    @ObservedObject var viewModel: WalletViewModel
    @Environment(\.dismiss) var dismiss
    @State private var recipientAddress = ""
    @State private var amount = ""
    @State private var showConfirmation = false
    @FocusState private var addressFocused: Bool
    @FocusState private var amountFocused: Bool

    var isValidInput: Bool {
        INTcoinSDK.validateAddress(recipientAddress) && !amount.isEmpty
    }

    var body: some View {
        NavigationView {
            ZStack {
                INTcoinColors.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 24) {
                        // Recipient Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Recipient")
                                .font(INTcoinTypography.headline)
                                .foregroundColor(INTcoinColors.textPrimary)

                            TextField("INT address", text: $recipientAddress)
                                .font(INTcoinTypography.monospace)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                                .intcoinTextField(isFocused: addressFocused)
                                .focused($addressFocused)

                            Button(action: {
                                // TODO: QR Scanner
                            }) {
                                HStack {
                                    Image(systemName: "qrcode.viewfinder")
                                    Text("Scan QR Code")
                                }
                                .font(INTcoinTypography.subheadline)
                                .foregroundColor(INTcoinColors.primary)
                            }
                        }
                        .padding(.horizontal)

                        // Amount Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Amount")
                                .font(INTcoinTypography.headline)
                                .foregroundColor(INTcoinColors.textPrimary)

                            HStack {
                                TextField("0.000000", text: $amount)
                                    .font(INTcoinTypography.title)
                                    .keyboardType(.decimalPad)
                                    .multilineTextAlignment(.center)
                                    .intcoinTextField(isFocused: amountFocused)
                                    .focused($amountFocused)

                                Text("INT")
                                    .font(INTcoinTypography.headline)
                                    .foregroundColor(INTcoinColors.textSecondary)
                            }

                            HStack {
                                Text("Available:")
                                    .font(INTcoinTypography.caption)
                                    .foregroundColor(INTcoinColors.textSecondary)

                                Text(viewModel.balance?.totalFormatted ?? "0.000000 INT")
                                    .font(INTcoinTypography.caption)
                                    .foregroundColor(INTcoinColors.success)

                                Spacer()

                                Button(action: {
                                    if let balance = viewModel.balance {
                                        amount = String(format: "%.6f", Double(balance.totalINTS) / 1_000_000)
                                    }
                                }) {
                                    Text("MAX")
                                        .font(INTcoinTypography.caption)
                                        .fontWeight(.semibold)
                                        .foregroundColor(INTcoinColors.primary)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(INTcoinColors.primary.opacity(0.2))
                                        .cornerRadius(4)
                                }
                            }
                        }
                        .padding(.horizontal)

                        Spacer().frame(height: 20)

                        // Send Button
                        Button(action: {
                            showConfirmation = true
                        }) {
                            HStack {
                                Image(systemName: "arrow.up.circle.fill")
                                Text("Review Transaction")
                            }
                        }
                        .buttonStyle(INTcoinPrimaryButtonStyle(isEnabled: isValidInput))
                        .disabled(!isValidInput)
                        .padding(.horizontal)

                        // Security Note
                        HStack(spacing: 8) {
                            Image(systemName: "shield.checkered")
                                .foregroundColor(INTcoinColors.success)
                            Text("Protected by Dilithium5 post-quantum signatures")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)
                        }
                        .padding()
                    }
                    .padding(.top, 24)
                }
            }
            .navigationTitle("Send INT")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(INTcoinColors.primary)
                }
            }
            .sheet(isPresented: $showConfirmation) {
                SendConfirmationView(
                    address: recipientAddress,
                    amount: amount,
                    viewModel: viewModel,
                    onConfirm: {
                        viewModel.sendTransaction(toAddress: recipientAddress, amount: amount)
                        dismiss()
                    }
                )
            }
        }
    }
}

// MARK: - Send Confirmation View

struct SendConfirmationView: View {
    let address: String
    let amount: String
    @ObservedObject var viewModel: WalletViewModel
    let onConfirm: () -> Void
    @Environment(\.dismiss) var dismiss

    var body: some View {
        ZStack {
            INTcoinColors.background.ignoresSafeArea()

            VStack(spacing: 24) {
                Text("Confirm Transaction")
                    .font(INTcoinTypography.title2)
                    .foregroundColor(INTcoinColors.textPrimary)

                VStack(spacing: 16) {
                    // Amount
                    VStack(spacing: 8) {
                        Text("Amount")
                            .font(INTcoinTypography.caption)
                            .foregroundColor(INTcoinColors.textSecondary)
                        Text("\(amount) INT")
                            .font(INTcoinTypography.title)
                            .foregroundColor(INTcoinColors.textPrimary)
                    }

                    Divider()
                        .background(INTcoinColors.border)

                    // Recipient
                    VStack(spacing: 8) {
                        Text("To")
                            .font(INTcoinTypography.caption)
                            .foregroundColor(INTcoinColors.textSecondary)
                        Text(address)
                            .font(INTcoinTypography.monospace)
                            .foregroundColor(INTcoinColors.textPrimary)
                            .multilineTextAlignment(.center)
                    }

                    Divider()
                        .background(INTcoinColors.border)

                    // Fee
                    HStack {
                        Text("Network Fee")
                            .font(INTcoinTypography.subheadline)
                            .foregroundColor(INTcoinColors.textSecondary)
                        Spacer()
                        Text("~0.000100 INT")
                            .font(INTcoinTypography.subheadline)
                            .foregroundColor(INTcoinColors.textPrimary)
                    }
                }
                .padding()
                .background(INTcoinColors.backgroundSecondary)
                .cornerRadius(INTcoinDesign.cornerRadiusMedium)
                .overlay(
                    RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusMedium)
                        .stroke(INTcoinColors.border, lineWidth: 1)
                )
                .padding(.horizontal)

                Spacer()

                VStack(spacing: 12) {
                    Button(action: onConfirm) {
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                            Text("Confirm & Send")
                        }
                    }
                    .buttonStyle(INTcoinPrimaryButtonStyle())
                    .padding(.horizontal)

                    Button(action: { dismiss() }) {
                        Text("Cancel")
                            .foregroundColor(INTcoinColors.textSecondary)
                    }
                }
                .padding(.bottom, 40)
            }
            .padding(.top, 40)
        }
    }
}

// MARK: - Settings View

struct SettingsView: View {
    @ObservedObject var viewModel: WalletViewModel
    @State private var showBackupPhrase = false
    @State private var showAbout = false

    var body: some View {
        ZStack {
            INTcoinColors.background.ignoresSafeArea()

            List {
                // Wallet Section
                Section {
                    SettingsRow(icon: "key.fill", title: "Backup Recovery Phrase", color: INTcoinColors.warning) {
                        showBackupPhrase = true
                    }

                    SettingsRow(icon: "lock.fill", title: "Change Password", color: INTcoinColors.primary) {
                        // TODO
                    }

                    SettingsRow(icon: "arrow.triangle.2.circlepath", title: "Rescan Blockchain", color: INTcoinColors.primary) {
                        viewModel.startSync()
                    }
                } header: {
                    Text("Wallet")
                        .foregroundColor(INTcoinColors.textSecondary)
                }
                .listRowBackground(INTcoinColors.backgroundSecondary)

                // Network Section
                Section {
                    HStack {
                        Image(systemName: "network")
                            .foregroundColor(INTcoinColors.primary)
                            .frame(width: 28)
                        Text("Network")
                        Spacer()
                        Text("Mainnet")
                            .foregroundColor(INTcoinColors.textSecondary)
                    }

                    HStack {
                        Image(systemName: "server.rack")
                            .foregroundColor(INTcoinColors.success)
                            .frame(width: 28)
                        Text("Node Status")
                        Spacer()
                        Text("Connected")
                            .foregroundColor(INTcoinColors.success)
                    }
                } header: {
                    Text("Network")
                        .foregroundColor(INTcoinColors.textSecondary)
                }
                .listRowBackground(INTcoinColors.backgroundSecondary)

                // Security Section
                Section {
                    HStack {
                        Image(systemName: "shield.checkered")
                            .foregroundColor(INTcoinColors.success)
                            .frame(width: 28)
                        Text("Signature Algorithm")
                        Spacer()
                        Text("Dilithium5")
                            .foregroundColor(INTcoinColors.textSecondary)
                    }

                    HStack {
                        Image(systemName: "lock.shield.fill")
                            .foregroundColor(INTcoinColors.success)
                            .frame(width: 28)
                        Text("Security Level")
                        Spacer()
                        Text("NIST Level 5")
                            .foregroundColor(INTcoinColors.textSecondary)
                    }
                } header: {
                    Text("Security")
                        .foregroundColor(INTcoinColors.textSecondary)
                }
                .listRowBackground(INTcoinColors.backgroundSecondary)

                // About Section
                Section {
                    SettingsRow(icon: "info.circle.fill", title: "About INTcoin", color: INTcoinColors.primary) {
                        showAbout = true
                    }

                    HStack {
                        Image(systemName: "app.badge")
                            .foregroundColor(INTcoinColors.textSecondary)
                            .frame(width: 28)
                        Text("Version")
                        Spacer()
                        Text("1.2.0")
                            .foregroundColor(INTcoinColors.textSecondary)
                    }
                } header: {
                    Text("About")
                        .foregroundColor(INTcoinColors.textSecondary)
                }
                .listRowBackground(INTcoinColors.backgroundSecondary)
            }
            .scrollContentBackground(.hidden)
            .listStyle(.insetGrouped)
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showAbout) {
            AboutView()
        }
    }
}

struct SettingsRow: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                    .frame(width: 28)
                Text(title)
                    .foregroundColor(INTcoinColors.textPrimary)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(INTcoinColors.textTertiary)
            }
        }
    }
}

// MARK: - About View

struct AboutView: View {
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ZStack {
                INTcoinColors.background.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 32) {
                        Spacer().frame(height: 20)

                        INTcoinLogo(size: 80)

                        Text("INTcoin")
                            .font(INTcoinTypography.title)
                            .foregroundColor(INTcoinColors.textPrimary)

                        Text("Post-Quantum Cryptocurrency")
                            .font(INTcoinTypography.subheadline)
                            .foregroundColor(INTcoinColors.accent)

                        VStack(alignment: .leading, spacing: 16) {
                            AboutInfoRow(title: "Signature Algorithm", value: "Dilithium5")
                            AboutInfoRow(title: "Hash Algorithm", value: "SHA3-256")
                            AboutInfoRow(title: "Mining Algorithm", value: "RandomX")
                            AboutInfoRow(title: "Security Level", value: "NIST Level 5")
                            AboutInfoRow(title: "Block Time", value: "60 seconds")
                            AboutInfoRow(title: "Max Supply", value: "21,000,000 INT")
                        }
                        .padding()
                        .background(INTcoinColors.backgroundSecondary)
                        .cornerRadius(INTcoinDesign.cornerRadiusMedium)
                        .padding(.horizontal)

                        VStack(spacing: 8) {
                            Text("Copyright 2025-2026 INTcoin Team")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textSecondary)

                            Text("MIT License")
                                .font(INTcoinTypography.caption)
                                .foregroundColor(INTcoinColors.textTertiary)
                        }

                        Spacer()
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundColor(INTcoinColors.primary)
                }
            }
        }
    }
}

struct AboutInfoRow: View {
    let title: String
    let value: String

    var body: some View {
        HStack {
            Text(title)
                .font(INTcoinTypography.subheadline)
                .foregroundColor(INTcoinColors.textSecondary)
            Spacer()
            Text(value)
                .font(INTcoinTypography.subheadline)
                .foregroundColor(INTcoinColors.textPrimary)
        }
    }
}

// MARK: - INTcoin Logo

struct INTcoinLogo: View {
    let size: CGFloat

    var body: some View {
        ZStack {
            // Outer ring
            Circle()
                .stroke(
                    LinearGradient(
                        colors: [INTcoinColors.primary, INTcoinColors.accent],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: size * 0.08
                )
                .frame(width: size, height: size)

            // Inner background
            Circle()
                .fill(INTcoinColors.backgroundSecondary)
                .frame(width: size * 0.85, height: size * 0.85)

            // INT text
            Text("INT")
                .font(.system(size: size * 0.28, weight: .bold, design: .rounded))
                .foregroundColor(INTcoinColors.primary)
        }
    }
}

// MARK: - View Model

class WalletViewModel: ObservableObject {
    @Published var isWalletOpen = false
    @Published var balance: Balance?
    @Published var currentAddress: String?
    @Published var transactions: [TransactionItem] = []
    @Published var isSyncing = false
    @Published var syncProgress: Double = 0.0
    @Published var errorMessage: String?

    var receivedCount: Int { transactions.filter { $0.isReceived }.count }
    var sentCount: Int { transactions.filter { !$0.isReceived }.count }
    var pendingCount: Int { transactions.filter { $0.confirmations < 6 }.count }

    private var sdk: INTcoinSDK?

    init() {
        do {
            sdk = try INTcoinSDK(network: "mainnet")
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func createWallet(password: String) -> String? {
        guard let sdk = sdk else { return nil }

        do {
            let mnemonic = try sdk.createWallet(password: password)
            loadWalletData()
            return mnemonic
        } catch {
            errorMessage = error.localizedDescription
            return nil
        }
    }

    func openWallet(password: String) {
        guard let sdk = sdk else { return }

        do {
            try sdk.openWallet(password: password)
            isWalletOpen = true
            loadWalletData()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func loadWalletData() {
        guard let sdk = sdk else { return }

        do {
            balance = try sdk.getBalance()
            currentAddress = try sdk.getNewAddress()
            // Load transactions (mock for now)
            loadMockTransactions()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func generateNewAddress() {
        guard let sdk = sdk else { return }

        do {
            currentAddress = try sdk.getNewAddress()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func startSync() {
        guard let sdk = sdk else { return }

        do {
            try sdk.startSync()
            isSyncing = true

            Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] timer in
                guard let self = self else {
                    timer.invalidate()
                    return
                }
                self.syncProgress = sdk.getSyncProgress()
                if self.syncProgress >= 1.0 {
                    self.isSyncing = false
                    timer.invalidate()
                    self.loadWalletData()
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func stopSync() {
        sdk?.stopSync()
        isSyncing = false
    }

    func sendTransaction(toAddress: String, amount: String) {
        guard let sdk = sdk,
              let amountINTS = INTcoinSDK.parseINTAmount(amount) else {
            errorMessage = "Invalid amount"
            return
        }

        do {
            let txHash = try sdk.sendTransaction(toAddress: toAddress, amountINTS: amountINTS)
            print("Transaction sent: \(txHash.hex)")
            loadWalletData()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func loadMockTransactions() {
        // Mock transactions for demo
        transactions = [
            TransactionItem(
                address: "int1q8h4k9j2m5n7p3r6s0t2v4w6x8y0z1a2b3c4d5e",
                amountINTS: 1_500_000,
                isReceived: true,
                confirmations: 156,
                date: Date().addingTimeInterval(-3600)
            ),
            TransactionItem(
                address: "int1m9n0p1q2r3s4t5u6v7w8x9y0z1a2b3c4d5e6f7",
                amountINTS: 250_000,
                isReceived: false,
                confirmations: 89,
                date: Date().addingTimeInterval(-7200)
            ),
            TransactionItem(
                address: "int1a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9",
                amountINTS: 5_000_000,
                isReceived: true,
                confirmations: 3,
                date: Date().addingTimeInterval(-1800)
            )
        ]
    }
}

// MARK: - Data Models

struct TransactionItem: Identifiable {
    let id = UUID()
    let address: String
    let amountINTS: UInt64
    let isReceived: Bool
    let confirmations: UInt32
    let date: Date

    var amountFormatted: String {
        let formatted = INTcoinSDK.formatINTS(amountINTS)
        return isReceived ? "+\(formatted)" : "-\(formatted)"
    }
}

// MARK: - Extensions

extension Data {
    var hex: String {
        map { String(format: "%02x", $0) }.joined()
    }
}
