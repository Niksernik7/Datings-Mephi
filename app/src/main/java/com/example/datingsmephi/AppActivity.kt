package com.example.datingsmephi

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class AppActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //ОТСЮДА ДО СЛЕДУЮЩЕЙ ПОМЕТКИ ДОЛЖНО ЛЕЖАТЬ В DOMAIN?
        //ВЫЗЫВАТЬСЯ ДОЛЖНО ПРИ НАЖАТИИ КНОПКИ PROFILESCREEN

        lateinit var viewModel: ImageSelectionViewModel
        viewModel = ViewModelProvider(this).get(ImageSelectionViewModel::class.java)

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    viewModel.updateImageUri(0, it) // Передаем URI напрямую
                }
            }
        }

        //ДО ЭТОГО МЕСТА


        //ОТСЮДА ДО СЛЕДУЮЩЕЙ ПОМЕТКИ ДОЛЖНО ЛЕЖАТЬ В DOMAIN?
        //ВЫЗЫВАТЬСЯ ДОЛЖНО ПРИ НАЖАТИИ КНОПКИ РЕДАКТИРОВАТЬ

        val login = intent.getStringExtra("login") // Получаем переданный логин
        val context = this@AppActivity

        val sharedPreferencesHelper = SharedPreferencesHelper(this)

        lifecycleScope.launch {
            try {
                val user_data = RetrofitInstance.api.getUserData(login.toString())
                val user_data1 = user_data.body()
                // Сохранение данных в SharedPreferences
                sharedPreferencesHelper.saveProfile(user_data.body())
                //////
                /*
                Toast.makeText(this@AppActivity, "Данные успешно загружены!", Toast.LENGTH_LONG).show()
                Log.d("User", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                Log.d("User", "group: ${user_data1?.group}")
                Log.d("User", "course: ${user_data1?.course}")
                Log.d("User", "averageGrade: ${user_data1?.averageGrade}")
                Log.d("User", "lastName: ${user_data1?.lastName}")
                Log.d("User", "firstName: ${user_data1?.firstName}")
                Log.d("User", "middleName: ${user_data1?.middleName}")
                Log.d("User", "gender: ${user_data1?.gender}")
                Log.d("User", "age: ${user_data1?.age}")
                Log.d("User", "height: ${user_data1?.height}")
                Log.d("User", "isSmoking: ${user_data1?.isSmoking}")
                Log.d("User", "isDrinking: ${user_data1?.isDrinking}")
                Log.d("User", "zodiacSign: ${user_data1?.zodiacSign}")
                Log.d("User", "sports: ${user_data1?.sports}")
                Log.d("User", "music: ${user_data1?.music}")
                Log.d("User", "aboutMe: ${user_data1?.aboutMe}")
                Log.d("User", "goals: ${user_data1?.goals?.joinToString(", ")}")
                Log.d("User", "interests: ${user_data1?.interests?.joinToString(", ")}")

                 */
                setContent {
                    if (user_data1 != null) {
                        AppScreen(user_data1, login, context, viewModel)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AppActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        // ДО ЭТОГО МЕСТА
    }
}


