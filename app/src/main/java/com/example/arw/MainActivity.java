
package com.example.arw;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private PreviewView previewView;
    private TextView wordText;
    private TextView translationText;
    private TextToSpeech tts;
    private TranslatorHelper translatorHelper;
    private ImageRecognizer imageRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        wordText = findViewById(R.id.wordText);
        translationText = findViewById(R.id.translationText);
        Button saveButton = findViewById(R.id.saveButton);
        Button studyButton = findViewById(R.id.studyButton);

        translatorHelper = new TranslatorHelper(this);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("pl", "PL"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });

        imageRecognizer = new ImageRecognizer(this, label -> {
            runOnUiThread(() -> {
                wordText.setText(label);
                translatorHelper.translate(label, translated -> {
                    translationText.setText(translated);

                    if (tts != null) {
                        tts.speak(translated, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
            });
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        saveButton.setOnClickListener(v -> {
            String word = wordText.getText().toString().trim();
            String translation = translationText.getText().toString().trim();
            if (!word.isEmpty() && !translation.isEmpty()) {
                FlashcardRepository repository = new FlashcardRepository(this);


                Executors.newSingleThreadExecutor().execute(() -> {
                    Flashcard existing = repository.getFlashcardByWord(word);
                    if (existing == null) {
                        Flashcard flashcard = new Flashcard();
                        flashcard.word = word;
                        flashcard.translation = translation;
                        flashcard.nextReviewDate = System.currentTimeMillis();
                        flashcard.difficultyLevel = 2;
                        repository.insert(flashcard);

                        runOnUiThread(() -> Toast.makeText(this, "Fiszka zapisana", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Taka fiszka już istnieje", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });


        studyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FlashcardStudyActivity.class);
            startActivity(intent);
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();


                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        image -> {
                            new Handler().postDelayed(() -> {
                                imageRecognizer.analyze(image);
                            }, 3000); // 3 sekundy opóźnienia
                        });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}