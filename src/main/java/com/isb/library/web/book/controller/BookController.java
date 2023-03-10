package com.isb.library.web.book.controller;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.Mode;
import com.isb.library.web.book.dao.CatalogueRepository;
import com.isb.library.web.book.dao.GenreRepository;
import com.isb.library.web.book.dao.StudentRepository;
import com.isb.library.web.book.entity.*;


import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.isb.library.web.user.dao.UserRepository;
import com.isb.library.web.user.entity.User;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.isb.library.web.book.dao.BookRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.isb.library.web.book.controller.bookImport.extractData;
import static org.krysalis.barcode4j.output.bitmap.BitmapBuilder.saveImage;

/**
 * The type Book controller.
 */
@Controller
@RequestMapping("/")
public class BookController {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CatalogueRepository catalogueRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * List page for a list of all the individual books
     *
     * @return Returns a ModelAndView object populated with the books from 'bookRepository' and the students from 'studentRepository' and they are added to the 'list-books.html' file
     */
    @GetMapping({"/list"})
    public ModelAndView getAllBooks() {
        ModelAndView mav = new ModelAndView("list-books");
        Checkout checkout = new Checkout();
        checkout.setBooks(bookRepository.findAll());
        checkout.setStudents(studentRepository.findAll());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        mav.addObject("user", user);
        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * Simple redirect to the catalogue page
     *
     * @return Returns a string to redirect
     */
    @GetMapping({"/"})
    public String redirectHome() {
        return "redirect:/catalogue";
    }

    /**
     * Catalogue page
     *
     * @return Returns an ModelAndView object that is populated with all the items retrieved from 'catalogueRepository'
     */
    @GetMapping({"/catalogue"})
    public ModelAndView getCatalogue() {
        ModelAndView mav = new ModelAndView("book-catalogue");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        mav.addObject("user", user);
        mav.addObject("catalogue", catalogueRepository.findAll());
        return mav;
    }

    /**
     * Method responsible for deleting all the books in the book list and reseting the ID increment
     *
     * @return Redirects to the catalogue page
     * @throws SQLException the sql exception
     */
    @GetMapping({"/deleteAll"})
    public String deleteAllBooks() throws SQLException {
        bookRepository.deleteAll();
        bookImport.resetIncrement();
        return "redirect:/";
    }


    /**
     * Saves either a new book or updates to a book
     *
     * @param book    Book to be saved
     * @param student Student who checks out the book
     * @return Returns a redirect to the book list page
     */
    @PostMapping({"/saveBook"})
    public String saveBook(@ModelAttribute Checkout checkout) {
        Book book = checkout.getBook();


        bookRepository.save(book);

        Catalogue catalogue = catalogueRepository.findById(String.valueOf(book.getCatalogue_number())).get();
        int tempQuantity = 0;
        int tempQuantityAvailable = 0;
        for(Book b : bookRepository.findAll()){
            if (Integer.valueOf(catalogue.getId()) == b.getCatalogue_number()){
                tempQuantity++;
                if(b.getCheckedOut() == 0){
                    tempQuantityAvailable++;
                }
            }
        }

        catalogue.setQuantity(tempQuantity);
        catalogue.setQuantity_available(tempQuantityAvailable);
        catalogueRepository.save(catalogue);

        return "redirect:/catalogue";
    }

    /**
     * Either adds a new catalogue object or saves updates to a catalogue item
     *
     * @param checkout checkout object containing the catalogue object to be saved
     * @return Returns a redirect to the catalogue page
     */
    @PostMapping("/saveCatalogue")
    public String updateCatalogue(@ModelAttribute Checkout checkout) {
        Catalogue catalogue = checkout.getCatalogue();
        boolean newBook = true;
        int id = 1;
        int quantity = catalogue.getQuantity();
        catalogue.setQuantity_available(quantity);

        //Checks to see if the catalogue is being updated or saved
        if (!catalogue.getId().equals("")) {
            catalogueRepository.save(catalogue);
            id = Integer.valueOf(catalogue.getId());

        }
        else{
            //catalogue is saved
            catalogueRepository.save(catalogue);
            List<Catalogue> catalogues = catalogueRepository.findAll();
            catalogue = catalogues.get(catalogues.size()-1);
            id = Integer.parseInt(catalogue.getId());
        }


        //Checks to see if there are books in the book list whos catalogue number matches the catalogue id and and adds them to a list
        List<Book> bookList = bookRepository.findAll();
        int currentNum = 0;
        int finalCopyNumber = 0;
        if (quantity != 0) {
            ArrayList<Book> finalBookList = new ArrayList<>();
            for (Book book : bookList) {
                if (book.getCatalogue_number() == id) {
                    currentNum++;
                    finalBookList.add(book);
                    try{
                        String[] arr = finalBookList.get(currentNum - 1).getCopy_number().split("-");
                        finalCopyNumber = Integer.parseInt(arr[1]);
                    }
                    catch(Exception e){

                    }


                }
            }
        }

        //Finds the number of books currently in the catalogue and sees if the catalogue object's quantity was updated
        //if the quantity was updated the number of books that are in the difference are added to the book list
        if (currentNum < quantity) {
            for (int i = 0; i < quantity - currentNum; i++) {
                Book book = new Book();
                book.setLastName(catalogue.getLastName());
                book.setFirstName(catalogue.getFirstName());
                book.setName(catalogue.getName());
                int bookNum = finalCopyNumber + 1 + i;
                book.setCopy_number(id + "-" + bookNum);
                book.setGenre(catalogue.getGenre());
                book.setCatalogue_number(Integer.valueOf(id));
                bookRepository.save(book);
            }
        }

        List<Book> books = new ArrayList<>();
        for(Book b : bookRepository.findAll()){
            if(b.getCatalogue_number() == id){
                books.add(b);
            }
        }

        if(books.get(0).getName() != catalogue.getName()){
            for(Book b : books){
                b.setName(catalogue.getName());
                bookRepository.save(b);
            }
        }
        if(books.get(0).getLastName() != catalogue.getLastName()){
            for(Book b : books){
                b.setLastName(catalogue.getLastName());
                bookRepository.save(b);
            }
        }
        if(books.get(0).getFirstName() != catalogue.getFirstName()){
            for(Book b : books){
                b.setFirstName(catalogue.getFirstName());
                bookRepository.save(b);
            }
        }
        if(books.get(0).getLastName() != catalogue.getLastName()){
            for(Book b : books){
                b.setLastName(catalogue.getLastName());
                bookRepository.save(b);
            }
        }
        if(books.get(0).getGenre() != catalogue.getGenre()){
            for(Book b : books){
                b.setGenre(catalogue.getGenre());
                bookRepository.save(b);
            }
        }
        if(books.get(0).getLastName() != catalogue.getLastName()){
            for(Book b : books){
                b.setLastName(catalogue.getLastName());
                bookRepository.save(b);
            }
        }


        return "redirect:/catalogue";
    }

    /**
     * Finds a list of books with a matching catalogue ID and returns a narrowed down book list view showing only those books
     *
     * @param catalogueNumber Catalogue number of the books ot be searched by
     * @return Returns a ModelAndView object of the html file "books-with-title" with a list of all the books
     */
    @GetMapping("/booksWithTitle")
    public ModelAndView booksWithTitle(@RequestParam int catalogueNumber) {
        ModelAndView mav = new ModelAndView("books-with-title");

        Checkout checkout = new Checkout();
        List<Book> tempBooks = bookRepository.findAll();
        List<Student> students = studentRepository.findAll();
        ArrayList<Book> books = new ArrayList<>();
        for (Book book : tempBooks) {
            if (book.getCatalogue_number() == catalogueNumber) {
                books.add(book);
            }
        }
        checkout.setBooks(books);
        checkout.setStudents(students);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        mav.addObject("user", user);
        mav.addObject("checkout", checkout);

        return mav;
    }

    /**
     * Finds student from a given ID
     * @param studentId id of the student to be found
     * @return Returns the name of the student with the matching ID
     */
    @GetMapping("/findStudent")
    public String findStudent(@RequestParam String studentId) {
        Student student = studentRepository.findById(studentId).get();

        return student.getName();
    }

    /**
     * Shows a form where the user can add a new book to the books list
     *
     * @return Returns a ModelAndView object of the "add-book-form" html file with a list of all the students passed
     * to it and a new book object passed to it as well
     */
    @GetMapping("/addBookForm")
    public ModelAndView addBookForm() {
        ModelAndView mav = new ModelAndView("add-book-form");
        Book newBook = new Book();
        List<Student> students = studentRepository.findAll();
        Checkout checkout = new Checkout();

        checkout.setGenres(genreRepository.findAll());
        checkout.setBook(newBook);
        checkout.setStudents(students);
        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * Similar to the Add Book form but instead of adding a new book it upadtes the same book
     *
     * @param bookId ID of the book to be updated
     * @return Returns a ModelAndView object of the "add-book-form" html file with a list of all the students passed
     * to it and a book object passed to it as well. These are passed to the ModelAndView through the checkout object
     * which contains them
     */
    @GetMapping("/showUpdateForm")
    public ModelAndView showUpdateForm(@RequestParam String bookId) {
        ModelAndView mav = new ModelAndView("add-book-form");

        Checkout checkout = new Checkout();
        Book book = bookRepository.findById(bookId).get();
        checkout.setBook(book);
        Student student = new Student();
        if (book.getCurrentOwner() != null) {
            String id = "";
            for(Student s : studentRepository.findAll()){
                if(s.getName().equals(book.getCurrentOwner())){
                    id = s.getId();
                }
            }
            student = studentRepository.findById(id).get();
        }
        checkout.setStudent(student);
        checkout.setStudents(studentRepository.findAll());
        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * A form to add a catalogue object
     *
     * @return Returns a ModelAndView object of the "add-book-catalogue-form" html with a new catalogue object passed to it
     */
    @GetMapping("/addCatalogueForm")
    public ModelAndView addCatalogueForm() {
        ModelAndView mav = new ModelAndView("add-book-catalogue-form");
        Catalogue newCatalogue = new Catalogue();
        Checkout checkout = new Checkout();
        checkout.setCatalogue(newCatalogue);
        checkout.setGenres(genreRepository.findAll());
        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * A form for the user to update an existing catalogue object
     *
     * @param catalogueId the catalogue id
     * @return Returns a ModelAndView object of the "add-book-catalogue-form" html with an existing catalogue object
     * passed to it the catalogue object is passed through the checkout object
     */
    @GetMapping("/showUpdateCatalogueForm")
    public ModelAndView addToCatalogue(@RequestParam String catalogueId) {
        ModelAndView mav = new ModelAndView("add-book-catalogue-form");
        Catalogue catalogue = catalogueRepository.findById(catalogueId).get();
        Checkout checkout = new Checkout();
        checkout.setCatalogue(catalogue);
        checkout.setGenres(genreRepository.findAll());
        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * Deletes a book from a given ID
     *
     * @param bookId The ID of the book to be deleted
     * @return Returns a string to redirect the user to the catalogue page
     */
    @GetMapping("/deleteBook")
    public String deleteBook(@RequestParam String bookId) {
        Book book = bookRepository.findById(bookId).get();

        Catalogue catalogue = catalogueRepository.findById(String.valueOf(book.getCatalogue_number())).get();
        catalogue.setQuantity(catalogue.getQuantity() - 1);
        if (book.getCheckedOut() == 0) {
            catalogue.setQuantity_available(catalogue.getQuantity_available() - 1);
        }
        bookRepository.deleteById(bookId);
        return "redirect:/catalogue";
    }

    /**
     * Deletes a catalogue object and all books in the book list with a matching catalogue ID number
     *
     * @param catalogueId the catalogue id
     * @return Returns a string that redirects the user to the catalogue page
     */
    @GetMapping("/deleteInCatalogue")
    public String deleteBookInCatalogue(@RequestParam String catalogueId) {
        catalogueRepository.deleteById(catalogueId);
        List<Book> bookList = bookRepository.findAll();
        for (Book book : bookList) {
            if (book.getCatalogue_number() == Integer.valueOf(catalogueId)) {
                bookRepository.deleteById(book.getId());
            }
        }
        return "redirect:/";
    }

    /**
     * Import students form model and view.
     *
     * @return the model and view
     */
    @GetMapping("/upload")
    public ModelAndView importStudentsForm() {
        ModelAndView mav = new ModelAndView("import-students-form");
        return mav;
    }

    /**
     * @deprecated
     * Temporary Method used to import initial data into the database
     * @return Redirects to the catalogue page
     */
    @GetMapping("/saveTesting")
    public String saveTesting() {
        ArrayList<String> titleData = extractData("C:\\Users\\xxx\\Desktop\\Titles.xlsx", false);
        ArrayList<String> lastNameData = extractData("C:\\Users\\xxx\\Desktop\\Last Name.xlsx", false);
        ArrayList<String> firstNameData = extractData("C:\\Users\\xxx\\Desktop\\First Name.xlsx", false);
        ArrayList<String> genreData = extractData("C:\\Users\\xxx\\Desktop\\Genre.xlsx", false);
        ArrayList<String> idData = extractData("C:\\Users\\xxx\\Desktop\\Book ID.xlsx", false);
        ArrayList<String> studentData = extractData("C:\\Users\\xxx\\Desktop\\Student Names.xlsx", true);
        ArrayList<String> teacherData = extractData("C:\\Users\\xxx\\Desktop\\Teachers.xlsx", true);


        for (int i = 0; i < titleData.size(); i++) {
            Book temp = new Book();
            temp.setTeacher(teacherData.get(i));

            temp.setName(titleData.get(i));
            temp.setLastName(lastNameData.get(i));
            temp.setFirstName(firstNameData.get(i));
            temp.setGenre(genreData.get(i));
            temp.setCopy_number(idData.get(i));
            bookRepository.save(temp);
        }


        return "redirect:/catalogue";
    }


    /**
     * @deprecated
     * Temporary method to import data into the database
     * @return Redirects user to the catalogue page
     */
    @GetMapping("/catalogueSaveTesting")
    public String catalogueSaveTesting() {
        //Methods used to initially import all the titles of books into the database and update both the quantities of books as well as the catalogue number of each book

//        ArrayList<String> titleData = extractData("C:\\Users\\xxx\\Desktop\\Catalogue Titles.xlsx");
//        for(String title: titleData){
//            Catalogue temp = new Catalogue();
//            temp.setName(title);
//            catalogueRepository.save(temp);
//        }
//        List<Book> books = bookRepository.findAll();
//        List<Catalogue> catalogue = catalogueRepository.findAll();
//
//        for(Catalogue e : catalogue){
//            for(Book book : books){
//                if(book.getName().equals(e.getName())){
//                    book.setCatalogue_number(Integer.parseInt(e.getId()));
//                    bookRepository.save(book);
//                    int quantity = e.getQuantity();
//                    e.setQuantity(quantity + 1);
//                }
//            }
//        }

        List<Catalogue> catalogue = catalogueRepository.findAll();

        ArrayList<String> lastNameData = extractData("C:\\Users\\xxx\\Desktop\\Catalogue Languages.xlsx", true);


        for (int i = 0; i < lastNameData.size(); i++) {
            catalogue.get(i).setOriginal_language(lastNameData.get(i));
            catalogueRepository.save(catalogue.get(i));
        }


        return "redirect:/catalogue";
    }


    /**
     * Method used to upload a list of students
     * @param fileUploadForm Object containing the multipart file that the user uploads
     * @param model Similar to the ModelAndView
     * @return Returns a redirect to the catalouge or opens up the original html page and displays an error
     * @throws SQLException
     * @throws IOException
     */
    @PostMapping("/upload")
    public String saveStudents(@ModelAttribute("fileUploadForm") FileUploadForm fileUploadForm, Model model) throws SQLException, IOException {

        MultipartFile file = fileUploadForm.getFile();
        if (file != null) {

            ArrayList<ArrayList<String>> masterArrayList = studentImport.studentImport(file);
            ArrayList<String> ninth = masterArrayList.get(0);
            ArrayList<String> tenth = masterArrayList.get(1);
            ArrayList<String> eleventh = masterArrayList.get(2);
            ArrayList<String> twelfth = masterArrayList.get(3);

            studentRepository.deleteAll();
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
        } else {
            model.addAttribute("error", "Please choose a file to upload");
            return "file-upload-form";
        }


    }

    /**
     * Method to open the initial checkout page
     *
     * @param bookID ID of the book to be checked out
     * @return Returns a ModelAndView object populated with the information of the book to be checked out and all
     * of the students in 'studentRepository'
     */
    @GetMapping("/checkout")
    public ModelAndView checkout(@RequestParam String bookID) {
        ModelAndView mav = new ModelAndView("checkout");
        Book book = bookRepository.findById(bookID).get();
        Checkout checkout = new Checkout();

        List<Student> students = studentRepository.findAll();
        List<User> tempUsers = userRepository.findAll();
        List<User> users = new ArrayList<>();
        for(int i = 1; i < tempUsers.size(); i++) {
            users.add(tempUsers.get(i));
        }
        Student student = new Student();

        checkout.setStudents(students);
        checkout.setBook(book);
        checkout.setStudent(student);
        checkout.setUsers(users);


        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * Post method to save the information updated in the initial checkout form
     *
     * @param book    Book that is to be checked out
     * @param student Student that has checked out thebook
     * @return Returns a string to redirect the user to the catalogue page
     */
    @PostMapping("/checkout")
    public String checkout(@ModelAttribute Checkout checkout) {
        Book book = checkout.getBook();

        Student student = checkout.getStudent();

            String id = "";
            for(Student s : studentRepository.findAll()){
                if(s.getName().equals(checkout.getStudent().getName())){
                    id = s.getId();
                }
            }
            student = studentRepository.findById(id).get();

        String bookId = book.getId();
        Book temp = bookRepository.findById(bookId).get();
        Catalogue catalogue = catalogueRepository.findById(String.valueOf(temp.getCatalogue_number())).get();
        catalogue.setQuantity_available(catalogue.getQuantity_available() - 1);
        temp.setCurrentOwner(student.getName());
        temp.setTeacher(book.getTeacher());

        temp.setCheckedOut(1);
        bookRepository.save(temp);
        return "redirect:/catalogue";
    }

    /**
     * Method to return a book based on a given ID
     *
     * @param bookID ID of the book to be returned
     * @return Redirect back to the catalogue page
     */
    @GetMapping("/return")
    public String returnBook(@RequestParam String bookID) {
        Book book = bookRepository.findById(bookID).get();
        book.setCurrentOwner(null);
        book.setCheckedOut(0);
        book.setTeacher("");
        Catalogue catalogue = catalogueRepository.findById(String.valueOf(book.getCatalogue_number())).get();
        catalogue.setQuantity_available(catalogue.getQuantity_available() + 1);
        bookRepository.save(book);
        return "redirect:/catalogue";
    }

    /**
     * Opens up a page where the user can generate and dowload a barcode with information they enter
     *
     * @return Returns a ModelAndView object of the barcode generator page
     */
    @GetMapping("/barcode")
    public ModelAndView generateBarcode() {
        ModelAndView mav = new ModelAndView("barcode-generator");
        return mav;
    }

    /**
     * Generates a barcode for a specific book
     *
     * @param bookID ID of the book that the barcode is to be made for
     * @return Returns a RedirectView object that initially downloads the image then redirects  them to the catalogue page
     * @throws IOException the io exception
     */
    @GetMapping("/generateBarcode")
    public RedirectView barcodeFor(@RequestParam String bookID) throws IOException {
        Book book = bookRepository.findById(bookID).get();

        String title = book.getName();
        String[] arr = title.split(" ");
        if(arr.length > 1){
            title = arr[0] + " " + arr[1];
        }
        else{
            title= arr[0];
        }
        String copyNum = book.getCopy_number();

        String imageUrl = "https://barcode.tec-it.com/barcode.ashx?data="
                + title + " " + copyNum + "&code=Code128&translate-esc=true&download=true&redirect="
                + URLEncoder.encode("/catalogue", "UTF-8");


        return new RedirectView(imageUrl, true, false);
    }

    /**
     * @deprecated
     * Temporary method to update the quantities and remaining quantities of the catalogue objects
     * @return Redirects the user to the catalogue page
     */
    @GetMapping("/updateQuantities")
    public String updateQuantities() {
        List<Book> books = bookRepository.findAll();
        List<Catalogue> catalogue = catalogueRepository.findAll();

        for (Catalogue item : catalogue) {
            ArrayList<Book> temp = new ArrayList<>();
            for (Book book : books) {
                if (item.getName().equals(book.getName())) {
                    temp.add(book);
                }
            }
            int checked_out = 0;
            for (Book book : temp) {
                if (book.getCheckedOut() == 1) {
                    checked_out++;
                }
            }
            item.setQuantity_available(item.getQuantity() - checked_out);
            catalogueRepository.save(item);
        }
        return "redirect:/catalogue";
    }

    /**
     * Opens the admin options HTML page
     * @return A ModelAndView of the admin options page
     */

    @GetMapping("/options")
    public ModelAndView options() {
        ModelAndView mav = new ModelAndView("options");
        return mav;
    }

    /**
     * Opens a page to add a new genre
     * @return A ModelAndView of the add genre form
     */
    @GetMapping("/addGenre")
    public ModelAndView addGenre() {
        ModelAndView mav = new ModelAndView("add-genre");
        return mav;
    }

    /**
     * Post of the add genre form, that is responsible for checking if the genre already exists
     * if the genre is a new genre it will be saved to the genre table
     * @param genreName Name of the genre to be saved
     * @return Returns an ModelAndView of either the options page or the add-genre page depending on whether
     * the action of saving the genre was successfull
     */
    @PostMapping("/addGenre")
    public ModelAndView addGenre(@RequestParam("genreName") String genreName) {
        ModelAndView mav = new ModelAndView();

        if (!isValidInput(genreName)) {
            mav.addObject("errorMessage", "Invalid genre name");
            mav.setViewName("add-genre");
            return mav;
        }

        // Check if the genre already exists
        for(Genre g : genreRepository.findAll()) {
            if(g.getGenre().equals(genreName)) {
                mav.addObject("errorMessage", "Genre already exists");
                mav.setViewName("add-genre");
                return mav;
            }
        }


        // Create a new genre
        Genre newGenre = new Genre();
        newGenre.setGenre(genreName);
        genreRepository.save(newGenre);

        mav.setViewName("redirect:/options");
        return mav;
    }

    /**
     * Method for displaying the delete genre page
     * @return Returns a ModelAndView of the delete genre form
     */
    @GetMapping("/deleteGenre")
    public ModelAndView deleteGenre() {
        ModelAndView mav = new ModelAndView("delete-genre");
        Genre genre = new Genre();
        List<Genre> genres = genreRepository.findAll();
        mav.addObject("genres", genres);
        mav.addObject("genre", genre);
        return mav;
    }

    /**
     * Post mapping for the delete genre form that deletes the genre based on the matching genre Id
     * @param genre Genre object to be deleted
     * @return Redirects the user to the admin options page
     */
    @PostMapping("/deleteGenre")
    public String deleteGenre(@ModelAttribute Genre genre) {
        genreRepository.deleteById(genre.getId());

        return "redirect:/options";
    }

    /**
     * Checks to see if a given input is valid (Contains only letters and spaces)
     * @param input String to be checked
     * @return Boolean to see if it is a valid input or not
     */
    private boolean isValidInput(String input) {
        // Check if the input is not empty and contains only letters and spaces
        return input != null && !input.isEmpty() && input.matches("^[a-zA-Z ]*$");
    }

    /**
     * @deprecated
     * Temporary method to update all the current owners in the books table
     * @return Redirects the user to the catalogue page
     */
    public String updateStudents(){
        for(Book book: bookRepository.findAll()){
            if(book.getCurrentOwner() != null){
                if(book.getCurrentOwner().length()<4) {
                    book.setCurrentOwner(studentRepository.findById(book.getCurrentOwner()).get().getName());
                    bookRepository.save(book);
                }
            }
        }

        return "redirect:/catalogue";
    }

    /**
     * Form to add a new student
     * @return ModelAndView object of the new student form
     */
    @GetMapping("/addNewStudent")
    public ModelAndView newStudent(){
        ModelAndView mav = new ModelAndView("new-student");

        mav.addObject("student", new Student() );
        return mav;
    }

    /**
     * Post mapping to save the new student
     * @param student Student object to be added/saved
     * @return Redirects the user to the Admin Options page
     */
    @PostMapping("/addNewStudent")
    public String newStudent(@ModelAttribute Student student){
        studentRepository.save(student);
        return"redirect:/options";
    }

    /**
     * Shows the form to delete a student in the list
     * @return ModelAndView object of the form
     */
    @GetMapping("/deleteStudent")
    public ModelAndView deleteStudent(){
        ModelAndView mav = new ModelAndView("delete-student");
        mav.addObject("students", studentRepository.findAll());
        mav.addObject("student", new Student());
        return mav;
    }

    /**
     * Post mapping to delete a student based on their ID
     * @param student Student to be deleted
     * @return Redirects the user to the Admin Options page
     */
    @PostMapping("/deleteStudent")
    public String deleteStudent(@ModelAttribute Student student){
        studentRepository.deleteById(student.getId());

        return "redirect:/options";
    }


    //Parallel Arrays

    /**
     * Saves a list of all the checked out books in the book list to the user's downloads
     * @return
     */
    @GetMapping("/downloadBookList")
    public ResponseEntity<ByteArrayResource> downloadBookList() throws IOException {
        List<Book> books = bookRepository.findAll();

        ArrayList<String> bookNames = new ArrayList<>();
        ArrayList<String> studentNames = new ArrayList<>();
        ArrayList<String> teacherNames = new ArrayList<>();

        for(Book b : books){
            if(b.getCheckedOut() == 1){
                bookNames.add(b.getName());
                studentNames.add(b.getCurrentOwner());
                teacherNames.add(b.getTeacher());
            }
        }
        String filename = "checked-out-books.xlsx";
        saveToExcel(bookNames, studentNames, teacherNames, filename);

        // Load the file into a ByteArrayResource
        File file = new File(filename);
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

        // Create a ResponseEntity and set the headers for the file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());


        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    /**
     * Method to download a list of books that are checked out
     * @param bookNames List of the book titles that are checked out
     * @param studentNames List of the students who checked out the corresponding books
     * @param teacherNames List of the teachers who checked the books out to the students
     * @param filename The name of the file
     */
    public static void saveToExcel(List<String> bookNames, List<String> studentNames, List<String> teacherNames, String filename) {
        // Create a new workbook object
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Create a new sheet
        Sheet sheet = workbook.createSheet("Checked Out Books");

        // Create header row
        Row headerRow = sheet.createRow(0);

        // Create header cells
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Book Names");

        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Student Names");

        Cell headerCell3 = headerRow.createCell(2);
        headerCell3.setCellValue("Teacher Names");

        // Create data rows
        int rowCount = 1;
        for (int i = 0; i < bookNames.size(); i++) {
            Row row = sheet.createRow(rowCount++);

            // Create cells for list1 and list2
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(bookNames.get(i));

            Cell cell2 = row.createCell(1);
            cell2.setCellValue(studentNames.get(i));

            Cell cell3 = row.createCell(2);
            cell3.setCellValue(teacherNames.get(i));
        }

        // Write the workbook to an Excel file
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
