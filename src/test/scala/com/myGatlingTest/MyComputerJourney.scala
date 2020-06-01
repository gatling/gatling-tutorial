package com.myGatlingTest

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import scala.concurrent.duration.DurationInt

class MyComputerJourney extends Simulation {

	val httpProtocol = http
		.baseUrl("https://computer-database.gatling.io")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-GB,en-US;q=0.9,en;q=0.8")
		.upgradeInsecureRequestsHeader("1")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36")

	val csvFeeder = csv("data/computerCsvFile.csv").circular


	val scn = scenario("MyComputerJourney")
			.feed(csvFeeder)
			.exec(http("LoadHomePage")
				.get("/computers"))
				.pause(5)
				.exec(http("LoadNewComputerPage")
					.get("/computers/new"))
				.pause(5)
				.exec(http("CreateNewComputer")
					.post("/computers")
					.formParam("name", "${computerName}")
					.formParam("introduced", "${introduced}")
					.formParam("discontinued", "${discontinued}")
					.formParam("company", "${companyId}"))
				.pause(5)
				.exec(http("FilterComputer")
					.get("/computers?f=GatlingComputer")
					.check(regex("""computers\/([0-9]{3,5})""").exists.saveAs("topComputerInList")))
				.exec(http("GetSingleComputer")
					.get("/computers/${topComputerInList}"))
				.exec(http("DeleteComputer")
					.post("/computers/${topComputerInList}/delete"))

	setUp(
		scn.inject(
			nothingFor(5 seconds),
			atOnceUsers(1),
			rampUsers(5) during (10 seconds),
			constantUsersPerSec(20) during (20 seconds)
		).protocols(httpProtocol)
	)

}