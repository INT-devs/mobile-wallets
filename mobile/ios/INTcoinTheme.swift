// Copyright (c) 2024-2026 The INTcoin Core developers
// Distributed under the MIT software license

import SwiftUI

/// INTcoin brand colors - dark theme matching Qt desktop wallet
/// WCAG 2.1 AA compliant with proper contrast ratios
struct INTcoinColors {
    // MARK: - Primary Backgrounds
    static let background = Color(hex: "0a0f1e")           // Deep navy - main background
    static let backgroundSecondary = Color(hex: "141b2e")  // Slightly lighter - cards
    static let backgroundTertiary = Color(hex: "1e2842")   // Input fields, buttons

    // MARK: - Accent Colors
    static let primary = Color(hex: "3399ff")              // INTcoin blue - primary actions
    static let primaryHover = Color(hex: "66b3ff")         // Blue hover state
    static let accent = Color(hex: "00d4ff")               // Cyan - highlights, selected states

    // MARK: - Semantic Colors
    static let success = Color(hex: "10b75f")              // Green - received, confirmed
    static let warning = Color(hex: "f59e0b")              // Amber - pending, unconfirmed
    static let error = Color(hex: "ef4444")                // Red - errors, failed, send

    // MARK: - Text Colors
    static let textPrimary = Color(hex: "f8fafc")          // White - primary text
    static let textSecondary = Color(hex: "94a3b8")        // Gray - secondary text
    static let textTertiary = Color(hex: "64748b")         // Darker gray - hints

    // MARK: - Border Colors
    static let border = Color(hex: "334155")               // Border, dividers
    static let borderFocused = Color(hex: "3399ff")        // Focus ring

    // MARK: - Gradients
    static let gradientPrimary = LinearGradient(
        colors: [Color(hex: "3399ff"), Color(hex: "00d4ff")],
        startPoint: .leading,
        endPoint: .trailing
    )

    static let gradientBackground = LinearGradient(
        colors: [Color(hex: "0a0f1e"), Color(hex: "141b2e")],
        startPoint: .top,
        endPoint: .bottom
    )
}

/// INTcoin typography styles
struct INTcoinTypography {
    static let largeTitle = Font.system(size: 34, weight: .bold, design: .rounded)
    static let title = Font.system(size: 28, weight: .bold, design: .rounded)
    static let title2 = Font.system(size: 22, weight: .semibold, design: .rounded)
    static let headline = Font.system(size: 17, weight: .semibold)
    static let body = Font.system(size: 17)
    static let callout = Font.system(size: 16)
    static let subheadline = Font.system(size: 15)
    static let footnote = Font.system(size: 13)
    static let caption = Font.system(size: 12)
    static let monospace = Font.system(size: 14, weight: .regular, design: .monospaced)
    static let balance = Font.system(size: 42, weight: .bold, design: .rounded)
}

/// INTcoin design constants
struct INTcoinDesign {
    static let cornerRadiusSmall: CGFloat = 8
    static let cornerRadiusMedium: CGFloat = 12
    static let cornerRadiusLarge: CGFloat = 16
    static let cornerRadiusXL: CGFloat = 24

    static let paddingSmall: CGFloat = 8
    static let paddingMedium: CGFloat = 16
    static let paddingLarge: CGFloat = 24
    static let paddingXL: CGFloat = 32

    static let minTouchTarget: CGFloat = 44
    static let buttonHeight: CGFloat = 52
    static let iconSizeMedium: CGFloat = 24
    static let iconSizeLarge: CGFloat = 32
    static let iconSizeXL: CGFloat = 48
}

// MARK: - Color Extension

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - View Modifiers

struct INTcoinCardStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(INTcoinColors.backgroundSecondary)
            .cornerRadius(INTcoinDesign.cornerRadiusMedium)
            .overlay(
                RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusMedium)
                    .stroke(INTcoinColors.border, lineWidth: 1)
            )
    }
}

struct INTcoinPrimaryButtonStyle: ButtonStyle {
    var isEnabled: Bool = true

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(INTcoinTypography.headline)
            .foregroundColor(isEnabled ? INTcoinColors.background : INTcoinColors.textTertiary)
            .frame(maxWidth: .infinity)
            .frame(height: INTcoinDesign.buttonHeight)
            .background(isEnabled ?
                (configuration.isPressed ? INTcoinColors.primaryHover : INTcoinColors.primary) :
                INTcoinColors.backgroundTertiary
            )
            .cornerRadius(INTcoinDesign.cornerRadiusSmall)
    }
}

struct INTcoinSecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(INTcoinTypography.headline)
            .foregroundColor(INTcoinColors.primary)
            .frame(maxWidth: .infinity)
            .frame(height: INTcoinDesign.buttonHeight)
            .background(configuration.isPressed ?
                INTcoinColors.primary.opacity(0.2) :
                INTcoinColors.backgroundTertiary
            )
            .cornerRadius(INTcoinDesign.cornerRadiusSmall)
            .overlay(
                RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusSmall)
                    .stroke(INTcoinColors.primary, lineWidth: 2)
            )
    }
}

struct INTcoinTextFieldStyle: ViewModifier {
    var isFocused: Bool = false

    func body(content: Content) -> some View {
        content
            .padding(INTcoinDesign.paddingMedium)
            .background(INTcoinColors.backgroundSecondary)
            .foregroundColor(INTcoinColors.textPrimary)
            .cornerRadius(INTcoinDesign.cornerRadiusSmall)
            .overlay(
                RoundedRectangle(cornerRadius: INTcoinDesign.cornerRadiusSmall)
                    .stroke(isFocused ? INTcoinColors.borderFocused : INTcoinColors.border, lineWidth: 2)
            )
    }
}

// MARK: - View Extensions

extension View {
    func intcoinCard() -> some View {
        modifier(INTcoinCardStyle())
    }

    func intcoinTextField(isFocused: Bool = false) -> some View {
        modifier(INTcoinTextFieldStyle(isFocused: isFocused))
    }
}
