package com.pranay.fleettrack.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pranay.fleettrack.model.User
import com.pranay.fleettrack.model.UserRole
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("fleet_track_prefs", Context.MODE_PRIVATE)
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Authentication failed: no user returned"))

            val user = fetchUserFromFirestore(firebaseUser.uid)
                ?: return Result.failure(Exception("User document not found in Firestore"))

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginDriverWithCode(code: String): Result<User> {
        return try {
            val snapshot = firestore.collection("drivers")
                .whereEqualTo("loginCode", code)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Invalid Driver Code"))
            }

            val driverDoc = snapshot.documents.first()
            val driverId = driverDoc.id
            val driverName = driverDoc.getString("name") ?: "Driver"
            
            // Save local session
            prefs?.edit()?.apply {
                putString("driverId", driverId)
                putString("driverName", driverName)
                putString("loginCode", code)
                apply()
            }

            val user = User(
                uid = driverId, // using driverId as uid locally
                email = "",
                displayName = driverName,
                role = UserRole.DRIVER,
                driverId = driverId
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        prefs?.edit()?.clear()?.apply()
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            return fetchUserFromFirestore(firebaseUser.uid)
        }
        
        // Fallback to local driver session
        val driverId = prefs?.getString("driverId", null)
        val driverName = prefs?.getString("driverName", "")
        if (driverId != null) {
            return User(
                uid = driverId,
                email = "",
                displayName = driverName ?: "Driver",
                role = UserRole.DRIVER,
                driverId = driverId
            )
        }
        return null
    }

    fun isLoggedIn(): Boolean {
        val hasFirebaseAuth = auth.currentUser != null
        val hasDriverSession = prefs?.getString("driverId", null) != null
        return hasFirebaseAuth || hasDriverSession
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    private suspend fun fetchUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (!document.exists()) return null

            val email = document.getString("email") ?: ""
            val displayName = document.getString("displayName") ?: ""
            val roleString = document.getString("role") ?: "DRIVER"
            val role = try {
                UserRole.valueOf(roleString)
            } catch (e: IllegalArgumentException) {
                UserRole.DRIVER
            }
            val driverId = document.getString("driverId")

            User(
                uid = uid,
                email = email,
                displayName = displayName,
                role = role,
                driverId = driverId
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        val instance: AuthRepository by lazy { AuthRepository() }
    }
}
