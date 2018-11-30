package eu.qrpth.sanctions

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

@Filter()
class HttpLogger: HttpClientFilter {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
        logger.info("Requesing {} {}", request.method, request.uri)
        return chain.proceed(request)
    }
}
