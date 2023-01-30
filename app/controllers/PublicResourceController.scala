package controllers

import controllers.Assets.Asset
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent, ControllerHelpers}

import javax.inject.{Inject, Singleton}

@Singleton
class PublicResourceController @Inject()(
                                          errorHandler: HttpErrorHandler,
                                          meta: AssetsMetadata
                                        ) extends ControllerHelpers {

  private val assetsBuilder: AssetsBuilder = new AssetsBuilder(errorHandler, meta)

  def versioned(path: String, file: Asset, version: String): Action[AnyContent] = {
    assetsBuilder.versioned(path = path, file = file)
  }

}
