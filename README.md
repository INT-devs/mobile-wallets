# INTcoin Mobile Wallets

Official mobile wallet SDKs and applications for the INTcoin cryptocurrency.

**Repository**: https://github.com/INT-devs/mobile-wallets

## Overview

This repository contains mobile wallet implementations for iOS and Android:

- **iOS SDK**: Swift-based SDK with SPV support
- **Android SDK**: Kotlin-based SDK with SPV support
- **Core SDK**: C++ mobile SDK for cross-platform functionality

## Features

### Wallet Functionality
- **SPV (Simplified Payment Verification)**: Lightweight blockchain validation
- **Bloom Filters**: Efficient transaction filtering
- **Post-Quantum Security**: Dilithium3 signatures for quantum-resistant transactions
- **HD Wallet**: BIP32/BIP39 hierarchical deterministic wallet
- **Cross-Platform**: Shared C++ core with native bindings

### v1.6.0 Features
- **Dark Mode**: System-aware theme switching
- **Accessibility**: Full VoiceOver (iOS) and TalkBack (Android) support
- **Stealth Addresses**: Enhanced privacy with one-time payment addresses
- **QR Code Generation**: Payment URI support with amount and labels
- **Transaction History**: Searchable, filterable transaction list
- **Address Book**: Contact management with labels
- **Biometric Security**: Face ID, Touch ID, and fingerprint authentication

### NOT Supported

**Mining is NOT supported on mobile devices.** Mining cryptocurrency on mobile devices is:
- Not economically viable (extremely low hashrate vs power cost)
- Harmful to device battery and hardware longevity
- Inefficient compared to desktop/dedicated mining hardware

Mobile wallets are designed for sending, receiving, and managing INT only.

## Directory Structure

```
mobile-wallets/
├── mobile/
│   ├── ios/                # iOS SDK and example app
│   │   ├── INTcoinSDK.swift    # Main SDK implementation
│   │   ├── Package.swift       # Swift Package Manager config
│   │   └── ExampleApp.swift    # Example iOS application
│   └── android/            # Android SDK
│       ├── build.gradle.kts    # Gradle build config
│       └── src/main/java/      # Kotlin SDK source
├── src/mobile/             # C++ mobile SDK core
│   ├── mobile_sdk.cpp          # SDK implementation
│   └── mobile_rpc.cpp          # Mobile RPC client
├── include/intcoin/        # Header files
│   └── mobile_sdk.h            # SDK public API
└── docs/                   # Documentation
    ├── MOBILE_WALLET.md        # Wallet architecture
    └── MOBILE_SDK.md           # SDK reference
```

## iOS Quick Start

### Requirements
- iOS 15.0+
- Xcode 15.0+
- Swift 5.9+

### Swift Package Manager

Add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/INT-devs/mobile-wallets.git", from: "1.6.0")
]
```

### Usage

```swift
import INTcoinSDK

// Initialize SDK
let sdk = try INTcoinSDK(network: "mainnet")

// Create new wallet
let mnemonic = try sdk.createWallet(password: "secure_password")
// IMPORTANT: Store mnemonic securely - it's the only way to recover your wallet!

// Open existing wallet
try sdk.openWallet(password: "secure_password")

// Get new receiving address
let address = try sdk.getNewAddress()

// Get wallet balance
let balance = try sdk.getBalance()
print("Confirmed: \(balance.confirmedFormatted)")
print("Pending: \(balance.unconfirmedFormatted)")

// Send transaction
let txHash = try sdk.sendTransaction(toAddress: "int1...", amountINTS: 1_000_000)

// Generate payment QR code URI
let uri = INTcoinSDK.generatePaymentURI(
    address: address,
    amountINTS: 5_000_000,
    label: "Coffee Shop",
    message: "Payment for order #123"
)
```

### Accessibility (iOS)

The iOS SDK fully supports VoiceOver:

```swift
// Balance labels automatically announce currency
balance.confirmedFormatted // "1.234567 INT"

// Transaction events include accessibility descriptions
sdk.setTransactionCallback { event in
    // VoiceOver announces: "Received 1.5 INT, 3 confirmations"
}
```

## Android Quick Start

### Requirements
- Android 8.0 (API 26)+
- Kotlin 1.9+
- Android Studio Hedgehog+

### Gradle

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.intcoin:sdk:1.6.0")
}
```

### Usage

```kotlin
import org.intcoin.sdk.INTcoinSDK

// Initialize
val sdk = INTcoinSDK(context, network = "mainnet")

// Create wallet
val mnemonic = sdk.createWallet(password = "secure_password")
// IMPORTANT: Store mnemonic securely!

// Open existing wallet
sdk.openWallet(password = "secure_password")

// Get new address
val address = sdk.getNewAddress()

// Get balance
val balance = sdk.getBalance()
println("Confirmed: ${balance.confirmedFormatted}")

// Send transaction
val txHash = sdk.sendTransaction(
    toAddress = "int1...",
    amountINTS = 1_000_000
)
```

### Accessibility (Android)

The Android SDK fully supports TalkBack:

```kotlin
// Content descriptions are automatically set
balanceTextView.contentDescription = "Balance: ${balance.confirmedFormatted}"

// Live regions announce balance changes
balanceTextView.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
```

## Security

### Post-Quantum Cryptography
- **Dilithium3**: All transactions signed with quantum-resistant signatures
- **SHA3-256**: Quantum-resistant hashing

### Wallet Security
- **AES-256-GCM**: Wallet encryption
- **BIP39**: 24-word mnemonic recovery phrase
- **Biometric Auth**: Optional Face ID / Touch ID / Fingerprint

### Best Practices
1. **Never share your mnemonic phrase** with anyone
2. **Store backups offline** - paper or metal backup
3. **Enable biometric authentication** for additional security
4. **Verify addresses** before sending large amounts
5. **Keep your app updated** for security patches

## Network Configuration

### Mainnet (Default)
```swift
let sdk = try INTcoinSDK(network: "mainnet")
```

### Testnet
```swift
let sdk = try INTcoinSDK(
    network: "testnet",
    rpcEndpoint: "http://testnet.international-coin.org:12210"
)
```

## Documentation

| Document | Description |
|----------|-------------|
| [MOBILE_WALLET.md](docs/MOBILE_WALLET.md) | Mobile wallet architecture and design |
| [MOBILE_SDK.md](docs/MOBILE_SDK.md) | SDK API reference and integration guide |

## Building

### iOS

```bash
cd mobile/ios
swift build
```

### Android

```bash
cd mobile/android
./gradlew build
```

### C++ Core

```bash
mkdir build && cd build
cmake -DBUILD_MOBILE=ON ..
make -j$(nproc)
```

## Related Repositories

- [INT-devs/intcoin](https://github.com/INT-devs/intcoin) - INTcoin core node
- [INT-devs/mining-pool](https://github.com/INT-devs/mining-pool) - Mining pool software (desktop only)

## Support

- **Discord**: https://discord.gg/Y7dX4Ps2Ha
- **Website**: https://international-coin.org
- **Email**: support@international-coin.org

## License

MIT License - See LICENSE file for details.

---

**Version**: 1.6.0
**Last Updated**: January 17, 2026
