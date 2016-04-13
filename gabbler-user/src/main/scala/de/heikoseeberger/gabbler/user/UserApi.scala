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

import akka.actor.Status.Failure
import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import java.net.InetSocketAddress

object UserApi {

  final val Name = "user-api"

  def props(address: String, port: Int): Props = Props(new UserApi(address, port))

  private[user] def route = {
    import Directives._
    complete {
      "Hello, world!"
    }
  }
}

final class UserApi(address: String, port: Int) extends Actor with ActorLogging {
  import UserApi._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  Http(context.system)
    .bindAndHandle(route, address, port)
    .pipeTo(self)

  override def receive = {
    case Http.ServerBinding(socketAddress) => handleServerBinding(socketAddress)
    case Failure(cause)                    => handleBindFailure(cause)
  }

  private def handleServerBinding(socketAddress: InetSocketAddress) = {
    log.info(s"Listening on $socketAddress")
    context.become(Actor.emptyBehavior)
  }

  private def handleBindFailure(cause: Throwable) = {
    log.error(cause, s"Can't bind to $address:$port!")
    context.stop(self)
  }
}
