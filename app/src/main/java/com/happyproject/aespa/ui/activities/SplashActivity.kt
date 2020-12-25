package com.happyproject.aespa.ui.activities

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.widget.TextView
import com.afollestad.rxkprefs.Pref
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.ktx.storage
import com.happyproject.aespa.PREF_APP_THEME
import com.happyproject.aespa.R
import com.happyproject.aespa.constants.AppThemes
import com.happyproject.aespa.constants.Constants.APP_PACKAGE_NAME
import com.happyproject.aespa.extensions.attachLifecycle
import com.happyproject.aespa.extensions.toast
import com.happyproject.aespa.ui.activities.base.PermissionsActivity
import io.reactivex.functions.Consumer
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream

class SplashActivity : PermissionsActivity() {

    private val appThemePref by inject<Pref<AppThemes>>(name = PREF_APP_THEME)
    private lateinit var textDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appThemePref.get().themeRes)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        textDescription = findViewById(R.id.textDescription)

        if (!permissionsManager.hasStoragePermission()) {
            permissionsManager.requestStoragePermission().subscribe(Consumer {
                checkSavedSong()
            }).attachLifecycle(this)
            return
        }

        checkSavedSong()
    }

    private fun goToMain() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        startActivity(intent)
        finish()
    }

    private fun checkSavedSong() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()

        val dir = File(
            Environment.getExternalStorageDirectory().toString() + "/" + APP_PACKAGE_NAME
        )
        if (dir.exists() && dir.isDirectory) {
            val children = dir.listFiles()
            if (children.isNullOrEmpty()) {
                // toast("load song")

                copy()
                // downloadSong()
            } else {
                goToMain()
            }
        } else {
            toast("directory not found")
            dir.mkdirs()
            copy()
            // downloadSong()
        }
    }

    private fun copy() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            fetchTimeoutInSeconds = 1
            minimumFetchIntervalInSeconds = 60
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote)
        remoteConfig.fetchAndActivate()

        if (remoteConfig.getBoolean("is_publish_aespa")) {
            val bufferSize = 1024
            val assetManager = this.assets
            val assetFiles = assetManager.list("")

            if (assetFiles.isNullOrEmpty()) toast("list song empty")

            val totalSong = assetFiles?.size ?: 0

            assetFiles?.forEachIndexed { index, item ->
                val position = index + 1
                textDescription.text = "Load songs... ($position of $totalSong)"

                if (item.contains(".mp3")) {
                    val inputStream = assetManager.open(item)
                    val outputStream = FileOutputStream(
                        File(
                            Environment.getExternalStorageDirectory()
                                .toString() + "/" + APP_PACKAGE_NAME,
                            item
                        )
                    )

                    try {
                        inputStream.copyTo(outputStream, bufferSize)
                    } finally {
                        inputStream.close()
                        outputStream.flush()
                        outputStream.close()
                    }

                    MediaScannerConnection.scanFile(
                        this,
                        arrayOf(
                            Environment.getExternalStorageDirectory()
                                .toString() + "/" + APP_PACKAGE_NAME + "/$item"
                        ),
                        null
                    ) { path, uri -> }


                    sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse(
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + APP_PACKAGE_NAME + "/$item"
                            )
                        )
                    )
                }
            }

            // textDescription.text = "Wait a second"

            Handler().postDelayed({
                goToMain()
            }, 1000)
        } else {
            textDescription.text = "App not published.\nPlease wait a minute"
            Handler().postDelayed({
                recreate()
            }, 3000)
        }
    }

    private fun downloadSong() {
        val storage = Firebase.storage
        val listRef = storage.reference.child("audio")
        listRef.listAll().addOnSuccessListener {

        }

        listRef.listAll()
            .addOnSuccessListener { listResult ->
                val itemList = listResult.items
                val totalSong = itemList.size

                itemList.forEachIndexed { index, item ->
                    if (item.name.contains(".mp3")) {
                        val position = index + 1

                        textDescription.text = "Download songs... ($position of $totalSong)"

                        try {
                            val file = File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + APP_PACKAGE_NAME,
                                item.name
                            )
                            item.getFile(file)
                        } finally {
                        }

                        MediaScannerConnection.scanFile(
                            this,
                            arrayOf(
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + APP_PACKAGE_NAME + "/${item.name}"
                            ),
                            null
                        ) { path, uri -> }

                        sendBroadcast(
                            Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.parse(
                                    Environment.getExternalStorageDirectory()
                                        .toString() + "/" + APP_PACKAGE_NAME + "/${item.name}"
                                )
                            )
                        )
                    }
                }

                toast("Download completed")

                Handler().postDelayed({
                    goToMain()
                }, 2000)
            }
            .addOnFailureListener {
                toast("Failed when fetch songs")
                // Uh-oh, an error occurred!
            }
    }
}
