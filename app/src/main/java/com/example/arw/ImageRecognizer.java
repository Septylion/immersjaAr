package com.example.arw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ImageRecognizer {
    private final Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ImageLabeler labeler;
    private final RecognitionCallback callback;
    private long lastRecognitionTime = 0;

    public interface RecognitionCallback {
        void onObjectRecognized(String label);
    }

    public ImageRecognizer(Context context, RecognitionCallback callback) {
        this.context = context;
        this.callback = callback;
        this.labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
    }

    public Executor getExecutor() {
        return executor;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public ImageAnalysis.Analyzer getAnalyzer() {
        return imageProxy -> {
            long now = System.currentTimeMillis();
            if (now - lastRecognitionTime < 3000) { // ⏳ opóźnienie 3 sekundy
                imageProxy.close();
                return;
            }

            if (imageProxy == null || imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            try {
                InputImage inputImage = InputImage.fromMediaImage(
                        imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                labeler.process(inputImage)
                        .addOnSuccessListener(labels -> {
                            if (!labels.isEmpty()) {
                                lastRecognitionTime = System.currentTimeMillis();
                                String label = labels.get(0).getText();
                                Log.d("ImageRecognizer", "Rozpoznano: " + label);
                                callback.onObjectRecognized(label);
                            }
                        })
                        .addOnFailureListener(e -> Log.e("ImageRecognizer", "Błąd ML Kit", e))
                        .addOnCompleteListener(task -> imageProxy.close());
            } catch (Exception e) {
                Log.e("ImageRecognizer", "Błąd InputImage", e);
                imageProxy.close();
            }
        };
    }
}
