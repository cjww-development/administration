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

package common

import com.cjwwdev.auth.backend.BaseAuth
import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.request.RequestParsers
import com.cjwwdev.responses.ApiResponse
import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{BaseController, Request, Result}

import scala.concurrent.{ExecutionContext => ExC, Future}
import scala.reflect.ClassTag
import scala.util.Try

trait BackendController extends BaseController with RequestParsers with IdentifierValidation with BaseAuth with ApiResponse {

  implicit val ec: ExC

  def createNewFromRequest[T](reads: Reads[T])(f: T => Future[Result])(implicit request: Request[String], tag: ClassTag[T]): Future[Result] = {
    val deObfuscator: DeObfuscator[T] = new DeObfuscator[T] {
      override def decrypt(value: String): Either[T, DecryptionError] = DeObfuscation.deObfuscate[T](value)(reads, tag)
    }

    deObfuscator.decrypt(request.body).fold(
      data => f(data),
      err  => Try(Json.parse(err.message)).fold(
        _ => withFutureJsonResponseBody(BAD_REQUEST, s"Couldn't decrypt request body on ${request.path}") { json =>
          Future(BadRequest(json))
        },
        jsError => withFutureJsonResponseBody(BAD_REQUEST, jsError, "Decrypted json was missing a field") { json =>
          Future(BadRequest(json))
        }
      )
    )
  }
}
