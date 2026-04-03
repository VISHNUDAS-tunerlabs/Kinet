package com.example.kinet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.kinet.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val NunitoFont = GoogleFont("Nunito")

val NunitoFontFamily = FontFamily(
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = NunitoFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

val Typography = Typography(
    // Major text — explicit dark black
    displayLarge   = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold,      fontSize = 57.sp, lineHeight = 64.sp,  color = TextPrimary),
    displayMedium  = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold,      fontSize = 45.sp, lineHeight = 52.sp,  color = TextPrimary),
    displaySmall   = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold,      fontSize = 36.sp, lineHeight = 44.sp,  color = TextPrimary),
    headlineLarge  = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold,      fontSize = 32.sp, lineHeight = 40.sp,  color = TextPrimary),
    headlineMedium = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 36.sp,  color = TextPrimary),
    headlineSmall  = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.SemiBold,  fontSize = 24.sp, lineHeight = 32.sp,  color = TextPrimary),
    titleLarge     = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.SemiBold,  fontSize = 22.sp, lineHeight = 28.sp,  color = TextPrimary),
    titleMedium    = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp,  color = TextPrimary, letterSpacing = 0.15.sp),
    titleSmall     = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp,  color = TextPrimary, letterSpacing = 0.1.sp),
    // Body & label — secondary dark, let theme override as needed
    bodyLarge      = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp,  color = TextSecondary, letterSpacing = 0.5.sp),
    bodyMedium     = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp,  color = TextSecondary, letterSpacing = 0.25.sp),
    bodySmall      = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp,  color = TextSecondary, letterSpacing = 0.4.sp),
    labelLarge     = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp,  color = TextSecondary, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp,  color = TextSecondary, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontFamily = NunitoFontFamily, fontWeight = FontWeight.Medium,    fontSize = 11.sp, lineHeight = 16.sp,  color = TextSecondary, letterSpacing = 0.5.sp),
)
