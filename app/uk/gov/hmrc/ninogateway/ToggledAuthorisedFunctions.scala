/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.ninogateway

import uk.gov.hmrc.auth.core.AuthorisedFunctions
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait ToggledAuthorisedFunctions extends AuthorisedFunctions {

  def toggledAuthorised(enabled: Boolean, predicate: Predicate): ToggledAuthorisedFunction = new ToggledAuthorisedFunction(enabled, predicate)

  class ToggledAuthorisedFunction(enabled: Boolean, predicate: Predicate) {
    def apply[A](body: => Future[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
      val result = if (enabled) authConnector.authorise(predicate, EmptyRetrieval) else Future.successful()
      result.flatMap(_ => body)
    }
  }
}