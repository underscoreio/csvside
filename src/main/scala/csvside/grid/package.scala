// package csvlist

// package object grid {
//   def load(file: File): Map[(CsvHead, CsvHead), CsvCell] = {
//     val in = new FileInputStream(file)
//     val csv = try load(in).toList finally in.close
//     val cols = csv.head.tail
//     (for {
//       row :: cells <- csv.tail
//       (col, cell)  <- cols zip cells
//     } yield ((row, col) -> cell)).toMap
//   }
// }
