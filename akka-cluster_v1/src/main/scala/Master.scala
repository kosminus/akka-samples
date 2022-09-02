import Domain.{ProcessFile, ProcessLine, ProcessLineResult}
import akka.actor.{Actor, ActorLogging, ActorRef, Address, ReceiveTimeout}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberJoined, MemberRemoved, MemberUp, UnreachableMember}
import akka.pattern.pipe
import akka.util.Timeout

import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.Random

class Master extends Actor with ActorLogging {

  import context.dispatcher
  implicit val timeout = Timeout(3 seconds)
  context.setReceiveTimeout(3 seconds)


  val cluster: Cluster = Cluster(context.system)
  var workers: Map[Address, ActorRef] = Map()

  override def preStart(): Unit = {
    cluster.subscribe(self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive(): Receive =
    handleCustomeEvents()
      .orElse(handleWorkerRegistration)
      .orElse(handleJob())
      .orElse(agregate(0))


  def handleJob(): Receive = {
    case ProcessFile(filename) =>
      scala.io.Source.fromFile(filename).getLines().foreach { line =>
        val workerIndex = Random.nextInt(workers.size)
        val worker: ActorRef = workers.values.toSeq(workerIndex)
        worker ! ProcessLine(line)
      }
  }

  def agregate(totalCount: Int): Receive = {
    case ProcessLineResult(count) =>
      context.become(agregate(totalCount + count))
    case ReceiveTimeout =>
      log.info(s"TOTAL COUNT: $totalCount")
     // context.setReceiveTimeout(Duration.Undefined)
  }

  def handleWorkerRegistration: Receive = {
    case pair: (Address, ActorRef) =>
      log.info(s"registering worker $pair")
      workers = workers + pair
  }

  def handleCustomeEvents(): Receive = {
    case MemberJoined(member) if member.hasRole("worker") =>
      log.info(s"new worker is up ${member.address}")
      val workerSelection = context.actorSelection(s"${member.address}/user/worker")
      workerSelection.resolveOne().map(ref => (member.address, ref)).pipeTo(self)

    case MemberUp(member) =>
      log.info(s"welcome member ${member.address}")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"poor  ${member.address} it was removed from ${previousStatus}")
    case UnreachableMember(member) =>
      log.info(s" member ${member.address} is unreachable")
    case m: MemberEvent =>
      log.info(s" member event ${m}")
  }
}
