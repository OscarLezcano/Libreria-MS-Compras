package com.bigobooks.purchases_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bigobooks.entities.book.Book;
import com.bigobooks.repository.BaseRepository;

public interface BookRepository extends BaseRepository<Book> {

    @Query("SELECT b.id FROM Book b")
    Page<Long> findAllIds(Pageable pageable);

    Optional<Book> findByTitle(String title);

    @Query("SELECT b FROM Book b JOIN b.genres bg WHERE bg.genre.id = :genreId")
    Page<Book> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors ba WHERE ba.author.id = :authorId")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("select b from Book b where lower(b.title) like lower(concat('%', :title, '%'))")
    Page<Book> searchByName(@Param("title") String title, Pageable pageable);

    @Query(value = "SELECT * FROM book WHERE is_deleted = true", nativeQuery = true)
    List<Book> findDeleted();

    @Query(value = "SELECT * FROM book", nativeQuery = true)
    List<Book> findAllIncludingDeleted();
}
