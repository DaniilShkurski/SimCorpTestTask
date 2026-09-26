import cats.effect.{IO, IOApp, ExitCode}
import cats.implicits._

import fs2.{Stream, text}
import fs2.io.file.{Files, Path}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    args match {
      case List(destination, parallelismStr) =>
        parallelismStr.toIntOption match {
          case Some(parallelism) if parallelism > 0 => processData(destination, parallelism).as(ExitCode.Success)
          case _ => IO.println("Parallelism must be a positive integer")
            .as(ExitCode.Error)
        }
      case List(destination) =>
        processData(destination, 4).as(ExitCode.Success)
      case _ => IO.println("Please provide arguments. Example: (sbt run {destination} {parallelism_number})").as(ExitCode.Error)
    }
  }

  private def processData(destination: String, parallelism: Int) = {
    for {
      result <- findFilesIO(destination)
        .parEvalMapUnordered(parallelism)(processFile)
        .compile
        .fold(Map.empty[String, Long]) { case (map1, map2) =>
          map1.foldLeft(map2) { case (result, (word, count)) =>
            result.updatedWith(word) {
              case Some(existing) => Some(existing + count)
              case None => Some(count)
            }
          }
        }
      _ <- IO {
        result.foreach { case (word, count) =>
          println(s"$count: $word")
        }
      }
    } yield ()
  }

  private def findFilesIO(path: String): Stream[IO, Path] = {
    Files[IO]
      .walk(Path(path))
      .evalFilter(path => Files[IO].isRegularFile(path))
  }

  private def processFile(path: Path): IO[Map[String, Long]] = {
    //NOTE: The task does not specify which characters shouldn't be included in the search, so for now only whitespaces are skipped
    val regex = """\S+""".r
    Files[IO]
      .readAll(path)
      .through(text.utf8.decode)
      .through(text.lines)
      .flatMap { line =>
        Stream.fromIterator[IO](
          regex.findAllIn(line),
          chunkSize = 64
        )
      }
      .compile
      .fold(Map.empty[String, Long]) { (counts, word) =>
        counts.updatedWith(word) {
          case Some(count) => Some(count + 1)
          case None => Some(1)
        }
      }
  }
}
