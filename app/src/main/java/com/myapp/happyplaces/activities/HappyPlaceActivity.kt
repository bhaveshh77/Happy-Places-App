package com.myapp.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.internal.ApiKey
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.myapp.happyplaces.R
import com.myapp.happyplaces.database.DatabaseHandler
import com.myapp.happyplaces.databinding.ActivityHappyPlaceBinding
import com.myapp.happyplaces.models.HappyPlace
import com.myapp.happyplaces.utils.GetAddressFromLatLang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

class HappyPlaceActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHappyPlaceBinding.inflate(layoutInflater)
    }

    // Setting a listener for when the date is set in the DatePickerDialog
    private val calendar = Calendar.getInstance()
    private var saveImageToInternalStorage : Uri? = null
    private var longitude : Double = 0.0
    private var latitude : Double = 0.0
    private val apiKey = "AIzaSyC4F7EJUH3qMl8xS9s-9vgXpRh8jSyke4E"
//    var isAlertDialogShowing = false
    private lateinit var fusedLocationClient : FusedLocationProviderClient

//    private val autocompleteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val data: Intent? = result.data
//            // Handle the result data here
//            if (data != null) {
//                val place = Autocomplete.getPlaceFromIntent(data)
//                // Process the selected place
//                binding.etLocation.setText(place.address)
//                latitude = place.latLng!!.latitude
//                longitude = place.latLng!!.longitude
//            }
//        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
//            val status = Autocomplete.getStatusFromIntent(result.data)
//            // Handle the error
//        }
//    }


    private var updateHappyPlace  : HappyPlace? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.toolBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if(!Places.isInitialized()) {

            Places.initialize(this@HappyPlaceActivity, apiKey)
        }


        if (intent?.hasExtra("EXTRA_POSITION") == true) {
            updateHappyPlace = intent.getParcelableExtra("EXTRA_POSITION")
        }

        updateDateView()

        if(updateHappyPlace != null) {

            supportActionBar?.title = "EDIT HAPPY PLACE"

            binding.etTitle.setText(updateHappyPlace!!.title)
            binding.etDescription.setText(updateHappyPlace!!.description)
            binding.etLocation.setText(updateHappyPlace!!.location)
            binding.etDate.setText(updateHappyPlace!!.date)

            saveImageToInternalStorage = Uri.parse(updateHappyPlace!!.image)

            Log.d("ImageURI", "URI: $saveImageToInternalStorage")
            Glide.with(this@HappyPlaceActivity)
                .load(saveImageToInternalStorage.toString())
                .into(binding.ivPlaceImage)

            latitude = updateHappyPlace!!.latitude
            longitude = updateHappyPlace!!.longitude

            binding.btnSave.text = "UPDATE"
        }

        binding.etDate.setOnClickListener { view ->
            // Code to be executed when the EditText (transDate) is clicked
            val datePickerDialog = DatePickerDialog(this@HappyPlaceActivity)

            // Creating a DatePickerDialog, a pre-built dialog for picking a date
            datePickerDialog.setOnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->

//                Calendar calendar = Calendar.getInstance();: This creates a Calendar instance representing the current date and time. It will be used to manipulate and format the selected date.
                // Creating a Calendar instance to manipulate and format the selected date
                calendar[Calendar.DAY_OF_MONTH] = datePicker.dayOfMonth
                calendar[Calendar.MONTH] = datePicker.month
                calendar[Calendar.YEAR] = datePicker.year
                // Setting the selected date to the Calendar instance

                updateDateView()
            }
            datePickerDialog.show()
        }

//        binding.etLocation.setOnClickListener {
//            try {
//
//                val fields = listOf(Place.Field.ID,
//                    Place.Field.NAME,
//                    Place.Field.ADDRESS,
//                    Place.Field.LAT_LNG
//                    )
//
//                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
//                    .build(this@HappyPlaceActivity)
//                autocompleteLauncher.launch(intent)
//
//            } catch (e : Exception) {
//                e.printStackTrace()
//            }
//        }

        binding.tvAddImage.setOnClickListener {
            val pickerDialog = AlertDialog.Builder(this@HappyPlaceActivity)
            pickerDialog.setTitle("Select Action")
            val multiplePermissions = arrayOf("Select photo from Gallery", "Capture photo from Camera")
            pickerDialog.setItems(multiplePermissions) { dialog, which ->
           
                when(which) {
                    0 ->
                        choosePhotoFromGallery()
                    
                    1 ->

                        takePhotoFromCamera()
                }
            }.show()
        }

        binding.tvSelectCurrentLocation.setOnClickListener {
            requestNewLocationData()

        }


        binding.btnSave.setOnClickListener {

            when {
                binding.etTitle.text.isNullOrEmpty() -> {
                    Toast.makeText(this@HappyPlaceActivity, "Please add the Title!", Toast.LENGTH_SHORT).show()
                }
                binding.etDate.text.isNullOrEmpty() -> {
                    Toast.makeText(this@HappyPlaceActivity, "Please add the Date!", Toast.LENGTH_SHORT).show()
                }
                binding.etDescription.text.isNullOrEmpty() -> {
                    Toast.makeText(this@HappyPlaceActivity, "Please add the Description!", Toast.LENGTH_SHORT).show()
                }
                binding.etLocation.text.isNullOrEmpty() -> {
                    Toast.makeText(this@HappyPlaceActivity, "Please add the Location!", Toast.LENGTH_SHORT).show()
                }

                saveImageToInternalStorage == null -> {
                        Toast.makeText(
                            this@HappyPlaceActivity,
                            "Please select the Image!",
                            Toast.LENGTH_SHORT
                        ).show()
                } else -> {
                        val happyPlace = HappyPlace(
                            if(updateHappyPlace == null) 0 else updateHappyPlace!!.id,
                            binding.etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            latitude,
                            longitude
                        )
                val databaseHandler = DatabaseHandler(this)

                if(updateHappyPlace == null) {
                    val addHappyPlace = databaseHandler.addToHappyPlace(happyPlace)
                    if(addHappyPlace > 0) {
                        Toast.makeText(this@HappyPlaceActivity, "Added successfully!!", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                } else {
                    val updateHappyPlace = databaseHandler.updateHappyPlace(happyPlace)
                    if(updateHappyPlace> 0) {
                        Toast.makeText(this@HappyPlaceActivity, "Updated successfully!!", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
                }
            }
        }


    }

    private fun isLocationEnabled() : Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

//  Yes, that's correct! The isLocationEnabled function checks whether the device has the necessary location providers (like GPS or network-based providers) enabled. It doesn't specifically check for the hardware existence but rather whether the corresponding software services are available and enabled on the device.
    }

    private fun updateDateView() {

//                The Calendar class itself does not handle formatting for display. You typically use a SimpleDateFormat or another formatting mechanism to convert a Calendar instance into a formatted string:
        val simpleDateFormat =
            SimpleDateFormat("dd MMMM, yyyy")
        // Creating the date format
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMMM, yyyy");: Creates a date format pattern that defines how the date should be displayed.
//String formattedDate = simpleDateFormat.format(calendar.getTime());: Formats the selected date using the pattern and the Calendar instance.
        val formattedDate = simpleDateFormat.format(calendar.time)
        // Formatting the date using the pattern and the Calendar instance
        binding.etDate.setText(formattedDate)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) : Uri {
//        ContextWrapper is used to provide access to the application's context.
        val wrapper = ContextWrapper(applicationContext)
//        getDir is called on the ContextWrapper to retrieve or create a directory in the internal storage of the app. The directory is named IMAGE_DIRECTORY.
//The Context.MODE_PRIVATE parameter specifies that the directory is private to the app.
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
//        A new file is created inside the directory with a random UUID as the filename and a ".jpg" extension. This ensures that each saved image has a unique filename.
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
//            An OutputStream is created to write data to the file.
//The compress method of the Bitmap class is used to compress the bitmap data and write it to the OutputStream in JPEG format. The parameters Bitmap.CompressFormat.JPEG, 100 (compression quality), and stream are used in this process
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//            flush() ensures that any buffered data is written to the file.
            stream.flush()
//            close() closes the OutputStream, saving the changes to the file.
            stream.close()
        } catch (e : IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)

//        In summary, the code you provided is suitable for saving images for internal use within the app. If you need to share these images with other apps or components, you might explore using FileProvider and external storage.
    }

    private fun getLocationWithPermissionCheck() {
        if (isLocationEnabled()) {
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(requests: MultiplePermissionsReport?) {
                        if (requests!!.areAllPermissionsGranted()) {
                            // Permission granted, proceed with location code
                            requestNewLocationData()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showPermissionRationale()
                    }
                }).onSameThread().check()
        } else {
            Toast.makeText(
                this@HappyPlaceActivity,
                "Your Location Provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        if(isLocationPermissionGranted()) {
            val locationRequest = LocationRequest.create().apply {
                priority = Priority.PRIORITY_HIGH_ACCURACY
//                interval = 5000
//                fastestInterval = 5000
                interval = 1000
//                maxWaitTime = 6000
                numUpdates = 2
            }

//            locationRequest.
//            Log.e("Latitude", "$latitude")

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationRequestCallback,
                Looper.myLooper()
            )
//        Looper.myLooper(): This specifies the looper on which the callback events should be delivered. The Looper.myLooper() method returns the Looper associated with the current thread.
        } else {
            showPermissionRationale()
        }
    }

    private val locationRequestCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            super.onLocationResult(location)

            val lastLocation = location.lastLocation
            val latitudeCallback = lastLocation!!.latitude
            Log.e("Latitude", "$latitudeCallback")
            val longitudeCallback = lastLocation.longitude
            Log.e("Longitude", "$longitudeCallback")

//            Toast.makeText(
//                this@HappyPlaceActivity,
//                "$latitudeCallback + $longitudeCallback",
//                Toast.LENGTH_SHORT
//            ).show()

            lifecycleScope.launch {
                getLocationAddress(latitudeCallback, longitudeCallback)
            }

        }
    }
//
    suspend fun getLocationAddress(latitude : Double, longitude : Double) {
        withContext(Dispatchers.IO) {
            val address = GetAddressFromLatLang(this@HappyPlaceActivity, latitude, longitude).getAddress()
            Log.e("Address", address)
          withContext(Dispatchers.Main) {
              binding.etLocation.setText(address)
          }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
         return if(isLocationEnabled()) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            Toast.makeText(
                this@HappyPlaceActivity,
                "Your Location Provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
             false
        }
    }

    private fun choosePhotoFromGallery() {

        Dexter.withContext(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if(p0!!.areAllPermissionsGranted()) {

                        val intent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.
                            Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, GALLERY)

                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showPermissionRationale()
                }

            }).onSameThread().check()

    }


    private fun takePhotoFromCamera() {

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // Here after all the permission are granted launch the CAMERA to capture an image.
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showPermissionRationale()
                }
            }).onSameThread()
            .check()
    }

    private fun showPermissionRationale() {

       val alertDialog = AlertDialog.Builder(this)
            .setTitle("You have denied the permissions, Please allow it to continue!!")
            .setCancelable(false)
            .setPositiveButton("Go To Settings") { _, _ ->
                val intent = Intent()

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", packageName, null)
                } else {
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package : $packageName")
                }

                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()

//        isAlertDialogShowing = true/

        alertDialog.setOnDismissListener {
            alertDialog.dismiss()

//            isAlertDialogShowing = false
        }
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        // TODO (Step 3 : Saving an image which is selected from GALLERY. And printed the path in logcat.)
                        // START
//                        val saveImageToInternalStorage =  saveImageToInternalStorage(selectedImageBitmap)
//                        Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")
                        // END

                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap) // Set the selected image from GALLERY to imageView.
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@HappyPlaceActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera

                // TODO (Step 4 : Saving an image which is selected from CAMERA. And printed the path in logcat.)
                // START
//                val saveImageToInternalStorage =
//                    saveImageToInternalStorage(thumbnail)
//                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")
                // END

                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)

                binding.ivPlaceImage.setImageBitmap(thumbnail) // Set to the imageView.
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2

        // START
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        // END
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}

