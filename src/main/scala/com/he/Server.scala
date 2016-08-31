package com.he

import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}

class Server {
  val service = Endpoint.makeService()
  val server = Http.serve(":8080", service) //creates service
  Await.ready(server)

  def close(): Future[Unit] = {
    Await.ready(server.close())
  }
}

object Server extends Server with App {
  Await.ready(server)
}
