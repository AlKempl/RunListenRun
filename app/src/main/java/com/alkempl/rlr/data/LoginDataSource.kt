package com.alkempl.rlr.data

import com.alkempl.rlr.data.model.LoggedInUser
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.IOException
import javax.security.auth.callback.Callback

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance();

    init {

    }

    fun login(username: String, password: String): Result<FirebaseUser> {
        try {
            val res = await(auth.signInWithEmailAndPassword(username, password))
            return if (res.user != null && auth.currentUser != null){
                Result.Success(auth.currentUser!!)
            }else{
                Result.Error(IOException("Error logging in"))
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun register(username: String, password: String): Result<FirebaseUser> {
        try {
            return if(auth.createUserWithEmailAndPassword(username, password).isSuccessful
                    && auth.currentUser != null){
                        Result.Success(auth.currentUser!!)
                    }else{
                        Result.Error(IOException("Error registering Test"))
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