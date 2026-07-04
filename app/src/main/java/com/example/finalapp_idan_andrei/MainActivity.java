package com.example.finalapp_idan_andrei;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.finalapp_idan_andrei.databinding.ActivityMainBinding;

/**
 * The app's single Activity. It just hosts the Navigation graph (see nav_graph.xml) and
 * wires the bottom navigation bar to it - all real screens are Fragments
 * (SpeedTestFragment, HistoryFragment, SettingsFragment) swapped in and out of the
 * NavHostFragment defined in activity_main.xml.
 */
public class MainActivity extends AppCompatActivity {

    // View Binding: auto-generated from activity_main.xml, gives typed access to its views
    // (binding.main, binding.navHostFragment, binding.bottomNavigationView) without findViewById.
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // let the app draw behind the status/nav bars

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Since content can now draw under the system bars, manually pad the root view
        // so it isn't hidden behind the status bar / gesture nav bar.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Grab the NavController that the NavHostFragment (declared in activity_main.xml) is
        // managing, then hand it to the bottom nav bar so tapping a tab navigates to the
        // matching destination in nav_graph.xml (item ids match destination ids).
        FragmentContainerView navHostContainer = binding.navHostFragment;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(navHostContainer.getId());
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
    }
}
