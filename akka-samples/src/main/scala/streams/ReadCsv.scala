package streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvToMap
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import streams.ReadText.{actorSystem, consoleSink, filePath, flow, source}

import java.nio.file.Paths
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object ReadCsv extends App {
  val filePath = "src/main/resources/input/cars.csv"

  implicit val actorSystem = ActorSystem("FirstPrinciples")


  val source = FileIO.fromPath(Paths.get(filePath))

  val flow = CsvParsing.lineScanner(',').via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))

  //val consoleSink = Sink.foreach[Map[String,String]](s => println(s.get("Name")))
  val consoleSink = Sink.foreach[Map[String,String]](println)

  source.via(flow).runWith(consoleSink).andThen {
    case _ =>
      actorSystem.terminate()
      Await.ready(actorSystem.whenTerminated, 1 minute)
  }
}
