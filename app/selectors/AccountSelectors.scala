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

package selectors

import models.{Account, Credentials}
import reactivemongo.bson.BSONDocument

object AccountSelectors {
  val managementIdSelector: String => BSONDocument = managementId => BSONDocument("managementId" -> managementId)
  val userNameSelector: String => BSONDocument = username => BSONDocument("username" -> username)
  val emailSelector: String => BSONDocument = email => BSONDocument("email" -> email)

  def passwordSelector(managementId: String, password: String): BSONDocument = BSONDocument(
    "managementId" -> managementId,
    "password"     -> password
  )

  def loginSelector(credentials: Credentials): BSONDocument = BSONDocument(
    "username" -> credentials.username,
    "password" -> credentials.password
  )

  val passwordProjection: BSONDocument = BSONDocument("_id" -> 0, "managementId" -> 1, "password" -> 1)

  val rootSelector: Account => BSONDocument = acc => BSONDocument(
    "username" -> acc.username,
    "password" -> acc.password
  )
}
