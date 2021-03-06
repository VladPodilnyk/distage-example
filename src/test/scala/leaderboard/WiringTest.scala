package leaderboard

import com.typesafe.config.ConfigFactory
import distage.{DIKey, Injector}
import izumi.distage.config.AppConfigModule
import izumi.distage.model.definition.Activation
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.model.plan.GCMode
import izumi.distage.testkit.scalatest.DistageBIOSpecScalatest
import izumi.logstage.api.logger.LogRouter
import leaderboard.plugins.{LeaderboardPlugin, ZIOPlugin}
import logstage.di.LogstageModule
import zio.{IO, Task}

final class WiringTest extends DistageBIOSpecScalatest[IO] {
  "all dependencies are wired correctly" in {
    def checkActivation(activation: Activation): Task[Unit] = {
      Task {
        Injector(activation)
          .plan(
            Seq(
              LeaderboardPlugin,
              ZIOPlugin,
              // dummy logger + config modules,
              // normally the RoleAppMain or the testkit will provide real values here
              new LogstageModule(LogRouter.nullRouter, setupStaticLogRouter = false),
              new AppConfigModule(ConfigFactory.empty),
            ).merge,
            GCMode(DIKey.get[LeaderboardRole[zio.IO]]),
          )
          .assertImportsResolvedOrThrow()
      }
    }

    for {
      _ <- checkActivation(Activation(Repo -> Repo.Dummy))
      _ <- checkActivation(Activation(Repo -> Repo.Prod))
    } yield ()
  }
}
