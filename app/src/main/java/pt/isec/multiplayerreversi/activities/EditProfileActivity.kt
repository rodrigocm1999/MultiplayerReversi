package pt.isec.multiplayerreversi.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.activities.others.PermissionsHelper
import pt.isec.multiplayerreversi.databinding.ActivityEditProfileBinding
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.File
import kotlin.concurrent.thread

class EditProfileActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var binding: ActivityEditProfileBinding

    private var changedImage = false
    private var newImageFile: File? = null
    private lateinit var app: App
    private lateinit var profile: Profile
    private lateinit var permissionsHelper: PermissionsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.edit_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        permissionsHelper = PermissionsHelper(this)

        app = application as App

        profile = app.getProfile()
        binding.etNameChange.setText(profile.name)
        binding.imgBtnProfileChange.setImageDrawable(profile.icon)

        setOnClicks()
    }

    private fun setOnClicks() {
        binding.imgBtnProfileChange.setOnClickListener {
            permissionsHelper.withPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                newImageFile = File.createTempFile(
                    "avatar", ".img",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    val fileUri = FileProvider.getUriForFile(
                        this@EditProfileActivity,
                        "pt.isec.multiplayerreversi.android.fileprovider", newImageFile!!
                    )
                    addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                }
                Log.i(OURTAG, "Image path -> $newImageFile -----------------")
                startActivityForResultFoto.launch(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsHelper.onRequestPermissionsResult(requestCode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private var startActivityForResultFoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            removeTempImgFile()
            return@registerForActivityResult
        }
        thread {
            changedImage = true
            newImageFile?.absolutePath?.let {
                bitmap = BitmapFactory.decodeFile(it)
                removeTempImgFile()
                bitmap = bitmap.scale(600, 800)
                runOnUiThread { binding.imgBtnProfileChange.setImageBitmap(bitmap) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mnSave)
            saveProfile()
        return super.onOptionsItemSelected(item)
    }

    private fun saveProfile() {
        finish()
        val writtenName = binding.etNameChange.text.toString()
        if (writtenName.isNotBlank() && profile.name != writtenName)
            profile.name = writtenName

        if (changedImage)
            profile.icon = BitmapDrawable(resources, bitmap)

        app.saveProfile(profile)

        if (changedImage) {
            thread {
                openFileOutput(App.avatarFileName, MODE_PRIVATE).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
            }
        }
    }

    private fun removeTempImgFile() {
        //To guarantee that there are no leftovers
//        val asd = newImageFile!!.parentFile!!
//        asd.listFiles()?.forEach {
//            it.delete()
//        }
        newImageFile!!.delete()
    }

    override fun onBackPressed() {
        confirmationToLeave()

    }

    override fun onSupportNavigateUp(): Boolean {
        confirmationToLeave()
        return true
    }

    private fun confirmationToLeave() {
        val writtenName = binding.etNameChange.text.toString()
        if (profile.name != writtenName || changedImage) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle(supportActionBar?.title)
                .setMessage(getString(R.string.question_leave_without_save))
                .setPositiveButton(getString(R.string.yes)) { d, w -> finish() }
                .setNegativeButton(getString(R.string.no)) { dialog, w -> dialog.dismiss() }
                .setNeutralButton(getString(R.string.save_and_leave)){ d, w -> saveProfile()}
                .setCancelable(true)
                .create()
            alertDialog.show()
        }else{
            finish()
        }
    }

}