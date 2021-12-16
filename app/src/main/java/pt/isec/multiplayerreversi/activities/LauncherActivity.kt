package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityLauncherBinding
import pt.isec.multiplayerreversi.game.interactors.LocalPlayer
import kotlin.concurrent.thread
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.RC_SIGN_IN
import pt.isec.multiplayerreversi.game.logic.*

import java.net.URL

import java.io.IOException

import java.io.BufferedInputStream


class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private lateinit var app: App
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        app = application as App

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        setListeners()
    }

    private fun setListeners() {
        binding.avatarIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        binding.btnLocal.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)

            val players = ArrayList<Player>(2)
            players.add(Player(Profile(resources.getString(R.string.dark_piece)), Piece.Dark))
            players.add(Player(Profile(resources.getString(R.string.light_piece)), Piece.Light))

            val game = Game(GameData(app.sharedGamePreferences, players))
            app.game = game
            val proxy = LocalPlayer(game)
            players[0].callbacks = proxy
            app.gamePlayer = proxy
            startActivity(intent)
        }
        binding.btnRemote.setOnClickListener {
            val profile = app.getProfile()
            if (profile.name.isBlank()) {
                Toast.makeText(this, R.string.need_to_setup_user, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (!mWifi!!.isConnected) {
                Toast.makeText(this, R.string.need_wifi, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val intent = Intent(this, WaitingAreaActivity::class.java)
            startActivity(intent)
        }
        binding.signInButton?.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        // TODO history
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(OURTAG, "signInWithCredential:success")
                } else {
                    Log.w(OURTAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(OURTAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
                thread {
                    val profile = app.getProfile()
                    profile.name = account.displayName
                    profile.email = account.email
                    profile.icon = downloadImageFromUrl(account.photoUrl)
                    app.saveProfile(profile)
                    runOnUiThread {
                        binding.avatarIcon.visibility = View.VISIBLE
                        binding.signInButton!!.visibility = View.INVISIBLE

                        val intent = Intent(this, EditProfileActivity::class.java)
                        startActivity(intent)
                    }
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                binding.signInButton!!.visibility = View.VISIBLE
                binding.avatarIcon.visibility = View.INVISIBLE
                Log.w(OURTAG, "Google sign in failed", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (app.getProfile().name.isEmpty()) {
            binding.signInButton?.visibility = View.VISIBLE
            binding.signInButton?.setSize(SignInButton.SIZE_STANDARD)
            binding.avatarIcon.visibility = View.INVISIBLE
        } else {
            binding.signInButton?.visibility = View.INVISIBLE
        }
        thread {
            val profile = app.getProfile()
            runOnUiThread {
                profile.icon?.let { binding.avatarIcon.setImageDrawable(it) }
            }
        }
    }

    private fun downloadImageFromUrl(photoUrl: Uri?): BitmapDrawable? {
        if (photoUrl == null) return null
        val url: String = photoUrl.toString()
        var bitmapDrawable: BitmapDrawable? = null
        try {
            BufferedInputStream(URL(url).openStream()).use { inputStream ->
                bitmapDrawable = BitmapDrawable
                    .createFromStream(inputStream, "") as BitmapDrawable?
            }
        } catch (e: IOException) {
            Log.i(OURTAG, "", e)
        }
        return bitmapDrawable
    }
}