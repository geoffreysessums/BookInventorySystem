# Book Inventory IMS

Geoffrey Sessums  
Enterprise Software Engineering - Spring 2019  
Language: Java  
Libraries: JavaFX, Log4j, JDBC
Database: MySQL

## Description

An inventory manager that allows multiple users to search, add, edit, and delete books and book details while an audit trail tracks all changes made to the book records. 

### Preview

![alt text](https://github.com/geoffreysessums/BookInventorySystem/blob/master/screenshots/book_detail_view.png "Book Detail View")

### More Images

- [Book List](https://github.com/geoffreysessums/BookInventorySystem/blob/master/screenshots/book_list_view.png) __(Records are listed by id and title)__
- [Edit Author](https://github.com/geoffreysessums/BookInventorySystem/blob/master/screenshots/edit_author_view.png)
- [Login](https://github.com/geoffreysessums/BookInventorySystem/blob/master/screenshots/login.png)
- [Audit Trail](https://github.com/geoffreysessums/BookInventorySystem/blob/master/screenshots/audit_trail_view.png)
- [Add Book](https://github.com/geoffreysessums/BookInventorySystem/blob/master/screenshots/add_book_view.png)

## What I Learned

* Used Apache's Log4j to log errors and debugging
* Used Scene builder to create and edit fxml files
* Used JavaFX to create a desktop GUI application
* Implemented the Single Document Interface (SDI) GUI design pattern
* Used Model View Controller (MVC) to separate business logic, data manipulation, and presentation
* Used JDBC to communicate with a remote MySQL database
* Used phpMyAdmin to create, read, update, and delete tables and data
* Used SQL to read, write, and modify data
* Used the Table Data Gateway (TDG) pattern to communicate with database
* Created a MySQL store procedure to generate 10,000 records to test the application under load
* Implemented ACID transaction techniques
* Implemented optimistic locking for concurrent transactions
* Implemented the lazy-loading fetching pattern for pagination and high responsiveness
* Implemented basic authentication
* Implemented Role Based Access Control (RBAC)
