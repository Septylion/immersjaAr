package com.example.arw;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Query("SELECT * FROM flashcard WHERE nextReviewDate <= :currentTime")
    List<Flashcard> getDueFlashcards(long currentTime);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Query("SELECT * FROM flashcard")
    List<Flashcard> getAll();

    @Query("SELECT * FROM flashcard WHERE word = :word LIMIT 1")
    Flashcard getFlashcardByWord(String word);

}


