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

   // val accessToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY4MTg1ZmYxLTRlNTEtNGNlOS1hZjFjLTY4OTgxMjIwMzMxNyJ9.eyJUb2tlblR5cGUiOjUsIklzc3VlSW5zdGFudCI6MTU4NDU0MDExOCwiZXhwIjoxNTg0NTY4OTE4LCJVc2VySWQiOiJmNTczOWZhMi1kZGNkLTQwNmEtYTU5NS1lZDlhODk4NjJmMjEiLCJzaXRlaWQiOjEsInNjcCI6WyJzaWduYXR1cmUiLCJjbGljay5tYW5hZ2UiLCJvcmdhbml6YXRpb25fcmVhZCIsInJvb21fZm9ybXMiLCJncm91cF9yZWFkIiwicGVybWlzc2lvbl9yZWFkIiwidXNlcl9yZWFkIiwidXNlcl93cml0ZSIsImFjY291bnRfcmVhZCIsImRvbWFpbl9yZWFkIiwiaWRlbnRpdHlfcHJvdmlkZXJfcmVhZCIsImR0ci5yb29tcy5yZWFkIiwiZHRyLnJvb21zLndyaXRlIiwiZHRyLmRvY3VtZW50cy5yZWFkIiwiZHRyLmRvY3VtZW50cy53cml0ZSIsImR0ci5wcm9maWxlLnJlYWQiLCJkdHIucHJvZmlsZS53cml0ZSIsImR0ci5jb21wYW55LnJlYWQiLCJkdHIuY29tcGFueS53cml0ZSJdLCJhdWQiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJhenAiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJpc3MiOiJodHRwczovL2FjY291bnQtZC5kb2N1c2lnbi5jb20vIiwic3ViIjoiZjU3MzlmYTItZGRjZC00MDZhLWE1OTUtZWQ5YTg5ODYyZjIxIiwiYW1yIjpbImludGVyYWN0aXZlIl0sImF1dGhfdGltZSI6MTU4NDU0MDExNCwicHdpZCI6ImUwNjJkNzc2LTJlMTgtNGU3ZC1iNDlkLWYyNDQwMGVkNThmYSJ9.qz3dosVS018kUmE6ti1uzRYG7BXXgls-J6e_QzLmzOVNe2EwNm1fQvqzM1TkLdkVItTb5GdzeWLhsL041lXIvUXl5YCi77mLZZjbU2AfTccB3PrA_du3JhTLW-YE4OmHkKXtB5uZta55Ty637duqJw4Q7_nsSMTX-WREIh_NF9MeZ8cI9NeK6OCmUYl3Rp5eqShrTGyAep2z8RFaaEwwAIqg0vQ9XPc1JDmG4-6sIF71ntQ2Ri2k2rTs1trVqRn7kAyiUTh8WZtidZoB0flH1dxgxPkhuc1Y7IM1h_KaSctMOgUpx9UbPJiCpoZRg5gZ3BTNckI8bLjFOeJTLjRhuA"
    val accessToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY4MTg1ZmYxLTRlNTEtNGNlOS1hZjFjLTY4OTgxMjIwMzMxNyJ9.eyJUb2tlblR5cGUiOjUsIklzc3VlSW5zdGFudCI6MTU4NDc5MjQ1MywiZXhwIjoxNTg0ODIxMjUzLCJVc2VySWQiOiJmNTczOWZhMi1kZGNkLTQwNmEtYTU5NS1lZDlhODk4NjJmMjEiLCJzaXRlaWQiOjEsInNjcCI6WyJzaWduYXR1cmUiLCJjbGljay5tYW5hZ2UiLCJvcmdhbml6YXRpb25fcmVhZCIsInJvb21fZm9ybXMiLCJncm91cF9yZWFkIiwicGVybWlzc2lvbl9yZWFkIiwidXNlcl9yZWFkIiwidXNlcl93cml0ZSIsImFjY291bnRfcmVhZCIsImRvbWFpbl9yZWFkIiwiaWRlbnRpdHlfcHJvdmlkZXJfcmVhZCIsImR0ci5yb29tcy5yZWFkIiwiZHRyLnJvb21zLndyaXRlIiwiZHRyLmRvY3VtZW50cy5yZWFkIiwiZHRyLmRvY3VtZW50cy53cml0ZSIsImR0ci5wcm9maWxlLnJlYWQiLCJkdHIucHJvZmlsZS53cml0ZSIsImR0ci5jb21wYW55LnJlYWQiLCJkdHIuY29tcGFueS53cml0ZSJdLCJhdWQiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJhenAiOiJmMGYyN2YwZS04NTdkLTRhNzEtYTRkYS0zMmNlY2FlM2E5NzgiLCJpc3MiOiJodHRwczovL2FjY291bnQtZC5kb2N1c2lnbi5jb20vIiwic3ViIjoiZjU3MzlmYTItZGRjZC00MDZhLWE1OTUtZWQ5YTg5ODYyZjIxIiwiYXV0aF90aW1lIjoxNTg0NzkyMjY3LCJwd2lkIjoiZTA2MmQ3NzYtMmUxOC00ZTdkLWI0OWQtZjI0NDAwZWQ1OGZhIn0.OLwyntGin9OV_fPccJe4JdVikWZW_dg9CGbER7vhJNfLB3l8CndFdbRNkO516fgNgAxElWQGu8HqK-0dJcOC3Rw4WESMAvLBAVRXaT_d4-21ZTv1gSc10xX_huFpU1KX_edxtnLHtndYf69M4-MDy0W8yqsmolvwCPhRZTPa3bSzDLs_RpbfRHPhhhL6NxKNgRhtLeDmeC85fvH-UmTkxok3BXlFwh7mwjLsY8hYqVP3Z4yDujOVio2-c4y5DNXoIwfMD3ftLkZPqBDrzr6woWq06lTqFrFn3RvndTnoVyDNFN7GYsawghsgVvt0X60D0dsgg_0pMTtYr4YRWlXb5A"
    val accountID="10156924"
    val signerName="Prashant"
    val signerEmail="prashantkrv96@gmail.com"
    val baseUrl="http://localhost:9000"
    val clientUserID="123"
    val authenticationMethod="None"
    val basePath="https://demo.docusign.net/restapi"

   // val base64File=Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile("/home/persistence-1/projects/commitCentral/public/images/", "44678.pdf")))
    val fileByteArray=utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile("/home/persistence-1/projects/commitCentral/public/images/", "44678.pdf"))
    val base64File2= Base64.encode(fileByteArray).toString
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
    signHere.setXPosition("195")
    signHere.setYPosition("147")

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

    apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken)
   println(apiClient)
   println(envelopeDefinition)
    val envelopesApi=new EnvelopesApi(apiClient)
    val results=envelopesApi.createEnvelope(accountID, envelopeDefinition)
    val envelopeId = results.getEnvelopeId()

    val viewRequest = new RecipientViewRequest()
    viewRequest.setReturnUrl(baseUrl + "/ds-return")
    viewRequest.setAuthenticationMethod(authenticationMethod)
    viewRequest.setEmail(signerEmail)
    viewRequest.setUserName(signerName)
    viewRequest.setClientUserId(clientUserID)

    val results1 = envelopesApi.createRecipientView(accountID, envelopeId, viewRequest)
    val redirectUrl = results1.getUrl

    val redirect= Redirect(redirectUrl)

    redirect

  }
}