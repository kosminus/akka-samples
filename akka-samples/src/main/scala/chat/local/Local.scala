package chat.local

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.util.Timeout
import chat.SimpleActor
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Local {
  val localSystem: ActorSystem = ActorSystem("LocalSystem", ConfigFactory.load("chat.conf"))
  val localSimpleActor: ActorRef = localSystem.actorOf(Props[SimpleActor], "localSimpleActor")
  localSimpleActor ! "hello, this is the local actor!"

  val remoteActorSelection: ActorSelection = localSystem.actorSelection("akka://RemoteSystem@192.168.0.100:2552/user/remoteSimpleActor")

  import localSystem.dispatcher
  implicit val timeout: Timeout = Timeout(3 seconds)
  val remoteActorRefFuture: Future[ActorRef] = remoteActorSelection.resolveOne()


  def saySomething(actorRef: ActorRef, message: String): Unit =
    actorRef ! message


  def main(args: Array[String]): Unit = {
    var command = ""
    while (command != "exit") {
      command = scala.io.StdIn.readLine()

      remoteActorRefFuture.onComplete {
        case Success(actorRef) => actorRef !  command
        case Failure(ex) => println(s"I failed to resolve the remote actor because: $ex")
      }
    }
  }
}




