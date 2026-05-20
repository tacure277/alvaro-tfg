package com.alvaro.deto_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alvaro.deto_android.requests.LoginRequest;
import com.alvaro.deto_android.service.UsuarioService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etCorreo;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> hacerLogin());

        TextView btnRegistrarse = findViewById(R.id.btonRegistrase);
        btnRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void hacerLogin() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando...");

        LoginRequest request = new LoginRequest(correo, password);
        UsuarioService api = RetrofitClient.getUsuarioService();
        Call<ResponseBody> call = api.login(request);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar sesión");

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

                        String accessToken = jsonObject.get("access").getAsString();
                        String refreshToken = jsonObject.get("refresh").getAsString();

                        JsonObject usuario = jsonObject.getAsJsonObject("usuario");
                        int usuarioId = usuario.get("usuario_id").getAsInt();
                        String nombre = usuario.get("nombre").getAsString();
                        String correoUsuario = usuario.get("correo").getAsString();
                        String descripcion = usuario.has("descripcion") && !usuario.get("descripcion").isJsonNull()
                                ? usuario.get("descripcion").getAsString()
                                : "Sin descripción";
                        String fotoPerfilUrl = usuario.has("foto_perfil_url") && !usuario.get("foto_perfil_url").isJsonNull()
                                ? usuario.get("foto_perfil_url").getAsString()
                                : null;

                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putString("access_token", accessToken)
                                .putString("refresh_token", refreshToken)
                                .putInt("usuario_id", usuarioId)
                                .putString("nombre", nombre)
                                .putString("correo", correoUsuario)
                                .putString("descripcion", descripcion)
                                .putString("foto_perfil_url", fotoPerfilUrl)
                                .apply();

                        Toast.makeText(LoginActivity.this,
                                "¡Bienvenido " + nombre + "!",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this,
                                "Error al procesar respuesta",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Credenciales incorrectas",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar sesión");

                Toast.makeText(LoginActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}