package com.isb.library.web.book.controller;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.isb.library.web.book.dao.CatalogueRepository;
import com.isb.library.web.book.dao.StudentRepository;
import com.isb.library.web.book.entity.Checkout;
import com.isb.library.web.book.entity.Student;


import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.isb.library.web.book.entity.Book;
import com.isb.library.web.book.dao.BookRepository;
import com.isb.library.web.book.entity.Catalogue;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.isb.library.web.book.controller.bookImport.extractData;
import static org.krysalis.barcode4j.output.bitmap.BitmapBuilder.saveImage;

/**
 * The type Book controller.
 */
@Controller
@RequestMapping("/")
public class BookController {


    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CatalogueRepository catalogueRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * List page for a list of all the individual books
     *
     * @return Returns a ModelAndView object populated with the books from 'bookRepository' and the students from 'studentRepository' and they are added to the 'list-books.html' file
     */
    @GetMapping({"/list"})
    public ModelAndView getAllBooks(){
        ModelAndView mav = new ModelAndView("list-books");
        Checkout checkout = new Checkout();
        checkout.setBooks(bookRepository.findAll());
        checkout.setStudents(studentRepository.findAll());
        mav.addObject("books",bookRepository.findAll());
        mav.addObject("students", studentRepository.findAll());
        return mav;
    }

    /**
     * Simple redirect to the catalogue page
     *
     * @return Returns a string to redirect
     */
    @GetMapping({"/"})
    public String redirectHome(){
        return "redirect:/catalogue";
    }

    /**
     * Catalogue page
     *
     * @return Returns an ModelAndView object that is populated with all the items retrieved from 'catalogueRepository'
     */
    @GetMapping({"/catalogue"})
    public ModelAndView getCatalogue(){
        ModelAndView mav = new ModelAndView("book-catalogue");
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
        return"redirect:/";
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
        return "redirect:/catalogue";
    }

    /**
     * Either adds a new catalogue object or saves updates to a catalogue item
     *
     * @param catalogue Catalogue object to be saved
     * @return Returns a redirect to the catalogue page
     */
    @PostMapping("/saveCatalogue")
    public String updateCatalogue(@ModelAttribute Catalogue catalogue){
        int id = 1;
        //Checks to see if the catalogue is being updated or saved
        if(!catalogue.getId().equals("")) {
            id = Integer.valueOf(catalogue.getId());
        }

        //Updates the ID number to the next number in the sequence
        else {
            List<Catalogue> catalogue1 = catalogueRepository.findAll();
            int size = catalogue1.size();
            if(size != 0) {
                id = Integer.parseInt(catalogue1.get(size - 1).getId()) + 1;
            }

        }


        //Find the quantity of books in the catalogue object (By default this value is 0 so no null error checking is required)
        int quantity = catalogue.getQuantity();
        catalogue.setQuantity_available(quantity);


        //Checks to see if there are books in the book list whos catalogue number matches the catalogue id and and adds them to a list
        List<Book> bookList = bookRepository.findAll();
        int currentNum = 0;
        int finalCopyNumber = 0;
        if(quantity !=0) {
            ArrayList<Book> finalBookList = new ArrayList<>();
            for (Book book : bookList) {
                if (book.getCatalogue_number() == id) {
                    currentNum++;
                    finalBookList.add(book);
                    String[] arr = finalBookList.get(currentNum - 1).getCopy_number().split("-");
                    finalCopyNumber = Integer.parseInt(arr[1]);
                }
            }
        }

        //Finds the number of books currently in the catalogue and sees if the catalogue object's quantity was updated
        //if the quantity was updated the number of books that are in the difference are added to the book list
        if(currentNum < quantity){
            for (int i = 0; i < quantity-currentNum; i++) {
                Book book = new Book();
                book.setLastName(catalogue.getLastName());
                book.setFirstName(catalogue.getFirstName());
                book.setName(catalogue.getName());
                int bookNum = finalCopyNumber+1+i;
                book.setCopy_number(id + "-" + bookNum);
                book.setGenre(catalogue.getGenre());
                book.setCatalogue_number(Integer.valueOf(id));
                bookRepository.save(book);
            }
        }

        //catalogue is saved
        catalogueRepository.save(catalogue);
       return "redirect:/catalogue";
    }

    /**
     * Finds a list of books with a matching title and returns a narrowed down book list view showing only those books
     *
     * @param title Title of the books ot be searched by
     * @return Returns a ModelAndView object of the html file "books-with-title" with a list of all the books
     */
    @GetMapping("/booksWithTitle")
    public ModelAndView booksWithTitle(@RequestParam String title){
        ModelAndView mav = new ModelAndView("books-with-title");
        List<Book> tempBooks = bookRepository.findAll();
        List<Student> students = studentRepository.findAll();
        ArrayList<Book> books = new ArrayList<>();
        for (Book book : tempBooks){
            if (book.getName().equals(title)){
                books.add(book);
            }
        }
        mav.addObject("books", books);
        mav.addObject("students", students);
        return mav;
    }

    @GetMapping("/findStudent")
    public String findStudent(@RequestParam String studentId){
        Student student = studentRepository.findById(studentId).get();

        return student.getName();
    }

    /**
     * Shows a form where the user can add a new book to the books list
     *
     * @return Returns a ModelAndView object of the "add-book-form" html file with a list of all the students passed to it and a new book object passed to it as well
     */
    @GetMapping("/addBookForm")
    public ModelAndView addBookForm() {
        ModelAndView mav = new ModelAndView("add-book-form");
        Book newBook = new Book();
        List<Student> students = studentRepository.findAll();
        Checkout checkout = new Checkout();
        checkout.setBook(newBook);
        checkout.setStudents(students);
        mav.addObject("checkout", checkout);
        return mav;
    }

    /**
     * Similar to the Add Book form but instead of adding a new book it upadtes the same book
     *
     * @param bookId ID of the book to be updated
     * @return Returns a ModelAndView object of the "add-book-form" html file with a list of all the students passed to it and a book object passed to it as well
     */
    @GetMapping("/showUpdateForm")
    public ModelAndView showUpdateForm(@RequestParam String bookId) {
        ModelAndView mav = new ModelAndView("add-book-form");

        Checkout checkout = new Checkout();
        Book book = bookRepository.findById(bookId).get();
        checkout.setBook(book);
        Student student =  new Student();
        if(book.getCurrentOwner() != null){
            student = studentRepository.findById(book.getCurrentOwner()).get();
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
        mav.addObject("catalogue", newCatalogue);
        return mav;
    }

    /**
     * A form for the user to update an existing catalogue object
     *
     * @param catalogueId the catalogue id
     * @return Returns a ModelAndView object of the "add-book-catalogue-form" html with an existing catalogue object passed to it
     */
    @GetMapping("/showUpdateCatalogueForm")
    public ModelAndView addToCatalogue(@RequestParam String catalogueId){
        ModelAndView mav = new ModelAndView("add-book-catalogue-form");
        Catalogue catalogue = catalogueRepository.findById(catalogueId).get();
        mav.addObject("catalogue", catalogue);
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
        catalogue.setQuantity(catalogue.getQuantity()-1);
        if(book.getCheckedOut() == 0){
            catalogue.setQuantity_available(catalogue.getQuantity_available()-1);
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

    /**
     * Import students form model and view.
     *
     * @return the model and view
     */
    @GetMapping("/showStudentsForm")
    public ModelAndView importStudentsForm() {
        ModelAndView mav = new ModelAndView("import-students-form");
        return mav;
    }

    /**
     * Temporary Method used to import initial data into the database
     *
     * @return Redirects to the catalogue page
     */
    @GetMapping("/saveTesting")
    public String saveTesting() {
//        ArrayList<String> titleData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Titles.xlsx", false);
//        ArrayList<String> lastNameData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Last Name.xlsx", false);
//        ArrayList<String> firstNameData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\First Name.xlsx", false);
//        ArrayList<String> genreData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Genre.xlsx", false);
//        ArrayList<String> idData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Book ID.xlsx", false);
//        ArrayList<String> studentData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Student Names.xlsx", true);
        ArrayList<String> teacherData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Teachers.xlsx", true);
        List<Student> students = studentRepository.findAll();
        List<Book> books = bookRepository.findAll();

        for(int i = 0; i< teacherData.size(); i++){
            books.get(i).setTeacher(teacherData.get(i));

//            temp.setName(titleData.get(i));
//            temp.setLastName(lastNameData.get(i));
//            temp.setFirstName(firstNameData.get(i));
//            temp.setGenre(genreData.get(i));
//            temp.setCopy_number(idData.get(i));
            bookRepository.save(books.get(i));
        }


        return"redirect:/catalogue";
    }


    /**
     * Temporary method to import data into the datapase
     *
     * @return Redirects user to the catalogue page
     */
    @GetMapping("/catalogueSaveTesting")
    public String catalogueSaveTesting(){
        //Methods used to initially import all the titles of books into the database and update both the quantities of books as well as the catalogue number of each book

//        ArrayList<String> titleData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Catalogue Titles.xlsx");
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

        ArrayList<String> lastNameData = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Catalogue Languages.xlsx", true);


        for(int i = 0; i < lastNameData.size(); i++){
            catalogue.get(i).setOriginal_language(lastNameData.get(i));
            catalogueRepository.save(catalogue.get(i));
        }


        return "redirect:/catalogue";
    }

    /**
     * Method used to save students from an excel file to the database
     *
     * @return Redirects the user to the catalogue page
     * @throws SQLException the sql exception
     * @throws IOException  the io exception
     */
    @GetMapping("/saveStudents")
    public String saveStudents() throws SQLException, IOException {


        //studentRepository.deleteAll();

        ArrayList<ArrayList<String>> masterArrayList= studentImport.studentImport("C:\\Users\\Joel\\Downloads\\Book Room Inventory.xlsx");
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

    /**
     * Method to open the initial checkout page
     *
     * @param bookID ID of the book to be checked out
     * @return Returns a ModelAndView object populated with the information of the book to be checked out and all of the students in 'studentRepository'
     */
    @GetMapping("/checkout")
    public ModelAndView checkout(@RequestParam String bookID) {
        ModelAndView mav = new ModelAndView("checkout");
        Book book = bookRepository.findById(bookID).get();
        Checkout checkout = new Checkout();

        List<Student> students = studentRepository.findAll();

        Student student = new Student();

        checkout.setStudents(students);
        checkout.setBook(book);
        checkout.setStudent(student);


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
    public String checkout(@ModelAttribute Checkout checkout){
        Book book = checkout.getBook();
        Student student = studentRepository.findById(checkout.getStudent().getId()).get();
        String bookId = book.getId();
        Book temp = bookRepository.findById(bookId).get();
        Catalogue catalogue = catalogueRepository.findById(String.valueOf(temp.getCatalogue_number())).get();
        catalogue.setQuantity_available(catalogue.getQuantity_available()-1);
        temp.setCurrentOwner(student.getId());

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
    public String returnBook(@RequestParam String bookID){
        Book book = bookRepository.findById(bookID).get();
        book.setCurrentOwner(null);
        book.setCheckedOut(0);
        Catalogue catalogue = catalogueRepository.findById(String.valueOf(book.getCatalogue_number())).get();
        catalogue.setQuantity_available(catalogue.getQuantity_available()+1);
        bookRepository.save(book);
        return "redirect:/catalogue";
    }

    /**
     * Opens up a page where the user can generate and dowload a barcode with information they enter
     *
     * @return Returns a ModelAndView object of the barcode generator page
     */
    @GetMapping("/barcode")
    public ModelAndView generateBarcode(){
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
        String copyNum = book.getCopy_number();

        String imageUrl = "https://barcode.tec-it.com/barcode.ashx?data="
                + title + " " + copyNum + "&code=Code128&translate-esc=true&download=true&redirect="
                + URLEncoder.encode("/catalogue", "UTF-8");


        return new RedirectView(imageUrl, true, false);
    }

    /**
     * Temporary method to update the quantities and remaining quantities of the catalogue objects
     * @return Redirects the user to the catalogue page
     */
    @GetMapping("/updateQuantities")
    public String updateQuantities(){
        List<Book> books = bookRepository.findAll();
        List<Catalogue> catalogue = catalogueRepository.findAll();

        for(Catalogue item : catalogue){
            ArrayList<Book> temp = new ArrayList<>();
            for(Book book: books){
                if (item.getName().equals(book.getName())){
                    temp.add(book);
                }
            }
            int checked_out = 0;
            for(Book book: temp){
                if(book.getCheckedOut() == 1){
                    checked_out++;
                }
            }
            item.setQuantity_available(item.getQuantity() - checked_out);
            catalogueRepository.save(item);
        }
        return "redirect:/catalogue";
    }


    @GetMapping("testModal")
    public ModelAndView modal(){
        ModelAndView mav = new ModelAndView("demo-modal");
        return mav;
    }




}
