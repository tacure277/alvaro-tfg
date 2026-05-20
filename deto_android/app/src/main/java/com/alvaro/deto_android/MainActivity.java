package com.alvaro.deto_android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.alvaro.deto_android.fragments.BuscarUsuariosFragment;
import com.alvaro.deto_android.fragments.HomeFragment;
import com.alvaro.deto_android.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav =
                findViewById(R.id.bottom_navigation);

        String selectedFragment =
                getIntent().getStringExtra("fragment");

        Fragment fragment;

        if ("perfil".equals(selectedFragment)) {

            fragment = new ProfileFragment();
            bottomNav.setSelectedItemId(R.id.nav_perfil);

        } else if ("buscar".equals(selectedFragment)) {

            fragment = new BuscarUsuariosFragment();
            bottomNav.setSelectedItemId(R.id.nav_buscar);

        } else {

            fragment = new HomeFragment();
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment selected = null;

            if (item.getItemId() == R.id.nav_home) {

                selected = new HomeFragment();

            } else if (item.getItemId() == R.id.nav_perfil) {

                selected = new ProfileFragment();

            } else if (item.getItemId() == R.id.nav_buscar) {

                selected = new BuscarUsuariosFragment();
            }

            if (selected != null) {

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }

            return true;
        });
    }
}