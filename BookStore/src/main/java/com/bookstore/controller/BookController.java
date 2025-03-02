package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.entity.User;
import com.bookstore.services.BookServices;
import com.bookstore.services.CategoryServices;
import com.bookstore.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookServices bookService;

    @Autowired
    private CategoryServices categoryService;

    @Autowired
    private UserServices userServices;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String showAllBooks(Model model) {
        List<Book> books = bookService.getALlBooks();
        model.addAttribute("books", books);
        return "book/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String addBook(@Valid @ModelAttribute("book") Book book, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User user = userServices.findByUsername(currentUsername);
        if (user == null) {
            result.rejectValue("user", "error.book", "Người dùng không hợp lệ");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }

        book.setUser(user);
        bookService.addBook(book);
        return "redirect:/books";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String editBookForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book != null) {
            model.addAttribute("book", book);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        return "redirect:/books";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String updateBook(@PathVariable("id") Long id, @Valid @ModelAttribute("book") Book book, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        bookService.updateBook(book);
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }
}
