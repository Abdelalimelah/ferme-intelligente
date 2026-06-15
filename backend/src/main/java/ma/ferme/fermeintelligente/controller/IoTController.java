package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.IoTDataRequest;
import ma.ferme.fermeintelligente.dto.IoTDataResponse;
import ma.ferme.fermeintelligente.service.IoTService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
public class IoTController {

    private final IoTService ioTService;

    /**
     * Endpoint for Arduino/IoT devices to push sensor data.
     * No JWT required - uses API key in request body.
     */
    @PostMapping("/data")
    public ResponseEntity<IoTDataResponse> ingestData(@RequestBody IoTDataRequest request) {
        return ResponseEntity.ok(ioTService.ingestData(request));
    }

    /**
     * Batch ingestion for multiple sensor readings at once.
     */
    @PostMapping("/data/batch")
    public ResponseEntity<List<IoTDataResponse>> ingestBatch(@RequestBody List<IoTDataRequest> requests) {
        List<IoTDataResponse> responses = requests.stream()
                .map(ioTService::ingestData)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
