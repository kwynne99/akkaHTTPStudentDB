package com.studentdatabase

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class StudentRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  //#test-top

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val studentDatabase = testKit.spawn(StudentDatabase())
  lazy val routes = new StudentRoutes(studentDatabase).studentRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#set-up

  //#actual-test
  "StudentRoutes" should {
    "return no students if no present (GET /students)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/students")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"students":[]}""")
      }
    }
    //#actual-test

    //#testing-post
    "be able to add users (POST /students)" in {
      val student = Student("Kapi", 42, "ok", "3.2", "Computer Science")
      //val studentEntity = Marshal(student).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      val studentEntity = Marshal(student).to[MessageEntity].futureValue
      // using the RequestBuilding DSL:
      val request = Post("/students").withEntity(studentEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"Student Kapi has been added."}""")
      }
    }
    //#testing-post


    //#actual-test
  }
  //#actual-test

  //#set-up
}
//#set-up
//#student-routes-spec

