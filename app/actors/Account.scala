package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import models.blockchain
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

case class CreateAccountChildActorMessage(username: String, actorRef: ActorRef)
case class CreateAssetChildActorMessage(username: String, actorRef: ActorRef)
case class CreateFiatChildActorMessage(username: String, actorRef: ActorRef)
case class CreateOrderChildActorMessage(username: String, actorRef: ActorRef)
case class CreateNegotiationChildActorMessage(username: String, actorRef: ActorRef)

object MainAccountActor {
  def props(actorTimeout: FiniteDuration, actorSystem: ActorSystem) = Props(new MainAccountActor(actorTimeout, actorSystem))
}

@Singleton
class MainAccountActor @Inject()(actorTimeout: FiniteDuration, actorSystem: ActorSystem) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_MAIN_ACCOUNT

  def receive: PartialFunction[Any, Unit] = {
    case accountCometMessage: blockchain.AccountCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_ACCOUNT + "/" + accountCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + accountCometMessage.username + ": " + accountCometMessage.message)

          actorRef ! accountCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createAccountChildActorMessage: CreateAccountChildActorMessage => context.actorOf(props = UserAccountActor.props(createAccountChildActorMessage.actorRef, actorTimeout), name = createAccountChildActorMessage.username)
    case assetCometMessage: blockchain.AssetCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_ASSET + "/" + assetCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + assetCometMessage.username + ": " + assetCometMessage.message)
          actorRef ! assetCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createAssetChildActorMessage: CreateAssetChildActorMessage => context.actorOf(props = UserAssetActor.props(createAssetChildActorMessage.actorRef, actorTimeout), name = createAssetChildActorMessage.username)
    case fiatCometMessage: blockchain.FiatCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_FIAT + "/" + fiatCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + fiatCometMessage.username + ": " + fiatCometMessage.message)
          actorRef ! fiatCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createFiatChildActorMessage: CreateFiatChildActorMessage => context.actorOf(props = UserFiatActor.props(createFiatChildActorMessage.actorRef, actorTimeout), name = createFiatChildActorMessage.username)
    case negotiationCometMessage: blockchain.NegotiationCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_NEGOTIATION + "/" + negotiationCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + negotiationCometMessage.username + ": " + negotiationCometMessage.message)
          actorRef ! negotiationCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createNegotiationChildActorMessage: CreateNegotiationChildActorMessage => context.actorOf(props = UserNegotiationActor.props(createNegotiationChildActorMessage.actorRef, actorTimeout), name = createNegotiationChildActorMessage.username)
    case orderCometMessage: blockchain.OrderCometMessage =>
      actorSystem.actorSelection("/user/" + constants.Module.ACTOR_MAIN_ORDER + "/" + orderCometMessage.username).resolveOne().onComplete {
        case Success(actorRef) => logger.info(module + " " + orderCometMessage.username + ": " + orderCometMessage.message)
          actorRef ! orderCometMessage
        case Failure(ex) => logger.info(module + ": " + ex.getMessage)
      }
    case createOrderChildActorMessage: CreateOrderChildActorMessage => context.actorOf(props = UserOrderActor.props(createOrderChildActorMessage.actorRef, actorTimeout), name = createOrderChildActorMessage.username)

  }

}

object UserAccountActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAccountActor(systemUserActor, actorTimeout))
}

object UserAssetActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAccountActor(systemUserActor, actorTimeout))
}
object UserFiatActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAccountActor(systemUserActor, actorTimeout))
}
object UserOrderActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAccountActor(systemUserActor, actorTimeout))
}
object UserNegotiationActor {
  def props(systemUserActor: ActorRef, actorTimeout: FiniteDuration) = Props(new UserAccountActor(systemUserActor, actorTimeout))
}

class UserAccountActor(systemUserActor: ActorRef, actorTimeout: FiniteDuration) extends Actor with ActorLogging {

  private implicit val timeout: Timeout = Timeout(actorTimeout)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.ACTOR_USER_ACCOUNT

  def receive = {
    case accountCometMessage: blockchain.AccountCometMessage => systemUserActor ! accountCometMessage.message
    case negotiationCometMessage: blockchain.NegotiationCometMessage => systemUserActor ! negotiationCometMessage.message
    case orderCometMessage: blockchain.OrderCometMessage => systemUserActor ! orderCometMessage.message
    case fiatCometMessage: blockchain.FiatCometMessage => systemUserActor ! fiatCometMessage.message
    case assetCometMessage: blockchain.AssetCometMessage => systemUserActor ! assetCometMessage.message

    case _: ShutdownActorMessage =>
      systemUserActor ! PoisonPill
      context.stop(self)
  }
}