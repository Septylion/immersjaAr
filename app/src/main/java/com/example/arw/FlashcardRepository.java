package com.example.arw;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FlashcardRepository {
    private final FlashcardDao flashcardDao;

    public FlashcardRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        flashcardDao = db.flashcardDao();
    }

    public List<Flashcard> getDueFlashcards() {
        return flashcardDao.getDueFlashcards(System.currentTimeMillis());
    }

    public void update(Flashcard flashcard) {
        Executors.newSingleThreadExecutor().execute(() -> flashcardDao.update(flashcard));
    }

    public void insert(Flashcard flashcard) {
        Executors.newSingleThreadExecutor().execute(() -> flashcardDao.insert(flashcard));
    }

    public Flashcard getFlashcardByWord(String word) {
        return flashcardDao.getFlashcardByWord(word);
    }
}
