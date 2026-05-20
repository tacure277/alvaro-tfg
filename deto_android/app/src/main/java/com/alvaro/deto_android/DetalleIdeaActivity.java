package com.alvaro.deto_android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.alvaro.deto_android.fragments.DetalleIdea;
import com.google.android.material.appbar.MaterialToolbar;

public class DetalleIdeaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_idea);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        int ideaId = getIntent().getIntExtra("idea_id", 0);

        if (savedInstanceState == null) {
            DetalleIdea fragment = DetalleIdea.newInstance(ideaId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}