package com.studentdatabase

//#student-database-actor
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable
//#student-database-classes
final case class Student(name: String, emplID: Int, status: String, GPA: String)
final case class Students(students: immutable.Seq[Student])
//#student-database-classes
object StudentDatabase {
  sealed trait Command
  final case class GetStudents(replyTo: ActorRef[Students]) extends Command
  final case class NewStudent(student: Student, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetStudent(name: String, replyTo: ActorRef[GetStudentResponse]) extends Command
  final case class RemoveStudent(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetStudentResponse(maybeStudent: Option[Student])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = database(Set.empty)

  private def database(students: Set[Student]): Behavior[Command] = Behaviors.receiveMessage {
    case GetStudents(replyTo) =>
      replyTo ! Students(students.toSeq)
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
  }
}
//#student-database-actor