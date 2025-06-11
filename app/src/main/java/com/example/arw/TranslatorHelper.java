package com.example.arw;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.Translation;

public class TranslatorHelper {
    private final Translator translator;

    public TranslatorHelper(Context context) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH)
                .setTargetLanguage(com.google.mlkit.nl.translate.TranslateLanguage.POLISH)
                .build();

        translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> Log.d("TranslatorHelper", "Model downloaded"))
                .addOnFailureListener(e -> Log.e("TranslatorHelper", "Model download failed", e));
    }

    public void translate(String text, OnTranslationCompleteListener listener) {
        translator.translate(text)
                .addOnSuccessListener(listener::onTranslated)
                .addOnFailureListener(e -> {
                    Log.e("TranslatorHelper", "Translation failed", e);
                    listener.onTranslated("");
                });
    }

    public void close() {
        translator.close();
    }

    public interface OnTranslationCompleteListener {
        void onTranslated(String translatedText);
    }
}