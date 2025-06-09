package com.example.arw;

import android.content.Context;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ImageRecognizer {
    private final Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ObjectDetector detector;
    private final ImageLabeler fallbackLabeler;
    private final RecognitionCallback callback;
    private long lastRecognitionTime = 0;

    public interface RecognitionCallback {
        void onObjectRecognized(String label);
    }

    public ImageRecognizer(Context context, RecognitionCallback callback) {
        this.context = context;
        this.callback = callback;

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        detector = ObjectDetection.getClient(options);
        fallbackLabeler = com.google.mlkit.vision.label.ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
    }

    public Executor getExecutor() {
        return executor;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public ImageAnalysis.Analyzer getAnalyzer() {
        return imageProxy -> {
            long now = System.currentTimeMillis();
            if (now - lastRecognitionTime < 3000) {
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

                detector.process(inputImage)
                        .addOnSuccessListener(results -> {
                            boolean recognized = false;
                            for (DetectedObject obj : results) {
                                if (!obj.getLabels().isEmpty()) {
                                    lastRecognitionTime = System.currentTimeMillis();
                                    String label = obj.getLabels().get(0).getText();
                                    Log.d("ImageRecognizer", "ObjectDetector: " + label);
                                    callback.onObjectRecognized(label);
                                    recognized = true;
                                    break;
                                }
                            }

                            if (!recognized) {
                                fallbackLabeler.process(inputImage)
                                        .addOnSuccessListener(fallbackLabels -> {
                                            if (!fallbackLabels.isEmpty()) {
                                                lastRecognitionTime = System.currentTimeMillis();
                                                String label = fallbackLabels.get(0).getText();
                                                Log.d("ImageRecognizer", "FallbackLabeler: " + label);
                                                callback.onObjectRecognized(label);
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("ImageRecognizer", "Błąd fallback labelera", e))
                                        .addOnCompleteListener(t -> imageProxy.close());
                            } else {
                                imageProxy.close();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ImageRecognizer", "Błąd ObjectDetector", e);
                            imageProxy.close();
                        });

            } catch (Exception e) {
                Log.e("ImageRecognizer", "Błąd InputImage", e);
                imageProxy.close();
            }
        };
    }
}