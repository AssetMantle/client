package actors.blockchainModels

import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.external.ExternalShardAllocationStrategy.ShardRegion

object ShardSettings {

  val  numberOfShards = 10
  val numberOfEntities = 100

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case attempt@Get(_,address) => {
      val entityId = scala.util.Random.nextInt(10).hashCode.abs % numberOfEntities
      (entityId.toString, attempt)
    }

  }
  val extractShardId: ShardRegion.ExtractShardId = {
    case Get(_,address) =>{
      val shardId = scala.util.Random.nextInt(100).hashCode.abs % numberOfShards
      shardId.toString
    }
    case attempt@StartActor(actorRef) => {
      val shardId = actorRef.path.address.hashCode.abs % numberOfShards
      (shardId.toString)
    }
  }

}
