package com.example.arw;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String word; // polskie slowo
    public String translation; // obcy jezyk
    public long nextReviewDate; // timestamp millis
    public int difficultyLevel; // 0 = łatwe, 1 = średnie, 2 = trudne



    public Flashcard( String word, String translation) {

        this.word = word;
        this.translation = translation;

    }

    public Flashcard() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public long getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(long nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}