package br.com.sebratel.bff.dho;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dho")
@RequiredArgsConstructor
@Slf4j
public class DhoProxyController {

    private final DhoClient dhoClient;

    // People Endpoints
    @GetMapping("/people")
    public Mono<Object> getAllPeople() {
        log.info("BFF Gateway: Request GET /api/dho/people");
        return dhoClient.get("/api/dho/people");
    }

    @GetMapping("/people/{id}")
    public Mono<Object> getPersonById(@PathVariable Integer id) {
        log.info("BFF Gateway: Request GET /api/dho/people/{}", id);
        return dhoClient.get("/api/dho/people/" + id);
    }

    @PostMapping("/people")
    public Mono<Object> createPerson(@RequestBody Object person) {
        log.info("BFF Gateway: Request POST /api/dho/people with data: {}", person);
        return dhoClient.post("/api/dho/people", person);
    }

    @PutMapping("/people/{id}")
    public Mono<Object> updatePerson(@PathVariable Integer id, @RequestBody Object person) {
        log.info("BFF Gateway: Request PUT /api/dho/people/{} with data: {}", id, person);
        return dhoClient.put("/api/dho/people/" + id, person);
    }

    @DeleteMapping("/people/{id}")
    public Mono<Void> deletePerson(@PathVariable Integer id) {
        log.info("BFF Gateway: Request DELETE /api/dho/people/{}", id);
        return dhoClient.delete("/api/dho/people/" + id);
    }

    // Opportunities Endpoints
    @GetMapping("/opportunities")
    public Mono<Object> getAllOpportunities() {
        log.info("BFF Gateway: Request GET /api/dho/opportunities");
        return dhoClient.get("/api/dho/opportunities");
    }

    @GetMapping("/opportunities/{id}")
    public Mono<Object> getOpportunityById(@PathVariable Integer id) {
        log.info("BFF Gateway: Request GET /api/dho/opportunities/{}", id);
        return dhoClient.get("/api/dho/opportunities/" + id);
    }

    @PostMapping("/opportunities")
    public Mono<Object> createOpportunity(@RequestBody Object opportunity) {
        log.info("BFF Gateway: Request POST /api/dho/opportunities with data: {}", opportunity);
        return dhoClient.post("/api/dho/opportunities", opportunity);
    }

    @PutMapping("/opportunities/{id}")
    public Mono<Object> updateOpportunity(@PathVariable Integer id, @RequestBody Object opportunity) {
        log.info("BFF Gateway: Request PUT /api/dho/opportunities/{} with data: {}", id, opportunity);
        return dhoClient.put("/api/dho/opportunities/" + id, opportunity);
    }

    @DeleteMapping("/opportunities/{id}")
    public Mono<Void> deleteOpportunity(@PathVariable Integer id) {
        log.info("BFF Gateway: Request DELETE /api/dho/opportunities/{}", id);
        return dhoClient.delete("/api/dho/opportunities/" + id);
    }

    // Settings Endpoints
    @GetMapping("/settings")
    public Mono<Object> getAllSettings() {
        log.info("BFF Gateway: Request GET /api/dho/settings");
        return dhoClient.get("/api/dho/settings");
    }
}
