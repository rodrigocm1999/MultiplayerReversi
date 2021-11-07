package pt.isec.multiplayerreversi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityEditProfileBinding
import pt.isec.multiplayerreversi.databinding.ActivityLaucherBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notYetImplementedToast = View.OnClickListener {
            Toast.makeText(this, R.string.notYetImplemented, Toast.LENGTH_SHORT).show()
        }
        binding.btnSaveChanges.setOnClickListener(notYetImplementedToast)
    }
}