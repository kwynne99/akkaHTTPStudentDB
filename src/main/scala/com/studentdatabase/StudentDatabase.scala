package com.studentdatabase

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.collection.immutable

/*
 * Database Schema
 */
final case class Student(name: String, emplID: Int, status: String, GPA: String, Major: String)
final case class Students(students: immutable.Seq[Student])

/*
 * Database Operations
 */
object StudentDatabase {
  // Only extend this trait within the Database.
  sealed trait Operation
  
  final case class GetStudents(replyTo: ActorRef[Students]) extends Operation // Return the stored students.
  final case class FindStudents(name: String, replyTo: ActorRef[Students]) extends Operation // Perform a broad search for students by name.
  final case class NewStudent(student: Student, replyTo: ActorRef[ActionPerformed]) extends Operation // Add a student to the database. Does nothing if student has been added.
  final case class GetStudent(name: String, replyTo: ActorRef[GetStudentResponse]) extends Operation // Return a single student by name.
  final case class RemoveStudent(name: String, replyTo: ActorRef[ActionPerformed]) extends Operation // Remove a student from the database by name.
  final case class ClearDatabase(replyTo: ActorRef[ActionPerformed]) extends Operation // Resets the database with an empty set.

  // Messages to be passed to the top-level Actor System
  final case class GetStudentResponse(maybeStudent: Option[Student])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Operation] = database(Set.empty)

  // Each database operation passes a response to the given ActorRef.
  private def database(students: Set[Student]): Behavior[Operation] = Behaviors.receiveMessage {
    case GetStudents(replyTo) =>
      replyTo ! Students(students.toSeq)
      Behaviors.same
    case FindStudents(name, replyTo) =>
      replyTo ! Students(students.filter(_.name.contains(name)).toSeq)
      Behaviors.same
    case NewStudent(student, replyTo) if students.contains(student) =>
      replyTo ! ActionPerformed(s"Student ${student.name} already exists!")
      Behaviors.same    
    case NewStudent(student, replyTo) =>
      replyTo ! ActionPerformed(s"Student ${student.name} added to database.")
      database(students + student)
    case GetStudent(name, replyTo) =>
      replyTo ! GetStudentResponse(students.find(_.name == name))
      Behaviors.same
    case RemoveStudent(name, replyTo) =>
      replyTo ! ActionPerformed(s"Student $name removed from database.")
      database(students.filterNot(_.name == name))
    case ClearDatabase(replyTo) =>
      replyTo ! ActionPerformed(s"Student database has been cleared.")
      database(Set.empty)
  }
}
