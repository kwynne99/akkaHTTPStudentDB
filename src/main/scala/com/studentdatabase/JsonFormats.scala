package com.studentdatabase

import com.studentdatabase.StudentDatabase.ActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val studentJsonFormat = jsonFormat5(Student)
  implicit val studentsJsonFormat = jsonFormat1(Students)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
