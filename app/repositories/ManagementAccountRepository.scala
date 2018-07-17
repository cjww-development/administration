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

import com.cjwwdev.logging.Logging
import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import com.cjwwdev.mongo.responses._
import common.MissingAccountException
import javax.inject.Inject
import models.Account
import play.api.Configuration
import play.api.libs.json.JsValue
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import selectors.AccountSelectors

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DefaultManagementAccountRepository @Inject()(val config: Configuration) extends ManagementAccountRepository with ConnectionSettings

trait ManagementAccountRepository extends DatabaseRepository with Logging {
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

  def insertManagementAccount(account: Account): Future[MongoCreateResponse] = {
    for {
      col <- collection
      wr  <- col.insert[Account](account)
    } yield if(wr.ok) {
      MongoSuccessCreate
    } else {
      wr.writeErrors foreach(we => logger.error(s"code=[${we.code}] errormessage=[${we.errmsg}]"))
      MongoFailedCreate
    }
  }

  def getManagementUser(selector: BSONDocument): Future[Option[Account]] = {
    for {
      col <- collection
      res <- col.find(selector).one[Account]
    } yield res
  }

  def updateEmail(managementId: String, email: String): Future[MongoUpdatedResponse] = {
    for {
      col <- collection
      uwr <- col.update(AccountSelectors.managementIdSelector(managementId), BSONDocument("$set" -> BSONDocument("email" -> email)))
    } yield if(uwr.ok) {
      MongoSuccessUpdate
    } else {
      uwr.writeErrors foreach(we => logger.error(s"code=[${we.code}] errormessage=[${we.errmsg}]"))
      MongoFailedUpdate
    }
  }

  def updatePassword(managementId: String, oldPassword: String, newPassword: String): Future[MongoUpdatedResponse] = {
    for {
      col <- collection
      uwr <- col.update(AccountSelectors.passwordSelector(managementId, oldPassword), BSONDocument("$set" -> BSONDocument("password" -> newPassword)))
    } yield if(uwr.ok) {
      MongoSuccessUpdate
    } else {
      uwr.writeErrors foreach(we => logger.error(s"code=[${we.code}] errormessage=[${we.errmsg}]"))
      MongoFailedUpdate
    }
  }
}
