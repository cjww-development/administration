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

package repositories

import com.cjwwdev.logging.output.Logger
import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import com.cjwwdev.mongo.responses._
import javax.inject.Inject
import models.Account
import play.api.Configuration
import play.api.mvc.Request
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import selectors.AccountSelectors
import services.MetricsService

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultManagementAccountRepository @Inject()(val config: Configuration,
                                                   val metricsService: MetricsService) extends ManagementAccountRepository with ConnectionSettings

trait ManagementAccountRepository extends DatabaseRepository with Logger {

  val metricsService: MetricsService

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("managementId" -> IndexType.Ascending),
      name   = Some("ManagementId"),
      sparse = false,
      unique = true
    ),
    Index(
      key    = Seq("username" -> IndexType.Ascending),
      name   = Some("Username"),
      sparse = false,
      unique = true
    ),
    Index(
      key    = Seq("email" -> IndexType.Ascending),
      name   = Some("Email"),
      sparse = false,
      unique = true
    ),
    Index(
      key    = Seq("password" -> IndexType.Ascending),
      name   = Some("Password"),
      sparse = false,
      unique = false
    )
  )

  def insertManagementAccount(account: Account)(implicit ec: ExC, request: Request[_]): Future[MongoCreateResponse] = {
    metricsService.mongoQueryResponseTime(s"$collectionName-insert-management-account") {
      for {
        col <- collection
        wr  <- col.insert[Account](account)
      } yield if(wr.ok) {
        LogAt.info(s"[insertManagementAccount] - Management user ${account.managementId} successfully created")
        MongoSuccessCreate
      } else {
        wr.writeErrors foreach(we => LogAt.error(s"[insertManagementAccount] - code=[${we.code}] errormessage=[${we.errmsg}]"))
        MongoFailedCreate
      }
    }
  }

  def getManagementUser(selector: BSONDocument)(implicit ec: ExC): Future[Option[Account]] = {
    metricsService.mongoQueryResponseTime(s"$collectionName-get-management-user") {
      for {
        col <- collection
        res <- col.find(selector).one[Account]
      } yield res
    }
  }

  def getAllManagementUsers(implicit ec: ExC): Future[List[Account]] = {
    metricsService.mongoQueryResponseTime(s"$collectionName-get-all-management-users") {
      for {
        col <- collection
        res <- col.find(BSONDocument()).cursor[Account]().collect[List]()
      } yield res
    }
  }

  def updateEmail(managementId: String, email: String)(implicit ec: ExC, request: Request[_]): Future[MongoUpdatedResponse] = {
    metricsService.mongoQueryResponseTime(s"$collectionName-update-email") {
      for {
        col <- collection
        uwr <- col.update(AccountSelectors.managementIdSelector(managementId), BSONDocument("$set" -> BSONDocument("email" -> email)))
      } yield if(uwr.ok) {
        LogAt.info(s"[updateEmail] - Successfully updated email for user $managementId")
        MongoSuccessUpdate
      } else {
        uwr.writeErrors foreach(we => LogAt.error(s"[updateEmail] - code=[${we.code}] errormessage=[${we.errmsg}]"))
        MongoFailedUpdate
      }
    }
  }

  def updatePassword(managementId: String, oldPassword: String, newPassword: String)(implicit ec: ExC, request: Request[_]): Future[MongoUpdatedResponse] = {
    metricsService.mongoQueryResponseTime(s"$collectionName-update-password") {
      for {
        col <- collection
        uwr <- col.update(AccountSelectors.passwordSelector(managementId, oldPassword), BSONDocument("$set" -> BSONDocument("password" -> newPassword)))
      } yield if(uwr.ok) {
        LogAt.info(s"[updatePassword] - Successfully updated email for user $managementId")
        MongoSuccessUpdate
      } else {
        uwr.writeErrors foreach(we => LogAt.error(s"[updatePassword] - code=[${we.code}] errormessage=[${we.errmsg}]"))
        MongoFailedUpdate
      }
    }
  }

  def deleteManagementUser(managementId: String)(implicit ec: ExC, request: Request[_]): Future[MongoDeleteResponse] = {
    metricsService.mongoQueryResponseTime(s"$collectionName-delete-management-user") {
      for {
        col <- collection
        del <- col.remove[BSONDocument](AccountSelectors.managementIdSelector(managementId))
      } yield if(del.ok) {
        LogAt.info(s"[deleteManagementUser] - deleted user $managementId")
        MongoSuccessDelete
      } else {
        LogAt.error(s"[deleteManagementUser] - ssFailed to delete user $managementId")
        MongoFailedDelete
      }
    }
  }
}
