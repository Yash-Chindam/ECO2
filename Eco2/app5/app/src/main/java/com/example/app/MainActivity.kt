package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream
import android.content.Intent
import android.app.Activity.RESULT_OK
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import android.media.ExifInterface
import android.graphics.Matrix
import android.os.Looper
import androidx.compose.material3.Text

data class Item(val name: String, val quantity: String)

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var _location by mutableStateOf<Location?>(null)
    private val location: Location? get() = _location
    private var isTracking by mutableStateOf(false)
    private val cancellationTokenSource = CancellationTokenSource()
    private var lastLocation: Location? = null
    private var totalDistance = 0f

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Request location permission on app start
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }

        setContent {
            ShoppingBillApp()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now you can start location updates
                startLocationUpdates()
            } else {
                // Permission denied, handle it appropriately
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        // Get the last location (it's still nullable)
                        val lastReceivedLocation = locationResult.lastLocation

                        // Only proceed if we have a valid last location
                        lastReceivedLocation?.let { newLocation ->
                            // Update the location state
                            _location = newLocation

                            // Calculate the distance if lastLocation is not null
                            lastLocation?.let { previousLocation ->
                                totalDistance += previousLocation.distanceTo(newLocation)
                            }
                            lastLocation = newLocation
                        }
                    }
                },
                Looper.getMainLooper()
            )
            isTracking = true
        }
    }

    private fun stopLocationUpdates() {
        cancellationTokenSource.cancel()
        isTracking = false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShoppingBillApp() {
        val context = LocalContext.current
        var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
        var extractedText by remember { mutableStateOf("") }
        var showImage by remember { mutableStateOf(false) }
        var showResults by remember { mutableStateOf(false) }
        val items = remember { mutableStateListOf<Item>() }

        // Initialize the text recognizer for English (using default options)
        val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Create the image picker launcher using ACTION_OPEN_DOCUMENT
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result -> // This is the onResult lambda
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImage = getBitmapFromUri(context, uri)
                    showImage = true
                }
            }
        }

        // State for showing the starting screen
        var showStartingScreen by remember { mutableStateOf(true) }

        // State for showing the "Back" button
        var showBackButton by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showBackButton) {
                // Back button
                Button(
                    onClick = {
                        showImage = false
                        showResults = false
                        items.clear()
                        showStartingScreen = true
                        showBackButton = false // Hide the back button after going back
                    },
                    modifier = Modifier.padding(bottom = 16.dp) // Add padding to separate it from other content
                ) {
                    Text("Back")
                }
            }

            if (showStartingScreen) {
                Button(onClick = {
                    showStartingScreen = false
                }) {
                    Text("Start")
                }
            } else if (!showImage && !showResults) {
                Button(onClick = {
                    // Use ACTION_OPEN_DOCUMENT to allow the user to select a file
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "image/*" // Specify image MIME type
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    launcher.launch(intent)
                }) {
                    Text("Select Image")
                }
            } else if (showImage) {
                selectedImage?.let { image ->
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    selectedImage?.let {
                        showBackButton = true // Show the back button before extracting
                        extractTextFromImage(recognizer, it, context) { recognizedText ->
                            extractedText = recognizedText
                            showResults = true
                            showImage = false
                            parseBillText(extractedText, items)
                        }
                    }
                }) {
                    Text("Extract Text")
                }
            } else if (showResults) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (items.isNotEmpty()) {
                        LazyColumn {
                            items(items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween // Align items to opposite sides
                                ) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f) // Expand item name to fill available space
                                    )
                                    Text(
                                        text = item.quantity, // Assuming quantity is on the right side
                                        modifier = Modifier.weight(0.5f) // Adjust weight for quantity
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No items found in the bill.")
                    }
                    // "Back" button is already available outside this conditional block
                }
            }

            // Location tracking section
            if (location != null) {
                Text("Current Location: ${location?.latitude}, ${location?.longitude}", fontWeight = FontWeight.Bold)
            } else {
                Text("Location is not available")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isTracking) {
                Button(onClick = {
                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startLocationUpdates()
                    } else {
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
                    }
                }) {
                    Text("Start Tracking")
                }
            } else {
                Button(onClick = { stopLocationUpdates() }) {
                    Text("Stop Tracking")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Display the total distance
            Text(text = "Total Distance: ${String.format("%.2f", totalDistance)} meters", fontWeight = FontWeight.Bold)
        }
    }

    private fun extractTextFromImage(recognizer: TextRecognizer, image: Bitmap, context: android.content.Context,
                                     onTextExtracted: (String) -> Unit) {
        val imageInput = InputImage.fromBitmap(image, 0)

        recognizer.process(imageInput)
            .addOnSuccessListener { visionText: Text ->
                val extractedText = visionText.text
                onTextExtracted(extractedText)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseBillText(text: String, items: MutableList<Item>) {
        val lines = text.split("\n").filter { it.isNotBlank() }
        var foundDescription = false
        for (line in lines) {
            if (line.trim().startsWith("Description", ignoreCase = true)) {
                foundDescription = true
                continue
            }
            if (foundDescription) {
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.isNotEmpty()) {
                    val name = parts.subList(0, parts.size - 1).joinToString(" ")
                    val quantity = parts.lastOrNull() ?: ""
                    items.add(Item(name, quantity))
                }
            }
        }
    }

    // Get Bitmap from URI using Scoped Storage
    private fun getBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
        try {
            context.contentResolver.openInputStream(uri)?.use {
                // Decode the image to a Bitmap
                var bitmap = BitmapFactory.decodeStream(it)

                // Get the EXIF orientation from the image
                val exif = ExifInterface(context.contentResolver.openInputStream(uri)!!)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                // Rotate the bitmap according to the EXIF orientation
                bitmap = rotateBitmap(bitmap, orientation)

                return bitmap
            }
        } catch (e: Exception) {
            // Handle the exception appropriately
        }
        return null
    }

    // Helper function to rotate the Bitmap
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap // No rotation needed
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}