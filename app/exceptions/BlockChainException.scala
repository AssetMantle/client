package exceptions

class BlockChainException(val failure: constants.Response.Failure) extends Exception