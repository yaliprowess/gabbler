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

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UserRepositorySpec extends WordSpec with Matchers with BeforeAndAfterAll {
  import UserRepository._

  private implicit val system = ActorSystem()

  "UserRepository" should {
    "correctly handle adding and removing users" in {
      val sender = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      implicit val senderRef = sender.ref

      val userRepository = system.actorOf(UserRepository.props)
      userRepository ! GetUsers
      sender.expectMsg(Users(Set.empty))

      val user1 = User("user-1", "User One", "user1@gabbler.io")
      import user1._
      userRepository ! AddUser(username, nickname, email)
      sender.expectMsg(UserAdded(user1))
      userRepository ! GetUsers
      sender.expectMsg(Users(Set(user1)))

      userRepository ! AddUser(username, "User Two", "user2@gabbler.io")
      sender.expectMsg(UsernameTaken(username))

      userRepository ! RemoveUser(username)
      sender.expectMsg(UserRemoved(username))
      userRepository ! GetUsers
      sender.expectMsg(Users(Set.empty))

      userRepository ! RemoveUser(username)
      sender.expectMsg(UsernameUnknown(username))
    }
  }

  override protected def afterAll() = {
    Await.ready(system.terminate(), Duration.Inf)
    super.afterAll()
  }
}
