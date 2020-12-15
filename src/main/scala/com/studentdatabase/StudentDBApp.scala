package com.studentdatabase

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import java.net._
import java.io._

import scala.util.{Failure, Success}

object StudentDBApp {
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    // Set up server availability on the process IP address.
    val whatismyip: URL = new URL("http://checkip.amazonaws.com")
    val in: BufferedReader = new BufferedReader(new InputStreamReader(whatismyip.openStream()))
    val ip: String = in.readLine()

    /* Uncomment Localhost to run demonstration script. */
    // val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    val futureBinding = Http().newServerAt(InetAddress.getLocalHost.getHostAddress, 8080).bind(routes)
    //val futureBinding = Http().newServerAt(ip, 8080).bind(routes)

    // Take advantage of futures to ensure that Akka HTTP has properly bound to the IP address.
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

/*
 * Main Function
 */
  def main(args: Array[String]): Unit = {
    // The root behavior of the actor system is to spawn the database actor and routing service.
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
