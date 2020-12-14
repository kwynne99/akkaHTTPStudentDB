# akkaHTTPStudentDB
Student Database on a HTTP Server using Akka HTTP

A HTTP Server using Akka HTTP. Currently used as a database to contain students of a university/school.
Students must identify: name: String, emplID: Int, status: String, GPA: String, Major: String

Currently new students can be assed using POST, if the student already exists a duplicate will not be added and the user will be notified.
The set of all students can be retrieved, or just an individual student indentified by their name.
Students can be removed when identified by their name.
The whole database can be cleared by calling delete on the "/students" path.

WIP: New routes used for identifying classes available at the University, as well as which students are enrolled in them.
