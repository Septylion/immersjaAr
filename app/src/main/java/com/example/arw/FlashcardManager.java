package com.example.arw;

import android.content.Context;
import java.util.List;

public class FlashcardManager {
    private FlashcardRepository repository;

    public FlashcardManager(Context context) {
        repository = new FlashcardRepository(context);
    }

    public void addFlashcard(String word, String translation) {
        Flashcard flashcard = new Flashcard(word, translation);
        repository.insert(flashcard);
    }

    public List<Flashcard> getDueFlashcards() {
        return repository.getDueFlashcards();
    }

    public void markFlashcardAsKnown(Flashcard flashcard) {
        long newReviewTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // +1 day
        flashcard.setNextReviewDate(newReviewTime);
        repository.update(flashcard);
    }

    public void markFlashcardAsUnknown(Flashcard flashcard) {
        long newReviewTime = System.currentTimeMillis() + (5 * 60 * 1000); // +5 minutes
        flashcard.setNextReviewDate(newReviewTime);
        repository.update(flashcard);
    }
}
