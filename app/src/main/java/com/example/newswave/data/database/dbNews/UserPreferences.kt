package com.example.newswave.data.database.dbNews

import android.content.Context
import com.example.newswave.domain.entity.UserEntity
import javax.inject.Inject

class UserPreferences (private val context: Context) {

    fun saveUserData(user: UserEntity) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("id", user.id)
        editor.putString("first_name", user.firstName)
        editor.putString("last_name", user.lastName)
        editor.putString("email", user.email)
        editor.putString("username", user.username)
        editor.apply()
    }

    fun getUserData(): UserEntity? {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("id", null)
        val firstName = sharedPreferences.getString("first_name", null)
        val lastName = sharedPreferences.getString("last_name", null)
        val email = sharedPreferences.getString("email", null)
        val username = sharedPreferences.getString("username", null)
        return if (id != null && firstName != null && lastName != null && email != null && username != null) {
            UserEntity(id, username, email,null, firstName, lastName )
        } else {
            null
        }
    }
}