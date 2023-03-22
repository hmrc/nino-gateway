/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.ninogateway.controllers

import akka.stream.Materializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.routing.sird.{POST => SPOST, _}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.core.server.{Server, ServerConfig}
import uk.gov.hmrc.http.HeaderCarrier

class NinoInsightsControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  val insightsPort = 11222

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.nino-insights.port" -> insightsPort)
    .build()

  private val controller = app.injector.instanceOf[NinoInsightsController]
  implicit val mat: Materializer = app.injector.instanceOf[Materializer]

  "POST /check/insights" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    "forward a 200 response from the downstream service" in {
      val response = """{
                       |"ninoInsightsCorrelationId": "some-correlation-id",
                       |"riskScore": 100,
                       |"reason": "NINO_ON_WATCH_LIST"
                       |}""".stripMargin

      Server.withRouterFromComponents(ServerConfig(port = Some(insightsPort))) { components =>
        import components.{defaultActionBuilder => Action}
        {
          case r@SPOST(p"/nino-insights-proxy/check/insights") =>
            r.headers.get("True-Calling-Client") shouldBe Some("example-service")
            r.headers.get("Authorization") shouldBe Some("2345")
            Action(Ok(response).withHeaders("Content-Type" -> "application/json"))
        }
      } { _ =>

        val fakeRequest = FakeRequest("POST", "/nino-gateway/check/insights")
          .withJsonBody(Json.parse("""{"nino": "AB123456C"}"""))
          .withHeaders("True-Calling-Client" -> "example-service", "Content-Type" -> "application/json")

        val result = controller.any()(fakeRequest)
        status(result) shouldBe Status.OK
        contentAsString(result) shouldBe response
      }
    }

    "forward a 400 response from the downstream service" in {
      val errorResponse = """{"code": "MALFORMED_JSON", "path.missing: nino"}""".stripMargin

      Server.withRouterFromComponents(ServerConfig(port = Some(insightsPort))) { components =>
        import components.{defaultActionBuilder => Action}
        {
          case r@SPOST(p"/nino-insights-proxy/check/insights") => Action(
            BadRequest(errorResponse).withHeaders("Content-Type" -> "application/json"))
        }
      } { _ =>
        val fakeRequest = FakeRequest("POST", "/nino-gateway/check/insights")
          .withJsonBody(Json.parse("""{"non-nino": "AB123456C"}"""))
          .withHeaders("True-Calling-Client" -> "example-service", "Content-Type" -> "application/json")

        val result = controller.any()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        contentAsString(result) shouldBe errorResponse
      }
    }

    "handle a malformed json payload" in {
      val errorResponse = """{"code": "MALFORMED_JSON", "path.missing: nino"}""".stripMargin

      Server.withRouterFromComponents(ServerConfig(port = Some(insightsPort))) { components =>
        import components.{defaultActionBuilder => Action}
        {
          case r@SPOST(p"/nino-insights-proxy/check/insights") => Action(
            BadRequest(errorResponse).withHeaders("Content-Type" -> "application/json"))
        }
      } { _ =>
        val fakeRequest = FakeRequest("POST", "/nino-gateway/check/insights")
          .withTextBody("""{""")
          .withHeaders("True-Calling-Client" -> "example-service", "Content-Type" -> "application/json")

        val result = controller.any()(fakeRequest)
        status(result) shouldBe Status.BAD_REQUEST
        contentAsString(result) shouldBe errorResponse
      }
    }

    "return bad gateway if there is no connectivity to the downstream service" in {
      val errorResponse = """{"code": "REQUEST_DOWNSTREAM", "desc": "An issue occurred when the downstream service tried to handle the request"}""".stripMargin

      val fakeRequest = FakeRequest("POST", "/nino-gateway/check/insights")
        .withJsonBody(Json.parse("""{"nino": "AB123456C"}"""))
        .withHeaders("True-Calling-Client" -> "example-service", "Content-Type" -> "application/json")

      val result = controller.any()(fakeRequest)
      status(result) shouldBe Status.BAD_GATEWAY
      contentAsString(result) shouldBe errorResponse
    }

  }
}
