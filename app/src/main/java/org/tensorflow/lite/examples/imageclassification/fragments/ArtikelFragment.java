package org.tensorflow.lite.examples.imageclassification.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.examples.imageclassification.databinding.FragmentArtikelBinding;

public class ArtikelFragment extends Fragment {
    FragmentArtikelBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //WebView webView = new WebView(getContext());
        //setContentView(webView);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArtikelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.loadUrl("https://coffebeans1.wordpress.com/2023/06/27/perbedaan-antara-light-medium-dan-dark-roast-pada-kopi/ ");
        binding.webview.setWebViewClient(new WebViewClient());
    }
}
