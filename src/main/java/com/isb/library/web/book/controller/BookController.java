package com.isb.library.web.book.controller;
import com.isb.library.web.book.dao.CatalogueRepository;
import com.isb.library.web.book.dao.StudentRepository;
import com.isb.library.web.book.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.isb.library.web.book.entity.Book;
import com.isb.library.web.book.dao.BookRepository;
import com.isb.library.web.book.entity.Catalogue;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.isb.library.web.book.controller.bookImport.extractData;

@Controller
@RequestMapping("/")
public class BookController {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CatalogueRepository catalogueRepository;

    @Autowired
    private StudentRepository studentRepository;


    @GetMapping({"/list"})
    public ModelAndView getAllBooks(){
        ModelAndView mav = new ModelAndView("list-books");
        mav.addObject("books", bookRepository.findAll());
        return mav;
    }


    @GetMapping({"/"})
    public String redirectHome(){
        return "redirect:/catalogue";
    }

    @GetMapping({"/catalogue"})
    public ModelAndView getCatalogue(){
        ModelAndView mav = new ModelAndView("book-catalogue");
        mav.addObject("catalogue", catalogueRepository.findAll());
        return mav;
    }

    @GetMapping({"/deleteAll"})
    public String deleteAllBooks() throws SQLException {
        bookRepository.deleteAll();
        bookImport.resetIncrement();
        return"redirect:/";
    }




    @PostMapping({"/saveBook"})
    public String saveBook(@ModelAttribute Book book) {
        bookRepository.save(book);
        return "redirect:/list";
    }

    @PostMapping("/saveCatalogue")
    public String updateCatalogue(@ModelAttribute Catalogue catalogue){
        int id = 1;
        if(!catalogue.getId().equals("")) {
            id = Integer.valueOf(catalogue.getId());

        }
        else {
            List<Catalogue> catalogue1 = catalogueRepository.findAll();
            int size = catalogue1.size();
            if(size != 0) {
                id = Integer.parseInt(catalogue1.get(size - 1).getId()) + 1;
            }

        }



        int quantity = catalogue.getQuantity();

        List<Book> bookList = bookRepository.findAll();
        int currentNum = 0;
        int finalCopyNumber = 0;
        if(quantity !=0) {
            ArrayList<Book> finalBookList = new ArrayList<>();
            for (Book book : bookList) {
                if (book.getCatalogue_number() == id) {
                    currentNum++;
                    finalBookList.add(book);
                    finalCopyNumber = Integer.parseInt(finalBookList.get(currentNum - 1).getCopy_number().substring(2));
                }
            }
        }


        if(currentNum < quantity){
            for (int i = 0; i < quantity-currentNum; i++) {
                Book book = new Book();
                book.setLastName(catalogue.getLastName());
                book.setFirstName(catalogue.getFirstName());
                book.setName(catalogue.getName());
                int bookNum = finalCopyNumber+1+i;
                book.setCopy_number(id + "-" + bookNum);
                book.setDate(catalogue.getDate());
                book.setGenre(catalogue.getGenre());
                book.setLanguage(catalogue.getLanguage());
                book.setCatalogue_number(Integer.valueOf(id));
                bookRepository.save(book);
            }
        }

        catalogueRepository.save(catalogue);
       return "redirect:/catalogue";
    }

    @GetMapping("/addBookForm")
    public ModelAndView addBookForm() {
        ModelAndView mav = new ModelAndView("add-book-form");
        Book newBook = new Book();
        mav.addObject("book", newBook);
        return mav;
    }
    @GetMapping("/showUpdateForm")
    public ModelAndView showUpdateForm(@RequestParam String bookId) {
        ModelAndView mav = new ModelAndView("add-book-form");
        Book book = bookRepository.findById(bookId).get();
        mav.addObject("book", book);
        return mav;
    }

    @GetMapping("/addCatalogueForm")
    public ModelAndView addCatalogueForm() {
        ModelAndView mav = new ModelAndView("add-book-catalogue-form");
        Catalogue newCatalogue = new Catalogue();
        mav.addObject("catalogue", newCatalogue);
        return mav;
    }

    @GetMapping("/showUpdateCatalogueForm")
    public ModelAndView addToCatalogue(@RequestParam String catalogueId){
        ModelAndView mav = new ModelAndView("add-book-catalogue-form");
        Catalogue catalogue = catalogueRepository.findById(catalogueId).get();
        mav.addObject("catalogue", catalogue);
        return mav;

    }

    @GetMapping("/openBook")
    public String openBook(@RequestParam String bookId) {
        Book book = bookRepository.findById(bookId).get();


        return "hello";
    }

    @GetMapping("/deleteBook")
    public String deleteBook(@RequestParam String bookId) {
        Book book = bookRepository.findById(bookId).get();

        Catalogue catalogue = catalogueRepository.findById(String.valueOf(book.getCatalogue_number())).get();
        catalogue.setQuantity(catalogue.getQuantity()-1);

        bookRepository.deleteById(bookId);
        return "redirect:/list";
    }

    @GetMapping("/deleteInCatalogue")
    public String deleteBookInCatalogue(@RequestParam String catalogueId){
        catalogueRepository.deleteById(catalogueId);
        List<Book> bookList = bookRepository.findAll();
        for(Book book: bookList) {
            if (book.getCatalogue_number() == Integer.valueOf(catalogueId)){
                bookRepository.deleteById(book.getId());
            }
        }
        return "redirect:/";
    }

    @GetMapping("/showStudentsForm")
    public ModelAndView importStudentsForm() {


        ModelAndView mav = new ModelAndView("import-students-form");


        return mav;
    }

    @GetMapping("/saveTesting")
    public String saveTesting() {
        ArrayList<String> titleData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Titles.xlsx");
        ArrayList<String> lastNameData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Last Name.xlsx");
        ArrayList<String> firstNameData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\First Name.xlsx");
        ArrayList<String> genreData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Genre.xlsx");
        for(int i = 0; i< titleData.size(); i++){
            Book temp = new Book();
            temp.setName(titleData.get(i));
            temp.setLastName(lastNameData.get(i));
            temp.setGenre(genreData.get(i));
            bookRepository.save(temp);
        }


        return"redirect:/catalogue";
    }

    @GetMapping("/saveStudents")
    public String saveStudents(@RequestParam("file")MultipartFile file, RedirectAttributes redirectAttributes) throws SQLException, IOException {
        if(file.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Please select file to upload.");
            return "redirect:/importStudentsForm";
        }

        Path path = Path.of(String.valueOf(file));

        studentRepository.deleteAll();

        ArrayList<ArrayList<String>> masterArrayList= studentImport.studentImport(String.valueOf(path));
        ArrayList<String> ninth= masterArrayList.get(0);
        ArrayList<String> tenth= masterArrayList.get(1);
        ArrayList<String> eleventh= masterArrayList.get(2);
        ArrayList<String> twelfth= masterArrayList.get(3);

        studentImport.resetIncrement();

        for (String student : ninth) {
            Student tempStudent = new Student();
            tempStudent.setName(student);
            tempStudent.setGrade(9);
            studentRepository.save(tempStudent);
        }
        for (String student : tenth) {
            Student tempStudent = new Student();
            tempStudent.setName(student);
            tempStudent.setGrade(10);
            studentRepository.save(tempStudent);
        }
        for (String student : eleventh) {
            Student tempStudent = new Student();
            tempStudent.setName(student);
            tempStudent.setGrade(11);
            studentRepository.save(tempStudent);
        }
        for (String student : twelfth) {
            Student tempStudent = new Student();
            tempStudent.setName(student);
            tempStudent.setGrade(12);
            studentRepository.save(tempStudent);
        }
        return "redirect:/catalogue";
    }

}
