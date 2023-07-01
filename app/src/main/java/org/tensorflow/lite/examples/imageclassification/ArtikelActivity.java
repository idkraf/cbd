package org.tensorflow.lite.examples.imageclassification;

import android.os.Bundle;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.examples.imageclassification.databinding.ActivityArtikelBinding;

public class ArtikelActivity extends AppCompatActivity {
    ActivityArtikelBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtikelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Article");
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.loadUrl("https://coffebeans1.wordpress.com/2023/06/27/perbedaan-antara-light-medium-dan-dark-roast-pada-kopi/ ");
        binding.webview.setWebViewClient(new WebViewClient());
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
