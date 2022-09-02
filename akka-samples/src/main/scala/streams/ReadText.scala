package streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ReadText extends App {
  val filePath = "src/main/resources/input/cars.csv"

  implicit val actorSystem = ActorSystem("FirstPrinciples")
  import actorSystem.dispatcher


  val source = FileIO.fromPath(Paths.get(filePath))

  val flow = Framing.delimiter(ByteString("\n"), 256, true)
    .map(_.utf8String)

  val consoleSink = Sink.foreach[String](println)

  source.via(flow).runWith(consoleSink).andThen {
      case _ =>
        actorSystem.terminate()
        Await.ready(actorSystem.whenTerminated, 1 minute)
    }


}
