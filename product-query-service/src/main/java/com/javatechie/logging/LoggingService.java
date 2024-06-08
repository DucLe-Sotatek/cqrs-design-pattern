package com.javatechie.logging;


import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableScheduling
public class LoggingService {
    @Autowired private MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 500)
    public void logConnectionPoolStats() {
        Double activeConnections = meterRegistry.get("hikaricp.connections.active").gauge().value();
        Double idleConnections = meterRegistry.get("hikaricp.connections.idle").gauge().value();
        Double totalConnections = meterRegistry.get("hikaricp.connections.total").gauge().value();
        Double awaitingConnections =
                meterRegistry.get("hikaricp.connections.pending").gauge().value();

        log.info(
                """
                Active connection :: {}
                Idle Connections:: {}
                Total Connections:: {}
                Threads Awaiting Connections:: {}
                """,
                activeConnections,
                idleConnections,
                totalConnections,
                awaitingConnections);
    }
}
