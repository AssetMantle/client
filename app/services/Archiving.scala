package services

import models.{analytic, archive, blockchain, masterTransaction}
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class Archiving @Inject()(
                           analyticTransactionCounters: analytic.TransactionCounters,
                           blockchainBlocks: blockchain.Blocks,
                           blockchainTransactions: blockchain.Transactions,
                           masterTransactionWalletTransactions: masterTransaction.WalletTransactions,
                           archiveWalletTransactions: archive.WalletTransactions,
                           archiveTransactions: archive.Transactions,
                           archiveBlocks: archive.Blocks,
                           archiveTransactionCounter: archive.TransactionCounters,
                         )(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.SERVICES_ARCHIVING

  private implicit val logger: Logger = Logger(this.getClass)

  def checkAndUpdate(latestHeight: Int): Future[Unit] = {
    val firstHeight = blockchainBlocks.Service.getFirstHeight
    val latestBlock = blockchainBlocks.Service.tryGet(latestHeight)

    def checkAndMigrate(firstHeight: Int, latestHeight: Int, latestBlockEpoch: Long) = {
      if (firstHeight > 0 && latestHeight > (firstHeight + 250000 + 10000)) {
        migrateBlocks(start = firstHeight, end = firstHeight + 10000, latestBlockEpoch)
      } else Future()
    }

    for {
      firstHeight <- firstHeight
      latestBlock <- latestBlock
      _ <- checkAndMigrate(firstHeight = firstHeight, latestHeight = latestHeight, latestBlockEpoch = latestBlock.time)
    } yield ()
  }

  private def migrateBlocks(start: Int, end: Int, latestBlockEpoch: Long) = {

    val moveWalletTransactions = {
      val walletTransactions = Await.result(masterTransactionWalletTransactions.Service.getByHeight(start = start, end = end), Duration.Inf)
      if (walletTransactions.nonEmpty) archiveWalletTransactions.Service.add(walletTransactions.map(x => archive.WalletTransaction(address = x.address, txHash = x.txHash, height = x.height))) else Future(Seq())
    }

    def deleteWalletTransactions() = masterTransactionWalletTransactions.Service.deleteByHeight(start = start, end = end)

    def moveTransactions() = {
      val txs = Await.result(blockchainTransactions.Service.getByHeight(start = start, end = end), Duration.Inf)
      if (txs.nonEmpty) archiveTransactions.Service.create(txs.map(x => archive.Transaction(hash = x.hash, height = x.height, code = x.code, gasWanted = x.gasWanted, gasUsed = x.gasUsed, txBytes = x.txBytes, log = x.log)), end) else Future(Seq())
    }

    def deleteTransactions() = {
      blockchainTransactions.Service.deleteByHeight(start = start, end = end)
    }

    def moveBlocks() = {
      val blocks = Await.result(blockchainBlocks.Service.getByHeight(start = start, end = end), Duration.Inf)
      if (blocks.nonEmpty) archiveBlocks.Service.create(blocks.map(x => archive.Block(height = x.height, time = x.time, proposerAddress = x.proposerAddress, validators = x.validators))) else Future(Seq())
    }

    def deleteBlocks() = blockchainBlocks.Service.deleteByHeight(start = start, end = end)

    val endEpoch = latestBlockEpoch - (15 * 86400)

    def moveTxCounter() = {
      val counters = Await.result(analyticTransactionCounters.Service.getByStartAndEndEpoch(startEpoch = 0, endEpoch = endEpoch), Duration.Inf)
      if (counters.nonEmpty) archiveTransactionCounter.Service.create(counters.map(x => archive.TransactionCounter(epoch = x.epoch, totalTxs = x.totalTxs))) else Future(Seq())
    }

    def deleteTxCounters() = analyticTransactionCounters.Service.deleteByEpoch(startEpoch = 0, endEpoch = endEpoch)

    (for {
      moved1 <- moveWalletTransactions
      _ <- if (moved1.nonEmpty) deleteWalletTransactions() else Future(0)
      moved2 <- moveTransactions()
      _ <- if (moved2.nonEmpty) deleteTransactions() else Future(0)
      moved3 <- moveBlocks()
      _ <- if (moved3.nonEmpty) deleteBlocks() else Future(0)
      moved4 <- moveTxCounter()
      _ <- if (moved4.nonEmpty) deleteTxCounters() else Future(0)
    } yield ()
      ).recover {
      case exception: Exception => logger.error(exception.getLocalizedMessage)
    }
  }

  def setLastArchiveHeight(): Unit = {
    val lastArchiveHeight = Await.result(archiveBlocks.Service.getLatestHeight, Duration.Inf)
    archiveTransactions.Service.setLastArchiveHeight(lastArchiveHeight)
  }

}