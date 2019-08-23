package controllers.actions

import models.blockchain.ACL

case class LoginState(username: String ,userType: String, address: String, acl: Option[ACL])
