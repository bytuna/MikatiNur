package com.example.mkat_nur.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Lütfen e-posta ve şifrenizi girin.")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener {
                _authState.value = AuthState.Success("Giriş başarılı!")
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(mapFirebaseError(e.message))
            }
    }

    fun signUpWithEmail(name: String, email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Lütfen tüm alanları doldurun.")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Şifre en az 6 karakter olmalıdır.")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null && name.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                        .build()
                    user.updateProfile(profileUpdates)
                }
                _authState.value = AuthState.Success("Hesabınız başarıyla oluşturuldu!")
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(mapFirebaseError(e.message))
            }
    }

    fun signInWithGoogleToken(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _authState.value = AuthState.Success("Google ile giriş başarılı!")
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(mapFirebaseError(e.message))
            }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Lütfen e-posta adresinizi girin.")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                _authState.value = AuthState.Success("Şifre sıfırlama bağlantısı e-postanıza gönderildi.")
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(mapFirebaseError(e.message))
            }
    }

    // ------------------------------------------------------------------ Profil işlemleri

    /** Giriş e-posta+şifre ile mi yapılmış? (Google kullanıcıları şifreyle yeniden doğrulanamaz.) */
    fun isPasswordUser(): Boolean =
        auth.currentUser?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true

    fun isGoogleUser(): Boolean =
        auth.currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true

    fun updateDisplayName(name: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        val clean = name.trim()
        if (user == null) { onResult(false, "Oturum açık değil."); return }
        if (clean.length < 2) { onResult(false, "Ad en az 2 karakter olmalıdır."); return }
        if (clean.length > 40) { onResult(false, "Ad en fazla 40 karakter olabilir."); return }
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(clean).build())
            .addOnSuccessListener {
                user.reload().addOnCompleteListener {
                    _currentUser.value = auth.currentUser
                    onResult(true, "Adınız güncellendi.")
                }
            }
            .addOnFailureListener { e -> onResult(false, mapFirebaseError(e.message)) }
    }

    /** Oturum açık kullanıcının e-postasına şifre sıfırlama bağlantısı gönderir. */
    fun sendPasswordReset(onResult: (Boolean, String) -> Unit) {
        val email = auth.currentUser?.email
        if (email.isNullOrBlank()) { onResult(false, "Bu hesapta e-posta adresi yok."); return }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(true, "Şifre sıfırlama bağlantısı $email adresine gönderildi.") }
            .addOnFailureListener { e -> onResult(false, mapFirebaseError(e.message)) }
    }

    /**
     * Hesabı kalıcı olarak siler. Güvenlik için önce yeniden doğrulama yapılır:
     * e-posta kullanıcıları şifreyi, Google kullanıcıları yeni bir Google ID token'ı verir.
     * Buluttaki kaza verisi de silinir.
     */
    fun deleteAccount(password: String?, googleIdToken: String?, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) { onResult(false, "Oturum açık değil."); return }

        val credential = when {
            googleIdToken != null -> GoogleAuthProvider.getCredential(googleIdToken, null)
            !password.isNullOrEmpty() && !user.email.isNullOrBlank() ->
                EmailAuthProvider.getCredential(user.email!!, password)
            else -> { onResult(false, "Doğrulama bilgisi eksik."); return }
        }

        user.reauthenticate(credential)
            .addOnSuccessListener {
                val uid = user.uid
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .collection("data").document("kaza_debt").delete()
                    .addOnCompleteListener {
                        // Bulut verisi silinemese bile (örn. kayıt yoksa) hesabı silmeye devam et
                        user.delete()
                            .addOnSuccessListener {
                                _currentUser.value = null
                                _authState.value = AuthState.Idle
                                onResult(true, "Hesabınız ve bulut verileriniz silindi.")
                            }
                            .addOnFailureListener { e -> onResult(false, mapFirebaseError(e.message)) }
                    }
            }
            .addOnFailureListener { e -> onResult(false, mapFirebaseError(e.message)) }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    private fun mapFirebaseError(msg: String?): String {
        if (msg == null) return "Bir hata oluştu."
        return when {
            msg.contains("The email address is badly formatted", true) -> "Geçersiz e-posta formatı."
            msg.contains("The password is invalid", true) || msg.contains("invalid-credential", true) -> "E-posta veya şifre hatalı."
            msg.contains("The email address is already in use", true) -> "Bu e-posta adresi zaten kullanımda."
            msg.contains("There is no user record", true) -> "Bu e-posta adresiyle kayıtlı kullanıcı bulunamadı."
            msg.contains("network error", true) -> "İnternet bağlantınızı kontrol edin."
            else -> msg
        }
    }
}
