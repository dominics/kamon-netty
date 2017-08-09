package kamon.netty.playground

import java.net.SocketAddress

import io.netty.buffer.{ByteBufAllocator, Unpooled}
import io.netty.channel._
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpRequest}
import io.netty.util.CharsetUtil

class HttpHelloWorldServerHandler extends ChannelInboundHandlerAdapter {

  import HttpHelloWorldServerHandler.CONTENT

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit =
    msg match {
      case req: HttpRequest =>
        if (is100ContinueExpected(req)) {
          ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE))
        }
        val keepAlive = isKeepAlive(req)
        val response = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT.duplicate()) <| { r =>
          r.headers().set(CONTENT_TYPE, "text/plain")
          r.headers().set(CONTENT_LENGTH, r.content().readableBytes())
          if (keepAlive) r.headers().set(CONNECTION, Values.KEEP_ALIVE)
        }

        val future = ctx.write(response)
        if (!keepAlive) future.closeOnComplete()

      case _ =>
    }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
//    println(s"channelReadComplete .... ${ctx.channel().hashCode()}")
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}

object HttpHelloWorldServerHandler {
  private final val CONTENT =
    Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello World jaklfjal jsflkasflkj askl", CharsetUtil.US_ASCII))
}
