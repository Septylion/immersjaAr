package com.example.arw;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TranslatorHelper {
    private final Translator translator;

    public interface TranslationCallback {
        void onTranslated(String translatedText);
    }

    public TranslatorHelper(Context context) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.POLISH)
                .build();

        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> Log.d("TranslatorHelper", "Model gotowy."))
                .addOnFailureListener(e -> Log.e("TranslatorHelper", "Błąd pobierania modelu", e));
    }

    public void translate(String text, TranslationCallback callback) {
        String cleaned = text.toLowerCase().trim();
        if (cleaned.contains(" ")) {
            cleaned = cleaned.split(" ")[0];
        }
        translator.translate(cleaned)
                .addOnSuccessListener(callback::onTranslated)
                .addOnFailureListener(e -> {
                    Log.e("TranslatorHelper", "Błąd tłumaczenia", e);
                    callback.onTranslated("(błąd tłumaczenia)");
                });
    }
}