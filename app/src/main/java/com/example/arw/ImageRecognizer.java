package com.example.arw;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class ImageRecognizer implements ImageAnalysis.Analyzer {
    public interface OnLabelRecognizedListener {
        void onLabelRecognized(String label);
    }

    private final OnLabelRecognizedListener listener;
    private final ImageLabeler labeler;
    private long lastAnalyzedTime = 0;

    public ImageRecognizer(Context context, OnLabelRecognizedListener listener) {
        this.listener = listener;
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnalyzedTime < 3000) {  // opóźnienie 3 sekundy
            imageProxy.close();
            return;
        }
        lastAnalyzedTime = currentTime;

        @SuppressWarnings("UnsafeOptInUsageError")
        Bitmap bitmap = imageProxy.toBitmap();
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    if (!labels.isEmpty()) {
                        String bestLabel = labels.get(0).getText();
                        listener.onLabelRecognized(bestLabel);
                    } else {
                        listener.onLabelRecognized("Brak rozpoznania");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ImageRecognizer", "MLKit recognition failed", e);
                    listener.onLabelRecognized("Błąd rozpoznania");
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }
}
