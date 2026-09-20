package com.example.mkat_nur.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.viewmodel.AuthState
import com.example.mkat_nur.viewmodel.AuthViewModel

@Composable
fun AuthDialog(
    authViewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Giriş Yap, 1: Kaydol
    var isResetMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val themeColors = LocalAppThemeColors.current

    val primaryBtnTextTint = if (themeColors.accent == Color(0xFFFFD700) || themeColors.accent == Color(0xFFFBBF24) || themeColors.accent == Color(0xFFF59E0B)) Color(0xFF0D1B2A) else Color.White

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onDismiss()
            authViewModel.resetAuthState()
        }
    }

    Dialog(onDismissRequest = {
        authViewModel.resetAuthState()
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, themeColors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isResetMode) "Şifre Sıfırlama" else if (selectedTab == 0) "Giriş Yap" else "Kayıt Ol",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    IconButton(onClick = {
                        authViewModel.resetAuthState()
                        onDismiss()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = themeColors.textSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = themeColors.cardBorder
                )

                if (!isResetMode) {
                    // Tab Selector
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = themeColors.accent,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                authViewModel.resetAuthState()
                            },
                            text = { Text("Giriş Yap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) themeColors.accent else themeColors.textSecondary) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                authViewModel.resetAuthState()
                            },
                            text = { Text("Kayıt Ol", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) themeColors.accent else themeColors.textSecondary) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Error / Success Banner
                when (authState) {
                    is AuthState.Loading -> {
                        CircularProgressIndicator(
                            color = themeColors.accent,
                            modifier = Modifier.padding(vertical = 8.dp).size(36.dp)
                        )
                    }
                    is AuthState.Error -> {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    is AuthState.Success -> {
                        Text(
                            text = (authState as AuthState.Success).message,
                            color = Color(0xFF4CAF50),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    else -> {}
                }

                if (isResetMode) {
                    Text(
                        text = "E-posta adresinizi girin. Size şifre sıfırlama bağlantısı göndereceğiz.",
                        color = themeColors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta", color = themeColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = themeColors.accent) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { authViewModel.resetPassword(email) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sıfırlama Bağlantısı Gönder", color = primaryBtnTextTint, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { isResetMode = false }) {
                        Text("Giriş Ekranına Dön", color = themeColors.textSecondary, fontSize = 12.sp)
                    }
                } else if (selectedTab == 0) {
                    // Giriş Yap Form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta", color = themeColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = themeColors.accent) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Şifre", color = themeColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = themeColors.accent) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = themeColors.textSecondary
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            isResetMode = true
                            authViewModel.resetAuthState()
                        }) {
                            Text("Şifremi Unuttum?", color = themeColors.accent, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { authViewModel.signInWithEmail(email, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Giriş Yap", color = primaryBtnTextTint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                } else {
                    // Kayıt Ol Form
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ad Soyad", color = themeColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = themeColors.accent) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta", color = themeColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = themeColors.accent) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Şifre (Min 6 Karakter)", color = themeColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = themeColors.accent) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = themeColors.textSecondary
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { authViewModel.signUpWithEmail(name, email, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Kayıt Ol", color = primaryBtnTextTint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                if (!isResetMode) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = themeColors.cardBorder)
                        Text(
                            text = " VEYA ",
                            color = themeColors.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = themeColors.cardBorder)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google ile Giriş Yap Butonu
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(themeColors.cardBorder))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = themeColors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google ile Giriş Yap", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = themeColors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
