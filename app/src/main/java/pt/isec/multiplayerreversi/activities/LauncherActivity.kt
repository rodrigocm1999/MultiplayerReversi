package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityLaucherBinding

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaucherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaucherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notYetImplementedToast = View.OnClickListener {
            Toast.makeText(this, R.string.notYetImplemented, Toast.LENGTH_SHORT).show()
        }
        binding.btnLocal1v1.setOnClickListener(notYetImplementedToast)
        binding.btnRemote1v1.setOnClickListener(notYetImplementedToast)
        binding.btnRemote1v1v1.setOnClickListener(notYetImplementedToast)
        binding.avatarIcon.setOnClickListener(notYetImplementedToast)

        //TODO mudar drawable para icon do utilizador
        //TODO fazer logo da pixa
    }

}