package pt.isec.multiplayerreversi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.databinding.ActivityHistoryBinding
import pt.isec.multiplayerreversi.game.logic.GameEndStats

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHistoryBinding
    private lateinit var app: App
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as App

        db = Firebase.firestore

        //TODO ir fazer o layout + fazer aparecer

    }


}