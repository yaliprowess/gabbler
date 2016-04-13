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

import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor

object UserRepository {

  case object GetUsers
  case class Users(users: Set[User])

  case class AddUser(username: String, nickname: String, email: String)
  case class UsernameTaken(username: String)
  case class UserAdded(user: User)

  case class RemoveUser(username: String)
  case class UsernameUnknown(username: String)
  case class UserRemoved(username: String)

  final val Name = "user-repository"

  def props: Props = Props(new UserRepository)
}

final class UserRepository extends PersistentActor with ActorLogging {
  import UserRepository._

  override val persistenceId = Name

  private var users = Map.empty[String, User]

  override def receiveCommand = {
    case GetUsers                                            => sender() ! Users(users.valuesIterator.to[Set])
    case AddUser(username, _, _) if users.contains(username) => sender() ! UsernameTaken(username)
    case AddUser(username, nickname, email)                  => handleAddUser(username, nickname, email)
    case RemoveUser(username) if !users.contains(username)   => sender() ! UsernameUnknown(username)
    case RemoveUser(username)                                => handleRemoveUser(username)
  }

  override def receiveRecover = {
    case UserAdded(user)       => users += user.username -> user
    case UserRemoved(username) => users -= username
  }

  private def handleAddUser(username: String, nickname: String, email: String) =
    persist(UserAdded(User(username, nickname, email))) { userAdded =>
      receiveRecover(userAdded)
      log.info(s"Added user with username $username")
      sender() ! userAdded
    }

  private def handleRemoveUser(username: String) =
    persist(UserRemoved(username)) { userRemoved =>
      receiveRecover(userRemoved)
      log.info(s"Removed user with username $username")
      sender() ! userRemoved
    }
}
