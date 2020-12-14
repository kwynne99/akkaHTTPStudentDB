package com.studentdatabase

import com.studentdatabase.StudentDatabase.ActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  //implicit val userJsonFormat = jsonFormat4(Student)
  //implicit val usersJsonFormat = jsonFormat1(Students)
  implicit val studentJsonFormat = jsonFormat4(Student)
  implicit val studentsJsonFormat = jsonFormat1(Students)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-formats
