/*
 * Copyright 2016 Heiko Seeberger
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

package de.heikoseeberger.gabbler.user

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, SupervisorStrategy, Terminated }
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object UserApp {

  private class Root extends Actor with ActorLogging with ActorSettings {

    override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

    private val userRepository = context.actorOf(UserRepository.props, UserRepository.Name)

    private val userApi = context.actorOf(
      UserApi.props(
        settings.userApi.address,
        settings.userApi.port,
        userRepository,
        settings.userApi.userRepositoryTimeout
      ),
      UserApi.Name
    )

    context.watch(userRepository)
    context.watch(userApi)

    override def receive = {
      case Terminated(actor) =>
        log.error(s"Terminating the system because ${actor.path} terminated!")
        context.system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("gabbler-user-system")
    system.actorOf(Props(new Root), "root")
    Await.ready(system.whenTerminated, Duration.Inf)
  }
}
