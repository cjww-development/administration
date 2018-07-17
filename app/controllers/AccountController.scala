/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.mongo.responses.{MongoFailedCreate, MongoSuccessCreate}
import com.cjwwdev.security.encryption.DataSecurity
import common.{BackendController, MissingAccountException}
import javax.inject.Inject
import models.{Account, Credentials}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{ManagementAccountService, ValidationService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultAccountController @Inject()(val controllerComponents: ControllerComponents,
                                         val validationService: ValidationService,
                                         val managementAccountService: ManagementAccountService) extends AccountController

trait AccountController extends BackendController {

  val managementAccountService: ManagementAccountService
  val validationService: ValidationService

  def createNewUser(): Action[String] = Action.async(parse.text) { implicit request =>
    applicationVerification {
      withJsonBody[Account](Account.newAccountReads) { account =>
        for {
          emailInUse    <- validationService.isEmailInUse(account.email)
          userNameInUse <- validationService.isUserNameInUse(account.username)
          created       <- if(!emailInUse & !userNameInUse) {
            managementAccountService.insertNewManagementUser(account) map { resp =>
              val (status, body) = resp match {
                case MongoSuccessCreate => (CREATED, "Account created")
                case MongoFailedCreate  => (INTERNAL_SERVER_ERROR, "There was a problem creating the new account")
              }

              withJsonResponseBody(status, body) { json =>
                status match {
                  case CREATED               => Created(json)
                  case INTERNAL_SERVER_ERROR => InternalServerError(json)
                }
              }
            }
          } else {
            withFutureJsonResponseBody(CONFLICT, "Could not create new account; either the email or username is already in use") { json =>
              Future(Conflict(json))
            }
          }
        } yield created
      }
    }
  }

  def authenticateUser(): Action[String] = Action.async(parse.text) { implicit request =>
    applicationVerification {
      withJsonBody[Credentials](Credentials.format) { credentials =>
        managementAccountService.authenticateUser(credentials) map { managementId =>
          val (status, body) = managementId match {
            case Some(id) =>
              logger.info(s"User ${managementId.get} successfully authenticated")
              (OK, id.encrypt)
            case None =>
              logger.warn("No matching user found; authentication failed")
              (FORBIDDEN, "User could not be authenticated")
          }

          withJsonResponseBody(status, body) { json =>
            status match {
              case OK        => Ok(json)
              case FORBIDDEN => Forbidden(json)
            }
          }
        }
      }
    }
  }

  def getManagementUser(managementId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      managementAccountService.getManagementUser("managementId", managementId) map { acc =>
        withJsonResponseBody(OK, DataSecurity.encryptType(acc)(Account.outgoingAccountWrites)) { json =>
          Ok(json)
        }
      } recover {
        case _: MissingAccountException => withJsonResponseBody(NOT_FOUND, "No account found") { json =>
          NotFound(json)
        }
      }
    }
  }
}