package media.opensesame.syncstagequickstart

import media.opensesame.syncstagesdk.ConnectionData
import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import media.opensesame.syncstagequickstart.ui.theme.FfmpegTestTheme
import media.opensesame.syncstagesdk.SyncStage

class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    var accessToken =
        ""
    var userId = 0
    private var sdk: SyncStage? = null
    private var streamIdsLiveData = MutableLiveData<List<String>>(listOf())

    private val requestMultiplePermissions =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Log.e(TAG, "TEST ${it.key} = ${it.value}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FfmpegTestTheme {
                ExampleScreen(
                    initialUserId = userId,
                    initialDeveloperToken = accessToken
                )
            }
        }
    }

    private fun updateStreamIdsList(connectionData: ConnectionData){
        showToastWithSerializedObject(connectionData)
        streamIdsLiveData.postValue(sdk?.getStreamIds() ?: mutableListOf())
    }

    // SDK interfacing =============================================================================

    fun initSDK() {
        sdk = SyncStage(
            accessToken = accessToken,
            userId = userId,
            ctx = applicationContext,
            onInitializedListener = {
                showToastFromNonUIThread("SDK Initialized Successfully")
            },
            onInitializationErrorListener = { _, msg -> showToastFromNonUIThread(msg) },
            onOperationErrorListener = { _, msg -> showToastFromNonUIThread(msg) },
            onConnectionDataChange = {connectionData -> },
            onStreamListChange = {connectionData -> updateStreamIdsList(connectionData)},
            throwExceptionsOnErrors = false,
        )
        showToast("Initialization in progress...")
    }

    fun isInitialized(): Boolean {
        val initialized: Boolean = sdk?.isInitialized() ?: false
        val message = (initialized).toString()
        showToast(message)
        return initialized
    }

    private fun getExpirationTime() {
        val message = (sdk?.getExpirationTime() ?: "Not initialized").toString()
        showToast(message)
    }

    private fun getConnectionData(){
        if (sdk?.isInitialized() == true) {
            val connectionData = sdk?.getConnectionData()
            if (connectionData != null) {
                showToastWithSerializedObject(connectionData)
            }
        } else {
            showToast("SDK not initialized.")
        }
        return
    }

    private fun connect() {
        if (sdk?.isInitialized() == true) {
            sdk?.connect()
            showToast("Connected")
        } else {
            showToast("SDK not initialized.")
        }
    }

    private fun disconnect() {
        if (sdk?.isInitialized() == true) {
            sdk?.disconnect()
            showToast("Disconnected")
        } else {
            showToast("SDK not initialized.")
        }
    }
    // =============================================================================================

    @Composable
    fun ExampleScreen(
        initialUserId: Int,
        initialDeveloperToken: String,
        onUserChanged: (Int) -> Unit = { id ->
            userId = id
        },
        onTokenChanged: (String) -> Unit = { token ->
            accessToken = token
        }
    ) {
        LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        var recordAudioGranted by remember { mutableStateOf(false) }
        checkPermission(Manifest.permission.RECORD_AUDIO, onGranted = { recordAudioGranted = true })
        var permissionsGranted = recordAudioGranted

        var usersDropdownExpanded by remember { mutableStateOf(false) }
        var volumeDropdownExpanded by remember { mutableStateOf(false) }

        val userIds: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6)
        var selectedUserId by remember { mutableStateOf(initialUserId) }

        val streamIds: List<String> by streamIdsLiveData.observeAsState(listOf())
        var selectedVolumeStreamIndex by remember { mutableStateOf(0) }

        var token by remember { mutableStateOf(initialDeveloperToken) }
        var volume: Int by remember { mutableStateOf(100) }


        Surface(color = MaterialTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(120.dp)
                ) {
                    Image(
                        modifier = Modifier.scale(0.7f),
                        painter = painterResource(R.drawable.syncstage),
                        contentDescription = "syncstage"
                    )
                    Text("SDK quick start example", modifier = Modifier.fillMaxWidth())
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .padding(top = 0.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    OutlinedTextField(
                        value = token,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        onValueChange = {
                            token = it
                            onTokenChanged(it)
                        },
                        placeholder = { Text("Access token") },
                    )
                    Spacer(modifier=Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        var textfieldSize by remember { mutableStateOf(Size.Zero) }
                        OutlinedTextField(
                            value = "User: ${userIds[selectedUserId]}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    //This value is used to assign to the DropDown the same width
                                    textfieldSize = coordinates.size.toSize()
                                }
                                .clickable { usersDropdownExpanded = !usersDropdownExpanded },
                            readOnly = true,
                            enabled = false,
                            onValueChange = { },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "dropdown arrow",
                                    modifier = Modifier.clickable {
                                        if (sdk == null) {
                                            usersDropdownExpanded = !usersDropdownExpanded
                                        } else {
                                            showToast("Cannot change user once SDK initialized")
                                        }
                                    }
                                )
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Black,
                                unfocusedBorderColor = Black,
                                textColor = Black,
                                disabledTextColor = Black
                            )
                        )

                        DropdownMenu(
                            expanded = usersDropdownExpanded && sdk == null,
                            onDismissRequest = { usersDropdownExpanded = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { textfieldSize.width.toDp() })
                                .background(MaterialTheme.colors.surface)
                        ) {
                            userIds.forEachIndexed { index, userId ->
                                DropdownMenuItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        selectedUserId = index
                                        onUserChanged(index)
                                        usersDropdownExpanded = false
                                    }
                                ) {
                                    Text(text = "User $userId")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        requestMultiplePermissions.launch(
                            arrayOf(
                                Manifest.permission.RECORD_AUDIO
                            ),
                        )
                        recordAudioGranted = true
                    }) {
                        Text("Request permissions")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { initSDK() },
                        enabled = permissionsGranted
                    ) {
                        Text("Initialize SDK")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isInitialized() },
                        enabled = permissionsGranted
                    ) {
                        Text("Is initialized?")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { connect() },
                        enabled = permissionsGranted
                    ) {
                        Text("Connect")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { disconnect() },
                        enabled = permissionsGranted
                    ) {
                        Text("Disconnect")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { getConnectionData() },
                        enabled = permissionsGranted
                    ) {
                        Text("Get connection data")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { getExpirationTime() },
                        enabled = permissionsGranted
                    ) {
                        Text("Get expiration time")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    try {
                        streamIds[selectedVolumeStreamIndex]
                    }catch (e: IndexOutOfBoundsException){
                        if (streamIds.isNotEmpty()){
                            selectedVolumeStreamIndex = 0
                        }
                    }
                    if(streamIds.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Row(modifier = Modifier.width(200.dp)) {
                                var textfieldSize by remember { mutableStateOf(Size.Zero) }
                                OutlinedTextField(
                                    value = "Stream: ${streamIds[selectedVolumeStreamIndex].takeLast(6)}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onGloballyPositioned { coordinates ->
                                            //This value is used to assign to the DropDown the same width
                                            textfieldSize = coordinates.size.toSize()
                                        }
                                        .clickable {
                                            volumeDropdownExpanded = !volumeDropdownExpanded
                                        },
                                    readOnly = true,
                                    enabled = false,
                                    onValueChange = { },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "dropdown arrow",
                                            modifier = Modifier.clickable {
                                                volumeDropdownExpanded = !volumeDropdownExpanded
                                            }
                                        )
                                    },
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Black,
                                        unfocusedBorderColor = Black,
                                        textColor = Black,
                                        disabledTextColor = Black
                                    )
                                )

                                DropdownMenu(
                                    expanded = volumeDropdownExpanded && sdk != null,
                                    onDismissRequest = { volumeDropdownExpanded = false },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { textfieldSize.width.toDp() })
                                        .background(MaterialTheme.colors.surface)
                                ) {
                                    streamIds.forEachIndexed { index, streamId ->
                                        DropdownMenuItem(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                selectedVolumeStreamIndex = index
                                                volumeDropdownExpanded = false
                                                sdk?.getConnectionData()?.rxStreams?.forEach { stream ->
                                                    if (stream.streamId == streamIds[selectedVolumeStreamIndex]){
                                                        volume = stream.volume
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(text = "Stream: ${streamId.takeLast(6)}")
                                        }
                                    }
                                }
                            }

                            Slider(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(140.dp),
                                value = (volume.toFloat()) / 100,
                                onValueChange = { volume = (it * 100).toInt() },
                                onValueChangeFinished = {
                                    val streamIdToChangeVolume = streamIds[selectedVolumeStreamIndex]
                                    sdk?.changeStreamVolume(rxStreamId = streamIdToChangeVolume, volume)
                                }
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = BuildConfig.VERSION_NAME,
                    )
                }
            }
        }
    }

    private fun checkPermission(permission: String, onGranted: () -> Unit = {}){
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED){

            Log.d(TAG, "$permission permission granted")
            onGranted()
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT, printDebug: Boolean = true) {
        if (printDebug) {
            Log.d(TAG, message)
        }
        Toast.makeText(applicationContext, message, duration).show()
    }

    private fun showToastFromNonUIThread(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Handler(Looper.getMainLooper()).post {
            showToast(msg, duration)
        }
    }

    private fun showToastWithSerializedObject(obj: Any){
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        val connectionDataString: String = gsonPretty.toJson(obj)
        showToastFromNonUIThread(connectionDataString)
    }
}

