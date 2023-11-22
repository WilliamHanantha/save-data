package com.example.simpandata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simpandata.databinding.ActivityMainBinding
import com.example.simpandata.model.JsonModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader
import android.Manifest;
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var newArrayList: ArrayList<JsonModel>
    private lateinit var newRecyclerView: RecyclerView

    private companion object{
        //PERMISSION request constant, assign any value
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        newRecyclerView = binding.rvData
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf()

        val adapter = PersonAdapter(newArrayList)
        newRecyclerView.adapter = adapter

        binding.buttonSave.setOnClickListener {
            if (checkPermission()){
                Log.d(TAG, "onCreate: Permission already granted, create folder")
                newArrayList.clear()
                readDataFromFile(binding.editTextFilePath.text.toString() + ".txt")
                newArrayList.add(JsonModel(
                    binding.editTextName.text.toString(),
                    binding.editTextAge.text.toString().toInt(),
                    binding.editTextJob.text.toString()
                ))
                // Menyimpan data model JSON ke file teks
                saveModelToJsonFile(newArrayList, binding.editTextFilePath.text.toString() + ".txt")
            }
            else{
                Log.d(TAG, "onCreate: Permission was not granted, request")
                requestPermission()
            }
        }

        binding.buttonRead.setOnClickListener {
            newArrayList.clear()
            readDataFromFile(binding.editTextFilePath.text.toString() + ".txt")
            try {
                // Obtain the external file directory for your app
                val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                // Check if the directory is null (unlikely, but it's good to handle)
                if (externalDir != null) {
                    // Specify the file path within the app's external files directory
                    val filePathtxt = File(externalDir, binding.editTextFilePath.text.toString() + ".txt")
                    val filePathjson = File(externalDir, binding.editTextFilePath.text.toString() + ".json")

                    // Check if the file exists
                    if (filePathtxt.exists()) {
                        val fileInputStream = FileInputStream(filePathtxt)
                        val inputStreamReader = InputStreamReader(fileInputStream)
                        val bufferedReader = BufferedReader(inputStreamReader)
                        val stringBuilder = StringBuilder()
                        var text: String?

                        // Read the file content
                        while (bufferedReader.readLine().also { text = it } != null) {
                            stringBuilder.append(text).append('\n')
                        }
                        newRecyclerView.adapter?.notifyDataSetChanged()
                        Log.i("ws", "azsexrdctfvygbuhnij")
                        // Set the read content to the EditText
                        binding.tvTxt.text = (stringBuilder.toString().trim())
                    } else if (filePathjson.exists()){
                        val fileInputStream = FileInputStream(filePathjson)
                        val inputStreamReader = InputStreamReader(fileInputStream)
                        val bufferedReader = BufferedReader(inputStreamReader)
                        val stringBuilder = StringBuilder()
                        var text: String?

                        // Read the file content
                        while (bufferedReader.readLine().also { text = it } != null) {
                            stringBuilder.append(text).append('\n')
                        }

                        // Set the read content to the EditText
                        binding.tvTxt.text = (stringBuilder.toString().trim())
                    }
                    else {
                        println("File does not exist at $filePathtxt")
                    }
                } else {
                    println("External directory is null.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error reading from file: ${e.message}")
            }
        }
        binding.buttonClear.setOnClickListener{
            binding.editTextName.setText("")
            binding.editTextAge.setText("")
            binding.editTextJob.setText("")
            binding.tvTxt.text = ""
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            try {
                Log.d("TAG", "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else{
            //Android is below 11(R)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")
        //here we will handle the result of our intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            if (Environment.isExternalStorageManager()){
                //Manage External Storage Permission is granted
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")
            }
            else{
                //Manage External Storage Permission is denied....
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied....")
                toast("Manage External Storage Permission is denied....")
            }
        }
        else{
            //Android is below 11(R)
        }
    }

    private fun checkPermission(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        }
        else{
            //Android is below 11(R)
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                //check each permission if granted or not
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){
                    //External Storage Permission granted
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                }
                else{
                    //External Storage Permission denied...
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
                    toast("External Storage Permission denied...")
                }
            }
        }
    }


    private fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveModelToJsonFile(list: List<JsonModel>, fileName: String) {
        if (isExternalStorageWritable()) {
            // Obtain the external file directory for your app
            val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            // Check if the directory is null (unlikely, but it's good to handle)
            if (externalDir != null) {
                // Specify the file path within the app's external files directory
                val filePath = File(externalDir, fileName)
                try {
                    // Use BufferedWriter to efficiently write data to the file
                    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
                    val json = gson.toJson(list)

                    BufferedWriter(FileWriter(filePath)).use { writer ->
                        writer.write(json)
                        newRecyclerView.adapter?.notifyDataSetChanged()
                        toast(binding.editTextName.text.toString() + "has been written to $filePath")
                    }

                    println("Data has been written to $filePath")
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Error writing to file: ${e.message}")
                }
            } else {
                println("External directory is null.")
            }
        } else {
            println("External storage is not available or not writable.")
        }
    }

    private fun readDataFromFile(fileName: String) {
        try {
            // Obtain the external file directory for your app
            val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // Check if the directory is null (unlikely, but it's good to handle)
            if (externalDir != null) {
                // Specify the file path within the app's external files directory
                val filePath = File(externalDir, fileName)

                // Check if the file exists
                if (filePath.exists()) {
                    val fileInputStream = FileInputStream(filePath)
                    val inputStreamReader = InputStreamReader(fileInputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    val stringBuilder = StringBuilder()
                    var text: String?

                    // Read the file content into a StringBuilder
                    while (bufferedReader.readLine().also { text = it } != null) {
                        stringBuilder.append(text).append('\n')
                    }

                    // Parse the entire content as a list of JsonModel
                    val json = stringBuilder.toString().trim()
                    val type = object : TypeToken<List<JsonModel>>() {}.type
                    val jsonModelList : List<JsonModel> = Gson().fromJson(json, type)
                    newArrayList.clear()
                    newArrayList.addAll(jsonModelList)
                    println(newArrayList)
                    println("Data read success")
                } else {
                    println("File does not exist at $filePath")
                }
            } else {
                println("External directory is null.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error reading from file: ${e.message}")
        }
    }

}

private fun isExternalStorageWritable(): Boolean {
    val state = Environment.getExternalStorageState()
    return state == Environment.MEDIA_MOUNTED
}