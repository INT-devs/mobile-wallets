# INTcoin Mobile Wallets

Official mobile wallet SDKs and applications for the INTcoin cryptocurrency.

**Repository**: https://github.com/INT-devs/mobile-wallets

## Overview

This repository contains mobile wallet implementations for iOS and Android:

- **iOS SDK**: Swift-based SDK with SPV support
- **Android SDK**: Kotlin-based SDK with SPV support
- **Core SDK**: C++ mobile SDK for cross-platform functionality

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

## Features

- **SPV (Simplified Payment Verification)**: Lightweight blockchain validation
- **Bloom Filters**: Efficient transaction filtering
- **Post-Quantum Security**: Dilithium3 signatures
- **HD Wallet**: BIP32/BIP39 hierarchical deterministic wallet
- **Lightning Network**: Layer 2 payment support
- **Cross-Platform**: Shared C++ core with native bindings

## iOS Quick Start

### Swift Package Manager

Add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/INT-devs/mobile-wallets.git", from: "1.0.0")
]
```

### Usage

```swift
import INTcoinSDK

// Initialize wallet
let wallet = INTcoinWallet()
try await wallet.initialize()

// Create new wallet
let mnemonic = try wallet.createWallet()

// Get balance
let balance = try await wallet.getBalance()

// Send transaction
let txid = try await wallet.send(to: "int1...", amount: 1.0)
```

## Android Quick Start

### Gradle

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.intcoin:sdk:1.0.0")
}
```

### Usage

```kotlin
import org.intcoin.sdk.INTcoinSDK

// Initialize
val sdk = INTcoinSDK(context)
sdk.initialize()

// Create wallet
val mnemonic = sdk.createWallet()

// Get balance
val balance = sdk.getBalance()

// Send transaction
val txid = sdk.send("int1...", amount = 1.0)
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
- [INT-devs/mining-pool](https://github.com/INT-devs/mining-pool) - Mining pool software

## Support

- **Discord**: https://discord.gg/Y7dX4Ps2Ha
- **Website**: https://international-coin.org
- **Email**: support@international-coin.org

## License

MIT License - See LICENSE file for details.

---

**Version**: 1.0.0-beta
**Last Updated**: January 11, 2026
