package com.studentdatabase

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import java.net._
import java.io._

import scala.util.{Failure, Success}

object StartServer {
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val whatismyip: URL = new URL("http://checkip.amazonaws.com")
    val in: BufferedReader = new BufferedReader(new InputStreamReader(whatismyip.openStream()))
    val ip: String = in.readLine()

    //val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    val futureBinding = Http().newServerAt(InetAddress.getLocalHost.getHostAddress, 8080).bind(routes)
    //val futureBinding = Http().newServerAt(ip, 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val studentDatabaseActor = context.spawn(StudentDatabase(), "StudentDatabaseActor")
      context.watch(studentDatabaseActor)

      val routes = new StudentRoutes(studentDatabaseActor)(context.system)
      startHttpServer(routes.studentRoutes)(context.system)


      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "StudentDBAkkaHttpServer")
  }
}
