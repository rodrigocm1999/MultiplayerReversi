package pt.isec.multiplayerreversi.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityEditProfileBinding
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.File
import kotlin.concurrent.thread

class EditProfileActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var binding: ActivityEditProfileBinding

    private var permissionsGranted = false
    private var changedImage = false
    private var newImageFile: File? = null
    private lateinit var app: App
    private lateinit var profile: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.edit_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = application as App

        profile = app.getProfile()
        binding.etNameChange.setText(profile.name)
        binding.imgBtnProfileChange.setImageDrawable(profile.icon)

        //TODO 10 quando tentar sair confirmar se não quer guardar perguntar se deseja guardar talvez

        setOnClicks()
        handlePermissions()
    }

    private fun handlePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQ_PERM_CODE)
        } else
            permissionsGranted = true
    }

    private fun setOnClicks() {
        binding.imgBtnProfileChange.setOnClickListener {
            val imageFile = File.createTempFile("avatar_", ".img",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES))
            newImageFile = imageFile
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                val fileUri = FileProvider.getUriForFile(this@EditProfileActivity,
                    "pt.isec.multiplayerreversi.android.fileprovider", imageFile)
                putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            }
            startActivityForResultFoto.launch(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

/*
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.mnSave)?.isVisible = permissionsGranted
        return true
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERM_CODE) {
            permissionsGranted =
                (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    private var startActivityForResultFoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK)
            return@registerForActivityResult
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
        val asd = newImageFile!!.parentFile!!
        asd.listFiles()?.forEach {
            it.delete()
        }
        //newImageFile!!.delete()
    }

    companion object {
        private const val REQ_PERM_CODE = 1234
    }

}