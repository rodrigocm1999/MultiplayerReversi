package pt.isec.multiplayerreversi.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityEditProfileBinding
import pt.isec.multiplayerreversi.game.logic.Profile
import pt.isec.multiplayerreversi.game.utils.ImageUtils
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private var permissionsGranted = false
    private lateinit var profile : Profile
    private var changedImage = false;
    private var newImagePath : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //TODO guardar o perfil
        //TODO ESCOLHER FOTO
        //binding.etNameChange.hint = profile.name

        binding.imgBtnProfileChange.setOnClickListener{
            val imageFile = File.createTempFile(
                "img",
                ".img",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            newImagePath = imageFile.absolutePath
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                val fileUri = FileProvider.getUriForFile(
                    this@EditProfileActivity,
                    "pt.isec.multiplayerreversi.android.fileprovider",
                    imageFile
                )
                putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            }
            startActivityForResultFoto.launch(intent)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQ_PERM_CODE
            )
        } else
            permissionsGranted = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.mnSave)?.isVisible = permissionsGranted
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Companion.REQ_PERM_CODE) {
            permissionsGranted =
                (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED)
            binding.imgBtnProfileChange.isEnabled = permissionsGranted
        }
    }

    var startActivityForResultFoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            newImagePath = null
        }
        ImageUtils.setPic(binding.imgBtnProfileChange, newImagePath!!)
    }

    private fun checkChangesMade() : Boolean{
        if (profile.name != binding.etNameChange.text.toString()){
            return false
        }else if (changedImage){
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mnSave){
            if (!checkChangesMade()) finish()
            else{
                profile.name = binding.etNameChange.text.toString()
                profile.imagePath = newImagePath
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQ_PERM_CODE = 1234
    }

}