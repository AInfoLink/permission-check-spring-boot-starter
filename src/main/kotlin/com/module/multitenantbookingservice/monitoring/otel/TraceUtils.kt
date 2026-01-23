package com.module.multitenantbookingservice.monitoring.otel

import io.opentelemetry.api.trace.Span

class TraceUtils {
    companion object {
        fun getTraceId(): String {
            val span: Span = Span.current()
            val traceId = if (span.spanContext.isValid) span.spanContext.traceId else "N/A"
            return traceId
        }
    }
}
