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

import com.cjwwdev.config.{ConfigurationLoader, DefaultConfigurationLoader}
import com.cjwwdev.mongo.indexes.RepositoryIndexer
import common.startup.{DefaultRootUser, RootUser}
import controllers.{AccountController, DefaultAccountController}
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import repositories.{DefaultManagementAccountRepository, ManagementAccountRepository}
import services.{DefaultManagementAccountService, DefaultValidationService, ManagementAccountService, ValidationService}


class ServiceBindings extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    bindOther() ++ bindRepositories() ++ bindStartup() ++ bindServices() ++ bindControllers()

  private def bindOther(): Seq[Binding[_]] = Seq(
    bind(classOf[ConfigurationLoader]).to(classOf[DefaultConfigurationLoader]).eagerly(),
    bind(classOf[RepositoryIndexer]).to(classOf[AdminIndexing]).eagerly()
  )

  private def bindRepositories(): Seq[Binding[_]] = Seq(
    bind(classOf[ManagementAccountRepository]).to(classOf[DefaultManagementAccountRepository]).eagerly()
  )

  private def bindStartup(): Seq[Binding[_]] = Seq(
    bind(classOf[RootUser]).to(classOf[DefaultRootUser]).eagerly()
  )

  private def bindServices(): Seq[Binding[_]] = Seq(
    bind(classOf[ManagementAccountService]).to(classOf[DefaultManagementAccountService]).eagerly(),
    bind(classOf[ValidationService]).to(classOf[DefaultValidationService]).eagerly()
  )

  private def bindControllers(): Seq[Binding[_]] = Seq(
    bind(classOf[AccountController]).to(classOf[DefaultAccountController]).eagerly()
  )
}
