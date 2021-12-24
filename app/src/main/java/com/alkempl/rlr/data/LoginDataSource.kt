package com.alkempl.rlr.data

import android.util.Log
import com.alkempl.rlr.data.model.LoggedInUser
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.security.auth.callback.Callback

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance();

    init {

    }

    suspend fun login(username: String, password: String): Result<FirebaseUser> {
        try {
            val task = auth.signInWithEmailAndPassword(username, password).await()
            return if (auth.currentUser != null){
                Result.Success(auth.currentUser!!)
            }else{
                Result.Error(IOException("Error logging in Test"))
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in E", e))
        }
    }

    suspend fun register(username: String, password: String): Result<FirebaseUser> {
         try {
            val task = auth.createUserWithEmailAndPassword(username, password).await();
            if(auth.currentUser != null){
                Log.d("LDRE", auth.currentUser!!.email.toString());
                return Result.Success(auth.currentUser!!)
            }else{
                return Result.Error(IOException("Error registering Test"))
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error registering E: $e", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }

    private fun gotUser(user: FirebaseUser?) : Result<Any>{
        return if(user != null){
            Result.Success(user);
        }else{
            Result.Error(IOException("Error registering"))
        }
    }

    private fun gotError(user: Result<Any>) : Result<Any>{
        return user;
    }
}