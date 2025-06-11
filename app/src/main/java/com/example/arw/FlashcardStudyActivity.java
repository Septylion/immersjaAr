package com.example.arw;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardStudyActivity extends AppCompatActivity {

    private TextView questionText, answerText;
    private FlashcardRepository repository;
    private List<Flashcard> flashcards;
    private int currentIndex = 0;
    private TextToSpeech tts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_study);

        questionText = findViewById(R.id.questionText);
        answerText = findViewById(R.id.answerText);
        Button speakButton = findViewById(R.id.speakButton);

        repository = new FlashcardRepository(this);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pl", "PL"));
            }
        });

        executor.execute(() -> {
            flashcards = repository.getDueFlashcards();
            runOnUiThread(this::showNextFlashcard);
        });

        findViewById(R.id.knowButton).setOnClickListener(v -> handleAnswer(0));
        findViewById(R.id.maybeButton).setOnClickListener(v -> handleAnswer(1));
        findViewById(R.id.dontKnowButton).setOnClickListener(v -> handleAnswer(2));

        speakButton.setOnClickListener(v -> {
            if (tts != null && answerText != null) {
                String text = answerText.getText().toString();
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        prevButton.setOnClickListener(v -> showPreviousFlashcard());
        nextButton.setOnClickListener(v -> {
            currentIndex++;
            showNextFlashcard();
        });

    }


    private void showPreviousFlashcard() {
        if (flashcards == null || flashcards.isEmpty() || currentIndex <= 0) return;

        currentIndex--;
        showNextFlashcard();
    }

    private void showNextFlashcard() {
        if (flashcards == null || flashcards.isEmpty() || currentIndex >= flashcards.size()) {
            questionText.setText("Koniec fiszek");
            answerText.setText("");
            return;
        }
        Flashcard card = flashcards.get(currentIndex);
        questionText.setText(card.word);
        answerText.setText(card.translation);
    }

    private void handleAnswer(int difficulty) {
        if (flashcards == null || flashcards.isEmpty() || currentIndex >= flashcards.size()) return;

        Flashcard card = flashcards.get(currentIndex);
        long now = System.currentTimeMillis();
        long interval;

        switch (difficulty) {
            case 0: interval = 1000L * 60 * 60 * 24 * 5; break;
            case 1: interval = 1000L * 60 * 60 * 24 * 2; break;
            default: interval = 5000; break;
        }

        card.nextReviewDate = now + interval;
        card.difficultyLevel = difficulty;
        repository.update(card);


        flashcards.remove(currentIndex);


        if (flashcards.isEmpty()) {
            currentIndex = 0;
            questionText.setText("Koniec fiszek");
            answerText.setText("");
            return;
        }


        if (currentIndex >= flashcards.size()) {
            currentIndex = flashcards.size() - 1;
        }

        showNextFlashcard();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        executor.shutdown();
    }
}
