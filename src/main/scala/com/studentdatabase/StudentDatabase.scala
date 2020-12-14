package com.studentdatabase

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.IterableOnce.iterableOnceExtensionMethods
import scala.collection.immutable
final case class Student(name: String, emplID: Int, status: String, GPA: String, Major: String)
final case class Students(students: immutable.Seq[Student])
object StudentDatabase {
  sealed trait Command
  final case class GetStudents(replyTo: ActorRef[Students]) extends Command
  final case class FindStudents(name: String, replyTo:ActorRef[Students]) extends Command
  final case class NewStudent(student: Student, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetStudent(name: String, replyTo: ActorRef[GetStudentResponse]) extends Command
  final case class RemoveStudent(name: String, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class ClearDatabase(replyTo: ActorRef[ActionPerformed]) extends Command
  final case class ChangeMajor(name: String, major: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetStudentResponse(maybeStudent: Option[Student])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = database(Set.empty)

  private def database(students: Set[Student]): Behavior[Command] = Behaviors.receiveMessage {
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
    /*case ChangeMajor(name, major, replyTo) =>
      replyTo ! ActionPerformed(s"Student $name major changed to $major")
      val student = students.filter(_.name == name)
      database(student.last.Major = major)*/
    case ClearDatabase(replyTo) =>
      replyTo ! ActionPerformed(s"Student database has been cleared.")
      database(Set.empty)
  }
}
