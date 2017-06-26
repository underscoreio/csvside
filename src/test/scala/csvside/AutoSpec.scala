//package csvside
//
//import cats.syntax.validated._
//import org.scalatest._
//import unindent._
//
//class AutoSpec extends FreeSpec with Matchers {
//  "auto reader" - {
//    "valid" in new AutoFixtures {
//      val csv = i"""
//        a,b,c
//        abc,true,123
//        "a b",false,321
//        """
//
//      Csv.fromString[Test](csv).toList should equal(List(
//        CsvSuccess(2, "abc,true,123", Test("abc", 123, Some(true))),
//        CsvSuccess(3, "\"a b\",false,321", Test("a b", 321, Some(false)))
//      ))
//    }
//
//    "invalid" in new AutoFixtures {
//      val csv = i"""
//        a,b,c
//        ,,
//        abc,abc,abc
//        """
//
//      Csv.fromString[Test](csv).toList should equal(List(
//        Seq(
//          CsvError(CsvPath("c"), "Must be a whole number")
//        ).invalid,
//        Seq(
//          CsvError(CsvPath("c"), "Must be a whole number"),
//          CsvError(CsvPath("b"), "Must be a yes/no value or blank")
//        ).invalid
//      ))
//    }
//  }
//}
//
//trait AutoFixtures {
//  case class Test(a: String, b: Int, c: Option[Boolean])
//
//   implicit val testReader = implicitly[RowReader[Test]]
//   implicit val testWriter = implicitly[RowWriter[Test]]
//}
