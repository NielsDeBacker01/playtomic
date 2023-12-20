package ap.edu.play2mic

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        else
        {
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController
            bottomNavigationView.setupWithNavController(navController)

            navController.navigate(R.id.firstFragment)

            bottomNavigationView.setOnNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.firstFragment -> setCurrentFragment(SearchFragment())
                    R.id.allMatchesFragment -> setCurrentFragment(AllMatchesFragment())
                    R.id.myMatchesFragment -> setCurrentFragment(MyMatchesFragment())
                    R.id.secondFragment -> setCurrentFragment(ProfileFragment())
                }
                true
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment)
            commit()
        }
}