package pt.isec.multiplayerreversi.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.activities.others.PermissionsHelper
import pt.isec.multiplayerreversi.databinding.ActivityEditProfileBinding
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.File
import kotlin.concurrent.thread


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var app: App
    private lateinit var profile: Profile
    private lateinit var permissionsHelper: PermissionsHelper
    private var bitmapDrawable: BitmapDrawable? = null
    private var tempImageFile: File? = null
    private lateinit var gameSettings: App.GameSettings
    private var showPossibleMovesChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.edit_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        permissionsHelper = PermissionsHelper(this)

        app = application as App
        gameSettings= app.sharedGamePreferences

        profile = app.getProfile()
        binding.etNameChange.setText(profile.name)
        profile.icon?.let { binding.imgBtnProfileChange.setImageDrawable(it) }
        binding.switchPossibleMoves.isChecked = gameSettings.showPossibleMoves
        showPossibleMovesChange = gameSettings.showPossibleMoves

        setOnClicks()
    }

    private fun setOnClicks() {
        binding.imgBtnProfileChange.setOnClickListener {
            permissionsHelper.withPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            {
                tempImageFile = File.createTempFile(
                    "avatar", ".img",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    val fileUri = FileProvider.getUriForFile(
                        this@EditProfileActivity,
                        "pt.isec.multiplayerreversi.android.fileprovider", tempImageFile!!
                    )
                    addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                }
                startActivityForResultFoto.launch(intent)
            }
        }
        binding.switchPossibleMoves.setOnCheckedChangeListener { _, isChecked ->
            showPossibleMovesChange = isChecked
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsHelper.onRequestPermissionsResult(requestCode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (bitmapDrawable != null) {
            profile.icon = bitmapDrawable
            app.tempProfile = profile
            outState.putBoolean("changes", true)
        } else if (tempImageFile != null) {
            outState.putString("newImageFile", tempImageFile?.absolutePath)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val tempImagePath = savedInstanceState.getString("newImageFile")
        if (savedInstanceState.getBoolean("changes")) {
            profile = app.tempProfile!!
            bitmapDrawable = profile.icon
        } else tempImagePath?.let {
            val file = File(tempImagePath)
            tempImageFile = file
        }
    }

    private var startActivityForResultFoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            removeTempImgFile()
            return@registerForActivityResult
        }
        thread {
            tempImageFile?.absolutePath?.let { path ->
                var bitmap = BitmapFactory.decodeFile(path)
                removeTempImgFile()
                //Fixes rotation on dumb devices

                if (bitmap.width >= bitmap.height) {
                    val rotationMatrix = Matrix()
                    rotationMatrix.setRotate(-90f)
                    bitmap = Bitmap.createBitmap(
                        bitmap, 0, 0,
                        bitmap.width, bitmap.height, rotationMatrix, true
                    )
                }

                bitmap = bitmap.scale(300, 400)

                bitmapDrawable = BitmapDrawable(resources, bitmap)
                profile.icon = bitmapDrawable
                runOnUiThread { binding.imgBtnProfileChange.setImageDrawable(profile.icon) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnSave -> saveProfile()
            R.id.mnSignOut -> {
                Firebase.auth.signOut()
                app.resetProfile()
                finish()
            }
            R.id.mnAbout -> {
                aboutDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveProfile() {
        val writtenName = binding.etNameChange.text.toString()
        if (writtenName.isBlank()) {
            Toast.makeText(this, R.string.name_is_invalid, Toast.LENGTH_LONG).show()
            return
        }
        if (showPossibleMovesChange != gameSettings.showPossibleMoves)
            gameSettings.showPossibleMoves = showPossibleMovesChange

        if (profile.name != writtenName)
            profile.name = writtenName

        if (bitmapDrawable != null)
            profile.icon = bitmapDrawable

        app.saveProfile(profile, bitmapDrawable != null)
        app.tempProfile = null
        finish()
    }

    private fun removeTempImgFile() {
        tempImageFile!!.delete()
    }

    override fun onBackPressed() {
        confirmationToLeave()
    }

    override fun onSupportNavigateUp(): Boolean {
        confirmationToLeave()
        return true
    }

    private fun aboutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.about_layout)

        dialog.show()
    }

    private fun confirmationToLeave() {
        val writtenName = binding.etNameChange.text.toString()
        if (profile.name != writtenName || bitmapDrawable != null || showPossibleMovesChange != gameSettings.showPossibleMoves) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle(supportActionBar?.title)
                .setMessage(getString(R.string.question_leave_without_save))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> finish() }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                .setNeutralButton(getString(R.string.save_and_leave)) { _, _ -> saveProfile() }
                .setCancelable(true)
                .create()
            alertDialog.show()
        } else {
            finish()
        }
    }

}

