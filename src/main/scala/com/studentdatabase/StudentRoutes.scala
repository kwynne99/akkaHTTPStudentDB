package com.studentdatabase

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.studentdatabase.StudentDatabase._


class StudentRoutes(studentDatabase: ActorRef[StudentDatabase.Command])(implicit val system: ActorSystem[_]) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getStudents(): Future[Students] = studentDatabase.ask(GetStudents)
  def findStudents(name: String): Future[Students] = studentDatabase.ask(FindStudents(name, _))
  def getStudent(name: String): Future[GetStudentResponse] = studentDatabase.ask(GetStudent(name, _))
  def newStudent(student: Student): Future[ActionPerformed] = studentDatabase.ask(NewStudent(student, _))
  def removeStudent(name: String): Future[ActionPerformed] = studentDatabase.ask(RemoveStudent(name, _))
  def clearDatabase(): Future[ActionPerformed] = studentDatabase.ask(ClearDatabase)

  val studentRoutes: Route =
    pathPrefix("students") {
      concat(
        pathEnd {
          //get-post-delete (GetStudents, NewStudents, ClearDatabase)
          concat(
            get {
              complete(getStudents())
            },
            post {
              entity(as[Student]) { student =>
                onSuccess(newStudent(student)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            },
            delete {
              onSuccess(clearDatabase()) { performed => complete((StatusCodes.OK, performed))}
            }
          )
          //get-post-delete (GetStudents, NewStudents, ClearDatabase)
        },
        path(Segment) { name =>
          concat(
            //get-get-delete (GetStudent, FindStudents, RemoveStudent)
            get {
              rejectEmptyResponse {
                onSuccess(getStudent(name)) { response =>
                  complete(response.maybeStudent)
                }
              }
            },
            get {
              complete(findStudents(name))
            },
            delete {
              onSuccess(removeStudent(name)) { performed => complete((StatusCodes.OK, performed))}
            }
            //get-get-delete (GetStudent, FindStudents, RemoveStudent)
            )
        })
    }
  //all-routes
}