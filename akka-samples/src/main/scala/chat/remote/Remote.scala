package chat.remote

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import chat.SimpleActor
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Remote  {
  val remoteSystem = ActorSystem("RemoteSystem", ConfigFactory.load("chat.conf").getConfig("remoteSystem"))
  val remoteSimpleActor = remoteSystem.actorOf(Props[SimpleActor], "remoteSimpleActor")
  remoteSimpleActor ! "hello, this is the remote actor!"

  val remoteActorSelection = remoteSystem.actorSelection("akka://LocalSystem@192.168.0.100:2551/user/localSimpleActor")

  import remoteSystem.dispatcher
  implicit val timeout = Timeout(3 seconds)
  val remoteActorRefFuture = remoteActorSelection.resolveOne()



  def saySomething (actorRef: ActorRef, message:String):Unit =
    actorRef !  message

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
