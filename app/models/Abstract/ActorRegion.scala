package models.Abstract

import actors.models.blockchain
import akka.actor.{Actor, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}

trait ShardedActorRegion {

  def idExtractor: ShardRegion.ExtractEntityId

  def shardResolver: ShardRegion.ExtractShardId

  def regionName: String

  def props: Props

   val actorRegion = {
     ClusterSharding(blockchain.Service.actorSystem).start(
       typeName = regionName,
       entityProps = props,
       settings = ClusterShardingSettings(blockchain.Service.actorSystem),
       extractEntityId = idExtractor,
       extractShardId = shardResolver
     )
   }
 }
