package controllers

import java.io.{File, FileInputStream, FilterInputStream}

import com.docusign.esign.api._
import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.master.UpdateContact
import play.api.libs.Files
import java.io.File
import java.util.List
import javax.ws.rs.core.UriBuilderException
import com.sun.jersey.core.util.Base64
import com.docusign.esign.model.Document
import com.docusign.esign.model._
//import org.apache.commons.codec.binary.Base64
import java.util.Arrays

import com.docusign.esign.client.ApiClient

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class DocuSignController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterContacts: master.Contacts, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {


  def docuSignTest=Action{

    val accessToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY4MTg1ZmYxLTRlNTEtNGNlOS1hZjFjLTY4OTgxMjIwMzMxNyJ9.eyJUb2tlblR5cGUiOjUsIklzc3VlSW5zdGFudCI6MTU4NTEyODAzMiwiZXhwIjoxNTg1MTU2ODMyLCJVc2VySWQiOiJmNTczOWZhMi1kZGNkLTQwNmEtYTU5NS1lZDlhODk4NjJmMjEiLCJzaXRlaWQiOjEsInNjcCI6WyJzaWduYXR1cmUiLCJjbGljay5tYW5hZ2UiLCJvcmdhbml6YXRpb25fcmVhZCIsInJvb21fZm9ybXMiLCJncm91cF9yZWFkIiwicGVybWlzc2lvbl9yZWFkIiwidXNlcl9yZWFkIiwidXNlcl93cml0ZSIsImFjY291bnRfcmVhZCIsImRvbWFpbl9yZWFkIiwiaWRlbnRpdHlfcHJvdmlkZXJfcmVhZCIsImR0ci5yb29tcy5yZWFkIiwiZHRyLnJvb21zLndyaXRlIiwiZHRyLmRvY3VtZW50cy5yZWFkIiwiZHRyLmRvY3VtZW50cy53cml0ZSIsImR0ci5wcm9maWxlLnJlYWQiLCJkdHIucHJvZmlsZS53cml0ZSIsImR0ci5jb21wYW55LnJlYWQiLCJkdHIuY29tcGFueS53cml0ZSJdLCJhdWQiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJhenAiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJpc3MiOiJodHRwczovL2FjY291bnQtZC5kb2N1c2lnbi5jb20vIiwic3ViIjoiZjU3MzlmYTItZGRjZC00MDZhLWE1OTUtZWQ5YTg5ODYyZjIxIiwiYXV0aF90aW1lIjoxNTg1MTI3OTIyLCJwd2lkIjoiZTA2MmQ3NzYtMmUxOC00ZTdkLWI0OWQtZjI0NDAwZWQ1OGZhIn0.I5mTsfxkTHKU-KQ82q-3DOYO5LQqsUgHFukXMBeey3uSyCyg6Pg7wx6RWSpWbBo9ehMr6GzyEM0Z5-StPu3rD_CyV5XwkBZLp3suP_j_891Y49peS7JUWohJv21ctb1eUGLqcRrwBeb1wP0tpDK6tsSLlm28vkqh-z5vALgYy2Dt-W5CMroprk67YuJaszWm3n8wtXaYdNELpSY3lqXGbt2f9iaFx8tyHHg5y8qLJFtkAx36iVJbKHSXHmnYheNcMKVGiIhXw6cWtnWzdY4FEk_fZ7W7bNejYTKldifTz5HxeyvN5mcUwtAIj3eF4PuOzDLUBUd5cp1DSY-9nilEYw"
    val accountID="10156924"
    val signerName="Shiv Prasad"
    val signerEmail="chardinkichandani123@gmail.com"
    val baseUrl="http://localhost:9000"
    val clientUserID="789"
    val authenticationMethod="None"
    val basePath="https://demo.docusign.net/restapi"
    val tokenExpiration= (60.toLong).*(480:Long)

    // val base64File=Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile("/home/persistence-1/projects/commitCentral/public/images/", "44678.pdf")))
    val fileByteArray=utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile("/root/IdeaProjects/commitCentral/public/images/", "44678.pdf"))
    println("fileByteArray"+fileByteArray)
    val base64File= Base64.encode(fileByteArray).toString
    val base64File2=new String(Base64.encode(fileByteArray))
    println(base64File2)
    println("base64file"+base64File)
    val document= new Document()
    document.setDocumentBase64(base64File2)
    document.setName("Example document")
    document.setFileExtension("pdf")
    document.setDocumentId("1")

    val signer=new Signer()
    signer.setEmail(signerEmail)
    signer.setName(signerName)
    signer.clientUserId(clientUserID)
    signer.recipientId("1")

    val signHere= new SignHere()
    signHere.setDocumentId("1")
    signHere.setPageNumber("1")
    signHere.setRecipientId("1")
    signHere.setTabLabel("SignHereTab")
    signHere.setXPosition("100")
    signHere.setYPosition("100")

    val signerTabs=new Tabs()
    signerTabs.setSignHereTabs(Arrays.asList(signHere))
    signer.setTabs(signerTabs)

    val envelopeDefinition=new EnvelopeDefinition()
    envelopeDefinition.setEmailSubject("Please sign this document")
    envelopeDefinition.setDocuments(Arrays.asList(document))

    val recipients= new Recipients()
    recipients.setSigners(Arrays.asList(signer))

    envelopeDefinition.setRecipients(recipients)
    envelopeDefinition.setStatus("sent")


    val apiClient= new ApiClient(basePath)
    apiClient.setAccessToken(accessToken,tokenExpiration)
    //apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken)

    val envelopesApi=new EnvelopesApi(apiClient)
    val results=envelopesApi.createEnvelope(accountID, envelopeDefinition)
    val envelopeId = results.getEnvelopeId()

    val viewRequest = new RecipientViewRequest()
    viewRequest.setReturnUrl(baseUrl + "/docuSignReturn")
    viewRequest.setAuthenticationMethod(authenticationMethod)
    viewRequest.setEmail(signerEmail)
    viewRequest.setUserName(signerName)
    viewRequest.setClientUserId(clientUserID)

    val results1 = envelopesApi.createRecipientView(accountID, envelopeId, viewRequest)
    val redirectUrl = results1.getUrl
    println(redirectUrl)
    val redirect= Redirect(redirectUrl)

    redirect

  }

  def docuSignReturn(event:String)=Action{

    if(event=="signing_complete"){
      Ok("you have signed the document")
    }else{
      Ok("error occured")
    }
  }

  def docuSignEnvelopes=Action{

    Ok
  }
}