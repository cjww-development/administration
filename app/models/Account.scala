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

package models

import java.util.UUID

import com.cjwwdev.regex.RegexPack
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Account(managementId: String,
                   username: String,
                   email: String,
                   password: String,
                   permissions: List[String])

object Account extends RegexPack {
  implicit val format: OFormat[Account] = Json.format[Account]

  private def generateManagementId: String = s"management-${UUID.randomUUID}"

  private val userNameValidation = Reads.StringReads.filter(JsonValidationError("Invalid username"))(_.matches(userNameRegex.regex))
  private val emailValidation    = Reads.StringReads.filter(JsonValidationError("Invalid email"))(_.matches(emailRegex.regex))
  private val passwordValidation = Reads.StringReads.filter(JsonValidationError("Invalid Password"))(_.length == 128)

  val newAccountReads: Reads[Account] = (
    (__ \ "managementId").read[String](generateManagementId) and
    (__ \ "username").read[String](userNameValidation) and
    (__ \ "email").read[String](emailValidation) and
    (__ \ "password").read[String](passwordValidation) and
    (__ \ "permissions").read[List[String]]
  )(Account.apply _)

  val outgoingAccountWrites: Writes[Account] = Writes[Account] { acc =>
    Json.obj(
      "managementId" -> acc.managementId,
      "username"     -> acc.username,
      "email"        -> acc.email,
      "permissions"  -> Json.toJson(acc.permissions)
    )
  }

  val outgoingAccountListWrites: Writes[List[Account]] = Writes[List[Account]] { accounts =>
    val jsValues = accounts.map(acc => Json.toJson(acc)(outgoingAccountWrites))
    Json.toJson(jsValues)
  }
}
