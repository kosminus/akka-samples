import Domain.ProcessFile
import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Application extends App {

  def createNode(port: Int, role: String, props: Props, actorName: String): ActorRef = {
    val config = ConfigFactory.parseString(
      s"""
         |akka.cluster.roles = ["$role"]
         |akka.remote.artery.canonical.port = $port
       """.stripMargin)
      .withFallback(ConfigFactory.load("application.conf"))
    val system = ActorSystem("kosminusCluster", config)
    system.actorOf(props, actorName)
  }

  val master = createNode(2551, "master", Props[Master], "master")
  createNode(2552, "worker", Props[Worker], "worker")
  createNode(2553, "worker", Props[Worker], "worker")
  Thread.sleep(10000)
  master ! ProcessFile("src/main/resources/input/enwik8.txt")
}
